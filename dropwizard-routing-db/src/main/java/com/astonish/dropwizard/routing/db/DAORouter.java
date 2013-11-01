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

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

/**
 * Responsible for holding all DAOs for all possible routes.
 */
public class DAORouter {
    protected ImmutableMap<String, ImmutableMap<Class<?>, Object>> daosByRoute = ImmutableMap
            .<String, ImmutableMap<Class<?>, Object>> of();
    private String defaultRouteName;

    /**
     * @return the defaultRouteName
     */
    public String getDefaultRouteName() {
        return defaultRouteName;
    }

    /**
     * @param defaultRouteName
     *            the defaultRouteName to set
     */
    public void setDefaultRouteName(String defaultRouteName) {
        this.defaultRouteName = defaultRouteName;
    }

    /**
     * Retrieves a type-casted DAO from the {@link DAORouter}.
     * @param daoClass
     *            the the type of DAO
     * @return the DAO associated with the current route
     */
    @SuppressWarnings("unchecked")
    public <T> T getDAO(final Class<T> daoClass) {
        checkNotNull(daoClass, "daoClass is required");

        final ImmutableMap<Class<?>, Object> routeDAOs = daosByRoute.get(RouteStore.getInstance().getRoute());
        checkState(null != routeDAOs, "No route found for Route[" + RouteStore.getInstance().getRoute() + "]");
        checkState(null != routeDAOs.get(daoClass), "Unknown DAO[" + daoClass.getSimpleName() + "]");

        return (T) routeDAOs.get(daoClass);
    }

    /**
     * Get all routes for this {@link DAORouter}.
     * @return all routes
     */
    public ImmutableSet<String> allRoutes() {
        return daosByRoute.keySet();
    }
}
