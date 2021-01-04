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

package ru.art.config.extensions.sql;

import com.zaxxer.hikari.*;
import lombok.*;
import org.apache.tomcat.jdbc.pool.*;
import org.jooq.*;
import org.jooq.conf.*;
import ru.art.config.*;
import ru.art.sql.configuration.*;
import ru.art.sql.configuration.SqlModuleConfiguration.*;
import ru.art.sql.constants.*;
import ru.art.sql.listener.*;
import ru.art.sql.model.*;
import static java.util.Optional.*;
import static ru.art.config.extensions.ConfigExtensions.*;
import static ru.art.config.extensions.common.CommonConfigKeys.*;
import static ru.art.config.extensions.sql.SqlConfigKeys.*;
import static ru.art.core.extension.ExceptionExtensions.*;
import static ru.art.sql.constants.ConnectionPoolType.*;
import static ru.art.sql.factory.SqlDbDefaultsFactory.*;
import static ru.art.sql.module.SqlModule.*;
import java.util.*;
import java.util.function.*;

@Getter
public class SqlAgileConfiguration extends SqlModuleDefaultConfiguration {
    private Map<String, SqlDbConfiguration> dbConfigurations = super.getDbConfigurations();
    private ConnectionPoolInitializationMode initializationMode = super.getInitializationMode();

    public SqlAgileConfiguration() {
        refresh();
    }

    @Override
    public void refresh() {
        initializationMode = ifException(
                () -> ConnectionPoolInitializationMode.valueOf(configString(SQL_DB_INSTANCES_SECTION_ID, POOL_INITIALIZATION_MODE).toUpperCase()),
                initializationMode
        );
        BiFunction<String, Config, SqlDbConfiguration> mapper = (instance, config) -> {
            Settings defaultSettings = new Settings();
            DbConnectionProperties properties = DbConnectionProperties.builder()
                    .driver(DbProvider.valueOf(config.getString(PROVIDER).toUpperCase()))
                    .url(config.getString(URL))
                    .userName(config.getString(USER_NAME))
                    .password(config.getString(PASSWORD))
                    .build();

            Optional<HikariConfig> hikariPoolConfig = ofNullable(config.getConfig(POOL_HIKARI_SECTION_DI)).map(hikariConfig -> extractHikariPoolConfig(hikariConfig, properties));
            Optional<PoolProperties> tomcatPoolConfig = ofNullable(config.getConfig(POOL_TOMCAT_SECTION_DI)).map(tomcatConfig -> extractTomcatPoolConfig(tomcatConfig, properties));

            Configuration jooqConfiguration;
            ConnectionPoolType connectionPoolType = ifExceptionOrEmpty(() -> ConnectionPoolType.valueOf(config.getString(POOL_TYPE).toUpperCase()), TOMCAT);
            String driverClassName = properties.getDriver().getDriverClassName();
            switch (connectionPoolType) {
                case HIKARI:
                    jooqConfiguration = createDefaultJooqConfiguration(driverClassName, hikariPoolConfig.orElse(createDefaultHikariPoolConfig(properties)));
                    break;
                default:
                    jooqConfiguration = createDefaultJooqConfiguration(driverClassName, tomcatPoolConfig.orElse(createDefaultTomcatPoolConfig(properties)));
                    break;
            }
            return SqlDbConfiguration.builder()
                    .connectionPoolType(connectionPoolType)
                    .enableMetrics(ifExceptionOrEmpty(() -> config.getBool(ENABLE_METRICS), false))
                    .enableTracing(ifExceptionOrEmpty(() -> config.getBool(ENABLE_TRACING), false))
                    .jooqConfiguration(jooqConfiguration
                            .set(new JooqLoggingListener(() -> sqlModule().getDbConfiguration(instance).isEnableTracing()))
                            .set(defaultSettings.withQueryTimeout(config.getInt(QUERY_TIMEOUT_SECONDS))))
                    .hikariPoolConfig(hikariPoolConfig.orElse(createDefaultHikariPoolConfig(properties)))
                    .tomcatPoolConfig(tomcatPoolConfig.orElse(createDefaultTomcatPoolConfig(properties)))
                    .build();
        };
        dbConfigurations = configInnerMap(SQL_DB_INSTANCES_SECTION_ID, mapper, super.getDbConfigurations());
    }

