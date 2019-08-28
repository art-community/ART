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

import static org.apache.logging.log4j.ThreadContext.*;
import static ru.art.core.checker.CheckerForEmptiness.*;
import static ru.art.core.constants.DateConstants.*;
import static ru.art.core.context.Context.*;
import static ru.art.logging.LoggingModuleConstants.LoggingParameters.*;
import static ru.art.logging.ThreadContextExtensions.*;
import java.util.*;

public interface LoggingParametersManager {
    static void putApplicationLoggingParameters() {
        putIfNotNull(APPLICATION_MODULE_ID_KEY, contextConfiguration().getMainModuleId());
        putIfNotNull(MODULES_KEY, context().getModuleNames());
        putIfNotNull(APPLICATION_JAR_KEY, contextConfiguration().getModuleJarName());
    }

    static void putServiceCallLoggingParameters(ServiceCallLoggingParameters parameters) {
        putApplicationLoggingParameters();
        putIfNotNull(SERVICE_ID_KEY, parameters.getServiceId());
        putIfNotNull(SERVICE_METHOD_ID_KEY, parameters.getServiceMethodId());
        putIfNotNull(SERVICE_METHOD_COMMAND_KEY, parameters.getServiceMethodCommand());
        putIfNotNull(SERVICE_EVENT_TYPE_KEY, parameters.getLoggingEventType());
        putIfNotNull(REQUEST_START_TIME_KEY, YYYY_MM_DD_HH_MM_SS_24H_DASH_FORMAT.format(new Date()));
        putIfNotNull(SERVICES_KEY, parameters.getLoadedServices());
    }

    static void putProtocolCallLoggingParameters(ProtocolCallLoggingParameters parameters) {
        putApplicationLoggingParameters();
        putIfNotNull(PROTOCOL_KEY, parameters.getProtocol());
        putIfNotNull(TRACE_ID_KEY, parameters.getTraceId());
        putIfNotNull(REQUEST_ID_KEY, parameters.getRequestId());
        putIfNotNull(ENVIRONMENT_KEY, parameters.getEnvironment());
        String profile = parameters.getProfile();
        if (isNotEmpty(profile)) {
            putIfNotNull(PROFILE_KEY, profile);
        }
    }

    static void clearServiceCallLoggingParameters() {
        remove(SERVICES_KEY);
        remove(REQUEST_KEY);
        remove(RESPONSE_KEY);
        remove(SERVICE_ID_KEY);
        remove(SERVICE_METHOD_ID_KEY);
        remove(SERVICE_METHOD_COMMAND_KEY);
        remove(REQUEST_START_TIME_KEY);
        remove(REQUEST_END_TIME_KEY);
        remove(EXECUTION_TIME_KEY);
        remove(SERVICE_EXCEPTION_KEY);
        remove(SERVICE_TYPES_KEY);
        remove(SERVICE_EVENT_TYPE_KEY);
    }

    static void clearProtocolLoggingParameters() {
        remove(PROTOCOL_KEY);
        remove(TRACE_ID_KEY);
        remove(ENVIRONMENT_KEY);
        remove(REQUEST_ID_KEY);
    }
}
