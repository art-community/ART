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

public interface SqlConfigKeys {
    String SQL_DB_SECTION_ID = "sql.db";
    String SQL_DB_INSTANCES_SECTION_ID = "sql.db.instances";
    String POOL_HIKARI_SECTION_DI = "pool.hikari";
    String POOL_TOMCAT_SECTION_DI = "pool.tomcat";
    String POOL_NAME = "name";
    String POOL_INITIALIZATION_MODE = "poolInitializationMode";
    String POOL_TYPE = "pool.type";
    String USER_NAME = "userName";
    String PASSWORD = "password";
    String PROVIDER = "provider";
    String QUERY_TIMEOUT_SECONDS = "queryTimeoutSeconds";
    String DRIVER_PROPERTIES = "driverProperties";

    String TOMCAT_JMX_ENABLED = "jmxEnabled";
    String TOMCAT_TEST_WHILE_IDLE = "testWhileIdle";
    String TOMCAT_TEST_ON_RETURN = "testOnReturn";
    String TOMCAT_TEST_ON_BORROW = "testOnBorrow";
    String TOMCAT_TEST_ON_CONNECT = "testOnConnect";
    String TOMCAT_VALIDATION_QUERY = "validationQuery";
    String TOMCAT_VALIDATION_INTERVAL_MILLIS = "validationIntervalMillis";
    String TOMCAT_INITIAL_SIZE = "initialSize";
    String TOMCAT_MIN_IDLE = "minIdle";
    String TOMCAT_MAX_ACTIVE = "maxActive";
    String TOMCAT_MAX_IDLE = "maxIdle";
    String TOMCAT_MAX_LIFE_TIME_MILLIS = "maxLifeTimeMillis";
    String TOMCAT_TIME_BETWEEN_EVICTION_RUNS_MILLIS = "timeBetweenEvictionRunsMillis";
    String TOMCAT_MIN_EVICTABLE_IDLE_TIME_MILLIS = "minEvictableIdleTimeMillis";
    String TOMCAT_MAX_WAIT_MILLIS = "maxWaitMillis";
    String TOMCAT_LOG_ABANDONED = "logAbandoned";
    String TOMCAT_REMOVE_ABANDONED = "removeAbandoned";
    String TOMCAT_REMOVE_ABANDONED_TIMEOUT_MILLIS = "removeAbandonedTimeoutMillis";

    String HIKARI_REGISTER_MBEANS = "registerMbeans";
    String HIKARI_CONNECTION_TIMEOUT_MILLIS = "connectionTimeoutMillis";
    String HIKARI_IDLE_TIMEOUT_MILLIS = "idleTimeoutMillis";
    String HIKARI_MAX_LIFETIME_MILLIS = "maxLifetimeMillis";
    String HIKARI_MINIMUM_IDLE = "minimumIdle";
    String HIKARI_MAXIMUM_POOL_SIZE = "maximumPoolSize";
    String HIKARI_ALLOW_POOL_SUSPENSION = "allowPoolSuspension";
    String HIKARI_INITIALIZATION_FAIL_TIMEOUT_MILLIS = "initializationFailTimeoutMillis";
    String HIKARI_READ_ONLY = "readOnly";
    String HIKARI_VALIDATION_TIMEOUT_MILLIS = "validationTimeoutMillis";
    String HIKARI_LEAK_DETECTION_THRESHOLD_MILLIS = "leakDetectionThresholdMillis";
}
