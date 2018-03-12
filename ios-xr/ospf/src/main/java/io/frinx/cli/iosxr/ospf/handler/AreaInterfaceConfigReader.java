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

package io.frinx.cli.iosxr.ospf.handler;

import static io.frinx.cli.iosxr.ospf.handler.AreaInterfaceReader.realignOutput;
import static io.frinx.cli.unit.utils.ParsingUtils.NEWLINE;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.handlers.ospf.OspfReader;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.ParsingUtils;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.Protocol;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospf.types.rev170228.OspfMetric;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.area.interfaces.structure.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.area.interfaces.structure.interfaces.InterfaceBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.area.interfaces.structure.interfaces._interface.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.area.interfaces.structure.interfaces._interface.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.top.ospfv2.areas.Area;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

import javax.annotation.Nonnull;
import java.util.regex.Pattern;

public class AreaInterfaceConfigReader implements OspfReader.OspfConfigReader<Config, ConfigBuilder> {

    // TODO Once CliReader supports nested configuration subsets, switch to them
    private static final String SHOW_OSPF_INT = "do show running-config router ospf %s | utility egrep \"^ area|^  interface |^   cost\"";
    private static final Pattern COST_LINE = Pattern.compile(".*cost (?<cost>.+).*");
    private final Cli cli;

    public AreaInterfaceConfigReader(final Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributesForType(@Nonnull InstanceIdentifier<Config> instanceIdentifier, @Nonnull ConfigBuilder configBuilder, @Nonnull ReadContext readContext) throws ReadFailedException {
        final String interfaceId = instanceIdentifier.firstKeyOf(Interface.class).getId();
        final String ospfId = instanceIdentifier.firstKeyOf(Protocol.class).getName();
        final String areaId = AreaInterfaceReader.areaIdToString(instanceIdentifier.firstKeyOf(Area.class).getIdentifier());
        String output = blockingRead(String.format(SHOW_OSPF_INT, ospfId), cli, instanceIdentifier, readContext);
        parseCost(output, areaId, interfaceId, configBuilder);
    }

    @VisibleForTesting
    public static void parseCost(String output, String areaId, String interfaceId, ConfigBuilder configBuilder) {
        String areaLine = NEWLINE.splitAsStream(realignOutput(output))
                .filter(line -> line.contains(String.format("area %s", areaId)))
                .findFirst()
                .orElse("");

        String ifcLine = NEWLINE.splitAsStream(areaLine.replaceAll("interface", "\ninterface"))
                .filter(line -> line.contains(String.format("interface %s", interfaceId)))
                .findFirst()
                .orElse("");

        ParsingUtils.parseField(ifcLine,
            COST_LINE::matcher,
            matcher -> matcher.group("cost"),
            value -> configBuilder.setMetric(new OspfMetric(Integer.parseInt(value))));
    }

    @Override
    public void merge(@Nonnull Builder<? extends DataObject> parentBuilder, @Nonnull Config readValue) {
        ((InterfaceBuilder) parentBuilder).setConfig(readValue);
    }
}
