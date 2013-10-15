package com.astonish.dropwizard.jersey.route.filter;

import javax.ws.rs.core.MultivaluedMap;

import com.astonish.dropwizard.db.route.store.RouteStore;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;

/**
 * Checks all requests for a RouteKey header and stores the route in the {@link RouteStore}.
 */
public class RouteRequestFilterHeaderImpl implements ContainerRequestFilter {
    public static final String HEADER_NAME = "RouteKey";

    /*
     * (non-Javadoc)
     * 
     * @see com.sun.jersey.spi.container.ContainerRequestFilter#filter(com.sun.jersey.spi.container.ContainerRequest)
     */
    @Override
    public ContainerRequest filter(ContainerRequest request) {
        final MultivaluedMap<String, String> headerMap = request.getRequestHeaders();
        RouteStore.getInstance().setRoute(headerMap.getFirst(HEADER_NAME));

        return request;
    }
}
