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

package io.frinx.cli.iosxr.bgp.handler;

import com.google.common.base.Preconditions;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.handlers.bgp.BgpWriter;
import io.frinx.cli.io.Cli;
import io.frinx.openconfig.network.instance.NetworInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.bgp.rev170202.bgp.global.base.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.Protocol;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class GlobalConfigWriter implements BgpWriter<Config> {

    private Cli cli;

    public GlobalConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributesForType(InstanceIdentifier<Config> id, Config data,
                                              WriteContext writeContext) throws WriteFailedException {
        final String protName = id.firstKeyOf(Protocol.class).getName();
        String name = (protName.equals(NetworInstance.DEFAULT_NETWORK_NAME)) ? "" : " instance " + protName;
        blockingWriteAndRead(cli, id, data,
                f("router bgp %s %s", data.getAs().getValue(), name),
                "exit");
    }

    @Override
    public void updateCurrentAttributesForType(InstanceIdentifier<Config> id, Config dataBefore, Config dataAfter,
                                               WriteContext writeContext) {
        final String protName = id.firstKeyOf(Protocol.class).getName();
        Preconditions.checkArgument(dataBefore.getAs().equals(dataAfter.getAs()),
                "Cannot update AS number. Only one BGP instance in instance '{}' is allowed.", protName);
    }

    @Override
    public void deleteCurrentAttributesForType(InstanceIdentifier<Config> id, Config data,
                                               WriteContext writeContext) throws WriteFailedException {
        final String protName = id.firstKeyOf(Protocol.class).getName();
        String name = (protName.equals(NetworInstance.DEFAULT_NETWORK_NAME)) ? "" : " instance " + protName;
        blockingDeleteAndRead(cli, id,
                f("no router bgp %s %s",  data.getAs().getValue(), name));
    }
}
