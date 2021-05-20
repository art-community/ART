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

package io.art.logging.colorizer;

import io.art.logging.constants.*;
import lombok.experimental.*;
import static io.art.logging.colorizer.AnsiColorizer.*;

@UtilityClass
public class LogColorizer {
    public static String byLevel(LoggingLevel level, String message) {
        switch (level) {
            case ERROR:
                return error(message);
            case WARN:
                return warning(message);
            case INFO:
                return success(message);
            case DEBUG:
                return additional(message);
            default:
                return message;
        }
    }

}
