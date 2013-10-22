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
import com.sun.jersey.api.model.AbstractResourceMethod;
import com.sun.jersey.spi.container.ResourceMethodDispatchProvider;
import com.sun.jersey.spi.dispatch.RequestDispatcher;
import org.hibernate.SessionFactory;

/**
 * {@link ResourceMethodDispatchProvider} for {@link UnitOfWork} annotated methods using a routing Hibernate bundle.
 */
public class RoutingUnitOfWorkResourceMethodDispatchProvider implements ResourceMethodDispatchProvider {
    private final ResourceMethodDispatchProvider provider;
    private final ImmutableMap<Optional<String>, SessionFactory> sessionFactoryMap;

    /**
     * @param provider
     *            the provider
     * @param sessionFactoryMap
     *            the map of route key to session factory
     */
    public RoutingUnitOfWorkResourceMethodDispatchProvider(ResourceMethodDispatchProvider provider,
            ImmutableMap<Optional<String>, SessionFactory> sessionFactoryMap) {
        this.provider = provider;
        this.sessionFactoryMap = sessionFactoryMap;
    }

    /**
     * @return the provider
     */
    public ResourceMethodDispatchProvider getProvider() {
        return provider;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.sun.jersey.spi.container.ResourceMethodDispatchProvider#create(com.sun.jersey.api.model.AbstractResourceMethod
     * )
     */
    @Override
    public RequestDispatcher create(AbstractResourceMethod abstractResourceMethod) {
        final RequestDispatcher dispatcher = provider.create(abstractResourceMethod);
        final UnitOfWork unitOfWork = abstractResourceMethod.getMethod().getAnnotation(UnitOfWork.class);
        if (unitOfWork != null) {
            return new RoutingUnitOfWorkRequestDispatcher(unitOfWork, dispatcher, sessionFactoryMap);
        }
        return dispatcher;
    }
}
