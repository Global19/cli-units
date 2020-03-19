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

package io.frinx.cli.unit.cubro.unit.acl.handler.util;

import java.util.AbstractMap.SimpleEntry;
import java.util.regex.Matcher;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.frinx.openconfig.net.yang.acl.rev170526.ACLTYPE;

public class NameTypeEntry extends SimpleEntry<String, Class<? extends ACLTYPE>> {

    public NameTypeEntry(@Nonnull final String name, @Nonnull final Class<? extends ACLTYPE> type) {
        super(name, type);
    }

    public static NameTypeEntry fromMatcher(final Matcher matcher) {
        return new NameTypeEntry(
                matcher.group("name"),
                AclUtil.getType(matcher.group("type"))
        );
    }

    public String getName() {
        return getKey();
    }

    public Class<? extends ACLTYPE> getType() {
        return getValue();
    }
}