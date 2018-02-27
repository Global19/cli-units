/*
 * Copyright © 2018 Frinx and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package io.frinx.cli.iosxr.ospf;

import com.google.common.collect.Lists;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.iosxr.ospf.handler.MaxMetricConfigWriter;
import java.math.BigInteger;
import java.util.concurrent.CompletableFuture;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.Protocols;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.Protocol;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.network.instance.rev170228.network.instance.top.network.instances.network.instance.protocols.ProtocolKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospf.cisco.rev171124.MAXMETRICSUMMARYLSA;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospf.types.rev170228.MAXMETRICINCLUDESTUB;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospf.types.rev170228.MAXMETRICINCLUDETYPE2EXTERNAL;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.global.structural.global.timers.max.metric.Config;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.global.structural.global.timers.max.metric.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.policy.types.rev160512.OSPF;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;

public class MaxMetricConfigWriterTest {

    private static final String WRITE_INPUT = "router ospf default\n" +
            "max-metric router-lsa on-startup 1000 summary-lsa include-stub external-lsa \n" +
            "exit\n";

    private static final String WRITE_NO_INCLUDE_INPUT = "router ospf default\n" +
            "max-metric router-lsa on-startup 1000 \n" +
            "exit\n";

    private static final String UPDATE_INPUT = "router ospf default\n" +
            "max-metric router-lsa on-startup 500 summary-lsa include-stub \n" +
            "exit\n";

    private static final String REMOVE_TIMEOUT_INPUT = "router ospf default\n" +
            "max-metric router-lsa  summary-lsa include-stub external-lsa \n" +
            "exit\n";

    private static final String DELETE_INPUT = "router ospf default\n" +
            "no max-metric router-lsa on-startup 1000 summary-lsa include-stub external-lsa \n" +
            "exit\n";

    @Mock
    private Cli cli;

    @Mock
    private WriteContext context;

    private MaxMetricConfigWriter writer;

    private ArgumentCaptor<String> response = ArgumentCaptor.forClass(String.class);

    private InstanceIdentifier piid = KeyedInstanceIdentifier.create(Protocols.class)
            .child(Protocol.class, new ProtocolKey(OSPF.class,"default"));

    // test data
    private Config data;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        Mockito.when(cli.executeAndRead(Mockito.any())).then(invocation -> CompletableFuture.completedFuture(""));

        this.writer = new MaxMetricConfigWriter(this.cli);
        initializeData();
    }

    private void initializeData() {
        data = new ConfigBuilder().setSet(true)
                .setInclude(Lists.newArrayList(MAXMETRICSUMMARYLSA.class, MAXMETRICINCLUDESTUB.class, MAXMETRICINCLUDETYPE2EXTERNAL.class))
                .setTimeout(BigInteger.valueOf(1000L))
                .build();
    }

    @Test
    public void write() throws WriteFailedException {
        this.writer.writeCurrentAttributesForType(piid, data, context);

        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertEquals(WRITE_INPUT, response.getValue());
    }

    @Test
    public void writeNoInclude() throws WriteFailedException {
        // timeout to 500, removed external-lsa
        Config newData =  new ConfigBuilder().setSet(true)
                .setTimeout(BigInteger.valueOf(1000L))
                .build();

        this.writer.writeCurrentAttributesForType(piid, newData, context);

        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertEquals(WRITE_NO_INCLUDE_INPUT, response.getValue());
    }

    @Test
    public void update() throws WriteFailedException {
        // timeout to 500, removed external-lsa
        Config newData =  new ConfigBuilder().setSet(true)
                .setInclude(Lists.newArrayList(MAXMETRICSUMMARYLSA.class, MAXMETRICINCLUDESTUB.class))
                .setTimeout(BigInteger.valueOf(500L))
                .build();

        this.writer.updateCurrentAttributesForType(piid, data, newData, context);

        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertEquals(UPDATE_INPUT, response.getValue());
    }

    @Test
    public void updateNoTimeout() throws WriteFailedException {
        // removing timeout
        Config newData = new ConfigBuilder().setSet(true)
                .setInclude(Lists.newArrayList(MAXMETRICSUMMARYLSA.class, MAXMETRICINCLUDESTUB.class, MAXMETRICINCLUDETYPE2EXTERNAL.class))
                .build();

        this.writer.updateCurrentAttributesForType(piid, data, newData, context);

        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertEquals(REMOVE_TIMEOUT_INPUT, response.getValue());
    }

    @Test
    public void delete() throws WriteFailedException {
        this.writer.deleteCurrentAttributesForType(piid, data, context);

        Mockito.verify(cli).executeAndRead(response.capture());
        Assert.assertEquals(DELETE_INPUT, response.getValue());
    }
}