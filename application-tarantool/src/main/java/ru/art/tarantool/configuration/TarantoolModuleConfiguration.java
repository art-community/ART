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

package ru.art.tarantool.configuration;

import lombok.*;
import ru.art.core.module.*;
import ru.art.tarantool.constants.TarantoolModuleConstants.*;
import ru.art.tarantool.exception.*;
import ru.art.tarantool.model.*;
import static java.text.MessageFormat.*;
import static java.util.Objects.*;
import static ru.art.core.factory.CollectionsFactory.*;
import static ru.art.tarantool.constants.TarantoolModuleConstants.*;
import static ru.art.tarantool.constants.TarantoolModuleConstants.ExceptionMessages.*;
import static ru.art.tarantool.constants.TarantoolModuleConstants.TarantoolInitializationMode.*;
import static ru.art.tarantool.module.TarantoolModule.*;
import java.util.*;

public interface TarantoolModuleConfiguration extends ModuleConfiguration {
    Map<String, TarantoolConfiguration> getTarantoolConfigurations();

    TarantoolLocalConfiguration getLocalConfiguration();

    long getProbeConnectionTimeoutMillis();

    long getConnectionTimeoutMillis();

    boolean isEnableTracing();

    TarantoolInitializationMode getInitializationMode();

    TarantoolModuleDefaultConfiguration DEFAULT_CONFIGURATION = new TarantoolModuleDefaultConfiguration();

    @Getter
    class TarantoolModuleDefaultConfiguration implements TarantoolModuleConfiguration {
        private final Map<String, TarantoolConfiguration> tarantoolConfigurations = mapOf();
        private final long connectionTimeoutMillis = DEFAULT_TARANTOOL_CONNECTION_TIMEOUT;
        private final long probeConnectionTimeoutMillis = DEFAULT_TARANTOOL_PROBE_CONNECTION_TIMEOUT;
        private final boolean enableTracing = false;
        private final TarantoolLocalConfiguration localConfiguration = TarantoolLocalConfiguration.builder().build();
        private final TarantoolInitializationMode initializationMode = BOOTSTRAP;
    }

    static TarantoolEntityFieldsMapping fieldMapping(String instanceId, String entityName) {
        TarantoolConfiguration tarantoolConfiguration = getTarantoolConfiguration(instanceId, tarantoolModule().getTarantoolConfigurations());
        TarantoolEntityFieldsMapping entityFieldsMapping = tarantoolConfiguration
                .getEntityFieldsMappings()
                .get(entityName);
        if (isNull(entityFieldsMapping)) {
            throw new TarantoolConnectionException(format(ENTITY_FIELDS_MAPPING_IS_NULL, instanceId, entityName));
        }
        return entityFieldsMapping;
    }
}
