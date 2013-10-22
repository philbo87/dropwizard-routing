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
package com.astonish.dropwizard.routing.db;

import java.util.HashMap;
import java.util.Map;

/**
 * {@link ThreadLocal} store for current route key.
 */
public class RouteStore extends ThreadLocal<Map<String, Object>> {
    private static final RouteStore INSTANCE = new RouteStore();
    private static final String ROUTE = "ROUTE";

    private RouteStore() {

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
     * @param route
     *            the route
     */
    public void setRoute(final String route) {
        get().put(ROUTE, route);
    }

    /**
     * Retrieves the route from the store.
     * @return the stored route
     */
    public String getRoute() {
        return (String) get().get(ROUTE);
    }
}
