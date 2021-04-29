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

package io.art.value.tuple;

import io.art.core.collection.*;
import io.art.value.builder.*;
import io.art.value.constants.ValueModuleConstants.*;
import io.art.value.exception.*;
import io.art.value.immutable.*;
import io.art.value.tuple.schema.*;
import lombok.experimental.*;
import static io.art.core.checker.EmptinessChecker.*;
import static io.art.value.constants.ValueModuleConstants.ExceptionMessages.*;
import static io.art.value.factory.ArrayValueFactory.*;
import static io.art.value.factory.PrimitivesFactory.*;
import static io.art.value.immutable.BinaryValue.*;
import static io.art.value.immutable.Entity.*;
import static java.text.MessageFormat.*;
import static java.util.Objects.*;
import java.util.*;

@UtilityClass
public class PlainTupleReader {
    public static Value readTuple(List<?> tuple, ValueSchema schema) {
        if (isEmpty(tuple) || isNull(schema)) return null;
        if (Value.isPrimitiveType(schema.getType())) {
            return readPrimitive(schema.getType(), tuple.get(0));
        }
        switch (schema.getType()) {
            case ENTITY:
                return readEntity(tuple, (EntitySchema) schema);
            case BINARY:
                return binary((byte[]) tuple.get(0));
            case ARRAY:
                return readArray(tuple, (ArraySchema) schema);
        }
        throw new ValueMappingException(format(schema.getType().name(), TUPLE_NOT_SUPPORTED_VALUE_TYPE));
    }

    @SuppressWarnings("Duplicates")
    private static Primitive readPrimitive(ValueType type, Object value) {
        if (isNull(value)) {
            return null;
        }
        switch (type) {
            case STRING:
                return stringPrimitive((String) value);
            case LONG:
                return longPrimitive(((Number) value).longValue());
            case DOUBLE:
                return doublePrimitive((Double) value);
            case FLOAT:
                return floatPrimitive((Float) value);
            case INT:
                return intPrimitive((Integer) value);
            case BOOL:
                return boolPrimitive((Boolean) value);
            case BYTE:
                return bytePrimitive((Byte) value);
        }
        throw new ValueMappingException(format(type.name(), TUPLE_NOT_SUPPORTED_VALUE_TYPE));
    }

    private static Entity readEntity(List<?> entity, EntitySchema schema) {
        if (isNull(schema)) return null;
        if (isEmpty(entity)) return null;
        EntityBuilder entityBuilder = entityBuilder();
        ImmutableArray<EntitySchema.EntityFieldSchema> fieldsSchema = schema.getFieldsSchema();
        for (int index = 0, fieldsSchemaSize = fieldsSchema.size(); index < fieldsSchemaSize; index++) {
            EntitySchema.EntityFieldSchema fieldSchema = fieldsSchema.get(index);
            Object value = entity.get(index);
            if (isNull(value) || isNull(fieldSchema)) continue;
            switch (fieldSchema.getType()) {
                case STRING:
                    entityBuilder.lazyPut(fieldSchema.getName(), () -> stringPrimitive((String) value));
                    break;
                case LONG:
                    entityBuilder.lazyPut(fieldSchema.getName(), () -> longPrimitive(((Number) value).longValue()));
                    break;
                case DOUBLE:
                    entityBuilder.lazyPut(fieldSchema.getName(), () -> doublePrimitive((Double) value));
                    break;
                case FLOAT:
                    entityBuilder.lazyPut(fieldSchema.getName(), () -> floatPrimitive((Float) value));
                    break;
                case INT:
                    entityBuilder.lazyPut(fieldSchema.getName(), () -> intPrimitive((Integer) value));
                    break;
                case BOOL:
                    entityBuilder.lazyPut(fieldSchema.getName(), () -> boolPrimitive((Boolean) value));
                    break;
                case BYTE:
                    entityBuilder.lazyPut(fieldSchema.getName(), () -> bytePrimitive((Byte) value));
                    break;
                case ENTITY:
                    entityBuilder.lazyPut(fieldSchema.getName(), () -> readEntity((List<?>) value, (EntitySchema) fieldSchema.getSchema()));
                    break;
                case BINARY:
                    entityBuilder.lazyPut(fieldSchema.getName(), () -> binary((byte[]) value));
                    break;
                case ARRAY:
                    entityBuilder.lazyPut(fieldSchema.getName(), () -> readArray((List<?>) value, (ArraySchema) fieldSchema.getSchema()));
                    break;
            }
        }
        return entityBuilder.build();
    }

    private static ArrayValue readArray(List<?> array, ArraySchema schema) {
        if (isEmpty(array)) return null;
        if (isNull(schema)) return null;
        return array(index -> readArrayElement(array, schema, index), array::size);
    }

    private static Value readArrayElement(List<?> array, ArraySchema schema, Integer index) {
        ValueSchema valueSchema = schema.getElements().get(index);
        Object element = array.get(index);
        if (isNull(element)) return null;
        switch (valueSchema.getType()) {
            case ENTITY:
            case ARRAY:
                return readTuple((List<?>) element, valueSchema);
            case STRING:
                return stringPrimitive((String) element);
            case LONG:
                return longPrimitive(((Number) element).longValue());
            case DOUBLE:
                return doublePrimitive(((Number) element).doubleValue());
            case FLOAT:
                return floatPrimitive(((Number) element).floatValue());
            case INT:
                return intPrimitive(((Number) element).intValue());
            case BOOL:
                return boolPrimitive((Boolean) element);
            case BYTE:
                return bytePrimitive(((Number) element).byteValue());
            case BINARY:
                return binary((byte[]) element);
        }
        throw new ValueMappingException(format(valueSchema.getType().name(), TUPLE_NOT_SUPPORTED_VALUE_TYPE));
    }
}
