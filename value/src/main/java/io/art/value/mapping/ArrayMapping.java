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

package io.art.value.mapping;

import io.art.core.annotation.*;
import io.art.core.collection.*;
import io.art.value.factory.*;
import io.art.value.immutable.*;
import io.art.value.mapper.*;
import io.art.value.mapper.ValueFromModelMapper.*;
import io.art.value.mapper.ValueToModelMapper.*;
import lombok.experimental.*;
import static io.art.core.caster.Caster.*;
import static io.art.core.checker.NullityChecker.*;
import static io.art.core.collection.ImmutableArray.*;
import static io.art.value.factory.ArrayValueFactory.*;
import static java.util.Arrays.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

@UtilityClass
@UsedByGenerator
public class ArrayMapping {
    public ArrayToModelMapper<int[]> toIntArray = array -> let(array, ArrayValue::intArray);
    public ArrayToModelMapper<long[]> toLongArray = array -> let(array, ArrayValue::longArray);
    public ArrayToModelMapper<short[]> toShortArray = array -> let(array, ArrayValue::shortArray);
    public ArrayToModelMapper<double[]> toDoubleArray = array -> let(array, ArrayValue::doubleArray);
    public ArrayToModelMapper<float[]> toFloatArray = array -> let(array, ArrayValue::floatArray);
    public ArrayToModelMapper<byte[]> toByteArray = array -> let(array, ArrayValue::byteArray);
    public ArrayToModelMapper<char[]> toCharArray = array -> let(array, ArrayValue::charArray);
    public ArrayToModelMapper<boolean[]> toBoolArray = array -> let(array, ArrayValue::boolArray);

    public ArrayFromModelMapper<int[]> fromIntArray = array -> let(array, ArrayValueFactory::intArray);
    public ArrayFromModelMapper<long[]> fromLongArray = array -> let(array, ArrayValueFactory::longArray);
    public ArrayFromModelMapper<short[]> fromShortArray = array -> let(array, ArrayValueFactory::shortArray);
    public ArrayFromModelMapper<double[]> fromDoubleArray = array -> let(array, ArrayValueFactory::doubleArray);
    public ArrayFromModelMapper<float[]> fromFloatArray = array -> let(array, ArrayValueFactory::floatArray);
    public ArrayFromModelMapper<byte[]> fromByteArray = array -> let(array, ArrayValueFactory::byteArray);
    public ArrayFromModelMapper<char[]> fromCharArray = array -> let(array, ArrayValueFactory::charArray);
    public ArrayFromModelMapper<boolean[]> fromBoolArray = array -> let(array, ArrayValueFactory::boolArray);


    public static <T> ArrayToModelMapper<T[]> toArrayRaw(Function<Integer, ?> factory, ValueToModelMapper<T, ? extends Value> elementMapper) {
        Function<Integer, T[]> function = cast(factory);
        return array -> let(array, notNull -> notNull.asList(elementMapper).toArray(function.apply(array.size())));
    }

    public static <T> ArrayToModelMapper<Stream<T>> toStream(ValueToModelMapper<T, ? extends Value> elementMapper) {
        return array -> let(array, notNull -> notNull.asList(elementMapper).stream());
    }


    public static <T> ArrayToModelMapper<ImmutableArray<T>> toImmutableArray(ValueToModelMapper<T, ? extends Value> elementMapper) {
        return array -> let(array, notNull -> notNull.asImmutableArray(elementMapper));
    }

    public static <T> ArrayToModelMapper<ImmutableSet<T>> toImmutableSet(ValueToModelMapper<T, ? extends Value> elementMapper) {
        return array -> let(array, notNull -> notNull.asImmutableSet(elementMapper));
    }


    public static <T> ArrayFromModelMapper<ImmutableArray<T>> fromImmutableArray(ValueFromModelMapper<T, ? extends Value> elementMapper) {
        return list -> let(list, notNull -> array(list, elementMapper));
    }

    public static <T> ArrayFromModelMapper<ImmutableSet<T>> fromImmutableSet(ValueFromModelMapper<T, ? extends Value> elementMapper) {
        return list -> let(list, notNull -> array(list, elementMapper));
    }


    public static <T> ArrayFromModelMapper<Collection<T>> fromCollection(ValueFromModelMapper<T, ? extends Value> elementMapper) {
        return list -> let(list, notNull -> array(list, elementMapper));
    }

    public static <T> ArrayFromModelMapper<Stream<T>> fromStream(ValueFromModelMapper<T, ? extends Value> elementMapper) {
        return list -> let(list, notNull -> array(list.collect(immutableArrayCollector()), elementMapper));
    }

    public static <T> ArrayFromModelMapper<T[]> fromArray(ValueFromModelMapper<T, ? extends Value> elementMapper) {
        return array -> let(array, notNull -> array(asList(array), elementMapper));
    }

    public static <T> ArrayFromModelMapper<List<T>> fromList(ValueFromModelMapper<T, ? extends Value> elementMapper) {
        return list -> let(list, notNull -> array(list, elementMapper));
    }

    public static <T> ArrayFromModelMapper<Set<T>> fromSet(ValueFromModelMapper<T, ? extends Value> elementMapper) {
        return collection -> let(collection, notNull -> array(collection, elementMapper));
    }

    public static <T> ArrayFromModelMapper<Queue<T>> fromQueue(ValueFromModelMapper<T, ? extends Value> elementMapper) {
        return collection -> let(collection, notNull -> array(collection, elementMapper));
    }

    public static <T> ArrayFromModelMapper<Deque<T>> fromDeque(ValueFromModelMapper<T, ? extends Value> elementMapper) {
        return collection -> let(collection, notNull -> array(collection, elementMapper));
    }


    public static <T> ArrayToModelMapper<Collection<T>> toMutableCollection(ValueToModelMapper<T, ? extends Value> elementMapper) {
        return array -> let(array, notNull -> notNull.toList(elementMapper));
    }

    public static <T> ArrayToModelMapper<List<T>> toMutableList(ValueToModelMapper<T, ? extends Value> elementMapper) {
        return array -> let(array, notNull -> notNull.toList(elementMapper));
    }

    public static <T> ArrayToModelMapper<Set<T>> toMutableSet(ValueToModelMapper<T, ? extends Value> elementMapper) {
        return array -> let(array, notNull -> notNull.toSet(elementMapper));
    }

    public static <T> ArrayToModelMapper<Queue<T>> toMutableQueue(ValueToModelMapper<T, ? extends Value> elementMapper) {
        return array -> let(array, notNull -> notNull.toQueue(elementMapper));
    }

    public static <T> ArrayToModelMapper<Deque<T>> toMutableDeque(ValueToModelMapper<T, ? extends Value> elementMapper) {
        return array -> let(array, notNull -> notNull.toDeque(elementMapper));
    }
}
