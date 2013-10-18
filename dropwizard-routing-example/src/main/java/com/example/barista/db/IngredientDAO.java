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
package com.example.barista.db;

import static com.google.common.base.Preconditions.checkNotNull;

import org.hibernate.SessionFactory;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import com.codahale.dropwizard.hibernate.AbstractDAO;
import com.example.barista.core.Ingredient;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

/**
 * Ingredient data access.
 */
public class IngredientDAO extends AbstractDAO<Ingredient> {
    /**
     * @param sessionFactory
     */
    public IngredientDAO(SessionFactory sessionFactory) {
        super(sessionFactory);
    }

    /**
     * @return all ingredients
     */
    public ImmutableList<Ingredient> allIngredients() {
        return ImmutableList.copyOf(list(criteria()));
    }

    /**
     * @param name
     *            the ingredient name
     * @return the ingredient
     */
    public Optional<Ingredient> ingredientByName(String name) {
        checkNotNull(name, "name is required");
        return Optional.fromNullable(uniqueResult(criteria().add(Restrictions.eq("name", name.toLowerCase().trim()))));
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.codahale.dropwizard.hibernate.AbstractDAO#persist(java.lang.Object)
     */
    public Ingredient persist(Ingredient ingredient) {
        checkNotNull(ingredient, "ingredient is required");
        return super.persist(ingredient);
    }

    /**
     * @param recipe
     *            the recipe
     */
    public void delete(Ingredient ingredient) {
        checkNotNull(ingredient, "ingredient is required");
        currentSession().delete(ingredient);
    }

    /**
     * Unique name check.
     * @param name
     *            the name
     * @return true if name is unique, false otherwise
     */
    public boolean isNameUnique(String name) {
        return 0 == (long) criteria().add(Restrictions.eq("name", name)).setProjection(Projections.rowCount())
                .uniqueResult();
    }

    /**
     * Unique exclusive name check.
     * @param ingredient
     *            the ingredient
     * @return true if name is unique, false otherwise
     */
    public boolean isNameExclusivelyUnique(Ingredient ingredient) {
        return 0 == (long) criteria().add(Restrictions.eq("name", ingredient.getName()))
                .add(Restrictions.ne("id", ingredient.getId())).setProjection(Projections.rowCount()).uniqueResult();
    }
}
