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

package ru.art.sql.constants;

public interface SqlModuleConstants {
    String SQL_MODULE_ID = "SQL_MODULE";

    interface ConfigurationDefaults {
        String HIKARI_POOL_PREFIX = "hikari-db-pool-";
        String TOMCAT_POOL_PREFIX = "tomcat-db-pool-";
    }

    interface LoggingMessages {
        String CLOSING_POOL = "Closing DB connection pool: {0}";
        String STARING_POOL = "Starting connection pool to DataSource: {0}";
        String EXECUTING_QUERY = "Executing SQL query: {0}";
        String EXECUTING_BATCH_QUERY = "Executing batch SQL query: {0}";
        String EXECUTING_ROUTINE = "Executing SQL routine: {0}";
    }

    interface ExceptionMessages {
        String SQL_DB_CONFIGURATION_NOT_FOUND = "SQL DB configuration for instance ''{0}'' was not found";
    }
}