    private HikariConfig extractHikariPoolConfig(Config config, DbConnectionProperties properties) {
        HikariConfig hikariPoolConfig = createDefaultHikariPoolConfig(properties);
        hikariPoolConfig.setPoolName(ifExceptionOrEmpty(() -> config.getString(POOL_NAME), hikariPoolConfig.getPoolName()));
        hikariPoolConfig.setRegisterMbeans(ifExceptionOrEmpty(() -> config.getBool(HIKARI_REGISTER_MBEANS), hikariPoolConfig.isRegisterMbeans()));
        hikariPoolConfig.setConnectionTimeout(ifExceptionOrEmpty(() -> config.getLong(HIKARI_CONNECTION_TIMEOUT_MILLIS), hikariPoolConfig.getConnectionTimeout()));
        hikariPoolConfig.setIdleTimeout(ifExceptionOrEmpty(() -> config.getLong(HIKARI_IDLE_TIMEOUT_MILLIS), hikariPoolConfig.getIdleTimeout()));
        hikariPoolConfig.setMaxLifetime(ifExceptionOrEmpty(() -> config.getLong(HIKARI_MAX_LIFETIME_MILLIS), hikariPoolConfig.getMaxLifetime()));
        hikariPoolConfig.setMinimumIdle(ifExceptionOrEmpty(() -> config.getInt(HIKARI_MINIMUM_IDLE), hikariPoolConfig.getMinimumIdle()));
        hikariPoolConfig.setMaximumPoolSize(ifExceptionOrEmpty(() -> config.getInt(HIKARI_MAXIMUM_POOL_SIZE), hikariPoolConfig.getMaximumPoolSize()));
        hikariPoolConfig.setAllowPoolSuspension(ifExceptionOrEmpty(() -> config.getBool(HIKARI_ALLOW_POOL_SUSPENSION), hikariPoolConfig.isAllowPoolSuspension()));
        hikariPoolConfig.setInitializationFailTimeout(ifExceptionOrEmpty(
                () -> config.getLong(HIKARI_INITIALIZATION_FAIL_TIMEOUT_MILLIS),
                hikariPoolConfig.getInitializationFailTimeout())
        );
        hikariPoolConfig.setReadOnly(ifExceptionOrEmpty(() -> config.getBool(HIKARI_READ_ONLY), hikariPoolConfig.isReadOnly()));
        hikariPoolConfig.setValidationTimeout(ifExceptionOrEmpty(() -> config.getLong(HIKARI_VALIDATION_TIMEOUT_MILLIS), hikariPoolConfig.getValidationTimeout()));
        hikariPoolConfig.setLeakDetectionThreshold(ifExceptionOrEmpty(() -> config.getLong(HIKARI_LEAK_DETECTION_THRESHOLD_MILLIS), hikariPoolConfig.getLeakDetectionThreshold()));
        hikariPoolConfig.setDataSourceProperties(ifExceptionOrEmpty(() -> config.getProperties(DRIVER_PROPERTIES), new Properties()));
        return hikariPoolConfig;
    }

