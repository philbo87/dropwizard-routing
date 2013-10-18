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

import java.util.concurrent.ThreadLocalRandom;

import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import com.codahale.dropwizard.hibernate.AbstractDAO;
import com.example.barista.core.Recipe;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

/**
 * Recipe data access.
 */
public class RecipeDAO extends AbstractDAO<Recipe> {
    /**
     * @param sessionFactory
     */
    public RecipeDAO(SessionFactory sessionFactory) {
        super(sessionFactory);
    }

    /**
     * @return all recipes
     */
    public ImmutableList<Recipe> allRecipes() {
        return ImmutableList.copyOf(list(criteria().setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)));
    }

    /**
     * @param name
     *            the recipe name
     * @return the recipe
     */
    public Optional<Recipe> recipeByName(String name) {
        checkNotNull(name, "name is required");
        return Optional.fromNullable(uniqueResult(criteria().add(Restrictions.eq("name", name.toLowerCase().trim()))));
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.codahale.dropwizard.hibernate.AbstractDAO#persist(java.lang.Object)
     */
    public Recipe persist(Recipe recipe) {
        checkNotNull(recipe, "recipe is required");
        return super.persist(recipe);
    }

    /**
     * @param recipe
     *            the recipe
     */
    public void delete(Recipe recipe) {
        checkNotNull(recipe, "recipe is required");
        currentSession().delete(recipe);
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
     * @return a random recipe
     */
    public Recipe random() {
        final long recipeCount = (long) criteria().setProjection(Projections.count("id")).uniqueResult();
        return uniqueResult(criteria().add(
                Restrictions.eq("id", ThreadLocalRandom.current().nextLong(1, recipeCount + 1))));
    }
}
