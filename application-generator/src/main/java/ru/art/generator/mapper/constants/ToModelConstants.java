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

package ru.art.generator.mapper.constants;

import ru.art.entity.mapper.*;

/**
 * Interface for toModel variable's constants of mapper generator
 *
 * @see ValueToModelMapper
 */
public interface ToModelConstants {
    String TO_MODEL = "to";
    String ENTITY_TO_MODEL_LAMBDA = "entity -> isNotEmpty(entity) ? $T.builder()";
    String DEFAULT_ENTITY_BUILDER = ".build() : null";

    String GET_ENTITY_STRING = ".$L(entity.getString($N))";
    String GET_ENTITY_INT = ".$L(entity.getInt($N))";
    String GET_ENTITY_DOUBLE = ".$L(entity.getDouble($N))";
    String GET_ENTITY_LONG = ".$L(entity.getLong($N))";
    String GET_ENTITY_BYTE = ".$L(entity.getByte($N))";
    String GET_ENTITY_BOOL = ".$L(entity.getBool($N))";
    String GET_ENTITY_DATE = ".$L(entity.getDate($N))";
    String GET_ENTITY_FLOAT = ".$L(entity.getFloat($N))";
    String GET_ENTITY = ".$L(entity.getEntity($N))";

    String GET_VALUE = ".$L(entity.getValue($N, $T.$N))";
    String GET_ENUM_VALUE = ".$L($T.valueOf(entity.getString($N)))";

    String GET_ENTITY_LIST = ".$L(entity.getEntityList($N, $T.$N))";
    String GET_ENTITY_SET = ".$L(entity.getEntitySet($N, $T.$N))";
    String GET_ENTITY_QUEUE = ".$L(entity.getEntityQueue($N, $T.$N))";

    String GET_STRING_LIST = ".$L(entity.getStringList($N))";
    String GET_STRING_SET = ".$L(entity.getStringSet($N))";
    String GET_STRING_QUEUE = ".$L(entity.getStringQueue($N))";

    String GET_INT_LIST = ".$L(entity.getIntList($N))";
    String GET_INT_SET = ".$L(entity.getIntSet($N))";
    String GET_INT_QUEUE = ".$L(entity.getIntQueue($N))";

    String GET_DOUBLE_LIST = ".$L(entity.getDoubleList($N))";
    String GET_DOUBLE_SET = ".$L(entity.getDoubleSet($N))";
    String GET_DOUBLE_QUEUE = ".$L(entity.getDoubleQueue($N))";

    String GET_LONG_LIST = ".$L(entity.getLongList($N))";
    String GET_LONG_SET = ".$L(entity.getLongSet($N))";
    String GET_LONG_QUEUE = ".$L(entity.getLongQueue($N))";

    String GET_BYTE_LIST = ".$L(entity.getByteList($N))";
    String GET_BYTE_SET = ".$L(entity.getByteSet($N))";
    String GET_BYTE_QUEUE = ".$L(entity.getByteQueue($N))";

    String GET_BOOL_LIST = ".$L(entity.getBoolList($N))";
    String GET_BOOL_SET = ".$L(entity.getBoolSet($N))";
    String GET_BOOL_QUEUE = ".$L(entity.getBoolQueue($N))";

    String GET_FLOAT_LIST = ".$L(entity.getFloatList($N))";
    String GET_FLOAT_SET = ".$L(entity.getFloatSet($N))";
    String GET_FLOAT_QUEUE = ".$L(entity.getFloatQueue($N))";

    String GET_MAP_PRIMITIVE_KEY = ".$L(entity.getMap($N, $T.$L, $N))";
    String GET_MAP_PRIMITIVE_VALUE = ".$L(entity.getMap($N, $N, $T.$L))";
    String GET_MAP_PRIMITIVE_KEY_VALUE = ".$L(entity.getMap($N, $T.$L, $T.$L))";
    String GET_MAP = ".$L(entity.getMap($N, $N, $N))";

    String STRING_TO_MODEL = "StringPrimitive.toModel";
    String INT_TO_MODEL = "IntPrimitive.toModel";
    String LONG_TO_MODEL = "LongPrimitive.toModel";
    String DOUBLE_TO_MODEL = "DoublePrimitive.toModel";
    String BOOL_TO_MODEL = "BoolPrimitive.toModel";
    String BYTE_TO_MODEL = "BytePrimitive.toModel";
    String FLOAT_TO_MODEL = "FloatPrimitive.toModel";
}
