/*
 * Copyright © 2019 Frinx and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.frinx.cli.unit.iosxr.oam.handler.domain;

import io.fd.honeycomb.translate.util.RWUtils;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.oam.rev190619.oam.top.oam.cfm.domains.Domain;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.oam.rev190619.oam.top.oam.cfm.domains.domain.mas.ma.Config;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class CfmMaConfigWriter implements CliWriter<Config> {
    private Cli cli;

    private static final String CREATE_TEMPLATE = "ethernet cfm\n"
        + "domain {$domain.domain_name} level {$domain.level.value}\n"
        + "service {$config.ma_name} down-meps\n"
        + "root";

    private static final String DELETE_TEMPLATE = "ethernet cfm\n"
        + "domain {$domain.domain_name} level {$domain.level.value}\n"
        + "no service {$config.ma_name}\n"
        + "root";

    public CfmMaConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(
        InstanceIdentifier<Config> id,
        Config config,
        WriteContext writeContext) throws WriteFailedException {

        org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.oam.rev190619.oam.top.oam.cfm.domains.domain.Config
            domain = readAfterDomainConfig(id, writeContext);

        blockingWriteAndRead(cli, id, config,
                fT(CREATE_TEMPLATE,
                    "domain", domain,
                    "config", config));
    }

    @Override
    public void updateCurrentAttributes(
        InstanceIdentifier<Config> id,
        Config dataBefore, Config dataAfter,
        WriteContext writeContext) throws WriteFailedException {

        writeCurrentAttributes(id, dataAfter, writeContext);
    }

    @Override
    public void deleteCurrentAttributes(
        InstanceIdentifier<Config> id,
        Config config,
        WriteContext writeContext) throws WriteFailedException {

        org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.oam.rev190619.oam.top.oam.cfm.domains.domain.Config
            domain = readBeforeDomainConfig(id, writeContext);

        blockingDeleteAndRead(cli, id,
            fT(DELETE_TEMPLATE,
                "domain", domain,
                "config", config));
    }

    private static org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.oam.rev190619.oam.top.oam.cfm.domains
        .domain.Config readBeforeDomainConfig(InstanceIdentifier<Config> id, WriteContext writeContext) {

        InstanceIdentifier<org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.oam.rev190619.oam.top.oam.cfm
            .domains.domain.Config> configId = RWUtils.cutId(id, Domain.class)
                .child(org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.oam.rev190619.oam.top.oam.cfm.domains
                    .domain.Config.class);

        return writeContext.readBefore(configId).orNull();
    }

    private static org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.oam.rev190619.oam.top.oam.cfm.domains
        .domain.Config readAfterDomainConfig(InstanceIdentifier<Config> id, WriteContext writeContext) {

        InstanceIdentifier<org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.oam.rev190619.oam.top.oam.cfm
            .domains.domain.Config> configId = RWUtils.cutId(id, Domain.class)
                .child(org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.oam.rev190619.oam.top.oam.cfm.domains
                    .domain.Config.class);

        return writeContext.readAfter(configId).orNull();
    }
}
