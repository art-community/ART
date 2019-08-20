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

import ru.art.entity.Value;
import ru.art.entity.interceptor.ValueInterceptionResult;
import ru.art.entity.interceptor.ValueInterceptor;
import static java.text.MessageFormat.format;
import static ru.art.entity.interceptor.ValueInterceptionResult.nextInterceptor;
import static ru.art.logging.LoggingModule.*;
import static ru.art.logging.LoggingModuleConstants.VALUE_LOG_MESSAGE;

public class LoggingValueInterceptor implements ValueInterceptor<Value, Value> {
    @Override
    public ValueInterceptionResult<Value, Value> intercept(Value value) {
        loggingModule()
                .getLogger(LoggingValueInterceptor.class)
                .info(format(VALUE_LOG_MESSAGE, value));
        return nextInterceptor(value);
    }
}
