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

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.hibernate.SessionFactory;

import com.astonish.dropwizard.routing.db.DAORouter;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;

/**
 * Responsible for holding all DAOs for all possible routes.
 */
public abstract class AbstractHibernateDAORouter extends DAORouter {
    /**
     * @param sessionFactoryMap
     *            map of {@link Optional} route keys to their corresponding {@link SessionFactory}.
     * @throws NullPointerException
     *             if sessionFactoryMap is null or any {@link Entry} in sessionFactoryMap has a null value with a
     *             non-null key
     * @throws IllegalStateException
     *             if sessionFactoryMap is empty
     */
    public AbstractHibernateDAORouter(final ImmutableMap<Optional<String>, SessionFactory> sessionFactoryMap) {
        checkNotNull(sessionFactoryMap);
        checkState(!sessionFactoryMap.isEmpty());

        final Map<Optional<String>, ImmutableMap<Class<?>, Object>> daosByRoute = new LinkedHashMap<>();
        String defaultRouteName = null;
        for (Entry<Optional<String>, SessionFactory> e : sessionFactoryMap.entrySet()) {
            SessionFactory factory = checkNotNull(e.getValue());
            daosByRoute.put(e.getKey(), constructDAOs(factory));

            if (null == defaultRouteName && e.getKey().isPresent()) {
                setDefaultRouteName(e.getKey().get());
            }
        }

        this.daosByRoute = ImmutableMap.copyOf(daosByRoute);
    }

    /**
     * Constructs DAOs from a {@link SessionFactory} and returns an {@link ImmutableMap} of the DAOs keyed by
     * {@link Class}.
     * @param factory
     *            the {@link SessionFactory}
     * @return the {@link ImmutableMap} of DAOs keyed by {@link Class}
     */
    protected abstract ImmutableMap<Class<?>, Object> constructDAOs(final SessionFactory factory);
}
