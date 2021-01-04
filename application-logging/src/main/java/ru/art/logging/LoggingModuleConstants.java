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

package ru.art.logging;

public interface LoggingModuleConstants {
    String LOGGING_MODULE_ID = "LOGGING_MODULE";
    String DEFAULT_REQUEST_ID = "DEFAULT_REQUEST_ID";
    String PORT = "port";
    String PROTOCOL = "protocol";
    String ADDRESS = "address";
    String LOG4J2_YAML_FILE = "log4j2.yml";
    String LOG4J2_CONFIGURATION_FILE_PROPERTY = "log4j.configurationFile";
    String VALUE_LOG_MESSAGE = "Value: ''{0}''";

    enum LoggingMode {
        CONSOLE,
        FILE,
        SOCKET,
        ROLLING_FILE
    }

    interface LoggingParameters {
        String LOG_TIMESTAMP = "logTimestamp";
        String MODULES_KEY = "modules";
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
}
