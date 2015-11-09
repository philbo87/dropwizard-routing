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

import static com.google.common.base.Preconditions.checkNotNull;
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
import com.example.barista.core.Recipe;
import com.example.barista.db.routing.BaristaDaoRouter;
import com.google.common.base.Optional;

/**
 * RESTful recipe operations.
 */
@Path("/recipe")
@Produces(MediaType.APPLICATION_JSON)
public class RecipeResource {
    private final BaristaDaoRouter daoRouter;
    private final IngredientResource ingredientResource;

    /**
     * @param daoRouter
     *            the dao router
     */
    public RecipeResource(BaristaDaoRouter daoRouter) {
        this.daoRouter = daoRouter;
        this.ingredientResource = new IngredientResource(daoRouter);
    }

    /**
     * @return all recipes
     */
    @GET
    @UnitOfWork(readOnly = true)
    public List<Recipe> allRecipes() {
        return daoRouter.getRecipeDAO().allRecipes();
    }

    /**
     * @param name
     *            the recipe name
     * @return the recipe
     * @throws NotFoundException
     *             if no recipe found
     */
    @GET
    @Path("/{name}")
    @UnitOfWork(readOnly = true)
    public Recipe recipeByName(@PathParam("name") String name) {
        final Optional<Recipe> recipe = daoRouter.getRecipeDAO().recipeByName(name);
        if (!recipe.isPresent()) {
            throw new NotFoundException("No recipe found with name[" + name + "] at["
                    + RouteStore.getInstance().getRoute() + "]");
        }

        return recipe.get();
    }

    /**
     * Creates a new recipe.
     * @param recipe
     *            the recipe
     * @return the persisted recipe
     */
    @POST
    @UnitOfWork
    public Recipe createRecipe(@Valid Recipe recipe) {
        if (0 < recipe.getId()) {
            throw new WebApplicationException("Use PUT[/recipe] to update an existing recipe.", Status.CONFLICT);
        }

        checkNameUniqueness(recipe.getName());
        checkIngredients(recipe);
        return daoRouter.getRecipeDAO().persist(recipe);
    }

    /**
     * Updates an existing recipe.
     * @param name
     *            the name
     * @param recipe
     *            the recipe
     * @return the persisted recipe
     */
    @PUT
    @UnitOfWork
    public Recipe updateRecipe(@Valid Recipe recipe) {
        if (1 > recipe.getId()) {
            throw new WebApplicationException("Use POST[/recipe] to create a new barista.", Status.CONFLICT);
        }

        checkNameUniqueness(recipe.getName());
        checkIngredients(recipe);
        return daoRouter.getRecipeDAO().persist(recipe);
    }

    /**
     * @param name
     *            the name
     * @throws NotFoundException
     *             if no recipe found
     */
    @DELETE
    @Path("/{name}")
    @UnitOfWork
    public void deleteRecipeByName(@PathParam("name") String name) {
        daoRouter.getRecipeDAO().delete(recipeByName(name));
    }

    /**
     * @param name
     *            the name
     * @throws ConflictException
     *             if the name is not unique
     */
    private void checkNameUniqueness(String name) {
        if (!daoRouter.getRecipeDAO().isNameUnique(name)) {
            throw new WebApplicationException("Recipe[" + name + "] already exists at["
                    + RouteStore.getInstance().getRoute() + "]", Status.CONFLICT);
        }
    }

    /**
     * Check for unique ingredients
     * @param recipe
     *            the recipe
     */
    private void checkIngredients(Recipe recipe) {
        checkNotNull(recipe, "recipe is required");
        if (!recipe.getIngredients().isEmpty()) {
            for (Ingredient ingredient : recipe.getIngredients()) {
                if (1 > ingredient.getId()) {
                    ingredientResource.checkNameUniqueness(ingredient.getName());
                } else {
                    ingredientResource.checkExclusiveNameUniqueness(ingredient);
                }
            }
        }
    }
}
