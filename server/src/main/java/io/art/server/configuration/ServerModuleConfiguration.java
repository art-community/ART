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

package io.art.server.configuration;

import io.art.core.collection.*;
import io.art.core.model.*;
import io.art.core.module.*;
import io.art.core.source.*;
import io.art.server.model.*;
import io.art.server.refresher.*;
import io.art.server.registry.*;
import io.art.transport.payload.*;
import lombok.*;
import reactor.core.scheduler.*;
import static io.art.core.checker.NullityChecker.*;
import static io.art.core.collection.ImmutableMap.*;
import static io.art.server.constants.ServerModuleConstants.ConfigurationKeys.*;
import static io.art.server.constants.ServerModuleConstants.Defaults.*;
import static io.art.value.constants.ValueModuleConstants.*;
import static java.util.Objects.*;
import static java.util.Optional.*;
import java.util.*;
import java.util.function.*;


@RequiredArgsConstructor
public class ServerModuleConfiguration implements ModuleConfiguration {
    private final ServerModuleRefresher refresher;

    @Getter(lazy = true)
    private final ServerModuleRefresher.Consumer consumer = refresher.consumer();

    @Getter
    private ImmutableMap<String, ServiceConfiguration> configurations = emptyImmutableMap();

    @Getter
    private ServiceSpecificationRegistry registry = new ServiceSpecificationRegistry();

    @Getter
    private Scheduler blockingScheduler;

    @Getter
    private Function<DataFormat, TransportPayloadReader> reader;

    @Getter
    private Function<DataFormat, TransportPayloadWriter> writer;

    public Scheduler getBlockingScheduler(ServiceMethodIdentifier id) {
        return getMethodConfiguration(id)
                .map(ServiceMethodConfiguration::getBlockingScheduler)
                .orElseGet(() -> ofNullable(configurations.get(id.getServiceId()))
                        .map(ServiceConfiguration::getBlockingScheduler)
                        .orElse(blockingScheduler));
    }

    public TransportPayloadReader getReader(ServiceMethodIdentifier id, DataFormat dataFormat) {
        return getMethodConfiguration(id)
                .map(ServiceMethodConfiguration::getReader)
                .orElseGet(() -> ofNullable(configurations.get(id.getServiceId()))
                        .map(ServiceConfiguration::getReader)
                        .orElse(reader)).apply(dataFormat);
    }

    public TransportPayloadWriter getWriter(ServiceMethodIdentifier id, DataFormat dataFormat) {
        return getMethodConfiguration(id)
                .map(ServiceMethodConfiguration::getWriter)
                .orElseGet(() -> ofNullable(configurations.get(id.getServiceId()))
                        .map(ServiceConfiguration::getWriter)
                        .orElse(writer)).apply(dataFormat);
    }

    public Optional<ServiceMethodConfiguration> getMethodConfiguration(ServiceMethodIdentifier id) {
        return ofNullable(configurations.get(id.getServiceId())).map(configuration -> configuration.getMethods().get(id.getMethodId()));
    }

    public boolean isLogging(ServiceMethodIdentifier identifier) {
        boolean service = checkService(identifier, ServiceConfiguration::isLogging, true);
        boolean method = checkMethod(identifier, ServiceMethodConfiguration::isLogging, true);
        return service && method;
    }

    public boolean isValidating(ServiceMethodIdentifier identifier) {
        boolean service = checkService(identifier, ServiceConfiguration::isValidating, true);
        boolean method = checkMethod(identifier, ServiceMethodConfiguration::isValidating, true);
        return service && method;
    }

    public boolean isDeactivated(ServiceMethodIdentifier identifier) {
        boolean service = checkService(identifier, ServiceConfiguration::isDeactivated, false);
        boolean method = checkMethod(identifier, ServiceMethodConfiguration::isDeactivated, false);
        return service || method;
    }

    private <T> T checkService(ServiceMethodIdentifier identifier, Function<ServiceConfiguration, T> mapper, T defaultValue) {
        ServiceConfiguration serviceConfiguration = configurations.get(identifier.getServiceId());
        if (isNull(serviceConfiguration)) {
            return defaultValue;
        }
        return mapper.apply(serviceConfiguration);
    }

    private <T> T checkMethod(ServiceMethodIdentifier identifier, Function<ServiceMethodConfiguration, T> mapper, T defaultValue) {
        ServiceConfiguration serviceConfiguration = configurations.get(identifier.getServiceId());
        if (isNull(serviceConfiguration)) {
            return defaultValue;
        }
        ServiceMethodConfiguration methodConfiguration = serviceConfiguration.getMethods().get(identifier.getMethodId());
        if (isNull(methodConfiguration)) {
            return defaultValue;
        }
        return mapper.apply(methodConfiguration);
    }

    @RequiredArgsConstructor
    public static class Configurator implements ModuleConfigurator<ServerModuleConfiguration, Configurator> {
        private final ServerModuleConfiguration configuration;

        @Override
        public Configurator from(ConfigurationSource source) {
            configuration.blockingScheduler = DEFAULT_SERVICE_METHOD_BLOCKING_SCHEDULER;
            configuration.reader = TransportPayloadReader::new;
            configuration.writer = TransportPayloadWriter::new;
            configuration.configurations = ofNullable(source.getNested(SERVER_SECTION))
                    .map(server -> server.getNestedMap(SERVER_SERVICES_KEY, service -> ServiceConfiguration.from(configuration.refresher, service)))
                    .orElse(emptyImmutableMap());
            configuration.refresher.produce();
            return this;
        }

        @Override
        public Configurator configure(ServerModuleConfiguration configuration) {
            apply(configuration.getRegistry(), registry -> this.configuration.registry = registry);
            return this;
        }
    }
}
