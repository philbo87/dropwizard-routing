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
package com.astonish.dropwizard.routing.hibernate;

import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.db.ManagedDataSource;
import io.dropwizard.hibernate.SessionFactoryManager;
import io.dropwizard.setup.Environment;

import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import javax.sql.DataSource;

import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.cfg.Configuration;
import org.hibernate.engine.jdbc.connections.internal.DatasourceConnectionProviderImpl;
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;
import org.hibernate.service.ServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

/**
 * Provides nameable {@link SessionFactory}.
 */
public class RoutingSessionFactoryFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(RoutingSessionFactoryFactory.class);

    /**
     * Builds a {@link SessionFactory}
     * @param bundle
     *            the bundle
     * @param environment
     *            the environment
     * @param dbConfig
     *            the dbConfig
     * @param entities
     *            the persistent entities
     * @param name
     *            the key, will be the key for the health check
     * @return {@link SessionFactory}
     * @throws ClassNotFoundException
     */
    public SessionFactory build(RoutingHibernateBundle<?> bundle, Environment environment, DataSourceFactory dbConfig,
            List<Class<?>> entities, String name) throws ClassNotFoundException {
        final ManagedDataSource dataSource = dbConfig.build(environment.metrics(), name);
        return build(bundle, environment, dbConfig, dataSource, entities);
    }

    /**
     * Builds a {@link SessionFactory}
     * @param bundle
     *            the bundle
     * @param environment
     *            the environment
     * @param dbConfig
     *            the dbConfig
     * @param dataSource
     *            the datasource
     * @param entities
     *            the persistent entities
     * @return {@link SessionFactory}
     * @throws ClassNotFoundException
     */
    public SessionFactory build(RoutingHibernateBundle<?> bundle, Environment environment, DataSourceFactory dbConfig,
            ManagedDataSource dataSource, List<Class<?>> entities) throws ClassNotFoundException {
        final ConnectionProvider provider = buildConnectionProvider(dataSource, dbConfig.getProperties());
        final SessionFactory factory = buildSessionFactory(bundle, dbConfig, provider, dbConfig.getProperties(),
                entities);
        final SessionFactoryManager managedFactory = new SessionFactoryManager(factory, dataSource);
        environment.lifecycle().manage(managedFactory);
        return factory;
    }

    /**
     * Builds a {@link ConnectionProvider}
     * @param dataSource
     *            the datasource
     * @param properties
     *            the connection properties
     * @return {@link ConnectionProvider}
     */
    private ConnectionProvider buildConnectionProvider(DataSource dataSource, Map<String, String> properties) {
        final DatasourceConnectionProviderImpl connectionProvider = new DatasourceConnectionProviderImpl();
        connectionProvider.setDataSource(dataSource);
        connectionProvider.configure(properties);
        return connectionProvider;
    }

    /**
     * Builds a {@link SessionFactory}
     * @param bundle
     *            the bundle
     * @param dbConfig
     *            the dbconfig
     * @param connectionProvider
     *            the connection provider
     * @param properties
     *            the hibernate properties
     * @param entities
     *            the persistent entities
     * @return {@link SessionFactory}
     */
    private SessionFactory buildSessionFactory(RoutingHibernateBundle<?> bundle, DataSourceFactory dbConfig,
            ConnectionProvider connectionProvider, Map<String, String> properties, List<Class<?>> entities) {
        final Configuration configuration = new Configuration();
        configuration.setProperty(AvailableSettings.CURRENT_SESSION_CONTEXT_CLASS, "managed");
        configuration.setProperty(AvailableSettings.USE_SQL_COMMENTS,
                Boolean.toString(dbConfig.isAutoCommentsEnabled()));
        configuration.setProperty(AvailableSettings.USE_GET_GENERATED_KEYS, "true");
        configuration.setProperty(AvailableSettings.GENERATE_STATISTICS, "true");
        configuration.setProperty(AvailableSettings.USE_REFLECTION_OPTIMIZER, "true");
        configuration.setProperty(AvailableSettings.ORDER_UPDATES, "true");
        configuration.setProperty(AvailableSettings.ORDER_INSERTS, "true");
        configuration.setProperty(AvailableSettings.USE_NEW_ID_GENERATOR_MAPPINGS, "true");
        configuration.setProperty("jadira.usertype.autoRegisterUserTypes", "true");
        for (Map.Entry<String, String> property : properties.entrySet()) {
            configuration.setProperty(property.getKey(), property.getValue());
        }

        addAnnotatedClasses(configuration, entities);
        bundle.configure(configuration);

        final ServiceRegistry registry = new StandardServiceRegistryBuilder()
                .addService(ConnectionProvider.class, connectionProvider).applySettings(properties).build();

        return configuration.buildSessionFactory(registry);
    }

    /**
     * Adds annotated persistent entities.
     * @param configuration
     *            the configuration
     * @param entities
     *            the persistent entities
     */
    private void addAnnotatedClasses(Configuration configuration, Iterable<Class<?>> entities) {
        final SortedSet<String> entityClasses = Sets.newTreeSet();
        for (Class<?> klass : entities) {
            configuration.addAnnotatedClass(klass);
            entityClasses.add(klass.getCanonicalName());
        }
        LOGGER.info("Entity classes: {}", entityClasses);
    }
}
