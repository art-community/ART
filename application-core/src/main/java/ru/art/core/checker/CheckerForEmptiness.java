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

package ru.art.core.checker;

import lombok.experimental.*;
import static java.util.Objects.*;
import static ru.art.core.constants.StringConstants.*;
import java.util.*;
import java.util.stream.Stream;

@UtilityClass
public class CheckerForEmptiness {
    public static <T> boolean isNotEmpty(T val) {
        return !isEmpty(val);
    }

    public static <T> boolean isEmpty(T val) {
        String valAsString;
        return isNull(val) || (valAsString = val.toString().trim()).isEmpty() || (valAsString.equalsIgnoreCase(NULL_STRING));
    }

    public static <T> boolean isEmpty(T[] content) {
        return isNull(content) || content.length == 0;
    }

    public static boolean isEmpty(byte[] content) {
        return isNull(content) || content.length == 0;
    }

    public static boolean isEmpty(short[] content) {
        return isNull(content) || content.length == 0;
    }

    public static boolean isEmpty(int[] content) {
        return isNull(content) || content.length == 0;
    }

    public static boolean isEmpty(long[] content) {
        return isNull(content) || content.length == 0;
    }

    public static boolean isEmpty(float[] content) {
        return isNull(content) || content.length == 0;
    }

    public static boolean isEmpty(double[] content) {
        return isNull(content) || content.length == 0;
    }

    public static boolean isEmpty(boolean[] content) {
        return isNull(content) || content.length == 0;
    }

    public static boolean isEmpty(char[] content) {
        return isNull(content) || content.length == 0;
    }

    public static boolean isEmpty(Map<?, ?> map) {
        return isNull(map) || map.isEmpty();
    }

    public static boolean isNotEmpty(Collection<?> collection) {
        return !isEmpty(collection);
    }

    public static boolean isEmpty(Collection<?> collection) {
        return isNull(collection) || collection.isEmpty();
    }

    public static boolean isEmpty(Stream<?> stream) {
        return isNull(stream) || stream.count() == 0;
    }

    public static boolean isNotEmpty(Stream<?> stream) {
        return !isEmpty(stream);
    }

    public static <T> T ifEmpty(T val, T ifEmpty) {
        return isEmpty(val) ? ifEmpty : val;
    }
}
