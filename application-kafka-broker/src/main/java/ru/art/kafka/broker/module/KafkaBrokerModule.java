/*
 * ART Java
 *
 * Copyright 2019 ART
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

package ru.art.kafka.broker.module;

import lombok.*;
import ru.art.core.module.Module;
import ru.art.kafka.broker.configuration.*;
import ru.art.kafka.broker.embedded.*;
import ru.art.kafka.broker.state.*;
import static lombok.AccessLevel.*;
import static ru.art.core.context.Context.*;
import static ru.art.core.extension.NullCheckingExtensions.*;
import static ru.art.core.wrapper.ExceptionWrapper.*;
import static ru.art.kafka.broker.configuration.KafkaBrokerModuleConfiguration.*;
import static ru.art.kafka.broker.constants.KafkaBrokerModuleConstants.*;

@Getter
public class KafkaBrokerModule implements Module<KafkaBrokerModuleConfiguration, KafkaBrokerModuleState> {
    @Getter(lazy = true, value = PRIVATE)
    private final static KafkaBrokerModuleConfiguration kafkaBrokerModule = context().getModule(KAFKA_BROKER_MODULE_ID, KafkaBrokerModule::new);
    @Getter(lazy = true, value = PRIVATE)
    private final static KafkaBrokerModuleState kafkaBrokerModuleState = context().getModuleState(KAFKA_BROKER_MODULE_ID, KafkaBrokerModule::new);
    private final String id = KAFKA_BROKER_MODULE_ID;
    private final KafkaBrokerModuleConfiguration defaultConfiguration = new KafkaBrokerModuleDefaultConfiguration();
    private final KafkaBrokerModuleState state = new KafkaBrokerModuleState();

    public static KafkaBrokerModuleConfiguration kafkaBrokerModule() {
        if (contextIsNotReady()) {
            return DEFAULT_CONFIGURATION;
        }
        return getKafkaBrokerModule();
    }

    @Override
    public void onUnload() {
        ignoreException(() -> doIfNotNull(kafkaBrokerModuleState().getBroker(), EmbeddedKafkaBroker::shutdown));
        ignoreException(() -> doIfNotNull(kafkaBrokerModuleState().getZookeeper(), EmbeddedZookeeper::shutdown));
    }

    public static KafkaBrokerModuleState kafkaBrokerModuleState() {
        return getKafkaBrokerModuleState();
    }
}
