package ru.art.sql.factory;

import com.zaxxer.hikari.*;
import lombok.experimental.*;
import org.apache.tomcat.jdbc.pool.*;
import org.jooq.*;
import org.jooq.impl.*;
import ru.art.sql.exception.*;
import ru.art.sql.model.*;
import static java.lang.Class.*;
import static ru.art.core.context.Context.*;
import static ru.art.sql.constants.SqlModuleConstants.ConfigurationDefaults.*;

@UtilityClass
public class SqlDbDefaultsFactory {
    public static Configuration createDefaultJooqConfiguration(String driveClassName, HikariConfig config) {
        try {
            forName(driveClassName);
            return new DefaultConfiguration().set(new HikariDataSource(config));
        } catch (Throwable throwable) {
            throw new SqlModuleException(throwable);
        }
    }

    public static Configuration createDefaultJooqConfiguration(String driveClassName, PoolProperties config) {
        try {
            forName(driveClassName);
            return new DefaultConfiguration().set(new DataSource(config));
        } catch (Throwable throwable) {
            throw new SqlModuleException(throwable);
        }
    }

    public static HikariConfig createDefaultHikariPoolConfig(DbConnectionProperties properties) {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setPoolName(HIKARI_POOL_PREFIX + contextConfiguration().getMainModuleId());
        hikariConfig.setJdbcUrl(properties.getUrl());
        hikariConfig.setUsername(properties.getUserName());
        hikariConfig.setPassword(properties.getPassword());
        hikariConfig.setDriverClassName(properties.getDriver().getDriverClassName());

        hikariConfig.setRegisterMbeans(false);
        hikariConfig.setConnectionTimeout(60 * 1000);
        hikariConfig.setIdleTimeout(60 * 1000);
        hikariConfig.setMaxLifetime(60 * 1000);
        hikariConfig.setMinimumIdle(10);
        hikariConfig.setMaximumPoolSize(50);
        hikariConfig.setAllowPoolSuspension(false);
        hikariConfig.setInitializationFailTimeout(0);
        hikariConfig.setReadOnly(false);
        hikariConfig.setValidationTimeout(5 * 1000);
        hikariConfig.setLeakDetectionThreshold(0);
        hikariConfig.setConnectionTestQuery(properties.getDriver().getValidationQuery());
        return hikariConfig;
    }

    public static PoolProperties createDefaultTomcatPoolConfig(DbConnectionProperties properties) {
        PoolProperties poolProperties = new PoolProperties();
        poolProperties.setName(TOMCAT_POOL_PREFIX + contextConfiguration().getMainModuleId());
        poolProperties.setUrl(properties.getUrl());
        poolProperties.setDriverClassName(properties.getDriver().getDriverClassName());
        poolProperties.setUsername(properties.getUserName());
        poolProperties.setPassword(properties.getPassword());

        poolProperties.setJmxEnabled(true);

        poolProperties.setTestWhileIdle(false);
        poolProperties.setTestOnReturn(false);
        poolProperties.setTestOnBorrow(true);

        poolProperties.setValidationQuery(properties.getDriver().getValidationQuery());

        poolProperties.setValidationInterval(30000);

        poolProperties.setInitialSize(10);
        poolProperties.setMinIdle(10);
        poolProperties.setMaxActive(100);
        poolProperties.setMaxIdle(100);

        poolProperties.setTimeBetweenEvictionRunsMillis(30000);
        poolProperties.setMinEvictableIdleTimeMillis(30000);

        poolProperties.setMaxWait(10000);

        poolProperties.setLogAbandoned(true);
        poolProperties.setRemoveAbandoned(true);
        poolProperties.setRemoveAbandonedTimeout(60);

        return poolProperties;
    }
}
