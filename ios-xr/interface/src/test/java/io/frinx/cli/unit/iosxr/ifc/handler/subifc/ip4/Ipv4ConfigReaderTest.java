/*
 * Copyright © 2018 Frinx and others.
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

package io.frinx.cli.unit.iosxr.ifc.handler.subifc.ip4;

import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.ifc.base.handler.subifc.ipv4.AbstractIpv4ConfigReaderTest;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv4.top.ipv4.addresses.address.ConfigBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4AddressNoZone;

public class Ipv4ConfigReaderTest {

    private static String SH_RUN_INT_IP = "Mon Feb 12 13:00:17.954 UTC\n"
            + " ipv4 address 192.168.1.214 255.255.255.0\n";

    private static String EXPECTED_COMMAND_SUBIF =
        "show running-config interface Bundle-Ether1000.200 | include ^ ipv4 address";
    private static String EXPECTED_COMMAND =
        "show running-config interface Bundle-Ether1000 | include ^ ipv4 address";

    @Test
    public void testParseConfigAddress() {
        ConfigBuilder configBuilder = new ConfigBuilder();
        new Ipv4ConfigReader(Mockito.mock(Cli.class)).parseAddressConfig(configBuilder, SH_RUN_INT_IP,
                new Ipv4AddressNoZone("192.168.1.214"));
        Assert.assertEquals(AbstractIpv4ConfigReaderTest.buildData("192.168.1.214", "24"), configBuilder.build());
    }

    @Test
    public void testGetReadCommand() {
        Ipv4ConfigReader reader = new Ipv4ConfigReader(Mockito.mock(Cli.class));
        Assert.assertEquals(EXPECTED_COMMAND_SUBIF, reader.getReadCommand("Bundle-Ether1000", 200L));
        Assert.assertEquals(EXPECTED_COMMAND, reader.getReadCommand("Bundle-Ether1000", 0L));
    }
}
