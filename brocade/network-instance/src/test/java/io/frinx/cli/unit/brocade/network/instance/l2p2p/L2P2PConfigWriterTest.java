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

package io.frinx.cli.unit.brocade.network.instance.l2p2p;

import io.frinx.cli.io.Cli;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.ConfigBuilder;

public class L2P2PConfigWriterTest {

    @Test
    public void writeTest() {
        L2P2PConfigWriter writer = new L2P2PConfigWriter(Mockito.mock(Cli.class));
        Assert.assertEquals("configure terminal\n"
                + "router mpls\n"
                + "vll network 41\n"
                + "vll-mtu 9100\n"
                + "end", writer.getCommand(L2P2PConfigWriter.VLL_MTU, null,
                new ConfigBuilder().setName("network").setMtu(9100).build(),
                41L));

        Assert.assertEquals("configure terminal\n"
                + "router mpls\n"
                + "vll network 41\n"
                + "no vll-mtu 9100\n"
                + "end", writer.getCommand(L2P2PConfigWriter.VLL_MTU,
                new ConfigBuilder().setName("network").setMtu(9100).build(),
                new ConfigBuilder().setName("network").build(), 41L));

        Assert.assertEquals("configure terminal\n"
                + "router mpls\n"
                + "vll network 41\n"
                + "no vll-mtu 9100\n"
                + "end", writer.getCommand(L2P2PConfigWriter.DELETE_VLL_MTU, null,
                new ConfigBuilder().setName("network").setMtu(9100).build(), 41L));

        Assert.assertEquals("", writer.getCommand(L2P2PConfigWriter.DELETE_VLL_MTU, null,
                new ConfigBuilder().setName("network").build(), 41L));
    }
}