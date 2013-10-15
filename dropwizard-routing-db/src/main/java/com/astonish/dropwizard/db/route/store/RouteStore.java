package com.astonish.dropwizard.db.route.store;

import java.util.HashMap;
import java.util.Map;

public class RouteStore extends ThreadLocal<Map<String, Object>> {
    private static final RouteStore INSTANCE = new RouteStore();
    private static final String ROUTE = "ROUTE";

    protected RouteStore() {

    }

    public static RouteStore getInstance() {
        return INSTANCE;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.ThreadLocal#initialValue()
     */
    @Override
    protected Map<String, Object> initialValue() {
        return new HashMap<>();
    }

    /**
     * Stores the route in the store.
     * 
     * @param route
     *            the route
     */
    public void setRoute(final String route) {
        get().put(ROUTE, route);
    }

    /**
     * Retrieves the route from the store.
     * 
     * @return the stored route
     */
    public String getRoute() {
        return (String) get().get(ROUTE);
    }
}
