package com.astonish.dropwizard.migrations.routing;

import com.astonish.dropwizard.db.route.RouteCapableDatabaseConfiguration;
import com.codahale.dropwizard.Configuration;
import liquibase.Liquibase;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;

public class RoutingDbGenerateDocsCommand<T extends Configuration> extends AbstractRoutingLiquibaseCommand<T> {
    public RoutingDbGenerateDocsCommand(RouteCapableDatabaseConfiguration<T> strategy, Class<T> configurationClass) {
        super("generate-docs", "Generate documentation about the database state.", strategy, configurationClass);
    }

    @Override
    public void configure(Subparser subparser) {
        super.configure(subparser);

        subparser.addArgument("output").nargs(1).help("output directory");
    }

    @Override
    public void run(Namespace namespace, Liquibase liquibase) throws Exception {
        liquibase.generateDocumentation(namespace.<String> getList("output").get(0));
    }
}
