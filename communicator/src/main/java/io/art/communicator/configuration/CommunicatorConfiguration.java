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

package io.art.communicator.configuration;

import io.art.core.source.*;
import lombok.*;
import reactor.core.scheduler.*;
import static io.art.communicator.constants.CommunicatorModuleConstants.ConfigurationKeys.*;
import static io.art.communicator.constants.CommunicatorModuleConstants.Defaults.*;
import static io.art.core.checker.NullityChecker.*;

@Getter
@AllArgsConstructor
public class CommunicatorConfiguration {
    private final boolean logging;
    private final Scheduler scheduler;

    public static CommunicatorConfiguration from(ConfigurationSource source) {
        boolean logging = orElse(source.getBool(LOGGING_KEY), false);
        Scheduler scheduler = DEFAULT_COMMUNICATOR_SCHEDULER;
        return new CommunicatorConfiguration(logging, scheduler);
    }
}
