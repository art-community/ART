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

package io.art.meta.transformer;

import lombok.experimental.*;
import java.time.*;

@UtilityClass
public class PrimitiveTransformers {
    public static final MetaTransformer<String> STRING_TRANSFORMER = new MetaTransformer<>() {
        @Override
        public String transform(Object value) {
            return value.toString();
        }
    };

    public static final MetaTransformer<Integer> INT_TRANSFORMER = new MetaTransformer<Integer>() {
        public Integer transform(Number value) {
            return value.intValue();
        }

        public Integer transform(String value) {
            return Integer.parseInt(value);
        }
    };

    public static final MetaTransformer<Float> FLOAT_TRANSFORMER = new MetaTransformer<Float>() {
        public Float transform(Number value) {
            return value.floatValue();
        }

        public Float transform(String value) {
            return Float.parseFloat(value);
        }
    };

    public static final MetaTransformer<Double> DOUBLE_TRANSFORMER = new MetaTransformer<Double>() {
        public Double transform(Number value) {
            return value.doubleValue();
        }

        public Double transform(String value) {
            return Double.parseDouble(value);
        }
    };

    public static final MetaTransformer<Short> SHORT_TRANSFORMER = new MetaTransformer<Short>() {
        public Short transform(Number value) {
            return value.shortValue();
        }

        public Short transform(String value) {
            return Short.parseShort(value);
        }
    };

    public static final MetaTransformer<Long> LONG_TRANSFORMER = new MetaTransformer<Long>() {
        public Long transform(Number value) {
            return value.longValue();
        }

        public Long transform(String value) {
            return Long.parseLong(value);
        }
    };

    public static final MetaTransformer<Byte> BYTE_TRANSFORMER = new MetaTransformer<Byte>() {
        public Byte transform(Number value) {
            return value.byteValue();
        }

        public Byte transform(String value) {
            return Byte.parseByte(value);
        }
    };

    public static final MetaTransformer<Boolean> BOOLEAN_TRANSFORMER = new MetaTransformer<Boolean>() {
        public Boolean transform(Boolean value) {
            return value;
        }

        public Boolean transform(String value) {
            return Boolean.parseBoolean(value);
        }
    };

    public static final MetaTransformer<Character> CHARACTER_TRANSFORMER = new MetaTransformer<Character>() {
        public Character transform(Character value) {
            return value;
        }
    };

    public static final MetaTransformer<Duration> DURATION_TRANSFORMER = new MetaTransformer<Duration>() {
        public Duration transform(Duration value) {
            return value;
        }

        public Duration transform(Long value) {
            return Duration.ofMillis(value);
        }

        public Duration transform(String value) {
            return Duration.parse(value);
        }
    };
}