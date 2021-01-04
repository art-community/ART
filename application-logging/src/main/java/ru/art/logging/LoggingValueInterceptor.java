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

import lombok.*;
import org.apache.logging.log4j.*;
import ru.art.entity.Value;
import ru.art.entity.interceptor.*;
import static java.text.MessageFormat.*;
import static java.util.Collections.*;
import static org.apache.logging.log4j.ThreadContext.*;
import static ru.art.core.caster.Caster.*;
import static ru.art.core.constants.StringConstants.*;
import static ru.art.entity.Value.*;
import static ru.art.entity.interceptor.ValueInterceptionResult.*;
import static ru.art.logging.LoggingModule.*;
import static ru.art.logging.LoggingModuleConstants.LoggingParameters.*;
import static ru.art.logging.LoggingModuleConstants.*;
import static ru.art.logging.ThreadContextExtensions.*;
import java.util.*;
import java.util.function.*;

@AllArgsConstructor
public class LoggingValueInterceptor<InValue extends Value, OutValue extends Value> implements ValueInterceptor<InValue, OutValue> {
    private final Supplier<Boolean> enableTracing;

    @Override
    public ValueInterceptionResult<InValue, OutValue> intercept(Value value) {
        putIfNotNull(REQUEST_VALUE_KEY, value);
        if (enableTracing.get()) {
            Map<String, String> dump = emptyMap();
            if (isEntity(value)) {
                dump = asEntity(value).dump();
                dump.forEach((key, field) -> putIfNotNull(REQUEST_VALUE_KEY + DOT + key, field));
            }
            loggingModule()
                    .getLogger(LoggingValueInterceptor.class)
                    .info(format(VALUE_LOG_MESSAGE, value));
            dump.keySet().forEach(ThreadContext::remove);
        }
        remove(REQUEST_VALUE_KEY);
        return cast(nextInterceptor(value));
    }
}
