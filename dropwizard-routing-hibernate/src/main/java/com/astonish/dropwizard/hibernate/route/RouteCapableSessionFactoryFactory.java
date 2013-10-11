/**
 * This file is a proprietary trade secret of Astonish Results, Copyright 2013.
 *
 * @author Matt Carrier (mcarrier@astonish.com)
 * @created Oct 11, 2013
 */
package com.astonish.dropwizard.hibernate.route;

import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import javax.sql.DataSource;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.ServiceRegistryBuilder;
import org.hibernate.service.jdbc.connections.internal.DatasourceConnectionProviderImpl;
import org.hibernate.service.jdbc.connections.spi.ConnectionProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.dropwizard.db.DataSourceFactory;
import com.codahale.dropwizard.db.ManagedDataSource;
import com.codahale.dropwizard.hibernate.SessionFactoryManager;
import com.codahale.dropwizard.setup.Environment;
import com.google.common.collect.Sets;

/**
 * Provides nameable {@link SessionFactory}.
 */
public class RouteCapableSessionFactoryFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(RouteCapableSessionFactoryFactory.class);

    public SessionFactory build(RouteCapableHibernateBundle<?> bundle, Environment environment,
            DataSourceFactory dbConfig, List<Class<?>> entities, String name) throws ClassNotFoundException {
        final ManagedDataSource dataSource = dbConfig.build(environment.metrics(), name);
        return build(bundle, environment, dbConfig, dataSource, entities);
    }

    public SessionFactory build(RouteCapableHibernateBundle<?> bundle, Environment environment,
            DataSourceFactory dbConfig, ManagedDataSource dataSource, List<Class<?>> entities)
            throws ClassNotFoundException {
        final ConnectionProvider provider = buildConnectionProvider(dataSource, dbConfig.getProperties());
        final SessionFactory factory = buildSessionFactory(bundle, dbConfig, provider, dbConfig.getProperties(),
                entities);
        final SessionFactoryManager managedFactory = new SessionFactoryManager(factory, dataSource);
        environment.lifecycle().manage(managedFactory);
        return factory;
    }

    private ConnectionProvider buildConnectionProvider(DataSource dataSource, Map<String, String> properties) {
        final DatasourceConnectionProviderImpl connectionProvider = new DatasourceConnectionProviderImpl();
        connectionProvider.setDataSource(dataSource);
        connectionProvider.configure(properties);
        return connectionProvider;
    }

    private SessionFactory buildSessionFactory(RouteCapableHibernateBundle<?> bundle, DataSourceFactory dbConfig,
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

        final ServiceRegistry registry = new ServiceRegistryBuilder()
                .addService(ConnectionProvider.class, connectionProvider).applySettings(properties)
                .buildServiceRegistry();

        return configuration.buildSessionFactory(registry);
    }

    private void addAnnotatedClasses(Configuration configuration, Iterable<Class<?>> entities) {
        final SortedSet<String> entityClasses = Sets.newTreeSet();
        for (Class<?> klass : entities) {
            configuration.addAnnotatedClass(klass);
            entityClasses.add(klass.getCanonicalName());
        }
        LOGGER.info("Entity classes: {}", entityClasses);
    }
}