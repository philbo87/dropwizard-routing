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

import io.dropwizard.hibernate.UnitOfWork;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.ext.Provider;

import org.glassfish.jersey.server.internal.process.MappableException;
import org.glassfish.jersey.server.model.Resource;
import org.glassfish.jersey.server.model.ResourceMethod;
import org.glassfish.jersey.server.monitoring.ApplicationEvent;
import org.glassfish.jersey.server.monitoring.ApplicationEventListener;
import org.glassfish.jersey.server.monitoring.RequestEvent;
import org.glassfish.jersey.server.monitoring.RequestEventListener;
import org.hibernate.SessionFactory;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.context.internal.ManagedSessionContext;

import com.astonish.dropwizard.routing.db.RouteStore;
import com.google.common.collect.ImmutableMap;

/**
 * An application event listener that listens for Jersey application initialization to be finished, then creates a map
 * of resource method that have metrics annotations.
 *
 * Finally, it listens for method start events, and returns a {@link RequestEventListener} that updates the relevant
 * metric for suitably annotated methods when it gets the request events indicating that the method is about to be
 * invoked, or just got done being invoked.
 */
@Provider
public class RoutingUnitOfWorkApplicationListener implements ApplicationEventListener {

    private final ImmutableMap<String, SessionFactory> sessionFactoryMap;

    ImmutableMap<String, SessionFactory> getSessionFactoryMap() {
        return sessionFactoryMap;
    }

    /**
     * Construct an application event listener using the given session factory.
     *
     * <p/>
     * When using this constructor, the {@link UnitOfWorkApplicationListener} should be added to a Jersey
     * {@code ResourceConfig} as a singleton.
     *
     * @param sessionFactory
     *            a {@link SessionFactory}
     */
    public RoutingUnitOfWorkApplicationListener(ImmutableMap<String, SessionFactory> sessionFactoryMap) {
        this.sessionFactoryMap = sessionFactoryMap;
    }

    private static class UnitOfWorkEventListener implements RequestEventListener {
        private final Map<Method, UnitOfWork> methodMap;
        private final ImmutableMap<String, SessionFactory> sessionFactoryMap;
        private UnitOfWork unitOfWork;
        private Session session;

        public UnitOfWorkEventListener(Map<Method, UnitOfWork> methodMap,
                ImmutableMap<String, SessionFactory> sessionFactoryMap) {
            this.methodMap = methodMap;
            this.sessionFactoryMap = sessionFactoryMap;
        }

        @Override
        public void onEvent(RequestEvent event) {
            if (event.getType() == RequestEvent.Type.RESOURCE_METHOD_START) {
                this.unitOfWork = this.methodMap.get(event.getUriInfo().getMatchedResourceMethod().getInvocable()
                        .getDefinitionMethod());
                if (unitOfWork != null) {
                    this.session = route().openSession();
                    try {
                        configureSession();
                        ManagedSessionContext.bind(this.session);
                        beginTransaction();
                    } catch (Throwable th) {
                        this.session.close();
                        this.session = null;
                        ManagedSessionContext.unbind(route());
                        throw th;
                    }
                }
            } else if (event.getType() == RequestEvent.Type.RESP_FILTERS_START) {
                if (this.session != null) {
                    try {
                        commitTransaction();
                    } catch (Exception e) {
                        rollbackTransaction();
                        throw new MappableException(e);
                    } finally {
                        this.session.close();
                        this.session = null;
                        ManagedSessionContext.unbind(route());
                    }
                }
            } else if (event.getType() == RequestEvent.Type.ON_EXCEPTION) {
                if (this.session != null) {
                    try {
                        rollbackTransaction();
                    } finally {
                        this.session.close();
                        this.session = null;
                        ManagedSessionContext.unbind(route());
                    }
                }
            }
        }

        private void beginTransaction() {
            if (this.unitOfWork.transactional()) {
                this.session.beginTransaction();
            }
        }

        private void configureSession() {
            this.session.setDefaultReadOnly(this.unitOfWork.readOnly());
            this.session.setCacheMode(this.unitOfWork.cacheMode());
            this.session.setFlushMode(this.unitOfWork.flushMode());
        }

        private void rollbackTransaction() {
            if (this.unitOfWork.transactional()) {
                final Transaction txn = this.session.getTransaction();
                if (txn != null && txn.isActive()) {
                    txn.rollback();
                }
            }
        }

        private void commitTransaction() {
            if (this.unitOfWork.transactional()) {
                final Transaction txn = this.session.getTransaction();
                if (txn != null && txn.isActive()) {
                    txn.commit();
                }
            }
        }

        /**
         * Retrieves the current {@link SessionFactory} based on the current route.
         * @return the current {@link SessionFactory}
         * @throws NotFoundException
         *             if a {@link SessionFactory} can not be found for the given route key
         */
        private SessionFactory route() {
            final SessionFactory factory = sessionFactoryMap.get(RouteStore.getInstance().getRoute());
            if (null == factory) {
                throw new NotFoundException("No SessionFactory found for RouteKey["
                        + RouteStore.getInstance().getRoute() + "]");
            }

            return factory;
        }
    }

    private Map<Method, UnitOfWork> methodMap = new HashMap<>();

    @Override
    public void onEvent(ApplicationEvent event) {
        if (event.getType() == ApplicationEvent.Type.INITIALIZATION_APP_FINISHED) {
            for (Resource resource : event.getResourceModel().getResources()) {
                for (ResourceMethod method : resource.getAllMethods()) {
                    registerUnitOfWorkAnnotations(method);
                }

                for (Resource childResource : resource.getChildResources()) {
                    for (ResourceMethod method : childResource.getAllMethods()) {
                        registerUnitOfWorkAnnotations(method);
                    }
                }
            }
        }
    }

    @Override
    public RequestEventListener onRequest(RequestEvent event) {
        RequestEventListener listener = new UnitOfWorkEventListener(methodMap, sessionFactoryMap);

        return listener;
    }

    private void registerUnitOfWorkAnnotations(ResourceMethod method) {
        UnitOfWork annotation = method.getInvocable().getDefinitionMethod().getAnnotation(UnitOfWork.class);

        if (annotation == null) {
            annotation = method.getInvocable().getHandlingMethod().getAnnotation(UnitOfWork.class);
        }

        if (annotation != null) {
            this.methodMap.put(method.getInvocable().getDefinitionMethod(), annotation);
        }

    }
}