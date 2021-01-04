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

package ru.art.config.extensions;

import lombok.*;
import lombok.experimental.*;
import org.apache.logging.log4j.*;
import ru.art.config.*;
import ru.art.config.exception.*;
import static java.util.Collections.*;
import static java.util.Objects.*;
import static java.util.function.Function.*;
import static java.util.stream.Collectors.*;
import static ru.art.config.ConfigProvider.*;
import static ru.art.config.constants.ConfigExceptionMessages.*;
import static ru.art.config.remote.provider.RemoteConfigProvider.*;
import static ru.art.core.checker.CheckerForEmptiness.*;
import static ru.art.core.constants.StringConstants.*;
import static ru.art.core.extension.ExceptionExtensions.*;
import static ru.art.core.factory.CollectionsFactory.*;
import static ru.art.logging.LoggingModule.*;
import java.util.*;
import java.util.function.*;

@UtilityClass
public class ConfigExtensions {
    @Getter
    private static final Logger logger = loggingModule().getLogger(ConfigExtensions.class);

    public static String configString(String sectionId, String path) {
        if (isEmpty(sectionId)) throw new ConfigException(SECTION_ID_IS_EMPTY);
        Config remoteConfig = remoteConfig(sectionId);
        if (Config.isNotEmpty(remoteConfig)) return remoteConfig.getString(path);
        return config(sectionId).getString(path);
    }

    public static Integer configInt(String sectionId, String path) {
        if (isEmpty(sectionId)) throw new ConfigException(SECTION_ID_IS_EMPTY);
        Config remoteConfig = remoteConfig(sectionId);
        if (Config.isNotEmpty(remoteConfig)) return remoteConfig.getInt(path);
        return config(sectionId).getInt(path);
    }

    public static Long configLong(String sectionId, String path) {
        if (isEmpty(sectionId)) throw new ConfigException(SECTION_ID_IS_EMPTY);
        Config remoteConfig = remoteConfig(sectionId);
        if (Config.isNotEmpty(remoteConfig)) return remoteConfig.getLong(path);
        return config(sectionId).getLong(path);
    }

    public static Double configDouble(String sectionId, String path) {
        if (isEmpty(sectionId)) throw new ConfigException(SECTION_ID_IS_EMPTY);
        Config remoteConfig = remoteConfig(sectionId);
        if (Config.isNotEmpty(remoteConfig)) return remoteConfig.getDouble(path);
        return config(sectionId).getDouble(path);
    }

    public static Boolean configBoolean(String sectionId, String path) {
        if (isEmpty(sectionId)) throw new ConfigException(SECTION_ID_IS_EMPTY);
        Config remoteConfig = remoteConfig(sectionId);
        if (Config.isNotEmpty(remoteConfig)) return remoteConfig.getBool(path);
        return config(sectionId).getBool(path);
    }


    public static List<String> configStringList(String sectionId, String path) {
        if (isEmpty(sectionId)) throw new ConfigException(SECTION_ID_IS_EMPTY);
        Config remoteConfig = remoteConfig(sectionId);
        if (Config.isNotEmpty(remoteConfig)) return remoteConfig.getStringList(path);
        return config(sectionId).getStringList(path);
    }

    public static List<Integer> configIntList(String sectionId, String path) {
        if (isEmpty(sectionId)) throw new ConfigException(SECTION_ID_IS_EMPTY);
        Config remoteConfig = remoteConfig(sectionId);
        if (Config.isNotEmpty(remoteConfig)) return remoteConfig.getIntList(path);
        return config(sectionId).getIntList(path);
    }

    public static List<Long> configLongList(String sectionId, String path) {
        if (isEmpty(sectionId)) throw new ConfigException(SECTION_ID_IS_EMPTY);
        Config remoteConfig = remoteConfig(sectionId);
        if (Config.isNotEmpty(remoteConfig)) return remoteConfig.getLongList(path);
        return config(sectionId).getLongList(path);
    }

    public static List<Double> configDoubleList(String sectionId, String path) {
        if (isEmpty(sectionId)) throw new ConfigException(SECTION_ID_IS_EMPTY);
        Config remoteConfig = remoteConfig(sectionId);
        if (Config.isNotEmpty(remoteConfig)) return remoteConfig.getDoubleList(path);
        return config(sectionId).getDoubleList(path);
    }

