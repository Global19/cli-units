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

package io.frinx.cli.unit.brocade.ifc.handler;

import com.google.common.base.Preconditions;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.frinx.cli.io.Cli;
import io.frinx.cli.unit.utils.CliWriter;
import java.util.Collections;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.interfaces.rev161222.interfaces.top.interfaces._interface.Config;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.EthernetCsmacd;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.iana._if.type.rev140508.SoftwareLoopback;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.interfaces.rev140508.InterfaceType;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public final class InterfaceConfigWriter implements CliWriter<Config> {

    private Cli cli;

    public InterfaceConfigWriter(Cli cli) {
        this.cli = cli;
    }

    @Override
    public void writeCurrentAttributes(@Nonnull InstanceIdentifier<Config> id,
                                       @Nonnull Config data,
                                       @Nonnull WriteContext writeContext) throws WriteFailedException {
        if (data.getType() == SoftwareLoopback.class) {
            writeLoopbackInterface(id, data, writeContext);
        } else {
            throw new WriteFailedException.CreateFailedException(id, data,
                    new IllegalArgumentException("Cannot create interface of type: " + data.getType()));
        }
    }

    private static final Pattern LOOPBACK_NAME_PATTERN = Pattern.compile("Loopback(?<number>[0-9]+)");

    private void writeLoopbackInterface(InstanceIdentifier<Config> id, Config data, WriteContext writeContext)
            throws WriteFailedException.CreateFailedException {

        Matcher matcher = LOOPBACK_NAME_PATTERN.matcher(data.getName());
        Preconditions.checkArgument(matcher.matches(), "Loopback name must be in format: Loopback45, not: %s",
                data.getName());

        blockingWriteAndRead(cli, id, data,
                "configure terminal",
                f("interface loopback %s", matcher.group("number")),
                f("port-name %s", data.getDescription()),
                data.isEnabled() != null && data.isEnabled() ? "enable" : "disable",
                "end");
    }

    public static final Set<Class<? extends InterfaceType>> PHYS_IFC_TYPES = Collections.singleton(EthernetCsmacd
            .class);

    public static boolean isPhysicalInterface(Config data) {
        return PHYS_IFC_TYPES.contains(data.getType());
    }

    @Override
    public void updateCurrentAttributes(@Nonnull InstanceIdentifier<Config> id,
                                        @Nonnull Config dataBefore,
                                        @Nonnull Config dataAfter,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        Preconditions.checkArgument(dataBefore.getType().equals(dataAfter.getType()),
                    "Changing interface type is not permitted. Before: %s, After: %s",
                    dataBefore.getType(), dataAfter.getType());

        if (isPhysicalInterface(dataAfter)) {
            updatePhysicalInterface(id, dataAfter, writeContext);
        } else if (dataAfter.getType() == SoftwareLoopback.class) {
            writeLoopbackInterface(id, dataAfter, writeContext);
        } else {
            throw new WriteFailedException.CreateFailedException(id, dataAfter,
                    new IllegalArgumentException("Unknown interface type: " + dataAfter.getType()));
        }
    }

    private void updatePhysicalInterface(InstanceIdentifier<Config> id, Config data, WriteContext writeContext)
            throws WriteFailedException.CreateFailedException {

        String ifcNumber = InterfaceConfigReader.getIfcNumber(data.getName());

        blockingWriteAndRead(cli, id, data,
                "configure terminal",
                f("interface %s %s", InterfaceConfigReader.getTypeOnDevice(data.getType()), ifcNumber),
                data.getDescription() == null ? "" : f("port-name %s", data.getDescription()),
                data.getMtu() == null ? "" : f("mtu %s", data.getMtu()),
                data.isEnabled() ? "enable" : "disable",
                "end");
    }

    @Override
    public void deleteCurrentAttributes(@Nonnull InstanceIdentifier<Config> id,
                                        @Nonnull Config dataBefore,
                                        @Nonnull WriteContext writeContext) throws WriteFailedException {
        if (isPhysicalInterface(dataBefore)) {
            throw new WriteFailedException.DeleteFailedException(id,
                    new IllegalArgumentException("Physical interface cannot be deleted"));
        } else if (dataBefore.getType() == SoftwareLoopback.class) {
            deleteLoopbackInterface(id, dataBefore, writeContext);
        } else {
            throw new WriteFailedException.CreateFailedException(id, dataBefore,
                    new IllegalArgumentException("Unknown interface type: " + dataBefore.getType()));
        }
    }

    private void deleteLoopbackInterface(InstanceIdentifier<Config> id, Config data, WriteContext writeContext)
            throws WriteFailedException.DeleteFailedException {
        Matcher matcher = LOOPBACK_NAME_PATTERN.matcher(data.getName());
        Preconditions.checkArgument(matcher.matches(),
                    "Loopback name must be in format: Loopback45, not: %s", data.getName());

        blockingDeleteAndRead(cli, id,
                "configure terminal",
                f("no interface loopback %s", matcher.group("number")),
                "end");
    }
}
