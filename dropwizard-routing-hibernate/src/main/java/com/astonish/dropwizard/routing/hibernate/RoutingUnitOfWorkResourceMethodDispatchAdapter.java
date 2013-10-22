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

import com.codahale.dropwizard.hibernate.UnitOfWork;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.sun.jersey.spi.container.ResourceMethodDispatchAdapter;
import com.sun.jersey.spi.container.ResourceMethodDispatchProvider;
import org.hibernate.SessionFactory;

import javax.ws.rs.ext.Provider;

/**
 * {@link ResourceMethodDispatchAdapter} for {@link UnitOfWork} annotated methods using a routing Hibernate bundle.
 */
@Provider
public class RoutingUnitOfWorkResourceMethodDispatchAdapter implements ResourceMethodDispatchAdapter {
    private final ImmutableMap<Optional<String>, SessionFactory> sessionFactoryMap;

    /**
     * @param sessionFactoryMap
     *            the map of route key to {@link SessionFactory}
     */
    public RoutingUnitOfWorkResourceMethodDispatchAdapter(
            ImmutableMap<Optional<String>, SessionFactory> sessionFactoryMap) {
        this.sessionFactoryMap = sessionFactoryMap;
    }

    /**
     * @return the sessionFactoryMap
     */
    ImmutableMap<Optional<String>, SessionFactory> getSessionFactoryMap() {
        return sessionFactoryMap;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sun.jersey.spi.container.ResourceMethodDispatchAdapter#adapt(com.sun.jersey.spi.container.
     * ResourceMethodDispatchProvider)
     */
    @Override
    public ResourceMethodDispatchProvider adapt(ResourceMethodDispatchProvider provider) {
        return new RoutingUnitOfWorkResourceMethodDispatchProvider(provider, sessionFactoryMap);
    }
}