    public static List<Boolean> configBooleanList(String sectionId, String path) {
        if (isEmpty(sectionId)) throw new ConfigException(SECTION_ID_IS_EMPTY);
        Config remoteConfig = remoteConfig(sectionId);
        if (Config.isNotEmpty(remoteConfig)) return remoteConfig.getBoolList(path);
        return config(sectionId).getBoolList(path);
    }


    public static Set<String> configStringSet(String sectionId, String path) {
        if (isEmpty(sectionId)) throw new ConfigException(SECTION_ID_IS_EMPTY);
        Config remoteConfig = remoteConfig(sectionId);
        if (Config.isNotEmpty(remoteConfig)) return setOf(remoteConfig.getStringList(path));
        return setOf(config(sectionId).getStringList(path));
    }

    public static Set<Integer> configIntSet(String sectionId, String path) {
        if (isEmpty(sectionId)) throw new ConfigException(SECTION_ID_IS_EMPTY);
        Config remoteConfig = remoteConfig(sectionId);
        if (Config.isNotEmpty(remoteConfig)) return setOf(remoteConfig.getIntList(path));
        return setOf(config(sectionId).getIntList(path));
    }

    public static Set<Long> configLongSet(String sectionId, String path) {
        if (isEmpty(sectionId)) throw new ConfigException(SECTION_ID_IS_EMPTY);
        Config remoteConfig = remoteConfig(sectionId);
        if (Config.isNotEmpty(remoteConfig)) return setOf(remoteConfig.getLongList(path));
        return setOf(config(sectionId).getLongList(path));
    }

    public static Set<Double> configDoubleSet(String sectionId, String path) {
        if (isEmpty(sectionId)) throw new ConfigException(SECTION_ID_IS_EMPTY);
        Config remoteConfig = remoteConfig(sectionId);
        if (Config.isNotEmpty(remoteConfig)) return setOf(remoteConfig.getDoubleList(path));
        return setOf(config(sectionId).getDoubleList(path));
    }

    public static Set<Boolean> configBooleanSet(String sectionId, String path) {
        if (isEmpty(sectionId)) throw new ConfigException(SECTION_ID_IS_EMPTY);
        Config remoteConfig = remoteConfig(sectionId);
        if (Config.isNotEmpty(remoteConfig)) return setOf(remoteConfig.getBoolList(path));
        return setOf(config(sectionId).getBoolList(path));
    }


    public static String configString(String path) {
        Config remoteConfig = remoteConfig();
        if (Config.isNotEmpty(remoteConfig)) return remoteConfig.getString(path);
        return config(EMPTY_STRING).getString(path);
    }

    public static Integer configInt(String path) {
        Config remoteConfig = remoteConfig();
        if (Config.isNotEmpty(remoteConfig)) return remoteConfig.getInt(path);
        return config(EMPTY_STRING).getInt(path);
    }

    public static Long configLong(String path) {
        Config remoteConfig = remoteConfig();
        if (Config.isNotEmpty(remoteConfig)) return remoteConfig.getLong(path);
        return config(EMPTY_STRING).getLong(path);
    }

    public static Double configDouble(String path) {
        Config remoteConfig = remoteConfig();
        if (Config.isNotEmpty(remoteConfig)) return remoteConfig.getDouble(path);
        return config(EMPTY_STRING).getDouble(path);
    }

    public static Boolean configBoolean(String path) {
        Config remoteConfig = remoteConfig();
        if (Config.isNotEmpty(remoteConfig)) return remoteConfig.getBool(path);
        return config(EMPTY_STRING).getBool(path);
    }


    public static List<String> configStringList(String path) {
        Config remoteConfig = remoteConfig();
        if (Config.isNotEmpty(remoteConfig)) return remoteConfig.getStringList(path);
        return config(EMPTY_STRING).getStringList(path);
    }

    public static List<Integer> configIntList(String path) {
        Config remoteConfig = remoteConfig();
        if (Config.isNotEmpty(remoteConfig)) return remoteConfig.getIntList(path);
        return config(EMPTY_STRING).getIntList(path);
    }

    public static List<Long> configLongList(String path) {
        Config remoteConfig = remoteConfig();
        if (Config.isNotEmpty(remoteConfig)) return remoteConfig.getLongList(path);
        return config(EMPTY_STRING).getLongList(path);
    }

    public static List<Double> configDoubleList(String path) {
        Config remoteConfig = remoteConfig();
        if (Config.isNotEmpty(remoteConfig)) return remoteConfig.getDoubleList(path);
        return config(EMPTY_STRING).getDoubleList(path);
    }

