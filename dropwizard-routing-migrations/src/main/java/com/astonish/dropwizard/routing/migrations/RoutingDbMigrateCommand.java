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

import io.dropwizard.Configuration;

import java.io.OutputStreamWriter;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.astonish.dropwizard.routing.db.RoutingDatabaseConfiguration;
import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import liquibase.Liquibase;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;

public class RoutingDbMigrateCommand<T extends Configuration> extends AbstractRoutingLiquibaseCommand<T> {
    private static final Logger LOGGER = LoggerFactory.getLogger("RoutingDbMigrateCommand");

    public RoutingDbMigrateCommand(RoutingDatabaseConfiguration<T> strategy, Class<T> configurationClass) {
        super("migrate", "Apply all pending change sets.", strategy, configurationClass);
    }

    @Override
    public void configure(Subparser subparser) {
        super.configure(subparser);

        subparser.addArgument("-n", "--dry-run").action(Arguments.storeTrue()).dest("dry-run")
                .setDefault(Boolean.FALSE).help("output the DDL to stdout, don't run it");

        subparser.addArgument("-c", "--count").type(Integer.class).dest("count")
                .help("only apply the next N change sets");

        subparser.addArgument("-i", "--include").action(Arguments.append()).dest("contexts")
                .help("include change sets from the given context");
    }

    @Override
    public void run(Namespace namespace, Liquibase liquibase) throws Exception {
        try {
            final String context = getContext(namespace);
            LOGGER.error("context: {}", context);
            final Integer count = namespace.getInt("count");
            LOGGER.error("count: {}", count);
            final Boolean dryRun = namespace.getBoolean("dry-run");
            LOGGER.error("dryRun: {}", dryRun);
            if (count != null) {
                if (dryRun) {
                    liquibase.update(count, context, new OutputStreamWriter(System.out, Charsets.UTF_8));
                } else {
                    liquibase.update(count, context);
                }
            } else {
                if (dryRun) {
                    liquibase.update(context, new OutputStreamWriter(System.out, Charsets.UTF_8));
                } else {
                    liquibase.update(context);
                }
            }
        } catch (Exception e) {
            LOGGER.error("Exception in RoutingDbMigrateCommand.run() - " + e.getMessage(), e);
            throw e;
        } catch (Throwable t) {
            LOGGER.error("Throwable in RoutingDbMigrateCommand.run() - " + t.getMessage(), t);
            throw t;
        }

    }

    private String getContext(Namespace namespace) {
        final List<Object> contexts = namespace.getList("contexts");
        if (contexts == null) {
            return "";
        }
        return Joiner.on(',').join(contexts);
    }
}
