package com.astonish.dropwizard.hibernate.route;

import java.util.LinkedHashMap;
import java.util.Map;

import com.astonish.dropwizard.db.route.DataSourceRoute;
import com.astonish.dropwizard.db.route.RouteCapableDatabaseConfiguration;
import com.codahale.dropwizard.Configuration;
import com.codahale.dropwizard.ConfiguredBundle;
import com.codahale.dropwizard.db.DataSourceFactory;
import com.codahale.dropwizard.hibernate.SessionFactoryHealthCheck;
import com.codahale.dropwizard.setup.Bootstrap;
import com.codahale.dropwizard.setup.Environment;
import com.fasterxml.jackson.datatype.hibernate4.Hibernate4Module;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import org.hibernate.SessionFactory;

public abstract class RouteCapableHibernateBundle<T extends Configuration> implements ConfiguredBundle<T>,
        RouteCapableDatabaseConfiguration<T> {
    private ImmutableMap<Optional<String>, SessionFactory> sessionFactoryMap;
    private final ImmutableList<Class<?>> entities;
    private final RouteCapableSessionFactoryFactory sessionFactoryFactory;

    public RouteCapableHibernateBundle(Class<?> entity, Class<?>... entities) {
        this(ImmutableList.<Class<?>> builder().add(entity).add(entities).build(),
                new RouteCapableSessionFactoryFactory());
    }

    public RouteCapableHibernateBundle(ImmutableList<Class<?>> entities,
            RouteCapableSessionFactoryFactory sessionFactoryFactory) {
        this.entities = entities;
        this.sessionFactoryFactory = sessionFactoryFactory;
    }

    @Override
    public final void initialize(Bootstrap<?> bootstrap) {
        bootstrap.getObjectMapper().registerModule(new Hibernate4Module());
    }

    protected void configure(org.hibernate.cfg.Configuration configuration) {
    }

    /**
     * @return the sessionFactoryMap
     */
    public ImmutableMap<Optional<String>, SessionFactory> getSessionFactoryMap() {
        return ImmutableMap.copyOf(sessionFactoryMap);
    }

    @Override
    public final void run(T configuration, Environment environment) throws Exception {
        final Map<Optional<String>, SessionFactory> sessionFactories = new LinkedHashMap<>();
        for (DataSourceRoute route : getDataSourceRoutes(configuration)) {
            final String routeKey = route.getRouteName();
            final DataSourceFactory dbConfig = route.getDatabase();

            final SessionFactory sessionFactory = sessionFactoryFactory.build(this, environment, dbConfig, entities,
                    routeKey);
            environment.healthChecks().register(routeKey,
                    new SessionFactoryHealthCheck(sessionFactory, dbConfig.getValidationQuery()));

            // the primary url will be the default route when no route key is provided
            if (sessionFactories.isEmpty()) {
                sessionFactories.put(Optional.<String> absent(), sessionFactory);
            }
            sessionFactories.put(Optional.of(routeKey), sessionFactory);
        }

        this.sessionFactoryMap = ImmutableMap.copyOf(sessionFactories);
        environment.jersey().register(new RouteCapableUnitOfWorkResourceMethodDispatchAdapter(this.sessionFactoryMap));
    }
}
