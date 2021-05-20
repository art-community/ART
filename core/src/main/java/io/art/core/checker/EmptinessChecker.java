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

package io.art.core.checker;

import io.art.core.collection.*;
import io.netty.buffer.*;
import lombok.experimental.*;
import static io.art.core.constants.CompilerSuppressingWarnings.*;
import static io.art.core.constants.StringConstants.*;
import static java.util.Objects.*;
import java.nio.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

@UtilityClass
public class EmptinessChecker {
    public static boolean isNotEmpty(Object value) {
        return !isEmpty(value);
    }

    public static boolean isNotEmpty(Object[] value) {
        return !isEmpty(value);
    }

    public static boolean isNotEmpty(Iterable<?> value) {
        return !isEmpty(value);
    }

    public static boolean isNotEmpty(byte[] content) {
        return !isEmpty(content);
    }

    public static boolean isNotEmpty(short[] content) {
        return !isEmpty(content);
    }

    public static boolean isNotEmpty(long[] content) {
        return !isEmpty(content);
    }

    public static boolean isNotEmpty(int[] content) {
        return !isEmpty(content);
    }

    public static boolean isNotEmpty(float[] content) {
        return !isEmpty(content);
    }

    public static boolean isNotEmpty(double[] content) {
        return !isEmpty(content);
    }

    public static boolean isNotEmpty(boolean[] content) {
        return !isEmpty(content);
    }

    public static boolean isNotEmpty(char[] content) {
        return !isEmpty(content);
    }

    public static boolean isNotEmpty(Collection<?> collection) {
        return !isEmpty(collection);
    }

    public static boolean isNotEmpty(Map<?, ?> map) {
        return !isEmpty(map);
    }

    public static boolean isNotEmpty(ImmutableMap<?, ?> map) {
        return !isEmpty(map);
    }

    public static boolean isNotEmpty(ImmutableCollection<?> collection) {
        return !isEmpty(collection);
    }

    public static boolean isNotEmpty(Stream<?> stream) {
        return !isEmpty(stream);
    }

    public static boolean isNotEmpty(ByteBuffer buffer) {
        return isEmpty(buffer);
    }

    public static boolean isNotEmpty(ByteBuf buffer) {
        return !isEmpty(buffer);
    }

    @SuppressWarnings(ALL)
    public static boolean isNotEmpty(Optional<?> optional) {
        return !isEmpty(optional);
    }


    public static boolean isEmpty(Object value) {
        String valAsString;
        return isNull(value) || (valAsString = value.toString().trim()).isEmpty() || (valAsString.equalsIgnoreCase(NULL_STRING));
    }

    public static boolean isEmpty(Iterable<?> value) {
        return isNull(value) || !value.iterator().hasNext();
    }

    public static boolean isEmpty(Object[] content) {
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

    public static boolean isEmpty(Collection<?> collection) {
        return isNull(collection) || collection.isEmpty();
    }

    public static boolean isEmpty(ImmutableMap<?, ?> map) {
        return isNull(map) || map.isEmpty();
    }

    public static boolean isEmpty(ImmutableCollection<?> collection) {
        return isNull(collection) || collection.isEmpty();
    }

    public static boolean isEmpty(Stream<?> stream) {
        return isNull(stream) || stream.count() == 0;
    }

    public static boolean isEmpty(ByteBuffer buffer) {
        return isNull(buffer) || buffer.remaining() == 0;
    }

    public static boolean isEmpty(ByteBuf buffer) {
        return isNull(buffer) || buffer.readableBytes() == 0;
    }

    @SuppressWarnings(ALL)
    public static boolean isEmpty(Optional<?> optional) {
        return isNull(optional) || !optional.isPresent();
    }


    public static <T> T ifEmpty(T value, T ifEmpty) {
        return isEmpty(value) ? ifEmpty : value;
    }

    public static <T> T ifEmpty(T value, Supplier<T> ifEmpty) {
        return isEmpty(value) ? ifEmpty.get() : value;
    }

    public static <T> void ifNotEmpty(T value, Consumer<T> action) {
        if (isEmpty(value)) {
            return;
        }
        action.accept(value);
    }

    public static <T, R> R letIfNotEmpty(T value, Function<T, R> action) {
        if (isEmpty(value)) {
            return null;
        }
        return action.apply(value);
    }

    public static <T, R> R letIfNotEmpty(T value, Function<T, R> action, R orElse) {
        if (isEmpty(value)) {
            return orElse;
        }
        return action.apply(value);
    }

    public static <T, R> R letIfNotEmpty(T value, Function<T, R> action, Supplier<R> orElse) {
        if (isEmpty(value)) {
            return orElse.get();
        }
        return action.apply(value);
    }
}
