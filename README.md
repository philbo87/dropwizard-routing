Dropwizard Routing
==================
*Dropwizard Routing is a db, hibernate (currently), and migrations replacement module that supports realtime database routing.*

Quick Start
-----------
Add dropwizard-routing jars to project

    <properties>
        <dropwizard.routing.version>0.1.0-SNAPSHOT</dropwizard.routing.version>
    </properties>

    <dependency>
        <groupId>com.astonish</groupId>
        <artifactId>dropwizard-routing-hibernate</artifactId>
        <version>${dropwizard.routing.version}</version>
        <scope>compile</scope>
    </dependency>
    <dependency>
        <groupId>com.astonish</groupId>
        <artifactId>dropwizard-routing-migrations</artifactId>
        <version>${dropwizard.routing.version}</version>
        <scope>compile</scope>
    </dependency>

Add the database routing information to the yaml

    # Database settings.
    databases:
        - routeName: dunkindonuts
        database:
            # the name of your JDBC driver
            driverClass: org.h2.Driver

            # the username
            user: sa

            # the password
            password: sa

            # the JDBC URL
            url: jdbc:h2:target/dunkindonuts
        - routeName: starbucks
        database:
            # the name of your JDBC driver
            driverClass: org.h2.Driver

            # the username
            user: sa

            # the password
            password: sa

            # the JDBC URL
            url: jdbc:h2:target/starbucks

Create a DAORouter for your project by extends AbstractHibernateDAORouter

    public class BaristaDaoRouter extends AbstractHibernateDAORouter {
    ...
        @Override
        protected ImmutableMap<Class<?>, Object> constructDAOs(SessionFactory factory) {
            final ImmutableMap.Builder<Class<?>, Object> bldr = new ImmutableMap.Builder<>();
            bldr.put(BaristaDAO.class, new BaristaDAO(factory));
            bldr.put(IngredientDAO.class, new IngredientDAO(factory));
            bldr.put(RecipeDAO.class, new RecipeDAO(factory));
            return bldr.build();
        }
    ...
    }

Finally create your application and add the hibernate and migrations bundle, instantiate your DAORouter, and add a RoutingRequestFilter

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
    
    ...

        public void run(BaristaConfiguration config, Environment environment) throws Exception {
            environment.jersey().getResourceConfig().getContainerRequestFilters().add(new RoutingRequestFilterHeaderImpl());

            final BaristaDaoRouter daoRouter = new BaristaDaoRouter(hibernateBundle.getSessionFactoryMap(),
                hibernateBundle.getDefaultRouteName());
            environment.jersey().register(new BaristaResource(daoRouter));
            environment.jersey().register(new IngredientResource(daoRouter));
            environment.jersey().register(new RecipeResource(daoRouter));
            environment.jersey().register(new StoreResource(daoRouter));
        }
    }

Please take a look at the example for more information.
