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
import static java.lang.Boolean.*;
import static java.lang.Byte.*;
import static java.lang.Double.*;
import static java.lang.Float.*;
import static java.lang.Integer.*;
import static java.lang.Long.*;
import static java.lang.Short.*;

@UtilityClass
public class PrimitiveParser {
    public static double parseOrElse(String string, double orElse) {
        if (isEmpty(string)) {
            return orElse;
        }
        return parseDouble(string);
    }

    public static int parseOrElse(String string, int orElse) {
        if (isEmpty(string)) {
            return orElse;
        }
        return parseInt(string);
    }

    public static long parseOrElse(String string, long orElse) {
        if (isEmpty(string)) {
            return orElse;
        }
        return parseLong(string);
    }

    public static boolean parseOrElse(String string, boolean orElse) {
        if (isEmpty(string)) {
            return orElse;
        }
        return parseBoolean(string);
    }

    public static short parseOrElse(String string, short orElse) {
        if (isEmpty(string)) {
            return orElse;
        }
        return parseShort(string);
    }

    public static double parseOrElse(String string, Double orElse) {
        if (isEmpty(string)) {
            return orElse;
        }
        return parseDouble(string);
    }

    public static int parseOrElse(String string, Integer orElse) {
        if (isEmpty(string)) {
            return orElse;
        }
        return parseInt(string);
    }

    public static long parseOrElse(String string, Long orElse) {
        if (isEmpty(string)) {
            return orElse;
        }
        return parseLong(string);
    }

    public static boolean parseOrElse(String string, Boolean orElse) {
        if (isEmpty(string)) {
            return orElse;
        }
        return parseBoolean(string);
    }

    public static short parseOrElse(String string, Short orElse) {
        if (isEmpty(string)) {
            return orElse;
        }
        return parseShort(string);
    }


    public static double tryParseOrElse(String string, double orElse) {
        if (isEmpty(string)) {
            return orElse;
        }
        try {
            return parseDouble(string);
        } catch (Throwable throwable) {
            return orElse;
        }
    }

    public static int tryParseOrElse(String string, int orElse) {
        if (isEmpty(string)) {
            return orElse;
        }
        try {
            return parseInt(string);
        } catch (Throwable throwable) {
            return orElse;
        }
    }

    public static long tryParseOrElse(String string, long orElse) {
        if (isEmpty(string)) {
            return orElse;
        }
        try {
            return parseLong(string);
        } catch (Throwable throwable) {
            return orElse;
        }
    }

    public static boolean tryParseOrElse(String string, boolean orElse) {
        if (isEmpty(string)) {
            return orElse;
        }
        try {
            return parseBoolean(string);
        } catch (Throwable throwable) {
            return orElse;
        }
    }

    public static double tryParseOrElse(String string, Double orElse) {
        if (isEmpty(string)) {
            return orElse;
        }
        try {
            return parseDouble(string);
        } catch (Throwable throwable) {
            return orElse;
        }
    }

    public static int tryParseOrElse(String string, Integer orElse) {
        if (isEmpty(string)) {
            return orElse;
        }
        try {
            return parseInt(string);
        } catch (Throwable throwable) {
            return orElse;
        }
    }

    public static long tryParseOrElse(String string, Long orElse) {
        if (isEmpty(string)) {
            return orElse;
        }
        try {
            return parseLong(string);
        } catch (Throwable throwable) {
            return orElse;
        }
    }

    public static boolean tryParseOrElse(String string, Boolean orElse) {
        if (isEmpty(string)) {
            return orElse;
        }
        try {
            return parseBoolean(string);
        } catch (Throwable throwable) {
            return orElse;
        }
    }

    public static short tryParseOrElse(String string, Short orElse) {
        if (isEmpty(string)) {
            return orElse;
        }
        try {
            return parseShort(string);
        } catch (Throwable throwable) {
            return orElse;
        }
    }


    public static Boolean tryParseBoolean(String string) {
        if (isEmpty(string)) {
            return null;
        }
        try {
            return parseBoolean(string);
        } catch (Throwable throwable) {
            return null;
        }
    }

    public static Double tryParseDouble(String string) {
        if (isEmpty(string)) {
            return null;
        }
        try {
            return parseDouble(string);
        } catch (Throwable throwable) {
            return null;
        }
    }

    public static Float tryParseFloat(String string) {
        if (isEmpty(string)) {
            return null;
        }
        try {
            return parseFloat(string);
        } catch (Throwable throwable) {
            return null;
        }
    }

    public static Integer tryParseInt(String string) {
        if (isEmpty(string)) {
            return null;
        }
        try {
            return parseInt(string);
        } catch (Throwable throwable) {
            return null;
        }
    }

    public static Long tryParseLong(String string) {
        if (isEmpty(string)) {
            return null;
        }
        try {
            return parseLong(string);
        } catch (Throwable throwable) {
            return null;
        }
    }

    public static Byte tryParseByte(String string) {
        if (isEmpty(string)) {
            return null;
        }
        try {
            return parseByte(string);
        } catch (Throwable throwable) {
            return null;
        }
    }

    public static Short tryParseShort(String string) {
        if (isEmpty(string)) {
            return null;
        }
        try {
            return parseShort(string);
        } catch (Throwable throwable) {
            return null;
        }
    }
}
