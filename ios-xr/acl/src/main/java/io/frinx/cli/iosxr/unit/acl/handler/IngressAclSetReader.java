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

package io.frinx.cli.iosxr.unit.acl.handler;

import com.google.common.annotations.VisibleForTesting;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliConfigListReader;
import io.frinx.cli.unit.utils.ParsingUtils;
import java.util.List;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.ACLIPV4;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526._interface.ingress.acl.top.IngressAclSetsBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526._interface.ingress.acl.top.ingress.acl.sets.IngressAclSet;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526._interface.ingress.acl.top.ingress.acl.sets.IngressAclSetBuilder;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526._interface.ingress.acl.top.ingress.acl.sets.IngressAclSetKey;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.acl.interfaces.top.interfaces.Interface;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.InterfaceId;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class IngressAclSetReader implements CliConfigListReader<IngressAclSet, IngressAclSetKey, IngressAclSetBuilder> {

    private static final String SH_ACL_INTF = "do show running-config interface %s";
    private static final Pattern ACL_LINE = Pattern.compile("ipv4 access-group (?<name>.+) ingress");

    private final Cli cli;

    public IngressAclSetReader(Cli cli) {
        this.cli = cli;
    }

    @Nonnull
    @Override
    public List<IngressAclSetKey> getAllIds(@Nonnull InstanceIdentifier<IngressAclSet> instanceIdentifier, @Nonnull ReadContext readContext) throws ReadFailedException {
        InterfaceId interfaceId = instanceIdentifier.firstKeyOf(Interface.class).getId();
        return parseAclKeys(blockingRead(String.format(SH_ACL_INTF, interfaceId.getValue()), cli, instanceIdentifier, readContext));
    }

    @VisibleForTesting
    public static List<IngressAclSetKey> parseAclKeys(String output) {
        return ParsingUtils.parseFields(output, 0,
            ACL_LINE::matcher,
            matcher -> matcher.group("name"),
            v -> new IngressAclSetKey(v, ACLIPV4.class));
    }


    @Override
    public void merge(@Nonnull Builder<? extends DataObject> builder, @Nonnull List<IngressAclSet> list) {
        ((IngressAclSetsBuilder) builder).setIngressAclSet(list);
    }

    @Override
    public void readCurrentAttributes(@Nonnull InstanceIdentifier<IngressAclSet> instanceIdentifier, @Nonnull IngressAclSetBuilder ingressAclSetBuilder, @Nonnull ReadContext readContext) throws ReadFailedException {
        ingressAclSetBuilder.setSetName(instanceIdentifier.firstKeyOf(IngressAclSet.class).getSetName());
        ingressAclSetBuilder.setType(ACLIPV4.class);
    }
}
