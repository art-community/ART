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

package io.art.server.model;

import io.art.core.changes.*;
import io.art.core.source.*;
import io.art.server.refresher.*;
import io.art.transport.payload.*;
import io.art.value.constants.ValueModuleConstants.*;
import lombok.*;
import reactor.core.scheduler.*;
import static io.art.core.checker.NullityChecker.*;
import static io.art.server.constants.ServerModuleConstants.ConfigurationKeys.*;
import static io.art.server.constants.ServerModuleConstants.Defaults.*;
import java.util.function.*;

@Getter
public class ServiceMethodConfiguration {
    private boolean deactivated;
    private boolean logging;
    private boolean validating;
    private Scheduler blockingScheduler;
    private Function<DataFormat, TransportPayloadReader> reader;
    private Function<DataFormat, TransportPayloadWriter> writer;

    public static ServiceMethodConfiguration from(ServerModuleRefresher refresher, ConfigurationSource source) {
        ServiceMethodConfiguration configuration = new ServiceMethodConfiguration();
        ChangesListener deactivationListener = refresher.deactivationListener();
        ChangesListener loggingListener = refresher.loggingListener();
        ChangesListener validationListener = refresher.validationListener();
        configuration.deactivated = deactivationListener.emit(orElse(source.getBool(DEACTIVATED_KEY), false));
        configuration.logging = loggingListener.emit(orElse(source.getBool(LOGGING_KEY), true));
        configuration.validating = validationListener.emit(orElse(source.getBool(VALIDATING_KEY), true));
        configuration.blockingScheduler = DEFAULT_SERVICE_METHOD_BLOCKING_SCHEDULER;
        configuration.reader = TransportPayloadReader::new;
        configuration.writer = TransportPayloadWriter::new;
        return configuration;
    }
}
