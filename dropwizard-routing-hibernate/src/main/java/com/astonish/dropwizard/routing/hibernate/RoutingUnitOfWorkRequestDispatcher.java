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
import io.dropwizard.hibernate.UnitOfWork;

import com.astonish.dropwizard.routing.db.RouteStore;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.sun.jersey.api.NotFoundException;
import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.spi.dispatch.RequestDispatcher;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.context.internal.ManagedSessionContext;

/**
 * {@link RequestDispatcher} for {@link UnitOfWork} annotated methods using a routing Hibernate bundle.
 */
public class RoutingUnitOfWorkRequestDispatcher implements RequestDispatcher {
    private final UnitOfWork unitOfWork;
    private final RequestDispatcher dispatcher;
    private final ImmutableMap<Optional<String>, SessionFactory> sessionFactoryMap;

    /**
     * @param unitOfWork
     *            the unitOfWork
     * @param dispatcher
     *            the dispatcher
     * @param sessionFactoryMap
     *            the sessionFactoryMap
     */
    public RoutingUnitOfWorkRequestDispatcher(final UnitOfWork unitOfWork, final RequestDispatcher dispatcher,
            final ImmutableMap<Optional<String>, SessionFactory> sessionFactoryMap) {
        this.unitOfWork = unitOfWork;
        this.dispatcher = dispatcher;
        this.sessionFactoryMap = checkNotNull(sessionFactoryMap);
    }

    /**
     * @return the unitOfWork
     */
    public UnitOfWork getUnitOfWork() {
        return unitOfWork;
    }

    /**
     * @return the dispatcher
     */
    public RequestDispatcher getDispatcher() {
        return dispatcher;
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
     * @see com.sun.jersey.spi.dispatch.RequestDispatcher#dispatch(java.lang.Object,
     * com.sun.jersey.api.core.HttpContext)
     */
    @Override
    public void dispatch(Object resource, HttpContext context) {
        final Session session = route().openSession();
        try {
            configureSession(session);
            ManagedSessionContext.bind(session);
            beginTransaction(session);
            try {
                dispatcher.dispatch(resource, context);
                commitTransaction(session);
            } catch (Exception e) {
                rollbackTransaction(session);
                this.<RuntimeException> rethrow(e);
            }
        } finally {
            session.close();
            ManagedSessionContext.unbind(route());
        }
    }

    /**
     * Starts a transaction.
     * @param session
     *            the session
     */
    private void beginTransaction(Session session) {
        if (unitOfWork.transactional()) {
            session.beginTransaction();
        }
    }

    /**
     * Configures the session.
     * @param session
     *            the session
     */
    private void configureSession(Session session) {
        session.setDefaultReadOnly(unitOfWork.readOnly());
        session.setCacheMode(unitOfWork.cacheMode());
        session.setFlushMode(unitOfWork.flushMode());
    }

    /**
     * Rollsback the transaction.
     * @param session
     *            the session
     */
    private void rollbackTransaction(Session session) {
        if (unitOfWork.transactional()) {
            final Transaction txn = session.getTransaction();
            if (txn != null && txn.isActive()) {
                txn.rollback();
            }
        }
    }

    /**
     * Commits the transaction.
     * @param session
     *            the session
     */
    private void commitTransaction(Session session) {
        if (unitOfWork.transactional()) {
            final Transaction txn = session.getTransaction();
            if (txn != null && txn.isActive()) {
                txn.commit();
            }
        }
    }

    /**
     * Re-throw the exception.
     * @param e
     *            the exception
     * @throws E
     *             the exception
     */
    @SuppressWarnings("unchecked")
    private <E extends Exception> void rethrow(Exception e) throws E {
        throw (E) e;
    }

    /**
     * Retrieves the current {@link SessionFactory} based on the current route.
     * @return the current {@link SessionFactory}
     * @throws NotFoundException
     *             if a {@link SessionFactory} can not be found for the given route key
     */
    private SessionFactory route() {
        final SessionFactory factory = sessionFactoryMap
                .get(Optional.fromNullable(RouteStore.getInstance().getRoute()));
        if (null == factory) {
            throw new NotFoundException("No SessionFactory found for RouteKey[" + RouteStore.getInstance().getRoute()
                    + "]");
        }

        return factory;
    }
}
