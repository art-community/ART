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

package io.art.core.printer;

import io.art.core.colorizer.*;
import io.art.core.constants.*;
import lombok.*;
import static com.google.common.base.Strings.*;
import static io.art.core.checker.NullityChecker.*;
import static io.art.core.colorizer.AnsiColorizer.*;
import static io.art.core.constants.StringConstants.*;

@NoArgsConstructor(staticName = "printer")
public class ColoredPrinter {
    private final StringBuilder builder = new StringBuilder();
    private int tabulation = 0;

    public ColoredPrinter mainSection(String message) {
        builder.append(repeat(TABULATION, tabulation)).append(NEW_LINE).append(AnsiColorizer.message(message + COLON + SPACE, AnsiColor.CYAN)).append(NEW_LINE);
        return this;
    }

    public ColoredPrinter subSection(String message) {
        builder.append(repeat(TABULATION, tabulation)).append(AnsiColorizer.message(message + COLON + SPACE, AnsiColor.BLUE_BOLD)).append(NEW_LINE);
        return this;
    }

    public ColoredPrinter message(String message) {
        builder.append(repeat(TABULATION, tabulation)).append(additional(message)).append(NEW_LINE);
        return this;
    }

    public ColoredPrinter value(String name, Object value) {
        return message(success(name + COLON + SPACE) + additional(let(value, Object::toString, NULL_STRING)));
    }

    public ColoredPrinter tabulation(int count) {
        tabulation = count;
        return this;
    }

    public String print() {
        return builder.substring(0, builder.length() - 1);
    }
}
