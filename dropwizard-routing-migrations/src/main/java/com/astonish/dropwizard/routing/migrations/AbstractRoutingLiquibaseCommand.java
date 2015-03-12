/* Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.astonish.dropwizard.routing.migrations;

import io.dropwizard.Configuration;
import io.dropwizard.cli.ConfiguredCommand;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.db.ManagedDataSource;
import io.dropwizard.migrations.CloseableLiquibase;
import io.dropwizard.setup.Bootstrap;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.joda.time.Duration;
import org.joda.time.format.ISOPeriodFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.astonish.dropwizard.routing.db.DataSourceRoute;
import com.astonish.dropwizard.routing.db.RoutingDatabaseConfiguration;
import com.codahale.metrics.MetricRegistry;
import com.google.common.collect.ImmutableList;
import liquibase.Liquibase;
import liquibase.exception.LiquibaseException;
import liquibase.exception.ValidationFailedException;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;

/**
 * Provides routing support for liquibase migrations.
 */
public abstract class AbstractRoutingLiquibaseCommand<T extends Configuration> extends ConfiguredCommand<T> {
    private final RoutingDatabaseConfiguration<T> strategy;
    private final Class<T> configurationClass;
    private static final Logger LOGGER = LoggerFactory.getLogger("liquibase");

    protected AbstractRoutingLiquibaseCommand(String name, String description,
            RoutingDatabaseConfiguration<T> strategy, Class<T> configurationClass) {
        super(name, description);
        this.strategy = strategy;
        this.configurationClass = configurationClass;
    }

    @Override
    protected Class<T> getConfigurationClass() {
        return configurationClass;
    }

    @Override
    public void configure(Subparser subparser) {
        super.configure(subparser);

        subparser.addArgument("--migrations").dest("migrations-file")
                .help("the file containing the Liquibase migrations for the application");

        // only issue liquibase command to one route
        subparser.addArgument("--route").dest("routeName").help("the database route name");

        subparser.addArgument("--threads").type(Integer.class).setDefault(4).dest("threads")
                .help("number of threads to use if running against all routes");

        subparser.addArgument("--time-limit").setDefault("PT30M").dest("timeLimit")
                .help("time limit in ISO8601 period format.");
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.codahale.dropwizard.cli.ConfiguredCommand#run(com.codahale.dropwizard.setup.Bootstrap,
     * net.sourceforge.argparse4j.inf.Namespace, com.codahale.dropwizard.Configuration)
     */
    @Override
    protected void run(Bootstrap<T> bootstrap, Namespace namespace, T configuration) throws Exception {
        final String routeName = (String) namespace.get("routeName");

        LOGGER.warn("routeName = {}", routeName);

        // if route is specified then only run the liquibase command against that route
        if (null != routeName) {
            final ImmutableList<DataSourceRoute> routes = strategy.getDataSourceRoutes(configuration);

            for (DataSourceRoute route : routes) {
                if (routeName.equals(route.getRouteName())) {
                    LOGGER.warn("Running for route: {}", route.getRouteName());
                    run(route, namespace, configuration);
                }
            }
        } else {
            LOGGER.warn("running for all routes");
            // run against all routes
            final ExecutorService executor = Executors.newFixedThreadPool((Integer) namespace.get("threads"));

            final List<Future<?>> futures = new ArrayList<>();
            for (DataSourceRoute route : strategy.getDataSourceRoutes(configuration)) {
                LOGGER.warn("Add Future task for route: {}", route.getRouteName());
                futures.add(executor.submit(new ThreadableCommand<T>(this, route, namespace)));
            }

            LOGGER.warn("Shutting down executor service");
            executor.shutdown();

            final Duration timeLimit = ISOPeriodFormat.standard().parsePeriod(namespace.getString("timeLimit"))
                    .toStandardDuration();
            LOGGER.warn("Start executor.awaitTermination({})", timeLimit.getMillis());
            executor.awaitTermination(timeLimit.getMillis(), TimeUnit.MILLISECONDS);
            LOGGER.warn("End executor.awaitTermination({})", timeLimit.getMillis());

            for (final Future<?> future : futures) {
                LOGGER.warn("Start future.get() Timestamp: {}", System.currentTimeMillis());
                future.get();
                LOGGER.warn("End future.get() Timestamp: {}", System.currentTimeMillis());
            }
        }
    }

    private void run(DataSourceRoute route, Namespace namespace, T configuration) throws Exception {
        final DataSourceFactory dbConfig = route.getDatabase();
        dbConfig.setMaxSize(1);
        dbConfig.setMinSize(1);
        dbConfig.setInitialSize(1);

        try (CloseableLiquibase liquibase = openLiquibase(dbConfig, namespace)) {
            LOGGER.warn("Before running liquibase. Timestamp: {}", System.currentTimeMillis());
            run(namespace, liquibase);
            LOGGER.warn("After running liquibase. Timestamp: {}", System.currentTimeMillis() );
        } catch (ValidationFailedException e) {
            LOGGER.error("AbstractRoutingLiquibaseCommand.run() ValidationFailedException: ", e);
            e.printDescriptiveError(System.err);
            throw e;
        }
    }

    CloseableLiquibase openLiquibase(DataSourceFactory dataSourceFactory, Namespace namespace)
            throws ClassNotFoundException, SQLException, LiquibaseException {
        final ManagedDataSource dataSource = dataSourceFactory.build(new MetricRegistry(), "liquibase");
        final String migrationsFile = (String) namespace.get("migrations-file");
        if (migrationsFile == null) {
            return new CloseableLiquibase(dataSource);
        }

        LOGGER.warn("Open Liquibase with migrations-file: {}", migrationsFile );
        return new CloseableLiquibase(dataSource, migrationsFile);
    }

    protected abstract void run(Namespace namespace, Liquibase liquibase) throws Exception;
}

class ThreadableCommand<T extends Configuration> implements Runnable {
    private final AbstractRoutingLiquibaseCommand<T> command;
    private final DataSourceFactory factory;
    private final Namespace namespace;
    private static final Logger LOGGER = LoggerFactory.getLogger("liquibase");

    public ThreadableCommand(final AbstractRoutingLiquibaseCommand<T> command, final DataSourceRoute route,
            final Namespace namespace) {
        this.command = command;

        final DataSourceFactory factory = route.getDatabase();
        factory.setMaxSize(1);
        factory.setMinSize(1);
        factory.setInitialSize(1);

        this.factory = factory;
        this.namespace = namespace;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
        CloseableLiquibase liquibase = null;
        try {
            liquibase = command.openLiquibase(factory, namespace);
            LOGGER.error("Start ThreadableCommand.run() Timestamp:{}", System.currentTimeMillis());
            command.run(namespace, liquibase);
            LOGGER.error("End ThreadableCommand.run() Timestamp:{}", System.currentTimeMillis());
        } catch (ValidationFailedException e) {
            LOGGER.error("ThreadableCommand.run() ValidationFailedException", e);
            e.printDescriptiveError(System.err);
            throw new RuntimeException(e);
        } catch (Exception e) {
            LOGGER.error("ThreadableCommand.run() Exception", e);
            throw new RuntimeException(e);
        } catch (Throwable e) {
            LOGGER.error("ThreadableCommand.run() Throwable", e);
            throw new RuntimeException(e);
        } finally {
            if(liquibase != null) {
                try {
                    liquibase.close();
                } catch (Exception e) {
                    LOGGER.error("Exception when calling liquibase.close()", e);
                }
            }
        }
    }
}
