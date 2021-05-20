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

package io.art.logging.configuration;

import io.art.core.source.*;
import lombok.*;
import static io.art.core.checker.NullityChecker.*;
import static io.art.logging.constants.LoggingModuleConstants.ConfigurationKeys.*;

@Getter
@Builder(toBuilder = true)
public class UdpWriterConfiguration {
    private final String host;
    private final int port;

    public static UdpWriterConfiguration from(ConfigurationSource source, UdpWriterConfiguration fallback) {
        UdpWriterConfiguration.UdpWriterConfigurationBuilder builder = UdpWriterConfiguration.builder();
        builder.host(orElse(source.getString(HOST_KEY), fallback.host));
        builder.port(orElse(source.getInt(PORT_KEY), fallback.port));
        return builder.build();
    }

    public static UdpWriterConfiguration defaults() {
        return UdpWriterConfiguration.builder().build();
    }
}
