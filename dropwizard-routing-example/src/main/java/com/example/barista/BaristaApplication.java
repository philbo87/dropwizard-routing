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
package com.example.barista;

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

import com.astonish.dropwizard.routing.db.DataSourceRoute;
import com.astonish.dropwizard.routing.db.filter.RoutingRequestFilterHeaderImpl;
import com.astonish.dropwizard.routing.hibernate.RoutingHibernateBundle;
import com.astonish.dropwizard.routing.migrations.RoutingMigrationsBundle;
import com.example.barista.core.Barista;
import com.example.barista.core.Ingredient;
import com.example.barista.core.Recipe;
import com.example.barista.db.routing.BaristaDaoRouter;
import com.example.barista.resource.BaristaResource;
import com.example.barista.resource.IngredientResource;
import com.example.barista.resource.RecipeResource;
import com.example.barista.resource.StoreResource;
import com.google.common.collect.ImmutableList;

/**
 * The Barista application.
 */
public class BaristaApplication extends Application<BaristaConfiguration> {
    private final RoutingHibernateBundle<BaristaConfiguration> hibernateBundle = new RoutingHibernateBundle<BaristaConfiguration>(
            Barista.class, Ingredient.class, Recipe.class) {
        @Override
        public ImmutableList<DataSourceRoute> getDataSourceRoutes(BaristaConfiguration configuration) {
            return configuration.getDatabases();
        }
    };

    private final RoutingMigrationsBundle<BaristaConfiguration> migrationsBundle = new RoutingMigrationsBundle<BaristaConfiguration>() {
        @Override
        public ImmutableList<DataSourceRoute> getDataSourceRoutes(BaristaConfiguration configuration) {
            return configuration.getDatabases();
        }
    };

    /*
     * (non-Javadoc)
     * 
     * @see com.codahale.dropwizard.Application#getName()
     */
    @Override
    public String getName() {
        return "barista";
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.codahale.dropwizard.Application#initialize(com.codahale.dropwizard.setup.Bootstrap)
     */
    @Override
    public void initialize(Bootstrap<BaristaConfiguration> bootstrap) {
        bootstrap.addBundle(hibernateBundle);
        bootstrap.addBundle(migrationsBundle);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.codahale.dropwizard.Application#run(com.codahale.dropwizard.Configuration,
     * com.codahale.dropwizard.setup.Environment)
     */
    @Override
    @SuppressWarnings("unchecked")
    public void run(BaristaConfiguration config, Environment environment) throws Exception {
        environment.jersey().getResourceConfig().getContainerRequestFilters().add(new RoutingRequestFilterHeaderImpl());

        final BaristaDaoRouter daoRouter = new BaristaDaoRouter(hibernateBundle.getSessionFactoryMap());
        environment.jersey().register(new BaristaResource(daoRouter));
        environment.jersey().register(new IngredientResource(daoRouter));
        environment.jersey().register(new RecipeResource(daoRouter));
        environment.jersey().register(new StoreResource());
    }

    public static void main(String[] args) throws Exception {
        new BaristaApplication().run(args);
    }
}
