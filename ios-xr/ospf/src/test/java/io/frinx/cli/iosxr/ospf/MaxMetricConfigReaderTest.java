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


package io.frinx.cli.iosxr.ospf;

import com.google.common.collect.Lists;
import io.frinx.cli.iosxr.ospf.handler.MaxMetricConfigReader;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospf.cisco.rev171124.MAXMETRICSUMMARYLSA;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospf.types.rev170228.MAXMETRICINCLUDESTUB;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospf.types.rev170228.MAXMETRICINCLUDETYPE2EXTERNAL;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.ospfv2.rev170228.ospfv2.global.structural.global.timers.max.metric.ConfigBuilder;

public class MaxMetricConfigReaderTest {

    private final String output = "Wed Feb 14 13:31:07.209 UTC\n" +
            " max-metric router-lsa on-startup 60 include-stub summary-lsa external-lsa";


    @Test
    public void test() {
        ConfigBuilder builder = new ConfigBuilder();
        MaxMetricConfigReader.parseTimers(output, builder);
        Assert.assertEquals(60, builder.getTimeout().intValue());

        Assert.assertEquals(Lists.newArrayList(MAXMETRICINCLUDESTUB.class, MAXMETRICSUMMARYLSA.class, MAXMETRICINCLUDETYPE2EXTERNAL.class),
                builder.getInclude());
    }
}
