package com.astonish.dropwizard.migrations.routing;

import com.astonish.dropwizard.db.route.DataSourceRoute;
import com.astonish.dropwizard.db.route.RouteCapableDatabaseConfiguration;
import com.codahale.dropwizard.Configuration;
import com.codahale.dropwizard.cli.ConfiguredCommand;
import com.codahale.dropwizard.db.DataSourceFactory;
import com.codahale.dropwizard.db.ManagedDataSource;
import com.codahale.dropwizard.migrations.CloseableLiquibase;
import com.codahale.dropwizard.setup.Bootstrap;
import com.codahale.metrics.MetricRegistry;
import com.google.common.collect.ImmutableList;

import liquibase.Liquibase;
import liquibase.exception.LiquibaseException;
import liquibase.exception.ValidationFailedException;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;

import java.sql.SQLException;

public abstract class AbstractRoutingLiquibaseCommand<T extends Configuration> extends ConfiguredCommand<T> {
    private final RouteCapableDatabaseConfiguration<T> strategy;
    private final Class<T> configurationClass;

    protected AbstractRoutingLiquibaseCommand(String name, String description,
            RouteCapableDatabaseConfiguration<T> strategy, Class<T> configurationClass) {
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

        subparser.addArgument("--route").dest("routeName").help("the database route name");
    }

    @Override
    protected void run(Bootstrap<T> bootstrap, Namespace namespace, T configuration) throws Exception {
        final String routeName = (String) namespace.get("routeName");
        if (null != routeName) {
            final ImmutableList<DataSourceRoute> routes = strategy.getDataSourceRoutes(configuration);

            for (DataSourceRoute route : routes) {
                if (routeName.equals(route.getRouteName())) {
                    run(route, namespace, configuration);
                }
            }
        } else {
            for (DataSourceRoute route : strategy.getDataSourceRoutes(configuration)) {
                run(route, namespace, configuration);
            }
        }
    }

    private void run(DataSourceRoute route, Namespace namespace, T configuration) throws Exception {
        final DataSourceFactory dbConfig = route.getDatabase();
        dbConfig.setMaxSize(1);
        dbConfig.setMinSize(1);
        dbConfig.setInitialSize(1);

        try (CloseableLiquibase liquibase = openLiquibase(dbConfig, namespace)) {
            run(namespace, liquibase);
        } catch (ValidationFailedException e) {
            e.printDescriptiveError(System.err);
            throw e;
        }
    }

    private CloseableLiquibase openLiquibase(DataSourceFactory dataSourceFactory, Namespace namespace)
            throws ClassNotFoundException, SQLException, LiquibaseException {
        final ManagedDataSource dataSource = dataSourceFactory.build(new MetricRegistry(), "liquibase");
        final String migrationsFile = (String) namespace.get("migrations-file");
        if (migrationsFile == null) {
            return new CloseableLiquibase(dataSource);
        }
        return new CloseableLiquibase(dataSource, migrationsFile);
    }

    protected abstract void run(Namespace namespace, Liquibase liquibase) throws Exception;
}
