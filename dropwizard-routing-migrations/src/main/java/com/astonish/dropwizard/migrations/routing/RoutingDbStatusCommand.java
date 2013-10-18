package com.astonish.dropwizard.migrations.routing;

import com.astonish.dropwizard.db.route.RouteCapableDatabaseConfiguration;
import com.codahale.dropwizard.Configuration;
import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import liquibase.Liquibase;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;

import java.io.OutputStreamWriter;
import java.util.List;

public class RoutingDbStatusCommand<T extends Configuration> extends AbstractRoutingLiquibaseCommand<T> {
    public RoutingDbStatusCommand(RouteCapableDatabaseConfiguration<T> strategy, Class<T> configurationClass) {
        super("status", "Check for pending change sets.", strategy, configurationClass);
    }

    @Override
    public void configure(Subparser subparser) {
        super.configure(subparser);

        subparser.addArgument("-v", "--verbose").action(Arguments.storeTrue()).dest("verbose")
                .help("Output verbose information");
        subparser.addArgument("-i", "--include").action(Arguments.append()).dest("contexts")
                .help("include change sets from the given context");
    }

    @Override
    public void run(Namespace namespace, Liquibase liquibase) throws Exception {
        liquibase.reportStatus(namespace.getBoolean("verbose"), getContext(namespace), new OutputStreamWriter(
                System.out, Charsets.UTF_8));
    }

    private String getContext(Namespace namespace) {
        final List<Object> contexts = namespace.getList("contexts");
        if (contexts == null) {
            return "";
        }
        return Joiner.on(',').join(contexts);
    }
}
