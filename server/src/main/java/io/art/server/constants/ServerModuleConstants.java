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

package io.art.server.constants;

import io.art.core.property.*;
import reactor.core.scheduler.*;
import static io.art.core.constants.ThreadConstants.*;
import static io.art.core.property.LazyProperty.*;
import static reactor.core.scheduler.Schedulers.*;

public interface ServerModuleConstants {
    interface Defaults {
        LazyProperty<Scheduler> DEFAULT_SERVICE_METHOD_BLOCKING_SCHEDULER = lazy(() -> newBoundedElastic(DEFAULT_THREAD_POOL_SIZE, Integer.MAX_VALUE, "(b):service-method"));
    }

    interface ConfigurationKeys {
        String SERVER_SECTION = "server";
        String SERVER_SERVICES_KEY = "services";
        String DEACTIVATED_KEY = "deactivated";
        String LOGGING_KEY = "logging";
        String VALIDATING_KEY = "validating";
        String METHODS_KEY = "methods";
    }

    interface ValidationExpressionType {
        String name();
    }

    enum ValidationExpressionTypes implements ValidationExpressionType {
        BETWEEN_DOUBLE,
        BETWEEN_INT,
        BETWEEN_LONG,
        CONTAINS,
        EQUALS,
        NOT_EMPTY_COLLECTION,
        NOT_EMPTY_MAP,
        NOT_EMPTY_STRING,
        NOT_EQUALS,
        NOT_NULL
    }

    interface LoggingMessages {
        String SERVICE_REGISTRATION_MESSAGE = "Registered service: ''{0}'' with methods: {1}";
        String SERVICE_SUBSCRIBED_MESSAGE = "Service subscribed: ''{0}.{1}''";
        String SERVICE_INPUT_DATA = "Service ''{0}.{1}'' input:\n{2}";
        String SERVICE_OUTPUT_DATA = "Service ''{0}.{1}'' output:\n{2}";
        String SERVICE_COMPLETED_MESSAGE = "Service completed: ''{0}.{1}''";
        String SERVICE_FAILED_MESSAGE = "Service failed: ''{0}.{1}''";
    }

    interface ExceptionMessages {
    }

    interface ValidationErrorPatterns {
        String NOT_BETWEEN_VALIDATION_ERROR = "Validation error. ''{0}'' = ''{1}'' not between ''{2,number,#}'' and ''{3,number,#}''";
        String NOT_EQUALS_VALIDATION_ERROR = "Validation error. ''{0}'' = ''{1}'' is not equals to ''{2}''";
        String NOT_CONTAINS_VALIDATION_ERROR = "Validation error. ''{0}'' = ''{1}'' is not contains to ''{2}''";
        String EQUALS_VALIDATION_ERROR = "Validation error. ''{0}'' = ''{1}'' is equals to ''{2}''";
        String EMPTY_VALIDATION_ERROR = "Validation error. ''{0}'' is empty";
        String NULL_VALIDATION_ERROR = "Validation error. ''{0}'' is null";
        String REQUEST_IS_NULL = "Validation error. Request is null";
    }

    interface StateKeys {
        String SPECIFICATION_KEY = "specification";
    }
}
