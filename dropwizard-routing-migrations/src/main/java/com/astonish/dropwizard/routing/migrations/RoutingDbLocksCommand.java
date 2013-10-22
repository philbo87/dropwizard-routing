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
import com.codahale.dropwizard.Configuration;
import liquibase.Liquibase;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;

public class RoutingDbLocksCommand<T extends Configuration> extends AbstractRoutingLiquibaseCommand<T> {
    public RoutingDbLocksCommand(RoutingDatabaseConfiguration<T> strategy, Class<T> configurationClass) {
        super("locks", "Manage database migration locks", strategy, configurationClass);
    }

    @Override
    public void configure(Subparser subparser) {
        super.configure(subparser);

        subparser.addArgument("-l", "--list").dest("list").action(Arguments.storeTrue()).setDefault(Boolean.FALSE)
                .help("list all open locks");

        subparser.addArgument("-r", "--force-release").dest("release").action(Arguments.storeTrue())
                .setDefault(Boolean.FALSE).help("forcibly release all open locks");
    }

    @Override
    public void run(Namespace namespace, Liquibase liquibase) throws Exception {
        final Boolean list = namespace.getBoolean("list");
        final Boolean release = namespace.getBoolean("release");

        if (!list && !release) {
            throw new IllegalArgumentException("Must specify either --list or --force-release");
        } else if (list) {
            liquibase.reportLocks(System.out);
        } else {
            liquibase.forceReleaseLocks();
        }
    }
}
