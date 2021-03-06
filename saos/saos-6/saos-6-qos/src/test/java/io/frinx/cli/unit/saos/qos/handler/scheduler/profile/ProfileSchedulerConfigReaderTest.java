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
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.rev161216.qos.scheduler.top.scheduler.policies.scheduler.policy.schedulers.scheduler.ConfigBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.saos.extension.rev200219.SaosQosSchedulerAug;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.qos.saos.extension.rev200219.SaosQosSchedulerConfig.Type;

public class ProfileSchedulerConfigReaderTest {

    private static final String OUTPUT =
        "traffic-profiling standard-profile create port 1 profile 1 name CIA_CoS0 cir 50048 eir 0 cbs 8 ebs 0\n"
        + "traffic-profiling standard-profile set port 1 profile CIA_CoS0 green-remark-rcos 0 yellow-remark-rcos 0\n"
        + "traffic-profiling standard-profile create port 1 profile 2 name V40 cir 10048 ebs 0 vs VLAN111222\n"
        + "traffic-profiling standard-profile create port 4 profile 1 name Prof_1 cir 10048 eir 0 cbs 128 ebs 0\n"
        + "traffic-profiling standard-profile create port 5 profile 1 name Test1 cir 10048 eir 0 cbs 128 ebs 0\n"
        + "traffic-profiling standard-profile create port 5 profile 2 name Test2 cir 10048 eir 0 cbs 128 ebs 0\n"
        + "traffic-profiling enable port 2\n"
        + "traffic-profiling enable port 6\n"
        + "traffic-profiling enable port 8\n"
        + "traffic-profiling enable\n";

    @Test
    public void parseSchedulerConfigTest() {
        ProfileSchedulerConfigReader reader = new ProfileSchedulerConfigReader(Mockito.mock(Cli.class));
        ConfigBuilder configBuilder = new ConfigBuilder();

        reader.parseSchedulerConfig(OUTPUT, configBuilder, "V40", 0L);
        Assert.assertEquals(Type.PortPolicy, configBuilder.getAugmentation(SaosQosSchedulerAug.class).getType());
        Assert.assertEquals("VLAN111222",
                configBuilder.getAugmentation(SaosQosSchedulerAug.class).getVsName());

        reader.parseSchedulerConfig(OUTPUT, configBuilder, "Prof_1", 0L);
        Assert.assertEquals(Type.PortPolicy, configBuilder.getAugmentation(SaosQosSchedulerAug.class).getType());
        Assert.assertEquals(null,
                configBuilder.getAugmentation(SaosQosSchedulerAug.class).getVsName());

        reader.parseSchedulerConfig(OUTPUT, configBuilder, "Prof_1", 0L);
        Assert.assertEquals(Type.PortPolicy, configBuilder.getAugmentation(SaosQosSchedulerAug.class).getType());
        Assert.assertEquals(null,
                configBuilder.getAugmentation(SaosQosSchedulerAug.class).getVsName());

        reader.parseSchedulerConfig(OUTPUT, configBuilder, "Test2", 0L);
        Assert.assertEquals(Type.PortPolicy, configBuilder.getAugmentation(SaosQosSchedulerAug.class).getType());
        Assert.assertEquals(null,
                configBuilder.getAugmentation(SaosQosSchedulerAug.class).getVsName());
    }
}
