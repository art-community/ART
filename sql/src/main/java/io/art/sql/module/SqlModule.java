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

package io.art.sql.module;

import com.zaxxer.hikari.*;
import io.dropwizard.db.*;
import lombok.*;
import org.apache.logging.log4j.*;
import org.apache.tomcat.jdbc.pool.*;
import io.art.core.caster.*;
import io.art.core.module.Module;
import io.art.sql.configuration.*;
import io.art.sql.exception.*;
import io.art.sql.state.*;
import static java.text.MessageFormat.*;
import static lombok.AccessLevel.*;
import static io.art.core.context.Context.*;
import static io.art.core.checker.NullityChecker.*;
import static io.art.core.wrapper.ExceptionWrapper.*;
import static io.art.logging.LoggingModule.*;
import static io.art.metrics.module.MetricsModule.*;
import static io.art.sql.configuration.SqlModuleConfiguration.*;
import static io.art.sql.constants.ConnectionPoolInitializationMode.*;
import static io.art.sql.constants.SqlModuleConstants.LoggingMessages.*;
import static io.art.sql.constants.SqlModuleConstants.*;
import javax.sql.DataSource;
import java.sql.*;
import java.util.function.*;

@Getter
public class SqlModule implements Module<SqlModuleConfiguration, SqlModuleState> {
    @Getter(lazy = true, value = PRIVATE)
    private static final SqlModuleConfiguration sqlModule = context().getModule(SQL_MODULE_ID, SqlModule::new);
    @Getter(lazy = true, value = PRIVATE)
    private static final SqlModuleState sqlModuleState = context().getModuleState(SQL_MODULE_ID, SqlModule::new);
    private final String id = SQL_MODULE_ID;
    private final SqlModuleConfiguration defaultConfiguration = DEFAULT_CONFIGURATION;
    private final SqlModuleState state = new SqlModuleState();
    @Getter(lazy = true, value = PRIVATE)
    private final Logger logger = loggingModule().getLogger(SqlModule.class);

    public static SqlModuleConfiguration sqlModule() {
        if (contextIsNotReady()) {
            return DEFAULT_CONFIGURATION;
        }
        return getSqlModule();
    }

    public static SqlModuleState sqlModuleState() {
        return getSqlModuleState();
    }

    @Override
    public void onLoad() {
        sqlModule().getDbConfigurations().values().forEach(this::initializeConnectionPool);
    }

    @Override
    public void beforeUnload() {
        sqlModule().getDbConfigurations().values().forEach(this::closeConnectionPool);
    }

    private void initializeConnectionPool(SqlDbConfiguration configuration) {
        DataSource dataSource;
        try {
            switch (configuration.getConnectionPoolType()) {
                case HIKARI:
                    dataSource = new HikariDataSource(configuration.getHikariPoolConfig());
                    sqlModuleState().hikariDataSource(dataSource);
                    if (sqlModule().getInitializationMode() == BOOTSTRAP) {
                        try (Connection ignored = dataSource.getConnection()) {
                        }
                        getLogger().info(format(STARING_POOL, dataSource));
                    }
                    break;
                default:
                    dataSource = configuration.isEnableMetrics()
                            ? new ManagedPooledDataSource(configuration.getTomcatPoolConfig(), metricsModule().getDropwizardMetricRegistry())
                            : new org.apache.tomcat.jdbc.pool.DataSource(configuration.getTomcatPoolConfig());
                    sqlModuleState().tomcatDataSource(dataSource);
                    if (sqlModule().getInitializationMode() == BOOTSTRAP) {
                        if (configuration.isEnableMetrics()) {
                            Caster.<ManagedPooledDataSource>cast(dataSource).start();
                        }
                        try (Connection ignored = dataSource.getConnection()) {
                        }
                        getLogger().info(format(STARING_POOL, dataSource));
                    }
                    break;
            }
            configuration.getJooqConfiguration().set(dataSource);
        } catch (Exception throwable) {
            throw new SqlModuleException(throwable);
        }
    }

    private void closeConnectionPool(SqlDbConfiguration configuration) {
        try {
            switch (configuration.getConnectionPoolType()) {
                case HIKARI:
                    let(sqlModuleState().hikariDataSource(), (Consumer<DataSource>) pool -> ignoreException(() -> {
                        Caster.<HikariDataSource>cast(pool).close();
                        getLogger().info(format(CLOSING_POOL, pool));
                    }, getLogger()::error));
                    break;
                case TOMCAT:
                    let(sqlModuleState().tomcatDataSource(), (Consumer<DataSource>) pool -> ignoreException(() -> {
                        Caster.<DataSourceProxy>cast(pool).close();
                        getLogger().info(format(CLOSING_POOL, pool));
                    }, getLogger()::error));
                    break;
            }
        } catch (Throwable throwable) {
            getLogger().error(throwable.getMessage(), throwable);
        }
    }
}
