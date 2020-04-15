/*
 * Copyright © 2020 Frinx and others.
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

package io.frinx.cli.unit.saos.ifc.handler;

import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.saos.ifc.handler.vlan.InterfaceVlanReader;
import java.util.Arrays;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.saos.extension.rev200205.IfSaosAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.saos.extension.rev200205.IfSaosAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.saos.extension.rev200205.SaosIfExtensionConfig.AcceptableFrameType;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.saos.extension.rev200205.SaosIfExtensionConfig.IngressToEgressQmap;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.saos.extension.rev200205.SaosIfExtensionConfig.PhysicalType;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.saos.extension.rev200205.SaosIfExtensionConfig.VlanEthertypePolicy;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.VlanSwitchedConfig;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.types.rev170714.VlanId;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.EthernetCsmacd;

public class InterfaceConfigReaderTest {

    private static final String SH_PORT_4 = "port disable port 4\n"
            + "port set port 4 mode rj45\n"
            + "port set port 4 max-frame-size 9216 description \"two words\" ingress-to-egress-qmap NNI-NNI\n"
            + "port set port 4 acceptable-frame-type tagged-only vs-ingress-filter on\n"
            + "virtual-circuit ethernet set port 4 vlan-ethertype-policy vlan-tpid\n"
            + "aggregation set port 4 activity passive\n"
            + "traffic-profiling set port 4 mode advanced\n"
            + "flow access-control set port 4 max-dynamic-macs 200 forward-unlearned off\n";

    private static final Config EXPECTED_INTERFACE_4 = new ConfigBuilder()
            .setType(EthernetCsmacd.class)
            .setEnabled(false)
            .setName("4")
            .setDescription("two words")
            .setMtu(9216)
            .addAugmentation(IfSaosAug.class, new IfSaosAugBuilder()
                    .setAcceptableFrameType(AcceptableFrameType.TaggedOnly)
                    .setPhysicalType(PhysicalType.Rj45)
                    .setVsIngressFilter(true)
                    .setVlanEthertypePolicy(VlanEthertypePolicy.VlanTpid)
                    .setIngressToEgressQmap(IngressToEgressQmap.NNINNI)
                    .setMaxDynamicMacs(200)
                    .setForwardUnlearned(false)
                    .build())
            .build();

    private static final String SH_PORT_1 = "port disable port 1\n"
            + "port set port 1 mode rj45\n"
            + "port set port 1 max-frame-size 9216 description TEST123\n"
            + "port set port 1 acceptable-frame-type tagged-only vs-ingress-filter on\n"
            + "aggregation set port 1 activity passive\n"
            + "traffic-profiling set port 1 mode advanced\n"
            + "flow access-control set port 1 max-dynamic-macs 200\n";

    private static final Config EXPECTED_INTERFACE_1 = new ConfigBuilder()
            .setType(EthernetCsmacd.class)
            .setEnabled(false)
            .setName("1")
            .setDescription("TEST123")
            .setMtu(9216)
            .addAugmentation(IfSaosAug.class, new IfSaosAugBuilder()
                    .setAcceptableFrameType(AcceptableFrameType.TaggedOnly)
                    .setPhysicalType(PhysicalType.Rj45)
                    .setVsIngressFilter(true)
                    .setMaxDynamicMacs(200)
                    .setForwardUnlearned(true)
                    .build())
            .build();

    private static final String SH_PORT_3 = "port set port 3 max-frame-size 9216 description \"Space test\"\n"
            + "port set port 4 acceptable-frame-type all vs-ingress-filter off\n"
            + "aggregation set port 4 activity passive\n"
            + "traffic-profiling set port 4 mode advanced\n"
            + "flow access-control set port 3 forward-unlearned off\n";

    private static final Config EXPECTED_INTERFACE_3 = new ConfigBuilder()
            .setType(EthernetCsmacd.class)
            .setEnabled(true)
            .setName("3")
            .setDescription("Space test")
            .setMtu(9216)
            .addAugmentation(IfSaosAug.class, new IfSaosAugBuilder()
                    .setAcceptableFrameType(AcceptableFrameType.All)
                    .setVsIngressFilter(false)
                    .setForwardUnlearned(false)
                    .build())
            .build();

    private static final String SH_PORT_WITHOUT_AC = "port disable port 1\n"
            + "port set port 1 mode rj45\n"
            + "port set port 1 max-frame-size 9216 description TEST123\n"
            + "port set port 1 acceptable-frame-type tagged-only vs-ingress-filter on\n"
            + "aggregation set port 1 activity passive\n"
            + "vlan add vlan 25,50 port 1\n"
            + "vlan remove vlan 1234 port 1\n"
            + "traffic-profiling set port 1 mode advanced\n";

    private static final String SH_PORT_2_VLANS = "vlan add vlan 2-4,8 port 1\n"
            + "vlan add vlan 11 port 1\n"
            + "vlan add vlan 199 port 10\n";

    private static final org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714.vlan.switched
            .top.switched.vlan.Config EXPECTED_INTERFACE_2_TRUNK_VLANS = new org.opendaylight.yang.gen.v1
            .http.frinx.openconfig.net.yang.vlan.rev170714.vlan.switched.top.switched.vlan.ConfigBuilder()
            .setTrunkVlans(Arrays.asList(
                new VlanSwitchedConfig.TrunkVlans(new VlanId(2)),
                new VlanSwitchedConfig.TrunkVlans(new VlanId(3)),
                new VlanSwitchedConfig.TrunkVlans(new VlanId(4)),
                new VlanSwitchedConfig.TrunkVlans(new VlanId(8)),
                new VlanSwitchedConfig.TrunkVlans(new VlanId(11))
            ))
            .build();

    @Test
    public void testParseInterface() {
        ConfigBuilder parsed = new ConfigBuilder();
        new InterfaceConfigReader(Mockito.mock(Cli.class))
                .parseInterface(SH_PORT_4, parsed,"4");
        Assert.assertEquals(EXPECTED_INTERFACE_4, parsed.build());

        new InterfaceConfigReader(Mockito.mock(Cli.class))
                .parseInterface(SH_PORT_1, parsed, "1");
        Assert.assertEquals(EXPECTED_INTERFACE_1, parsed.build());

        parsed = new ConfigBuilder();
        new InterfaceConfigReader(Mockito.mock(Cli.class))
                .parseInterface(SH_PORT_3, parsed, "3");
        Assert.assertEquals(EXPECTED_INTERFACE_3, parsed.build());

        org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.vlan.rev170714
                .vlan.switched.top.switched.vlan.ConfigBuilder parsedVlans = new org.opendaylight.yang.gen.v1
                .http.frinx.openconfig.net.yang.vlan.rev170714.vlan.switched.top.switched.vlan.ConfigBuilder();
        new InterfaceVlanReader(Mockito.mock(Cli.class))
                .setVlanIds(SH_PORT_2_VLANS, parsedVlans, "1");
        Assert.assertEquals(EXPECTED_INTERFACE_2_TRUNK_VLANS, parsedVlans.build());
    }

    @Test
    public void testNoAccessControl() {
        IfSaosAug expectedIfBuilder = new IfSaosAugBuilder().build();
        IfSaosAugBuilder ifSaosAugBuilderActual = new IfSaosAugBuilder();

        Assert.assertEquals(expectedIfBuilder, InterfaceConfigReader
                .setAccessControlAttributes(SH_PORT_WITHOUT_AC, ifSaosAugBuilderActual));
    }
}