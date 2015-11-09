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
package com.example.barista.resource;

import io.dropwizard.hibernate.UnitOfWork;

import java.util.List;

import javax.validation.Valid;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import com.astonish.dropwizard.routing.db.RouteStore;
import com.example.barista.core.Ingredient;
import com.example.barista.db.routing.BaristaDaoRouter;
import com.google.common.base.Optional;

/**
 * RESTful ingredient operations.
 */
@Path("/ingredient")
@Produces(MediaType.APPLICATION_JSON)
public class IngredientResource {
    private final BaristaDaoRouter daoRouter;

    /**
     * @param daoRouter
     *            the dao router
     */
    public IngredientResource(BaristaDaoRouter daoRouter) {
        this.daoRouter = daoRouter;
    }

    /**
     * @return all ingredients
     */
    @GET
    @UnitOfWork(readOnly = true)
    public List<Ingredient> allIngredients() {
        return daoRouter.getIngredientDAO().allIngredients();
    }

    /**
     * @param name
     *            the ingredient name
     * @return the ingredient
     * @throws NotFoundException
     *             if no ingredient found
     */
    @GET
    @Path("/{name}")
    @UnitOfWork(readOnly = true)
    public Ingredient ingredientByName(@PathParam("name") String name) {
        final Optional<Ingredient> ingredient = daoRouter.getIngredientDAO().ingredientByName(name);
        if (!ingredient.isPresent()) {
            throw new NotFoundException("No ingredient found with name[" + name + "] at["
                    + RouteStore.getInstance().getRoute() + "]");
        }

        return ingredient.get();
    }

    /**
     * Creates a new ingredient.
     * @param ingredient
     *            the ingredient
     * @return the persisted ingredient
     */
    @POST
    @UnitOfWork
    public Ingredient createIngredient(@Valid Ingredient ingredient) {
        if (0 < ingredient.getId()) {
            throw new WebApplicationException("Use PUT[/ingredient] to update an existing ingredient.", Status.CONFLICT);
        }

        checkNameUniqueness(ingredient.getName());
        return daoRouter.getIngredientDAO().persist(ingredient);
    }

    /**
     * Updates an existing ingredient.
     * @param name
     *            the name
     * @param ingredient
     *            the ingredient
     * @return the persisted ingredient
     */
    @PUT
    @UnitOfWork
    public Ingredient updateIngredient(@Valid Ingredient ingredient) {
        if (1 > ingredient.getId()) {
            throw new WebApplicationException("Use POST[/ingredient] to create a new barista.", Status.CONFLICT);
        }

        checkNameUniqueness(ingredient.getName());
        return daoRouter.getIngredientDAO().persist(ingredient);
    }

    /**
     * @param name
     *            the name
     * @throws NotFoundException
     *             if no ingredient found
     */
    @DELETE
    @Path("/{name}")
    @UnitOfWork
    public void deleteIngredientByName(@PathParam("name") String name) {
        daoRouter.getIngredientDAO().delete(ingredientByName(name));
    }

    /**
     * @param name
     *            the name
     * @throws ConflictException
     *             if the name is not unique
     */
    void checkNameUniqueness(String name) {
        if (!daoRouter.getIngredientDAO().isNameUnique(name)) {
            throw new WebApplicationException("Ingredient[" + name + "] already exists at["
                    + RouteStore.getInstance().getRoute() + "]", Status.CONFLICT);
        }
    }

    /**
     * @param name
     *            the name
     * @throws ConflictException
     *             if the name is not unique
     */
    void checkExclusiveNameUniqueness(Ingredient ingredient) {
        if (!daoRouter.getIngredientDAO().isNameExclusivelyUnique(ingredient)) {
            throw new WebApplicationException("Ingredient[" + ingredient.getName() + "] already exists at["
                    + RouteStore.getInstance().getRoute() + "]", Status.CONFLICT);
        }
    }
}
