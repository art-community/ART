/*
 * ART
 *
 * Copyright 2020 ART
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

package io.art.entity.mapping;

import io.art.entity.factory.*;
import io.art.entity.immutable.*;
import io.art.entity.mapper.ValueFromModelMapper.*;
import io.art.entity.mapper.*;
import io.art.entity.mapper.ValueToModelMapper.*;
import static io.art.entity.mapper.ValueMapper.*;

public interface PrimitiveMapping {
    PrimitiveFromModelMapper<String> fromString = PrimitivesFactory::stringPrimitive;
    PrimitiveToModelMapper<String> toString = Primitive::getString;

    PrimitiveFromModelMapper<Integer> fromInt = PrimitivesFactory::intPrimitive;
    PrimitiveToModelMapper<Integer> toInt = Primitive::getInt;

    PrimitiveFromModelMapper<Long> fromLong = PrimitivesFactory::longPrimitive;
    PrimitiveToModelMapper<Long> toLong = Primitive::getLong;

    PrimitiveFromModelMapper<Double> fromDouble = PrimitivesFactory::doublePrimitive;
    PrimitiveToModelMapper<Double> toDouble = Primitive::getDouble;

    PrimitiveFromModelMapper<Boolean> fromBool = PrimitivesFactory::boolPrimitive;
    PrimitiveToModelMapper<Boolean> toBool = Primitive::getBool;

    PrimitiveFromModelMapper<Byte> fromByte = PrimitivesFactory::bytePrimitive;
    PrimitiveToModelMapper<Byte> toByte = Primitive::getByte;

    PrimitiveFromModelMapper<Float> fromFloat = PrimitivesFactory::floatPrimitive;
    PrimitiveToModelMapper<Float> toFloat = Primitive::getFloat;
}
