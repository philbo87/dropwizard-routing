package com.astonish.dropwizard.migrations.routing;

import com.astonish.dropwizard.db.route.RouteCapableDatabaseConfiguration;
import com.codahale.dropwizard.Bundle;
import com.codahale.dropwizard.Configuration;
import com.codahale.dropwizard.setup.Bootstrap;
import com.codahale.dropwizard.setup.Environment;
import com.codahale.dropwizard.util.Generics;

public abstract class RoutingMigrationsBundle<T extends Configuration> implements Bundle,
        RouteCapableDatabaseConfiguration<T> {
    @Override
    public final void initialize(Bootstrap<?> bootstrap) {
        final Class<T> klass = Generics.getTypeParameter(getClass(), Configuration.class);
        bootstrap.addCommand(new RoutingDbCommand<>(this, klass));
    }

    @Override
    public final void run(Environment environment) {
        // nothing doing
    }
}
