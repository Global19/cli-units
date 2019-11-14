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

package io.frinx.cli.unit.brocade.network.instance.vlan;

import com.google.common.collect.Lists;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.brocade.network.instance.l2p2p.vlan.L2P2PVlanReader;
import io.frinx.cli.unit.brocade.network.instance.l2vsi.vlan.L2VSIVlanReader;
import io.frinx.cli.unit.brocade.network.instance.vrf.vlan.DefaultVlanReader;
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.translate.unit.commons.handler.spi.CompositeListReader;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.top.vlans.Vlan;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.top.vlans.VlanBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.top.vlans.VlanKey;

public class VlanReader extends CompositeListReader<Vlan, VlanKey, VlanBuilder>
        implements CliConfigListReader<Vlan, VlanKey, VlanBuilder> {

    public VlanReader(Cli cli) {
        super(Lists.newArrayList(
                new DefaultVlanReader(cli),
                new L2P2PVlanReader(cli),
                new L2VSIVlanReader(cli)
        ));
    }
}