package com.astonish.dropwizard.migrations.routing;

import com.astonish.dropwizard.db.route.RouteCapableDatabaseConfiguration;
import com.codahale.dropwizard.Configuration;
import com.google.common.base.Joiner;
import liquibase.Liquibase;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;

import java.util.List;

public class RoutingDbTestCommand<T extends Configuration> extends AbstractRoutingLiquibaseCommand<T> {
    public RoutingDbTestCommand(RouteCapableDatabaseConfiguration<T> strategy, Class<T> configurationClass) {
        super("test", "Apply and rollback pending change sets.", strategy, configurationClass);
    }

    @Override
    public void configure(Subparser subparser) {
        super.configure(subparser);

        subparser.addArgument("-i", "--include").action(Arguments.append()).dest("contexts")
                .help("include change sets from the given context");
    }

    @Override
    public void run(Namespace namespace, Liquibase liquibase) throws Exception {
        liquibase.updateTestingRollback(getContext(namespace));
    }

    private String getContext(Namespace namespace) {
        final List<Object> contexts = namespace.getList("contexts");
        if (contexts == null) {
            return "";
        }
        return Joiner.on(',').join(contexts);
    }
}
