package com.astonish.dropwizard.migrations.routing;

import com.astonish.dropwizard.db.route.RouteCapableDatabaseConfiguration;
import com.codahale.dropwizard.Configuration;
import liquibase.Liquibase;
import net.sourceforge.argparse4j.inf.Namespace;

public class RoutingDbClearChecksumsCommand<T extends Configuration> extends AbstractRoutingLiquibaseCommand<T> {
    public RoutingDbClearChecksumsCommand(RouteCapableDatabaseConfiguration<T> strategy, Class<T> configurationClass) {
        super("clear-checksums", "Removes all saved checksums from the database log", strategy, configurationClass);
    }

    @Override
    public void run(Namespace namespace, Liquibase liquibase) throws Exception {
        liquibase.clearCheckSums();
    }
}
