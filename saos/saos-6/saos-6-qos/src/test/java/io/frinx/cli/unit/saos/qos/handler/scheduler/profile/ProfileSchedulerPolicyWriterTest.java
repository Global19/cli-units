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

package io.frinx.cli.unit.saos.qos.handler.scheduler.profile;

import io.frinx.cli.io.Cli;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler._2r3c.top.TwoRateThreeColorBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.SchedulerPolicy;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.SchedulerPolicyBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.scheduler.policy.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.scheduler.policy.SchedulersBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.scheduler.policy.schedulers.Scheduler;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.scheduler.policy.schedulers.SchedulerBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.saos.extension.rev200219.SaosQosScPolicyIfcId;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.saos.extension.rev200219.SaosQosScPolicyIfcIdBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.saos.extension.rev200219.SaosQosSchedulerAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.saos.extension.rev200219.SaosQosSchedulerAugBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.saos.extension.rev200219.SaosQosSchedulerConfig.Type;

public class ProfileSchedulerPolicyWriterTest {

    private ProfileSchedulerPolicyWriter writer;

    @Before
    public void setUp() throws Exception {
        writer = new ProfileSchedulerPolicyWriter(Mockito.mock(Cli.class));
    }

    @Test
    public void writeTemplateTest() {
        Assert.assertEquals(
                "traffic-profiling standard-profile create port 2 name Prof_1 vs VLAN111222 cir 10042\n"
                + "configuration save",
                writer.writeTemplate(createConfig("Prof_1","2",
                        createScheduler("VLAN111222", "10042")), "Prof_1"));
    }

    @Test
    public void updateTemplateTest() {
        Assert.assertEquals(
                "traffic-profiling standard-profile set port 1 profile Prof_1 cir 20048\nconfiguration save",
                writer.updateTemplate("Prof_1",
                        createConfig("Prof_1", "1", createScheduler("VLAN111222", "10048")),
                        createConfig("Prof_1", "1", createScheduler("VLAN111222", "20048"))));
    }

    @Test
    public void deleteTemplateTest() {
        Assert.assertEquals(
                "traffic-profiling standard-profile delete port 1 profile Profil_1\nconfiguration save",
                writer.deleteTemplate("Profil_1",
                        createConfig("Profil_1", "1", createScheduler("VLAN111222", "10043"))));
    }

    private SchedulerPolicy createConfig(String policyName, String ifcId, List<Scheduler> schedulerList) {
        return new SchedulerPolicyBuilder()
            .setName(policyName)
            .setConfig(new ConfigBuilder()
                .setName(policyName)
                .addAugmentation(SaosQosScPolicyIfcId.class, new SaosQosScPolicyIfcIdBuilder()
                    .setInterfaceId(ifcId)
                    .build())
                .build())
            .setSchedulers(new SchedulersBuilder()
                .setScheduler(schedulerList)
                .build())
            .build();
    }

    private List<Scheduler> createScheduler(String vsName, String cir) {
        List<Scheduler> schedulerList = new ArrayList<>();
        schedulerList.add(new SchedulerBuilder()
            .setSequence(0L)
            .setConfig(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos
                .scheduler.top.scheduler.policies.scheduler.policy.schedulers.scheduler.ConfigBuilder()
                .addAugmentation(SaosQosSchedulerAug.class, new SaosQosSchedulerAugBuilder()
                    .setVsName(vsName)
                    .setType(Type.PortPolicy)
                    .build())
                .build())
            .setTwoRateThreeColor(new TwoRateThreeColorBuilder()
                .setConfig(new org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler
                    ._2r3c.top.two.rate.three.color.ConfigBuilder()
                    .setCir(new BigInteger(cir))
                    .build())
                .build())
            .build());
        return schedulerList;
    }
}
