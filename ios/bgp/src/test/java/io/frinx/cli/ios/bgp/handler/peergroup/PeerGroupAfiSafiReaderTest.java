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

package io.frinx.cli.ios.bgp.handler.peergroup;

import java.util.Collections;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.peer.group.afi.safi.list.AfiSafiKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.types.rev170202.IPV4UNICAST;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstanceKey;

public class PeerGroupAfiSafiReaderTest {

    private static final String CONF_SNIPPET = "router bgp 17676\n"
            + " neighbor group_a_b-3 peer-group\n"
            + " neighbor group_a_b-3 remote-as 17676\n"
            + " neighbor group_a_b-3 transport connection-mode passive\n"
            + " neighbor group_a_b-3 password frinx18$%\n"
            + " neighbor group_a_b-3 update-source Loopback97\n"
            + " address-family ipv4\n"
            + "  neighbor group_a_b-3 send-community both\n"
            + "  neighbor group_a_b-3 route-reflector-client\n"
            + "  neighbor group_a_b-3 route-map TC-out out\n"
            + " address-family ipv6\n";

    @Test
    public void getAfiSafiKeysTest() {
        final NetworkInstanceKey networkInstanceKey = new NetworkInstanceKey("default");
        final List<AfiSafiKey> expectedKeys = Collections.singletonList(new AfiSafiKey(IPV4UNICAST.class));
        final List<AfiSafiKey> actualKeys = PeerGroupAfiSafiReader.getAfiSafiKeys(networkInstanceKey, CONF_SNIPPET);
        Assert.assertEquals(expectedKeys, actualKeys);
    }
}