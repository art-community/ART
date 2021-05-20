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

package io.art.communicator.configuration;

import io.art.communicator.refresher.*;
import io.art.communicator.registry.*;
import io.art.core.collection.*;
import io.art.core.model.*;
import io.art.core.module.*;
import io.art.core.source.*;
import io.art.resilience.configuration.*;
import io.art.transport.payload.*;
import io.art.value.constants.ValueModuleConstants.*;
import lombok.*;
import reactor.core.scheduler.*;
import static io.art.communicator.constants.CommunicatorModuleConstants.ConfigurationKeys.*;
import static io.art.communicator.constants.CommunicatorModuleConstants.Defaults.*;
import static io.art.core.checker.EmptinessChecker.*;
import static io.art.core.checker.NullityChecker.*;
import static io.art.core.collection.ImmutableMap.*;
import static io.art.core.model.CommunicatorActionIdentifier.*;
import static java.util.Objects.*;
import static java.util.Optional.*;
import java.util.*;
import java.util.function.*;

@RequiredArgsConstructor
public class CommunicatorModuleConfiguration implements ModuleConfiguration {
    private final CommunicatorModuleRefresher refresher;

    @Getter(lazy = true)
    private final CommunicatorModuleRefresher.Consumer consumer = refresher.consumer();

    @Getter
    private ImmutableMap<String, CommunicatorProxyConfiguration> configurations = emptyImmutableMap();

    @Getter
    private CommunicatorProxyRegistry registry = new CommunicatorProxyRegistry();

    @Getter
    private Scheduler scheduler;

    @Getter
    private Function<DataFormat, TransportPayloadReader> reader;

    @Getter
    private Function<DataFormat, TransportPayloadWriter> writer;

    public Optional<String> findConnectorId(String protocol, CommunicatorActionIdentifier id) {
        CommunicatorProxyConfiguration proxyConfiguration = configurations.get(id.getCommunicatorId());
        Optional<String> connectorId = ofNullable(proxyConfiguration).map(configuration -> configuration.getConnectors().get(protocol));
        if (connectorId.isPresent()) return connectorId;
        return getActionConfiguration(id).map(configuration -> configuration.getConnectors().get(protocol));
    }

    public Scheduler getBlockingScheduler(String communicatorId, String actionId) {
        return getActionConfiguration(communicatorAction(communicatorId, actionId))
                .map(CommunicatorActionConfiguration::getBlockingScheduler)
                .orElseGet(() -> ofNullable(getConfigurations().get(communicatorId))
                        .map(CommunicatorProxyConfiguration::getBlockingScheduler)
                        .orElse(scheduler));
    }

    public TransportPayloadReader getReader(CommunicatorActionIdentifier id, DataFormat dataFormat) {
        return getActionConfiguration(id)
                .map(CommunicatorActionConfiguration::getReader)
                .orElseGet(() -> ofNullable(getConfigurations().get(id.getCommunicatorId()))
                        .map(CommunicatorProxyConfiguration::getReader)
                        .orElse(reader)).apply(dataFormat);
    }

    public TransportPayloadWriter getWriter(CommunicatorActionIdentifier id, DataFormat dataFormat) {
        return getActionConfiguration(id)
                .map(CommunicatorActionConfiguration::getWriter)
                .orElseGet(() -> ofNullable(getConfigurations().get(id.getCommunicatorId()))
                        .map(CommunicatorProxyConfiguration::getWriter)
                        .orElse(writer)).apply(dataFormat);
    }

    public Optional<CommunicatorActionConfiguration> getActionConfiguration(CommunicatorActionIdentifier id) {
        CommunicatorProxyConfiguration proxyConfiguration = configurations.get(id.getCommunicatorId());
        return ofNullable(proxyConfiguration).map(configuration -> configuration.getActions().get(id.getActionId()));
    }

    public ResilienceConfiguration getResilienceConfiguration(CommunicatorActionIdentifier id) {
        return getActionConfiguration(id)
                .map(CommunicatorActionConfiguration::getResilienceConfiguration)
                .orElse(ResilienceConfiguration.builder().build());
    }

    public boolean isLogging(CommunicatorActionIdentifier identifier) {
        boolean communicator = checkCommunicator(identifier, CommunicatorProxyConfiguration::isLogging, true);
        boolean action = checkAction(identifier, CommunicatorActionConfiguration::isLogging, true);
        return communicator && action;
    }

    public boolean isDeactivated(CommunicatorActionIdentifier identifier) {
        boolean communicator = checkCommunicator(identifier, CommunicatorProxyConfiguration::isDeactivated, false);
        boolean action = checkAction(identifier, CommunicatorActionConfiguration::isDeactivated, false);
        return communicator && action;
    }

    private <T> T checkCommunicator(CommunicatorActionIdentifier identifier, Function<CommunicatorProxyConfiguration, T> mapper, T defaultValue) {
        CommunicatorProxyConfiguration proxyConfiguration = configurations.get(identifier.getCommunicatorId());
        if (isNull(proxyConfiguration)) {
            return defaultValue;
        }
        return mapper.apply(proxyConfiguration);
    }

    private <T> T checkAction(CommunicatorActionIdentifier identifier, Function<CommunicatorActionConfiguration, T> mapper, T defaultValue) {
        CommunicatorProxyConfiguration proxyConfiguration = configurations.get(identifier.getCommunicatorId());
        if (isNull(proxyConfiguration)) {
            return defaultValue;
        }
        CommunicatorActionConfiguration actionConfiguration = proxyConfiguration.getActions().get(identifier.getActionId());
        if (isNull(actionConfiguration)) {
            return defaultValue;
        }
        return mapper.apply(actionConfiguration);
    }

    @RequiredArgsConstructor
    public static class Configurator implements ModuleConfigurator<CommunicatorModuleConfiguration, Configurator> {
        private final CommunicatorModuleConfiguration configuration;

        @Override
        public Configurator from(ConfigurationSource source) {
            configuration.scheduler = DEFAULT_COMMUNICATOR_BLOCKING_SCHEDULER.get();
            configuration.reader = TransportPayloadReader::new;
            configuration.writer = TransportPayloadWriter::new;
            configuration.configurations = ofNullable(source.getNested(COMMUNICATOR_SECTION))
                    .map(communicator -> communicator.getNestedMap(PROXIES_SECTION, proxy -> CommunicatorProxyConfiguration.from(configuration.refresher, proxy)))
                    .orElse(emptyImmutableMap());
            configuration.refresher.produce();
            return this;
        }

        @Override
        public Configurator initialize(CommunicatorModuleConfiguration configuration) {
            ifNotEmpty(configuration.getConfigurations(), configurations -> this.configuration.configurations = configurations);
            apply(configuration.getRegistry(), registry -> this.configuration.registry = registry);
            return this;
        }
    }
}