    public static List<Boolean> configBooleanList(String path) {
        Config remoteConfig = remoteConfig();
        if (Config.isNotEmpty(remoteConfig)) return remoteConfig.getBoolList(path);
        return config(EMPTY_STRING).getBoolList(path);
    }


    public static String configString(String sectionId, String path, String defaultValue) {
        return ifExceptionOrEmpty(() -> configString(sectionId, path), defaultValue);
    }

    public static Integer configInt(String sectionId, String path, int defaultValue) {
        return ifExceptionOrEmpty(() -> configInt(sectionId, path), defaultValue);
    }

    public static Double configDouble(String sectionId, String path, double defaultValue) {
        return ifExceptionOrEmpty(() -> configDouble(sectionId, path), defaultValue);
    }

    public static Long configLong(String sectionId, String path, long defaultValue) {
        return ifExceptionOrEmpty(() -> configLong(sectionId, path), defaultValue);
    }

    public static Boolean configBoolean(String sectionId, String path, boolean defaultValue) {
        return ifExceptionOrEmpty(() -> configBoolean(sectionId, path), defaultValue);
    }


    public static List<String> configStringList(String sectionId, String path, List<String> defaultValue) {
        return ifExceptionOrEmpty(() -> configStringList(sectionId, path), defaultValue);
    }

    public static List<Integer> configIntList(String sectionId, String path, List<Integer> defaultValue) {
        return ifExceptionOrEmpty(() -> configIntList(sectionId, path), defaultValue);
    }

    public static List<Double> configDoubleList(String sectionId, String path, List<Double> defaultValue) {
        return ifExceptionOrEmpty(() -> configDoubleList(sectionId, path), defaultValue);
    }

    public static List<Long> configLongList(String sectionId, String path, List<Long> defaultValue) {
        return ifExceptionOrEmpty(() -> configLongList(sectionId, path), defaultValue);
    }

    public static List<Boolean> configBooleanList(String sectionId, String path, List<Boolean> defaultValue) {
        return ifExceptionOrEmpty(() -> configBooleanList(sectionId, path), defaultValue);
    }


    public static Set<String> configStringSet(String sectionId, String path, Set<String> defaultValue) {
        return ifExceptionOrEmpty(() -> configStringSet(sectionId, path), defaultValue);
    }

    public static Set<Integer> configIntSet(String sectionId, String path, Set<Integer> defaultValue) {
        return ifExceptionOrEmpty(() -> configIntSet(sectionId, path), defaultValue);
    }

    public static Set<Double> configDoubleSet(String sectionId, String path, Set<Double> defaultValue) {
        return ifExceptionOrEmpty(() -> configDoubleSet(sectionId, path), defaultValue);
    }

    public static Set<Long> configLongSet(String sectionId, String path, Set<Long> defaultValue) {
        return ifExceptionOrEmpty(() -> configLongSet(sectionId, path), defaultValue);
    }

    public static Set<Boolean> configBooleanSet(String sectionId, String path, Set<Boolean> defaultValue) {
        return ifExceptionOrEmpty(() -> configBooleanSet(sectionId, path), defaultValue);
    }


    public static Config configInner(String sectionId, String path) {
        if (isEmpty(sectionId)) throw new ConfigException(SECTION_ID_IS_EMPTY);
        Config remoteConfig = remoteConfig(sectionId);
        if (Config.isNotEmpty(remoteConfig)) return remoteConfig.getConfig(path);
        Config localConfig = config(sectionId);
        return localConfig.getConfig(path);
    }

    public static Config configInner(String path) {
        Config remoteConfig = remoteConfig();
        if (Config.isNotEmpty(remoteConfig)) return remoteConfig.getConfig(path);
        Config localConfig = config(EMPTY_STRING);
        return localConfig.getConfig(path);
    }


    public static <T> Map<String, T> configInnerMap(String path, Function<Config, T> configMapper) {
        return configInnerMap(path).entrySet().stream().collect(toMap(Map.Entry::getKey, entry -> configMapper.apply(entry.getValue())));
    }

    public static <T> Map<String, T> configInnerMap(String path, BiFunction<String, Config, T> configMapper) {
        return configInnerMap(path).entrySet().stream().collect(toMap(Map.Entry::getKey, entry -> configMapper.apply(entry.getKey(), entry.getValue())));
    }

