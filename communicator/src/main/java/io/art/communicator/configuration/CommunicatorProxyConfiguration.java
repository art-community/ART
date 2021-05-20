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
import io.art.core.changes.*;
import io.art.core.collection.*;
import io.art.core.source.*;
import io.art.resilience.configuration.*;
import io.art.transport.payload.*;
import io.art.value.constants.*;
import lombok.*;
import reactor.core.scheduler.*;
import static io.art.communicator.constants.CommunicatorModuleConstants.ConfigurationKeys.*;
import static io.art.communicator.constants.CommunicatorModuleConstants.Defaults.*;
import static io.art.core.checker.NullityChecker.*;
import static io.art.resilience.constants.ResilienceModuleConstants.ConfigurationKeys.*;
import java.util.function.*;

@Getter
public class CommunicatorProxyConfiguration {
    private boolean logging;
    private boolean deactivated;
    private Scheduler blockingScheduler;
    private ImmutableMap<String, CommunicatorActionConfiguration> actions;
    private ImmutableMap<String, String> connectors;
    private ResilienceConfiguration resilienceConfiguration;
    private Function<ValueModuleConstants.DataFormat, TransportPayloadReader> reader;
    private Function<ValueModuleConstants.DataFormat, TransportPayloadWriter> writer;

    public static CommunicatorProxyConfiguration from(CommunicatorModuleRefresher refresher, ConfigurationSource source) {
        CommunicatorProxyConfiguration configuration = new CommunicatorProxyConfiguration();
        ChangesListener loggingListener = refresher.loggingListener();
        ChangesListener deactivationListener = refresher.deactivationListener();
        configuration.logging = loggingListener.emit(orElse(source.getBool(LOGGING_KEY), false));
        configuration.deactivated = deactivationListener.emit(orElse(source.getBool(DEACTIVATED_KEY), false));
        configuration.blockingScheduler = DEFAULT_COMMUNICATOR_BLOCKING_SCHEDULER.get();
        configuration.connectors = source.getNestedMap(CONNECTORS_KEY, NestedConfiguration::asString);
        configuration.actions = source.getNestedMap(ACTIONS_SECTION, action -> CommunicatorActionConfiguration.from(refresher, action));
        configuration.resilienceConfiguration = source.getNested(RESILIENCE_SECTION, action -> ResilienceConfiguration.from(refresher.resilienceListener(), action));
        configuration.reader = TransportPayloadReader::new;
        configuration.writer = TransportPayloadWriter::new;
        return configuration;
    }
}
