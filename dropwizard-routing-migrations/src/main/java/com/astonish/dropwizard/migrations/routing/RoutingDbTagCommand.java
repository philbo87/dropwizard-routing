package com.astonish.dropwizard.migrations.routing;

import com.astonish.dropwizard.db.route.RouteCapableDatabaseConfiguration;
import com.codahale.dropwizard.Configuration;
import liquibase.Liquibase;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;

public class RoutingDbTagCommand<T extends Configuration> extends AbstractRoutingLiquibaseCommand<T> {
    public RoutingDbTagCommand(RouteCapableDatabaseConfiguration<T> strategy, Class<T> configurationClass) {
        super("tag", "Tag the database schema.", strategy, configurationClass);
    }

    @Override
    public void configure(Subparser subparser) {
        super.configure(subparser);

        subparser.addArgument("tag-name").nargs(1).required(true).help("The tag name");
    }

    @Override
    public void run(Namespace namespace, Liquibase liquibase) throws Exception {
        liquibase.tag(namespace.<String> getList("tag-name").get(0));
    }
}
