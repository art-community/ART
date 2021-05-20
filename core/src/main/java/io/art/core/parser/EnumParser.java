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

package io.art.core.parser;

import lombok.experimental.*;
import static io.art.core.checker.EmptinessChecker.*;
import java.util.function.*;

@UtilityClass
public class EnumParser {
    public <T extends Enum<T>> T enumOf(Function<String, T> parser, String value, T fallback) {
        if (isEmpty(value)) {
            return fallback;
        }
        try {
            return parser.apply(value);
        } catch (Throwable throwable) {
            return fallback;
        }
    }
}
