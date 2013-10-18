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
package com.example.barista.db.routing;

import org.hibernate.SessionFactory;

import com.astonish.dropwizard.hibernate.route.AHibernateDAORouter;
import com.example.barista.db.BaristaDAO;
import com.example.barista.db.IngredientDAO;
import com.example.barista.db.RecipeDAO;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;

/**
 * Schema router for the Barista application.
 */
public class BaristaDaoRouter extends AHibernateDAORouter {
    /**
     * @param sessionFactoryMap
     */
    public BaristaDaoRouter(ImmutableMap<Optional<String>, SessionFactory> sessionFactoryMap) {
        super(sessionFactoryMap);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.astonish.dropwizard.hibernate.route.AHibernateDAORouter#constructDAOs(org.hibernate.SessionFactory)
     */
    @Override
    protected ImmutableMap<Class<?>, Object> constructDAOs(SessionFactory factory) {
        final ImmutableMap.Builder<Class<?>, Object> bldr = new ImmutableMap.Builder<>();
        bldr.put(BaristaDAO.class, new BaristaDAO(factory));
        bldr.put(IngredientDAO.class, new IngredientDAO(factory));
        bldr.put(RecipeDAO.class, new RecipeDAO(factory));
        return bldr.build();
    }

    /**
     * @return the current route's {@link RecipeDAO}
     */
    public RecipeDAO getRecipeDAO() {
        return getDAO(RecipeDAO.class);
    }

    /**
     * @return the current route's {@link IngredientDAO}
     */
    public IngredientDAO getIngredientDAO() {
        return getDAO(IngredientDAO.class);
    }

    /**
     * @return the current route's {@link BaristaDAO}
     */
    public BaristaDAO getBaristaDAO() {
        return getDAO(BaristaDAO.class);
    }
}
