/*
 * Copyright © 2018 FRINX s.r.o. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the FRINX ODL End User License Agreement which accompanies this distribution,
 * and is available at https://frinx.io/wp-content/uploads/2017/01/EULA_ODL_20170104_v102.pdf
 */

package io.frinx.cli.unit.iosxr.netflow;

import com.google.common.collect.Sets;
import io.fd.honeycomb.rpc.RpcService;
import io.fd.honeycomb.translate.impl.read.GenericConfigListReader;
import io.fd.honeycomb.translate.impl.read.GenericConfigReader;
import io.fd.honeycomb.translate.impl.write.GenericWriter;
import io.fd.honeycomb.translate.read.registry.ModifiableReaderRegistryBuilder;
import io.fd.honeycomb.translate.write.registry.ModifiableWriterRegistryBuilder;
import io.frinx.cli.io.Cli;
import io.frinx.cli.registry.api.TranslationUnitCollector;
import io.frinx.cli.registry.api.TranslationUnitCollector.Registration;
import io.frinx.cli.registry.spi.TranslateUnit;
import io.frinx.cli.unit.iosxr.netflow.handler.EgressFlowConfigReader;
import io.frinx.cli.unit.iosxr.netflow.handler.EgressFlowConfigWriter;
import io.frinx.cli.unit.iosxr.netflow.handler.EgressFlowReader;
import io.frinx.cli.unit.iosxr.netflow.handler.IngressFlowConfigReader;
import io.frinx.cli.unit.iosxr.netflow.handler.IngressFlowConfigWriter;
import io.frinx.cli.unit.iosxr.netflow.handler.IngressFlowReader;
import io.frinx.cli.unit.iosxr.netflow.handler.NetflowInterfaceReader;
import io.frinx.cli.unit.utils.NoopCliListWriter;
import java.util.Set;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.netflow.rev180228._interface.egress.netflow.top.EgressFlowsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.netflow.rev180228._interface.ingress.netflow.top.IngressFlowsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.netflow.rev180228.netflow.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.netflow.rev180228.netflow.top.NetflowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.Device;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.cli.translate.registry.rev170520.DeviceIdBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;

public final class IosXRNetflowUnit implements TranslateUnit {

    private static final Device IOS_XR_ALL = new DeviceIdBuilder()
        .setDeviceType("ios xr")
        .setDeviceVersion("*")
        .build();

    private final TranslationUnitCollector registry;
    private Registration reg;

    // netflow IIDs
    private final InstanceIdentifier<Interface> NETFLOW_INTERFACE = io.frinx.openconfig.openconfig.netflow.IIDs.NE_IN_INTERFACE;
    private final InstanceIdentifier<org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.netflow.rev180228._interface.ingress.netflow.top.ingress.flows.ingress.flow.Config> NETFLOW_INGRESS_CONFIG_IID =
        io.frinx.openconfig.openconfig.netflow.IIDs.NE_IN_IN_IN_IN_CONFIG;
    private final InstanceIdentifier<org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.netflow.rev180228._interface.egress.netflow.top.egress.flows.egress.flow.Config> NETFLOW_EGRESS_CONFIG_IID =
        io.frinx.openconfig.openconfig.netflow.IIDs.NE_IN_IN_EG_EG_CONFIG;

    public IosXRNetflowUnit(@Nonnull final TranslationUnitCollector registry) {
        this.registry = registry;
    }

    public void init() {
        reg = registry.registerTranslateUnit(IOS_XR_ALL, this);
    }

    public void close() {
        if (reg != null) {
            reg.close();
        }
    }

    @Override
    public Set<YangModuleInfo> getYangSchemas() {
        return Sets.newHashSet(
            org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.netflow.rev180228.$YangModuleInfoImpl.getInstance()
        );
    }

    @Override
    public Set<RpcService<?, ?>> getRpcs(@Nonnull final Context context) {
        return Sets.newHashSet();
    }

    @Override
    public void provideHandlers(@Nonnull final ModifiableReaderRegistryBuilder rRegistry,
                                @Nonnull final ModifiableWriterRegistryBuilder wRegistry,
                                @Nonnull final Context context) {
        Cli cli = context.getTransport();

        provideReaders(rRegistry, cli);
        provideWriters(wRegistry, cli);
    }

    private void provideWriters(final ModifiableWriterRegistryBuilder wRegistry, final Cli cli) {
        wRegistry.add(new GenericWriter<>(io.frinx.openconfig.openconfig.netflow.IIDs.NE_IN_IN_IN_INGRESSFLOW, new NoopCliListWriter<>()));
        wRegistry.add(new GenericWriter<>(NETFLOW_INGRESS_CONFIG_IID, new IngressFlowConfigWriter(cli)));
        wRegistry.add(new GenericWriter<>(io.frinx.openconfig.openconfig.netflow.IIDs.NE_IN_IN_EG_EGRESSFLOW, new NoopCliListWriter<>()));
        wRegistry.add(new GenericWriter<>(NETFLOW_EGRESS_CONFIG_IID, new EgressFlowConfigWriter(cli)));
    }

    private void provideReaders(final ModifiableReaderRegistryBuilder rRegistry, final Cli cli) {
        rRegistry.addStructuralReader(io.frinx.openconfig.openconfig.netflow.IIDs.NETFLOW, NetflowBuilder.class);
        rRegistry.addStructuralReader(io.frinx.openconfig.openconfig.netflow.IIDs.NE_INTERFACES, org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.netflow.rev180228.netflow.interfaces.top.InterfacesBuilder.class);
        rRegistry.add(new GenericConfigListReader<>(NETFLOW_INTERFACE, new NetflowInterfaceReader(cli)));

        rRegistry.addStructuralReader(io.frinx.openconfig.openconfig.netflow.IIDs.NE_IN_IN_INGRESSFLOWS, IngressFlowsBuilder.class);
        rRegistry.add(new GenericConfigListReader<>(io.frinx.openconfig.openconfig.netflow.IIDs.NE_IN_IN_IN_INGRESSFLOW, new IngressFlowReader(cli)));
        rRegistry.add(new GenericConfigReader<>(NETFLOW_INGRESS_CONFIG_IID, new IngressFlowConfigReader(cli)));

        rRegistry.addStructuralReader(io.frinx.openconfig.openconfig.netflow.IIDs.NE_IN_IN_EGRESSFLOWS, EgressFlowsBuilder.class);
        rRegistry.add(new GenericConfigListReader<>(io.frinx.openconfig.openconfig.netflow.IIDs.NE_IN_IN_EG_EGRESSFLOW, new EgressFlowReader(cli)));
        rRegistry.add(new GenericConfigReader<>(NETFLOW_EGRESS_CONFIG_IID, new EgressFlowConfigReader(cli)));
    }

    @Override
    public String toString() {
        return "IOS XR Netflow translate unit";
    }

}