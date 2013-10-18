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

import java.util.Date;
import java.util.List;

import javax.validation.Valid;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.codahale.dropwizard.hibernate.UnitOfWork;
import com.example.barista.core.Barista;
import com.example.barista.core.Drink;
import com.example.barista.core.Recipe;
import com.example.barista.db.routing.BaristaDaoRouter;
import com.google.common.base.Optional;
import com.sun.jersey.api.ConflictException;
import com.sun.jersey.api.NotFoundException;

/**
 * RESTful barista operations.
 */
@Path("/barista")
@Produces(MediaType.APPLICATION_JSON)
public class BaristaResource {
    private final BaristaDaoRouter daoRouter;
    private final RecipeResource recipeResource;
    private final StoreResource storeResource;

    /**
     * @param daoRouter
     *            the dao router
     */
    public BaristaResource(BaristaDaoRouter daoRouter) {
        this.daoRouter = daoRouter;
        this.recipeResource = new RecipeResource(daoRouter);
        this.storeResource = new StoreResource(daoRouter);
    }

    /**
     * @return all baristas
     */
    @GET
    @UnitOfWork(readOnly = true)
    public List<Barista> allBaristas() {
        final List<Barista> baristas = daoRouter.getBaristaDAO().allBaristas();
        return baristas;
    }

    /**
     * @param name
     *            the barista's name
     * @return the barista
     * @throws NotFoundException
     *             if no barista found
     */
    @GET
    @Path("/{name}")
    @UnitOfWork(readOnly = true)
    public Barista baristaByName(@PathParam("name") String name) {
        final Optional<Barista> barista = daoRouter.getBaristaDAO().baristaByName(name);
        if (!barista.isPresent()) {
            throw new NotFoundException("No barista found with name[" + name + "] at[" + daoRouter.currentRoute() + "]");
        }

        return barista.get();
    }

    /**
     * Creates a new barista.
     * @param barista
     *            the barista
     * @return the persisted barista
     */
    @POST
    @UnitOfWork
    public Barista createBarista(@Valid Barista barista) {
        if (0 < barista.getId()) {
            throw new ConflictException("Use PUT[/barista] to update an existing barista.");
        }

        checkNameUniqueness(barista.getName());
        return daoRouter.getBaristaDAO().persist(barista);
    }

    /**
     * Updates an existing barista.
     * @param name
     *            the name
     * @param barista
     *            the barista
     * @return the persisted barista
     */
    @PUT
    @UnitOfWork
    public Barista updateBarista(@Valid Barista barista) {
        if (1 > barista.getId()) {
            throw new ConflictException("Use POST[/barista] to create a new barista.");
        }

        checkNameUniqueness(barista.getName());
        return daoRouter.getBaristaDAO().persist(barista);
    }

    /**
     * @param name
     *            the barista's name
     * @throws NotFoundException
     *             if no barista found
     */
    @DELETE
    @Path("/{name}")
    @UnitOfWork
    public void deleteBaristaByName(@PathParam("name") String name) {
        daoRouter.getBaristaDAO().delete(baristaByName(name));
    }

    /**
     * Brews a drink.
     * @param baristaName
     *            the baristaName
     * @param recipeName
     *            the recipeName
     * @return the brewed drink
     */
    @GET
    @Path("/{baristaName}/brew/{recipeName}")
    @UnitOfWork(readOnly = true)
    public Drink brew(@PathParam("baristaName") String baristaName, @PathParam("recipeName") String recipeName) {
        return new Drink(baristaByName(baristaName), new Date(), recipeName, recipeResource.recipeByName(recipeName),
                storeResource.currentStore());
    }

    /**
     * Random barista brews a drink.
     * @param recipeName
     *            the recipeName
     * @return the brewed drink
     */
    @GET
    @Path("/brew/{recipeName}")
    @UnitOfWork(readOnly = true)
    public Drink brew(@PathParam("recipeName") String recipeName) {
        return new Drink(daoRouter.getBaristaDAO().random(), new Date(), recipeName,
                recipeResource.recipeByName(recipeName), storeResource.currentStore());
    }

    /**
     * Random barista brews a drink using a random recipe.
     * @param recipeName
     *            the recipeName
     * @return the brewed drink
     */
    @GET
    @Path("/brew")
    @UnitOfWork(readOnly = true)
    public Drink brew() {
        final Recipe recipe = daoRouter.getRecipeDAO().random();
        return new Drink(daoRouter.getBaristaDAO().random(), new Date(), recipe.getName(), recipe,
                storeResource.currentStore());
    }

    /**
     * @param name
     *            the name
     * @throws ConflictException
     *             if the name is not unique
     */
    private void checkNameUniqueness(String name) {
        if (!daoRouter.getBaristaDAO().isNameUnique(name)) {
            throw new ConflictException("Barista[" + name + "] already exists at[" + daoRouter.currentRoute() + "]");
        }
    }
}
