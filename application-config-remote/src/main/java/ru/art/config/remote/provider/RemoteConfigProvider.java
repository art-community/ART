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

package ru.art.config.remote.provider;

import ru.art.config.*;
import ru.art.config.cache.*;
import ru.art.config.exception.*;
import ru.art.config.remote.specification.*;
import ru.art.config.state.*;
import ru.art.configurator.api.specification.*;
import static java.util.Objects.*;
import static ru.art.config.ConfigProvider.*;
import static ru.art.config.constants.ConfigType.*;
import static ru.art.config.module.ConfigModule.*;
import static ru.art.config.remote.constants.RemoteConfigLoaderConstants.*;
import static ru.art.config.remote.constants.RemoteConfigLoaderConstants.LocalConfigKeys.*;
import static ru.art.config.remote.loader.RemoteConfigLoader.*;
import static ru.art.config.state.ConfigModuleState.ConfigurationMode.*;
import static ru.art.core.constants.StringConstants.*;
import static ru.art.core.context.Context.*;
import static ru.art.entity.Entity.*;
import static ru.art.logging.LoggingModule.*;
import static ru.art.service.ServiceModule.*;

public class RemoteConfigProvider {
    private static final ConfigCacheContainer CONFIG_CACHE_CONTAINER = new ConfigCacheContainer();

    public static void useRemoteConfigurations() {
        try {
            Config localConfig = config(EMPTY_STRING);
            if (!localConfig.hasPath(CONFIGURATOR_HOST) || !localConfig.hasPath(CONFIGURATOR_PORT) || !localConfig.hasPath(CONFIGURATOR_PATH)) {
                loggingModule().getLogger(RemoteConfigProvider.class).warn(CONFIGURATOR_CONNECTION_PROPERTIES_NOT_EXISTS);
                return;
            }
            String configuratorHost = localConfig.getString(CONFIGURATOR_HOST);
            Integer configuratorPort = localConfig.getInt(CONFIGURATOR_PORT);
            String configuratorPath = localConfig.getString(CONFIGURATOR_PATH);
            configModuleState().remoteConfigProperties(ConfigModuleState.RemoteConfigConnectionProperties.builder()
                    .host(configuratorHost)
                    .port(configuratorPort)
                    .path(configuratorPath)
                    .build());
            serviceModuleState()
                    .getServiceRegistry()
                    .registerService(new ConfiguratorCommunicationSpecification(configuratorHost, configuratorPort, configuratorPath))
                    .registerService(new RemoteConfigServiceSpecification());
            configModuleState().configurationMode(REMOTE);
        } catch (Throwable throwable) {
            loggingModule().getLogger(RemoteConfigProvider.class).warn(CONFIGURATOR_CONNECTION_PROPERTIES_NOT_EXISTS);
        }
    }

    public static Config remoteConfig(String sectionId) {
        if (EMPTY_STRING.equals(sectionId)) {
            return remoteConfig();
        }
        return remoteConfig().getConfig(sectionId);
    }

    public static Config remoteConfig() {
        if (configModuleState().configurationMode() != REMOTE) {
            return new Config(entityBuilder().build(), REMOTE_ENTITY_CONFIG);
        }

        String moduleId = contextConfiguration().getMainModuleId();

        if (isNull(moduleId)) throw new ConfigException(MODULE_ID_IS_EMPTY);
        if (CONFIG_CACHE_CONTAINER.containsConfig(moduleId, REMOTE_ENTITY_CONFIG) &&
                CONFIG_CACHE_CONTAINER.configCacheActualized(moduleId, REMOTE_ENTITY_CONFIG)) {
            return CONFIG_CACHE_CONTAINER.getConfigFromCache(moduleId, REMOTE_ENTITY_CONFIG);
        }
        return CONFIG_CACHE_CONTAINER
                .putConfigToCache(moduleId, REMOTE_ENTITY_CONFIG, new Config(loadRemoteConfig(), REMOTE_ENTITY_CONFIG));
    }

}
