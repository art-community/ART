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

package io.art.logging;

public interface LoggingModuleConstants {
    String LOG4J2_YML_FILE = "log4j2.yml";
    String LOG4J2_YAML_FILE = "log4j2.yaml";
    String LOG42_CONFIGURATION_FILE_PROPERTY = "log4j2.configurationFile";
    String DEFAULT_REQUEST_ID = "default-request-id";

    interface ConfigurationKeys {
        String LOGGING_SECTION = "logging";
        String COLORED_KEY = "colored";
        String ENABLED_KEY = "enabled";
        String ASYNCHRONOUS_KEY = "asynchronous";
        String CONFIGURATION_PATH_KEY = "configuration.path";
    }

    interface LoggingParameters {
        String LOG_TIMESTAMP = "logTimestamp";
        String MODULES_KEY = "get";
        String SERVICES_KEY = "services";
        String PROTOCOL_KEY = "protocol";
        String TRACE_ID_KEY = "traceId";
        String REQUEST_ID_KEY = "requestId";
        String PROFILE_KEY = "profile";
        String ENVIRONMENT_KEY = "environment";
        String REQUEST_KEY = "request";
        String RESPONSE_KEY = "response";
        String SERVICE_ID_KEY = "serviceId";
        String SERVICE_METHOD_ID_KEY = "serviceMethodId";
        String SERVICE_METHOD_COMMAND_KEY = "serviceMethodCommand";
        String MAIN_MODULE_ID_KEY = "mainModuleId";
        String REQUEST_START_TIME_KEY = "requestStartTime";
        String REQUEST_END_TIME_KEY = "requestEndTime";
        String EXECUTION_TIME_KEY = "executionTime";
        String SERVICE_EXCEPTION_KEY = "serviceException";
        String SERVICE_TYPES_KEY = "serviceTypes";
        String LOG_EVENT_TYPE = "logEventType";
        String MODULE_JAR_KEY = "moduleJar";
        String REQUEST_VALUE_KEY = "requestValue";
        String SQL_QUERY_KEY = "sqlQuery";
        String SQL_ROUTINE_KEY = "sqlRoutine";
    }

    interface LoggingMessages {
        String CONFIGURE_FROM_CLASSPATH = "Configure Log4j2 from classpath by file {0}";
        String USE_SYNCHRONOUS_LOGGING = "Using synchronous Log4j2";
        String USE_ASYNCHRONOUS_LOGGING = "Using asynchronous Log4j2";
        String CONFIGURE_FROM_FILE = "Configure Log4j2 from by file (from -Dlog4j2.configurationFile) {0}";
    }
}
