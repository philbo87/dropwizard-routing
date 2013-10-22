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
import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import liquibase.Liquibase;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;

import java.io.OutputStreamWriter;
import java.util.List;

public class RoutingDbPrepareRollbackCommand<T extends Configuration> extends AbstractRoutingLiquibaseCommand<T> {
    public RoutingDbPrepareRollbackCommand(RoutingDatabaseConfiguration<T> strategy, Class<T> configurationClass) {
        super("prepare-rollback", "Generate rollback DDL scripts for pending change sets.", strategy,
                configurationClass);
    }

    @Override
    public void configure(Subparser subparser) {
        super.configure(subparser);

        subparser.addArgument("-c", "--count").dest("count").type(Integer.class)
                .help("limit script to the specified number of pending change sets");

        subparser.addArgument("-i", "--include").action(Arguments.append()).dest("contexts")
                .help("include change sets from the given context");
    }

    @Override
    public void run(Namespace namespace, Liquibase liquibase) throws Exception {
        final String context = getContext(namespace);
        final Integer count = namespace.getInt("count");
        if (count != null) {
            liquibase.futureRollbackSQL(count, context, new OutputStreamWriter(System.out, Charsets.UTF_8));
        } else {
            liquibase.futureRollbackSQL(context, new OutputStreamWriter(System.out, Charsets.UTF_8));
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
