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
import liquibase.change.CheckSum;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RoutingDbCalculateChecksumCommand<T extends Configuration> extends AbstractRoutingLiquibaseCommand<T> {
    private static final Logger LOGGER = LoggerFactory.getLogger("liquibase");

    public RoutingDbCalculateChecksumCommand(RoutingDatabaseConfiguration<T> strategy, Class<T> configurationClass) {
        super("calculate-checksum", "Calculates and prints a checksum for a change set", strategy, configurationClass);
    }

    @Override
    public void configure(Subparser subparser) {
        super.configure(subparser);

        subparser.addArgument("id").nargs(1).help("change set id");
        subparser.addArgument("author").nargs(1).help("author name");
    }

    @Override
    public void run(Namespace namespace, Liquibase liquibase) throws Exception {
        final CheckSum checkSum = liquibase.calculateCheckSum("migrations.xml",
                namespace.<String> getList("id").get(0), namespace.<String> getList("author").get(0));
        LOGGER.info("checksum = {}", checkSum);
    }
}
