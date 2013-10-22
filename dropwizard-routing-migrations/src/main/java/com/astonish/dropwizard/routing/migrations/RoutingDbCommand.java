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
package com.astonish.dropwizard.routing.migrations;

import com.astonish.dropwizard.routing.db.RoutingDatabaseConfiguration;
import com.google.common.collect.Maps;
import liquibase.Liquibase;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;

import io.dropwizard.Configuration;

import java.util.SortedMap;

public class RoutingDbCommand<T extends Configuration> extends AbstractRoutingLiquibaseCommand<T> {
    private static final String COMMAND_NAME_ATTR = "subcommand";
    private final SortedMap<String, AbstractRoutingLiquibaseCommand<T>> subcommands;

    public RoutingDbCommand(RoutingDatabaseConfiguration<T> strategy, Class<T> configurationClass) {
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
