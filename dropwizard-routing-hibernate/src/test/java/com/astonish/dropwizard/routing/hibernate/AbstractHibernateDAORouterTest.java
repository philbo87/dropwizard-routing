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

import java.util.LinkedHashMap;
import java.util.Map;

import org.hibernate.SessionFactory;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.astonish.dropwizard.routing.db.RouteStore;
import com.astonish.dropwizard.routing.hibernate.AbstractHibernateDAORouter;
import com.google.common.collect.ImmutableMap;

/**
 * Unit tests for {@link KrakenDAORouter}.
 */
@RunWith(MockitoJUnitRunner.class)
public class AbstractHibernateDAORouterTest {
    private static final String FACTORY1_ROUTE_KEY = "factory1routekey";
    private static final String FACTORY2_ROUTE_KEY = "factory2routekey";

    private DAORouter daoRouter;

    @Mock
    private SessionFactory factory1;
    @Mock
    private SessionFactory factory2;

    @Before
    public void setup() {
        final Map<String, SessionFactory> sessionFactoryMap = new LinkedHashMap<>();
        sessionFactoryMap.put(FACTORY1_ROUTE_KEY, factory1);
        sessionFactoryMap.put(FACTORY2_ROUTE_KEY, factory2);

        daoRouter = new DAORouter(ImmutableMap.copyOf(sessionFactoryMap));
        RouteStore.getInstance().setRoute(null);
    }

    /**
     * Null sessionFactoryMap results in a {@link NullPointerException}
     */
    @Test(expected = NullPointerException.class)
    public void nullSessionFactoryMap() {
        new DAORouter(null);
    }

    /**
     * Empty sessionFactoryMap results in an {@link IllegalStateException}
     */
    @Test(expected = IllegalStateException.class)
    public void emptySessionFactoryMap() {
        new DAORouter(ImmutableMap.<String, SessionFactory> of());
    }

    /**
     * A null value in the sessionFactoryMap results in a {@link NullPointerException}
     */
    @Test(expected = NullPointerException.class)
    public void nullValueSessionFactoryMap() {
        final Map<String, SessionFactory> sessionFactoryMap = new LinkedHashMap<>();
        sessionFactoryMap.put(FACTORY1_ROUTE_KEY, null);

        new DAORouter(ImmutableMap.copyOf(sessionFactoryMap));
    }

    /**
     * Null daoClass will result in a {@link NullPointerException}
     */
    @Test(expected = NullPointerException.class)
    public void nullDAOClassGetDAO() {
        RouteStore.getInstance().setRoute(FACTORY1_ROUTE_KEY);
        daoRouter.getDAO(null);
    }

    /**
     * Verify that the daoRouter returns distince values for two different route keys.
     */
    @Test
    public void route() {
        RouteStore.getInstance().setRoute(FACTORY1_ROUTE_KEY);
        final TestDAO addressDAO1 = daoRouter.getDAO(TestDAO.class);
        RouteStore.getInstance().setRoute(FACTORY2_ROUTE_KEY);
        final TestDAO addressDAO2 = daoRouter.getDAO(TestDAO.class);

        assertNotNull(addressDAO1);
        assertNotNull(addressDAO2);
        assertNotEquals(addressDAO1, addressDAO2);
    }
}

class DAORouter extends AbstractHibernateDAORouter {
    /**
     * @param sessionFactoryMap
     */
    public DAORouter(ImmutableMap<String, SessionFactory> sessionFactoryMap) {
        super(sessionFactoryMap);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.astonish.dropwizard.hibernate.route.AHibernateDAORouter#constructDAOs(org.hibernate.SessionFactory)
     */
    @Override
    protected ImmutableMap<Class<?>, Object> constructDAOs(final SessionFactory factory) {
        final ImmutableMap.Builder<Class<?>, Object> bldr = new ImmutableMap.Builder<>();
        bldr.put(TestDAO.class, new TestDAO());
        return bldr.build();
    }
}

class TestDAO {

}