    private PoolProperties extractTomcatPoolConfig(Config config, DbConnectionProperties properties) {
        PoolProperties tomcatPoolConfig = createDefaultTomcatPoolConfig(properties);
        tomcatPoolConfig.setName(ifExceptionOrEmpty(() -> config.getString(POOL_NAME), tomcatPoolConfig.getName()));
        tomcatPoolConfig.setJmxEnabled(ifExceptionOrEmpty(() -> config.getBool(TOMCAT_JMX_ENABLED), tomcatPoolConfig.isJmxEnabled()));
        tomcatPoolConfig.setTestWhileIdle(ifExceptionOrEmpty(() -> config.getBool(TOMCAT_TEST_WHILE_IDLE), tomcatPoolConfig.isTestWhileIdle()));
        tomcatPoolConfig.setTestOnReturn(ifExceptionOrEmpty(() -> config.getBool(TOMCAT_TEST_ON_RETURN), tomcatPoolConfig.isTestOnReturn()));
        tomcatPoolConfig.setTestOnBorrow(ifExceptionOrEmpty(() -> config.getBool(TOMCAT_TEST_ON_BORROW), tomcatPoolConfig.isTestOnBorrow()));
        tomcatPoolConfig.setTestOnConnect(ifExceptionOrEmpty(() -> config.getBool(TOMCAT_TEST_ON_CONNECT), tomcatPoolConfig.isTestOnConnect()));
        tomcatPoolConfig.setValidationQuery(ifExceptionOrEmpty(() -> config.getString(TOMCAT_VALIDATION_QUERY), tomcatPoolConfig.getValidationQuery()));
        tomcatPoolConfig.setValidationInterval(ifExceptionOrEmpty(() -> config.getLong(TOMCAT_VALIDATION_INTERVAL_MILLIS), tomcatPoolConfig.getValidationInterval()));
        tomcatPoolConfig.setInitialSize(ifExceptionOrEmpty(() -> config.getInt(TOMCAT_INITIAL_SIZE), tomcatPoolConfig.getInitialSize()));
        tomcatPoolConfig.setMinIdle(ifExceptionOrEmpty(() -> config.getInt(TOMCAT_MIN_IDLE), tomcatPoolConfig.getMinIdle()));
        tomcatPoolConfig.setMaxActive(ifExceptionOrEmpty(() -> config.getInt(TOMCAT_MAX_ACTIVE), tomcatPoolConfig.getMaxActive()));
        tomcatPoolConfig.setMaxIdle(ifExceptionOrEmpty(() -> config.getInt(TOMCAT_MAX_IDLE), tomcatPoolConfig.getMaxIdle()));
        tomcatPoolConfig.setMaxAge(ifExceptionOrEmpty(() -> config.getLong(TOMCAT_MAX_LIFE_TIME_MILLIS), tomcatPoolConfig.getMaxAge()));
        tomcatPoolConfig.setTimeBetweenEvictionRunsMillis(ifExceptionOrEmpty(
                () -> config.getInt(TOMCAT_TIME_BETWEEN_EVICTION_RUNS_MILLIS),
                tomcatPoolConfig.getTimeBetweenEvictionRunsMillis())
        );
        tomcatPoolConfig.setMinEvictableIdleTimeMillis(ifExceptionOrEmpty(
                () -> config.getInt(TOMCAT_MIN_EVICTABLE_IDLE_TIME_MILLIS),
                tomcatPoolConfig.getMinEvictableIdleTimeMillis()
        ));
        tomcatPoolConfig.setMaxWait(ifExceptionOrEmpty(() -> config.getInt(TOMCAT_MAX_WAIT_MILLIS), tomcatPoolConfig.getMaxWait()));
        tomcatPoolConfig.setLogAbandoned(ifExceptionOrEmpty(() -> config.getBool(TOMCAT_LOG_ABANDONED), tomcatPoolConfig.isLogAbandoned()));
        tomcatPoolConfig.setRemoveAbandoned(ifExceptionOrEmpty(() -> config.getBool(TOMCAT_REMOVE_ABANDONED), tomcatPoolConfig.isRemoveAbandoned()));
        tomcatPoolConfig.setRemoveAbandonedTimeout(ifExceptionOrEmpty(() -> config.getInt(TOMCAT_REMOVE_ABANDONED_TIMEOUT_MILLIS), tomcatPoolConfig.getRemoveAbandonedTimeout()));
        tomcatPoolConfig.setDbProperties(ifExceptionOrEmpty(() -> config.getProperties(DRIVER_PROPERTIES), new Properties()));
        return tomcatPoolConfig;
    }
}
