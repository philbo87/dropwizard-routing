package com.astonish.dropwizard.migrations.routing;

import com.astonish.dropwizard.db.route.RouteCapableDatabaseConfiguration;
import com.codahale.dropwizard.Configuration;
import com.google.common.collect.Maps;
import liquibase.Liquibase;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;

import java.util.SortedMap;

public class RoutingDbCommand<T extends Configuration> extends AbstractRoutingLiquibaseCommand<T> {
    private static final String COMMAND_NAME_ATTR = "subcommand";
    private final SortedMap<String, AbstractRoutingLiquibaseCommand<T>> subcommands;

    public RoutingDbCommand(RouteCapableDatabaseConfiguration<T> strategy, Class<T> configurationClass) {
        super("routingdb", "Run database migration tasks", strategy, configurationClass);
        this.subcommands = Maps.newTreeMap();
        addSubcommand(new RoutingDbCalculateChecksumCommand<>(strategy, configurationClass));
        addSubcommand(new RoutingDbClearChecksumsCommand<>(strategy, configurationClass));
        addSubcommand(new RoutingDbDropAllCommand<>(strategy, configurationClass));
        addSubcommand(new RoutingDbDumpCommand<>(strategy, configurationClass));
        addSubcommand(new RoutingDbFastForwardCommand<>(strategy, configurationClass));
        addSubcommand(new RoutingDbGenerateDocsCommand<>(strategy, configurationClass));
        addSubcommand(new RoutingDbLocksCommand<>(strategy, configurationClass));
        addSubcommand(new RoutingDbMigrateCommand<>(strategy, configurationClass));
        addSubcommand(new RoutingDbPrepareRollbackCommand<>(strategy, configurationClass));
        addSubcommand(new RoutingDbRollbackCommand<>(strategy, configurationClass));
        addSubcommand(new RoutingDbStatusCommand<>(strategy, configurationClass));
        addSubcommand(new RoutingDbTagCommand<>(strategy, configurationClass));
        addSubcommand(new RoutingDbTestCommand<>(strategy, configurationClass));
    }

    private void addSubcommand(AbstractRoutingLiquibaseCommand<T> subcommand) {
        subcommands.put(subcommand.getName(), subcommand);
    }

    @Override
    public void configure(Subparser subparser) {
        for (AbstractRoutingLiquibaseCommand<T> subcommand : subcommands.values()) {
            final Subparser cmdParser = subparser.addSubparsers().addParser(subcommand.getName())
                    .setDefault(COMMAND_NAME_ATTR, subcommand.getName()).description(subcommand.getDescription());
            subcommand.configure(cmdParser);
        }
    }

    @Override
    public void run(Namespace namespace, Liquibase liquibase) throws Exception {
        final AbstractRoutingLiquibaseCommand<T> subcommand = subcommands.get(namespace.getString(COMMAND_NAME_ATTR));
        subcommand.run(namespace, liquibase);
    }
}
