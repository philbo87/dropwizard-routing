package com.astonish.dropwizard.hibernate.route;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.sun.jersey.spi.container.ResourceMethodDispatchAdapter;
import com.sun.jersey.spi.container.ResourceMethodDispatchProvider;
import org.hibernate.SessionFactory;

import javax.ws.rs.ext.Provider;

@Provider
public class RouteCapableUnitOfWorkResourceMethodDispatchAdapter implements ResourceMethodDispatchAdapter {
    private final ImmutableMap<Optional<String>, SessionFactory> sessionFactoryMap;

    public RouteCapableUnitOfWorkResourceMethodDispatchAdapter(
            ImmutableMap<Optional<String>, SessionFactory> sessionFactoryMap) {
        this.sessionFactoryMap = sessionFactoryMap;
    }

    @Override
    public ResourceMethodDispatchProvider adapt(ResourceMethodDispatchProvider provider) {
        return new RouteCapableUnitOfWorkResourceMethodDispatchProvider(provider, sessionFactoryMap);
    }

    ImmutableMap<Optional<String>, SessionFactory> getSessionFactoryMap() {
        return sessionFactoryMap;
    }
}
