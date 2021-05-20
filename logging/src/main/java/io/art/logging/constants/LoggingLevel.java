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

package io.art.logging.constants;

import lombok.*;
import static java.util.Arrays.*;

@Getter
@AllArgsConstructor
public enum LoggingLevel {
    ERROR((byte) 0),
    WARN((byte) 1),
    INFO((byte) 2),
    TRACE((byte) 3),
    DEBUG((byte) 4);

    private final byte level;

    public static LoggingLevel parse(String level, LoggingLevel defaultLevel) {
        return stream(LoggingLevel.values())
                .filter(known -> known.name().toLowerCase().equals(level))
                .findFirst()
                .orElse(defaultLevel);
    }
}
