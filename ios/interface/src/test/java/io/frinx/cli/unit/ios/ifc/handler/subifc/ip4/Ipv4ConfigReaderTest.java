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

package io.frinx.cli.unit.ios.ifc.handler.subifc.ip4;

import io.frinx.cli.ifc.base.handler.subifc.ipv4.AbstractIpv4ConfigReaderTest;
import io.frinx.cli.io.Cli;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.ip.rev161222.ipv4.top.ipv4.addresses.address.ConfigBuilder;

public class Ipv4ConfigReaderTest {

    @Test
    public void testParseConfigAddress() {
        ConfigBuilder actual = new ConfigBuilder();
        new Ipv4ConfigReader(Mockito.mock(Cli.class)).parseAddressConfig(actual,
                " ip address 192.168.1.44 255.255.255.0\n");
        Assert.assertEquals(AbstractIpv4ConfigReaderTest.buildData("192.168.1.44", "24"), actual.build());
    }
}