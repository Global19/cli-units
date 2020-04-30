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

package io.frinx.cli.unit.saos8.network.instance.handler.l2vsi.ifc;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.fd.honeycomb.translate.spi.builder.BasicCheck;
import io.fd.honeycomb.translate.spi.builder.Check;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigReader;
import io.frinx.translate.unit.commons.handler.spi.CompositeReader;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.NetworkInstance;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.interfaces._interface.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.interfaces._interface.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.saos._interface.rev200414.Saos8NiIfcAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.saos._interface.rev200414.Saos8NiIfcAugBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.Ieee8023adLag;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class L2VSISubPortConfigReader implements CliConfigReader<Config, ConfigBuilder>,
        CompositeReader.Child<Config, ConfigBuilder> {

    private Cli cli;

    public L2VSISubPortConfigReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<Config> instanceIdentifier,
                                      @Nonnull ConfigBuilder configBuilder,
                                      @Nonnull ReadContext readContext) throws ReadFailedException {
        String vsName = instanceIdentifier.firstKeyOf(NetworkInstance.class).getName();
        if (isLag(instanceIdentifier, readContext, vsName)) {
            parseSubPortConfig(configBuilder, instanceIdentifier.firstKeyOf(Interface.class).getId());
        }
    }

    @VisibleForTesting
    static void parseSubPortConfig(ConfigBuilder builder, String id) {
        Saos8NiIfcAugBuilder augBuilder = new Saos8NiIfcAugBuilder();

        builder.setId(id);
        builder.setInterface(id);
        augBuilder.setType(Ieee8023adLag.class);

        builder.addAugmentation(Saos8NiIfcAug.class, augBuilder.build());
    }

    private boolean isLag(InstanceIdentifier<Config> id, ReadContext readContext,
                          String vsName) throws ReadFailedException {
        return L2VSISubPortReader.getAllIds(cli, this, id, readContext, vsName)
                .contains(id.firstKeyOf(Interface.class));
    }

    @Override
    public Check getCheck() {
        return BasicCheck.emptyCheck();
    }
}