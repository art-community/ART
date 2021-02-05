/*
 * ART
 *
 * Copyright 2020 ART
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

package io.art.rsocket.configuration;

import io.art.core.collection.*;
import io.art.core.source.*;
import lombok.*;
import static io.art.core.checker.NullityChecker.let;
import static io.art.rsocket.constants.RsocketModuleConstants.ConfigurationKeys.*;
import static java.util.Objects.*;

@Getter
public class RsocketCommunicatorConfiguration {
    private RsocketConnectorConfiguration defaultConnectorConfiguration;
    private ImmutableMap<String, RsocketConnectorConfiguration> connectors;

    public boolean isLogging(String connectorId) {
        RsocketConnectorConfiguration configuration = connectors.get(connectorId);
        if (isNull(configuration)) return defaultConnectorConfiguration.isLogging();
        return configuration.isLogging();
    }

    public static RsocketCommunicatorConfiguration from(ConfigurationSource source) {
        RsocketCommunicatorConfiguration configuration = new RsocketCommunicatorConfiguration();
        configuration.defaultConnectorConfiguration = let(source.getNested(DEFAULT_SECTION), RsocketConnectorConfiguration::from, RsocketConnectorConfiguration.defaults());
        configuration.connectors = source.getNestedMap(CONNECTORS_KEY, connector -> RsocketConnectorConfiguration.from(configuration, connector));
        return configuration;
    }
}
