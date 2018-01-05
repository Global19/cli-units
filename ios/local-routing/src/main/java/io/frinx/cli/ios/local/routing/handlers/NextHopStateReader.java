/*
 * Copyright © 2017 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.ios.local.routing.handlers;

import static io.frinx.openconfig.network.instance.NetworInstance.DEFAULT_NETWORK_NAME;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.ios.local.routing.common.LrReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.local.routing.rev170515.local._static.top._static.routes.Static;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.local.routing.rev170515.local._static.top._static.routes.StaticKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.local.routing.rev170515.local._static.top._static.routes._static.next.hops.NextHop;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.local.routing.rev170515.local._static.top._static.routes._static.next.hops.NextHopBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.local.routing.rev170515.local._static.top._static.routes._static.next.hops.NextHopKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.local.routing.rev170515.local._static.top._static.routes._static.next.hops.next.hop.State;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.local.routing.rev170515.local._static.top._static.routes._static.next.hops.next.hop.StateBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.Protocol;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.ProtocolKey;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class NextHopStateReader implements LrReader.LrOperReader<State, StateBuilder> {

    private static final String SHOW_IP_STATIC_ROUTE_NETWORK = "sh ip static route %s | include %s";
    private static final String SHOW_IP_STATIC_ROUTE_VRF_NETWORK = "sh ip static route vrf %s %s | include %s";

    private static final Pattern METRIC_LINE = Pattern.compile(".*\\[(?<metric>\\d+)/\\d+].*");
    private static final Pattern SPACE = Pattern.compile(" ");

    private Cli cli;

    public NextHopStateReader(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void readCurrentAttributesForType(@Nonnull InstanceIdentifier<State> id, @Nonnull StateBuilder builder,
                                             @Nonnull ReadContext ctx) throws ReadFailedException {
        ProtocolKey protocolKey = id.firstKeyOf(Protocol.class);

        StaticKey staticRouteKey = id.firstKeyOf(Static.class);
        String ipPrefix = staticRouteKey.getPrefix().getIpv4Prefix().getValue();

        NextHopKey nextHopKey = id.firstKeyOf(NextHop.class);
        String index = nextHopKey.getIndex();

        String showCommand = protocolKey.getName().equals(DEFAULT_NETWORK_NAME)
                ? String.format(SHOW_IP_STATIC_ROUTE_NETWORK, ipPrefix, switchIndex(index))
                : String.format(SHOW_IP_STATIC_ROUTE_VRF_NETWORK, protocolKey.getName(), ipPrefix, switchIndex(index));

        parseMetric(blockingRead(showCommand, cli, id, ctx), builder);

        builder.setIndex(index);
    }

    private static String switchIndex(String index) {
        return SPACE.splitAsStream(index)
                .reduce((iFace, ipAddress) -> String.format("%s %s", ipAddress, iFace))
                .orElse(index);
    }

    @VisibleForTesting
    static void parseMetric(String output, StateBuilder stateBuilder) {
        ParsingUtils.parseField(output, 0,
                METRIC_LINE::matcher,
                matcher -> Long.valueOf(matcher.group("metric")),
                stateBuilder::setMetric);
    }

    @Override
    public void merge(@Nonnull Builder<? extends DataObject> parentBuilder, @Nonnull State readValue) {
        ((NextHopBuilder) parentBuilder).setState(readValue);
    }
}