    @SuppressWarnings("Duplicates")
    public static Map<String, Config> configInnerMap(String path) {
        return configInnerMap(EMPTY_STRING, path);
    }

    public static <T> Map<String, T> configInnerMap(String sectionId, String path, Function<Config, T> configMapper) {
        return configInnerMap(sectionId, path).entrySet().stream().collect(toMap(Map.Entry::getKey, entry -> configMapper.apply(entry.getValue())));
    }

    @SuppressWarnings("Duplicates")
    public static Map<String, Config> configInnerMap(String sectionId, String path) {
        if (isNull(sectionId)) throw new ConfigException(SECTION_ID_IS_NULL);
        Config remoteConfig = remoteConfig(sectionId);
        if (Config.isNotEmpty(remoteConfig)) {
            return remoteConfig.getKeys(path).stream().collect(toMap(key -> key, key -> remoteConfig.getConfig(path + DOT + key)));
        }
        Config localConfig = config(sectionId);
        return localConfig.getKeys(path).stream().collect(toMap(key -> key, key -> localConfig.getConfig(path + DOT + key)));
    }


    public static <T> Map<String, T> configInnerMap(String path, Function<Config, T> configMapper, Map<String, T> defaultValues) {
        return doIfExceptionOrEmpty(() -> configInnerMap(path, configMapper), () -> defaultValues, exception -> {
            logger.error(exception.getMessage(), exception);
            return defaultValues;
        });
    }

    public static <T> Map<String, T> configInnerMap(String path, BiFunction<String, Config, T> configMapper, Map<String, T> defaultValues) {
        return doIfExceptionOrEmpty(() -> configInnerMap(path, configMapper), () -> defaultValues, exception -> {
            logger.error(exception.getMessage(), exception);
            return defaultValues;
        });
    }

    public static <T> Map<String, T> configInnerMap(String sectionId, String path, Function<Config, T> configMapper, Map<String, T> defaultValues) {
        return doIfExceptionOrEmpty(() -> configInnerMap(sectionId, path, configMapper), () -> defaultValues, exception -> {
            logger.error(exception.getMessage(), exception);
            return defaultValues;
        });
    }

    public static <T> Map<String, T> configMap(String path, BiFunction<String, Config, T> configMapper) {
        return configMap(EMPTY_STRING, path, configMapper);
    }

    public static <T> Map<String, T> configMap(String path, BiFunction<String, Config, T> configMapper, Map<String, T> defaultValues) {
        return ifExceptionOrEmpty(() -> configMap(path, configMapper), defaultValues);
    }

    public static <T> Map<String, T> configMap(String sectionId, String path, BiFunction<String, Config, T> configMapper) {
        if (isNull(sectionId)) throw new ConfigException(SECTION_ID_IS_NULL);
        Config remoteConfig = remoteConfig(sectionId);
        if (Config.isNotEmpty(remoteConfig)) {
            return remoteConfig.getKeys(path).stream().collect(toMap(key -> key, key -> configMapper.apply(path + DOT + key, remoteConfig)));
        }
        Config localConfig = config(sectionId);
        return localConfig.getKeys(path).stream().collect(toMap(key -> key, key -> configMapper.apply(path + DOT + key, localConfig)));
    }

    public static <T> Map<String, T> configMap(String sectionId, String path, BiFunction<String, Config, T> configMapper, Map<String, T> defaultValues) {
        return ifExceptionOrEmpty(() -> configMap(sectionId, path, configMapper), defaultValues);
    }

    public static boolean hasPath(String sectionId, String path) {
        if (isEmpty(sectionId)) throw new ConfigException(SECTION_ID_IS_EMPTY);
        Config remoteConfig = remoteConfig(sectionId);
        if (Config.isNotEmpty(remoteConfig)) return remoteConfig.hasPath(path);
        return config(sectionId).hasPath(path);
    }

    public static boolean hasPath(String path) {
        Config remoteConfig = remoteConfig();
        if (Config.isNotEmpty(remoteConfig)) return remoteConfig.asEntityConfig().getFields().containsKey(path);
        return config(EMPTY_STRING).hasPath(path);
    }


    public static Properties configProperties(String sectionId, String key) {
        Properties additionalProperties = new Properties();
        additionalProperties.putAll(configInnerMap(sectionId, key, propertyConfig -> propertyConfig
                .getKeys()
                .stream()
                .collect(toMap(identity(), propertyConfig::getString)), emptyMap()));
        return additionalProperties;
    }
}