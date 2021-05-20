/*
 * ART
 *
 * Copyright 2019-2021 ART
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.art.core.configuration;

import io.art.core.collection.*;
import io.art.core.context.*;
import io.art.core.module.Module;
import io.art.core.network.provider.*;
import lombok.Builder;
import lombok.*;
import static io.art.core.collection.ImmutableArray.*;
import static io.art.core.constants.ContextConstants.*;
import static io.art.core.constants.SystemProperties.*;
import static io.art.core.factory.MapFactory.*;
import static io.art.core.network.provider.IpAddressProvider.*;
import static java.lang.System.*;
import static java.nio.charset.StandardCharsets.*;
import static java.time.ZoneId.*;
import static java.util.Locale.Category.*;
import static java.util.Locale.*;
import static java.util.Optional.*;
import java.io.*;
import java.net.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.security.*;
import java.time.*;
import java.util.*;
import java.util.function.*;

@Getter
@Builder
public class ContextConfiguration {
    @Builder.Default
    private final String mainModuleId = DEFAULT_MAIN_MODULE_ID;
    @Builder.Default
    private final Charset charset = UTF_8;
    @Builder.Default
    private final String primaryIpAddress = getIpAddress();
    @Builder.Default
    private final ImmutableMap<String, String> ipAddresses = IpAddressProvider.getIpAddresses();
    @Builder.Default
    private final Locale locale = getDefault(FORMAT);
    @Builder.Default
    private final ZoneId zoneId = systemDefault();
    @Builder.Default
    private final String jar = ofNullable(Context.class.getProtectionDomain())
            .map(ProtectionDomain::getCodeSource)
            .map(CodeSource::getLocation)
            .map(URL::getPath)
            .map(File::new)
            .map(File::getPath)
            .orElse(DEFAULT_MODULE_JAR);
    @Builder.Default
    private final Path workingDirectory = Paths.get(getProperty(USER_DIR_PROPERTY));
    @Builder.Default
    private final ImmutableArray<String> arguments = emptyImmutableArray();
    @Builder.Default
    private final ImmutableMap<String, String> environment = immutableMapOf(getenv());
    @Builder.Default
    private final ImmutableMap<String, String> properties = immutableMapOf(System.getProperties());
    private final Runnable onLoad;
    private final Runnable onUnload;
    private final Runnable beforeReload;
    private final Consumer<Module<?, ?>> reload;
    private final Runnable afterReload;
}
