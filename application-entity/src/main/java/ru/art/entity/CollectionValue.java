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

package ru.art.entity;

import lombok.*;
import ru.art.core.checker.*;
import ru.art.core.factory.*;
import ru.art.entity.constants.*;
import ru.art.entity.constants.ValueType.*;
import ru.art.entity.exception.*;
import static java.lang.Boolean.*;
import static java.lang.Byte.*;
import static java.lang.Double.*;
import static java.lang.Float.*;
import static java.lang.Integer.*;
import static java.lang.Long.*;
import static java.text.MessageFormat.*;
import static java.util.Arrays.*;
import static java.util.Collections.*;
import static java.util.Objects.*;
import static java.util.stream.Collectors.*;
import static ru.art.core.caster.Caster.*;
import static ru.art.core.constants.ArrayConstants.*;
import static ru.art.core.constants.StringConstants.*;
import static ru.art.core.extension.StringExtensions.*;
import static ru.art.core.factory.CollectionsFactory.*;
import static ru.art.entity.PrimitivesFactory.*;
import static ru.art.entity.Value.*;
import static ru.art.entity.constants.CollectionMode.COLLECTION;
import static ru.art.entity.constants.CollectionMode.*;
import static ru.art.entity.constants.ValueMappingExceptionMessages.*;
import static ru.art.entity.constants.ValueType.CollectionElementsType.BOOL;
import static ru.art.entity.constants.ValueType.CollectionElementsType.BYTE;
import static ru.art.entity.constants.ValueType.CollectionElementsType.DOUBLE;
import static ru.art.entity.constants.ValueType.CollectionElementsType.ENTITY;
import static ru.art.entity.constants.ValueType.CollectionElementsType.FLOAT;
import static ru.art.entity.constants.ValueType.CollectionElementsType.INT;
import static ru.art.entity.constants.ValueType.CollectionElementsType.LONG;
import static ru.art.entity.constants.ValueType.CollectionElementsType.MAP;
import static ru.art.entity.constants.ValueType.CollectionElementsType.STRING;
import static ru.art.entity.constants.ValueType.CollectionElementsType.STRING_PARAMETERS_MAP;
import static ru.art.entity.constants.ValueType.CollectionElementsType.*;
import java.util.*;

@Getter
public class CollectionValue<T> implements Value {
    private final CollectionElementsType elementsType;
    private final Collection<T> elements;
    private final ValueType type = ValueType.COLLECTION;
    private final CollectionMode collectionMode;
    private byte[] byteElements;
    private int[] intElements;
    private long[] longElements;
    private double[] doubleElements;
    private boolean[] boolElements;
    private float[] floatElements;

    CollectionValue() {
        this.elements = emptyList();
        this.elementsType = VALUE;
        collectionMode = COLLECTION;
    }

    CollectionValue(CollectionElementsType elementsType, Collection<T> elements) {
        this.elementsType = elementsType;
        this.elements = elements;
        collectionMode = COLLECTION;
    }

    CollectionValue(byte[] byteElements) {
        this.byteElements = byteElements;
        elementsType = BYTE;
        elements = emptyList();
        collectionMode = PRIMITIVE_ARRAY;
    }

    CollectionValue(int[] intElements) {
        this.intElements = intElements;
        elementsType = INT;
        elements = null;
        collectionMode = PRIMITIVE_ARRAY;
    }

    CollectionValue(long[] longElements) {
        this.longElements = longElements;
        elementsType = LONG;
        elements = null;
        collectionMode = PRIMITIVE_ARRAY;
    }

    CollectionValue(double[] doubleElements) {
        this.doubleElements = doubleElements;
        elementsType = DOUBLE;
        elements = null;
        collectionMode = PRIMITIVE_ARRAY;
    }

    CollectionValue(boolean[] boolElements) {
        this.boolElements = boolElements;
        elementsType = BOOL;
        elements = null;
        collectionMode = PRIMITIVE_ARRAY;
    }

    CollectionValue(float[] floatElements) {
        this.floatElements = floatElements;
        elementsType = FLOAT;
        elements = null;
        collectionMode = PRIMITIVE_ARRAY;
    }


    public List<T> getList() {
        if (isEmpty()) return emptyList();
        if (collectionMode == PRIMITIVE_ARRAY) {
            switch (elementsType) {
                case LONG:
                    return cast(arrayOf(longElements));
                case DOUBLE:
                    return cast(arrayOf(doubleElements));
                case FLOAT:
                    return cast(arrayOf(floatElements));
                case INT:
                    return cast(arrayOf(intElements));
                case BOOL:
                    return cast(arrayOf(boolElements));
                case BYTE:
                    return cast(arrayOf(byteElements));
            }
        }
        return fixedArrayOf(elements);
    }

    public Set<T> getSet() {
        if (isEmpty()) return emptySet();
        if (collectionMode == PRIMITIVE_ARRAY) {
            switch (elementsType) {
                case LONG:
                    return cast(setOf(longElements));
                case DOUBLE:
                    return cast(setOf(doubleElements));
                case FLOAT:
                    return cast(setOf(floatElements));
                case INT:
                    return cast(setOf(intElements));
                case BOOL:
                    return cast(setOf(boolElements));
                case BYTE:
                    return cast(setOf(byteElements));
            }
        }
        return cast(setOf((Collection<?>) elements));
    }

    public Queue<T> getQueue() {
        if (isEmpty()) return queueOf();
        if (collectionMode == PRIMITIVE_ARRAY) {
            switch (elementsType) {
                case LONG:
                    return cast(queueOf(longElements));
                case DOUBLE:
                    return cast(queueOf(doubleElements));
                case FLOAT:
                    return cast(queueOf(floatElements));
                case INT:
                    return cast(queueOf(intElements));
                case BOOL:
                    return cast(queueOf(boolElements));
                case BYTE:
                    return cast(queueOf(byteElements));
            }
        }
        return cast(queueOf((Collection<?>) elements));
    }


    public List<Value> getValueList() {
        if (isEmpty()) return emptyList();
        List<Value> list = dynamicArrayOf();
        switch (elementsType) {
            case STRING:
                return elements
                        .stream()
                        .filter(Objects::nonNull)
                        .map(element -> stringPrimitive(cast(element)))
                        .collect(toList());
            case LONG: {
                if (collectionMode == PRIMITIVE_ARRAY) {
                    for (long element : longElements) {
                        list.add(longPrimitive(element));
                    }
                    return list;
                }
                return elements.stream()
                        .filter(Objects::nonNull)
                        .map(element -> longPrimitive(((Number) element).longValue()))
                        .collect(toList());
            }
            case DOUBLE: {
                if (collectionMode == PRIMITIVE_ARRAY) {
                    for (double element : doubleElements) {
                        list.add(doublePrimitive(element));
                    }
                    return list;
                }
                return elements.stream()
                        .filter(Objects::nonNull)
                        .map(element -> doublePrimitive(((Number) element).doubleValue()))
                        .collect(toList());
            }
            case FLOAT: {
                if (collectionMode == PRIMITIVE_ARRAY) {
                    for (float element : floatElements) {
                        list.add(floatPrimitive(element));
                    }
                    return list;
                }
                return elements.stream()
                        .filter(Objects::nonNull)
                        .map(element -> floatPrimitive(((Number) element).floatValue()))
                        .collect(toList());
            }
            case INT: {
                if (collectionMode == PRIMITIVE_ARRAY) {
                    for (int element : intElements) {
                        list.add(intPrimitive(element));
                    }
                    return list;
                }
                return elements.stream()
                        .filter(Objects::nonNull)
                        .map(element -> intPrimitive(((Number) element).intValue()))
                        .collect(toList());
            }
            case BOOL: {
                if (collectionMode == PRIMITIVE_ARRAY) {
                    for (boolean element : boolElements) {
                        list.add(boolPrimitive(element));
                    }
                    return list;
                }
                return elements.stream()
                        .filter(Objects::nonNull)
                        .map(element -> boolPrimitive(cast(element)))
                        .collect(toList());
            }
            case BYTE: {
                if (collectionMode == PRIMITIVE_ARRAY) {
                    for (byte element : byteElements) {
                        list.add(bytePrimitive(element));
                    }
                    return list;
                }
                return elements.stream()
                        .filter(Objects::nonNull)
                        .map(element -> bytePrimitive(((Number) element).byteValue()))
                        .collect(toList());
            }
            case ENTITY:
                return cast(getEntityList());
            case COLLECTION:
                return cast(getCollectionsList());
            case MAP:
                return cast(getMapValueList());
            case STRING_PARAMETERS_MAP:
                return cast(getStringParametersList());
            case VALUE:
                return cast(getList());
        }
        return cast(getList());
    }

    public List<MapValue> getMapValueList() {
        if (isEmpty()) return emptyList();
        if (elementsType != MAP && elementsType != VALUE) {
            throw new ValueMappingException(format(REQUEST_LIST_ELEMENTS_TYPE_INVALID, MAP.toString(), elementsType.toString()));
        }
        return cast(getList());
    }

    public List<CollectionValue<?>> getCollectionsList() {
        if (isEmpty()) return emptyList();
        if (elementsType != CollectionElementsType.COLLECTION && elementsType != VALUE) {
            throw new ValueMappingException(format(REQUEST_LIST_ELEMENTS_TYPE_INVALID, COLLECTION.toString(), elementsType.toString()));
        }
        return cast(getList());
    }

    public List<StringParametersMap> getStringParametersList() {
        if (isEmpty()) return emptyList();
        if (elementsType != STRING_PARAMETERS_MAP && elementsType != VALUE) {
            throw new ValueMappingException(format(REQUEST_LIST_ELEMENTS_TYPE_INVALID, STRING_PARAMETERS_MAP.toString(), elementsType.toString()));
        }
        return cast(getList());
    }

    public List<Entity> getEntityList() {
        if (isEmpty()) return emptyList();
        if (elementsType != ENTITY && elementsType != VALUE) {
            throw new ValueMappingException(format(REQUEST_LIST_ELEMENTS_TYPE_INVALID, ENTITY.toString(), elementsType.toString()));
        }
        return cast(getList());
    }

    public List<Long> getLongList() {
        if (isEmpty()) return emptyList();
        List<Long> list = dynamicArrayOf();
        switch (elementsType) {
            case VALUE:
                return elements.stream()
                        .filter(element -> isPrimitive(cast(element)))
                        .map(element -> asPrimitive(cast(element)).getLong())
                        .collect(toList());
            case STRING:
                return elements.stream()
                        .filter(Objects::nonNull)
                        .map(element -> parseLong(cast(element)))
                        .collect(toList());
            case LONG: {
                if (collectionMode == PRIMITIVE_ARRAY) {
                    for (long element : longElements) {
                        list.add(element);
                    }
                    return list;
                }
                return cast(fixedArrayOf(elements));
            }
            case DOUBLE: {
                if (collectionMode == PRIMITIVE_ARRAY) {
                    for (double element : doubleElements) {
                        list.add((long) element);
                    }
                    return list;
                }
                return elements.stream()
                        .filter(Objects::nonNull)
                        .map(element -> ((Number) element).longValue())
                        .collect(toList());
            }
            case FLOAT: {
                if (collectionMode == PRIMITIVE_ARRAY) {
                    for (float element : floatElements) {
                        list.add((long) element);
                    }
                    return list;
                }
                return elements.stream()
                        .filter(Objects::nonNull)
                        .map(element -> ((Number) element).longValue())
                        .collect(toList());
            }
            case INT: {
                if (collectionMode == PRIMITIVE_ARRAY) {
                    for (int element : intElements) {
                        list.add((long) element);
                    }
                    return list;
                }
                return elements.stream()
                        .filter(Objects::nonNull)
                        .map(element -> ((Number) element).longValue())
                        .collect(toList());
            }
            case BYTE: {
                if (collectionMode == PRIMITIVE_ARRAY) {
                    for (byte element : byteElements) {
                        list.add((long) element);
                    }
                    return list;
                }
                return elements.stream()
                        .filter(Objects::nonNull)
                        .map(element -> ((Number) element).longValue())
                        .collect(toList());
            }
        }
        throw new ValueMappingException(format(REQUEST_LIST_ELEMENTS_TYPE_INVALID, LONG.toString(), elementsType.toString()));
    }

    public List<String> getStringList() {
        if (isEmpty()) return emptyList();
        List<String> list = dynamicArrayOf();
        if (collectionMode == PRIMITIVE_ARRAY) {
            switch (elementsType) {
                case LONG: {
                    for (long element : longElements) {
                        list.add(EMPTY_STRING + element);
                    }
                    return list;
                }
                case DOUBLE: {
                    for (double element : doubleElements) {
                        list.add(EMPTY_STRING + element);
                    }
                    return list;
                }
                case FLOAT: {
                    for (float element : floatElements) {
                        list.add(EMPTY_STRING + element);
                    }
                    return list;
                }
                case INT: {
                    for (int element : intElements) {
                        list.add(EMPTY_STRING + element);
                    }
                    return list;
                }
                case BOOL: {
                    for (boolean element : boolElements) {
                        list.add(EMPTY_STRING + element);
                    }
                    return list;
                }
                case BYTE: {
                    for (byte element : byteElements) {
                        list.add(EMPTY_STRING + element);
                    }
                    return list;
                }
            }
        }
        if (elementsType == STRING) {
            return cast(getList());
        }
        return getList().stream().filter(Objects::nonNull).map(Object::toString).collect(toList());
    }

    public List<Boolean> getBoolList() {
        if (isEmpty()) return emptyList();
        if (collectionMode == PRIMITIVE_ARRAY) {
            if (elementsType != BOOL) {
                throw new ValueMappingException(format(REQUEST_LIST_ELEMENTS_TYPE_INVALID, BOOL.toString(), elementsType.toString()));
            }
            List<Boolean> list = dynamicArrayOf();
            for (boolean element : boolElements) {
                list.add(element);
            }
            return list;
        }
        switch (elementsType) {
            case VALUE:
                return getList()
                        .stream()
                        .filter(element -> isPrimitive(cast(element)))
                        .map(element -> asPrimitive(cast(element)).getBool())
                        .collect(toList());
            case STRING:
                return elements.stream()
                        .filter(Objects::nonNull)
                        .map(element -> parseBoolean((cast(element))))
                        .collect(toList());
            case BOOL:
                return cast(getList());
        }
        throw new ValueMappingException(format(REQUEST_LIST_ELEMENTS_TYPE_INVALID, BOOL.toString(), elementsType.toString()));
    }

    public List<Integer> getIntList() {
        if (isEmpty()) return emptyList();
        List<Integer> list = dynamicArrayOf();
        switch (elementsType) {
            case VALUE:
                return elements.stream()
                        .filter(element -> isPrimitive(cast(element)))
                        .map(element -> asPrimitive(cast(element)).getInt())
                        .collect(toList());
            case STRING:
                return elements.stream()
                        .filter(Objects::nonNull)
                        .map(element -> parseInt(cast(element)))
                        .collect(toList());
            case LONG: {
                if (collectionMode == PRIMITIVE_ARRAY) {
                    for (long element : longElements) {
                        list.add((int) element);
                    }
                    return list;
                }
                return elements.stream()
                        .filter(Objects::nonNull)
                        .map(element -> ((Number) element).intValue())
                        .collect(toList());
            }
            case DOUBLE: {
                if (collectionMode == PRIMITIVE_ARRAY) {
                    for (double element : doubleElements) {
                        list.add((int) element);
                    }
                    return list;
                }
                return elements.stream()
                        .filter(Objects::nonNull)
                        .map(element -> ((Number) element).intValue())
                        .collect(toList());
            }
            case FLOAT: {
                if (collectionMode == PRIMITIVE_ARRAY) {
                    for (float element : floatElements) {
                        list.add((int) element);
                    }
                    return list;
                }
                return elements.stream()
                        .filter(Objects::nonNull)
                        .map(element -> ((Number) element).intValue())
                        .collect(toList());
            }
            case INT: {
                if (collectionMode == PRIMITIVE_ARRAY) {
                    for (int element : intElements) {
                        list.add(element);
                    }
                    return list;
                }
                return elements.stream()
                        .filter(Objects::nonNull)
                        .map(element -> ((Number) element).intValue())
                        .collect(toList());
            }
            case BYTE: {
                if (collectionMode == PRIMITIVE_ARRAY) {
                    for (byte element : byteElements) {
                        list.add((int) element);
                    }
                    return list;
                }
                return elements.stream()
                        .filter(Objects::nonNull)
                        .map(element -> ((Number) element).intValue())
                        .collect(toList());
            }
        }
        throw new ValueMappingException(format(REQUEST_LIST_ELEMENTS_TYPE_INVALID, INT.toString(), elementsType.toString()));
    }

    public List<Byte> getByteList() {
        if (isEmpty()) return emptyList();
        List<Byte> list = dynamicArrayOf();
        switch (elementsType) {
            case VALUE:
                return elements.stream()
                        .filter(element -> isPrimitive(cast(element)))
                        .map(element -> asPrimitive(cast(element)).getByte())
                        .collect(toList());
            case STRING:
                return elements.stream()
                        .filter(Objects::nonNull)
                        .map(element -> parseByte(cast(element)))
                        .collect(toList());
            case LONG: {
                if (collectionMode == PRIMITIVE_ARRAY) {
                    for (long element : longElements) {
                        list.add((byte) element);
                    }
                    return list;
                }
                return elements.stream()
                        .filter(Objects::nonNull)
                        .map(element -> ((Number) element).byteValue())
                        .collect(toList());
            }
            case DOUBLE: {
                if (collectionMode == PRIMITIVE_ARRAY) {
                    for (double element : doubleElements) {
                        list.add((byte) element);
                    }
                    return list;
                }
                return elements.stream()
                        .filter(Objects::nonNull)
                        .map(element -> ((Number) element).byteValue())
                        .collect(toList());
            }
            case FLOAT: {
                if (collectionMode == PRIMITIVE_ARRAY) {
                    for (float element : floatElements) {
                        list.add((byte) element);
                    }
                    return list;
                }
                return elements.stream()
                        .filter(Objects::nonNull)
                        .map(element -> ((Number) element).byteValue())
                        .collect(toList());
            }
            case INT: {
                if (collectionMode == PRIMITIVE_ARRAY) {
                    for (int element : intElements) {
                        list.add((byte) element);
                    }
                    return list;
                }
                return elements.stream()
                        .filter(Objects::nonNull)
                        .map(element -> ((Number) element).byteValue())
                        .collect(toList());
            }
            case BYTE: {
                if (collectionMode == PRIMITIVE_ARRAY) {
                    for (byte element : byteElements) {
                        list.add(element);
                    }
                    return list;
                }
                return elements.stream()
                        .filter(Objects::nonNull)
                        .map(element -> ((Number) element).byteValue())
                        .collect(toList());
            }
        }
        throw new ValueMappingException(format(REQUEST_LIST_ELEMENTS_TYPE_INVALID, BYTE.toString(), elementsType.toString()));
    }

    public List<Double> getDoubleList() {
        if (isEmpty()) return emptyList();
        List<Double> list = dynamicArrayOf();
        switch (elementsType) {
            case VALUE:
                return elements.stream()
                        .filter(element -> isPrimitive(cast(element)))
                        .map(element -> asPrimitive(cast(element)).getDouble())
                        .collect(toList());
            case STRING:
                return elements.stream()
                        .filter(Objects::nonNull)
                        .map(element -> parseDouble(cast(element)))
                        .collect(toList());
            case LONG: {
                if (collectionMode == PRIMITIVE_ARRAY) {
                    for (long element : longElements) {
                        list.add((double) element);
                    }
                    return list;
                }
                return elements.stream()
                        .filter(Objects::nonNull)
                        .map(element -> ((Number) element).doubleValue())
                        .collect(toList());
            }
            case DOUBLE: {
                if (collectionMode == PRIMITIVE_ARRAY) {
                    for (double element : doubleElements) {
                        list.add(element);
                    }
                    return list;
                }
                return elements.stream()
                        .filter(Objects::nonNull)
                        .map(element -> ((Number) element).doubleValue())
                        .collect(toList());
            }
            case FLOAT: {
                if (collectionMode == PRIMITIVE_ARRAY) {
                    for (float element : floatElements) {
                        list.add((double) element);
                    }
                    return list;
                }
                return elements.stream()
                        .filter(Objects::nonNull)
                        .map(element -> ((Number) element).doubleValue())
                        .collect(toList());
            }
            case INT: {
                if (collectionMode == PRIMITIVE_ARRAY) {
                    for (int element : intElements) {
                        list.add((double) element);
                    }
                    return list;
                }
                return elements.stream()
                        .filter(Objects::nonNull)
                        .map(element -> ((Number) element).doubleValue())
                        .collect(toList());
            }
            case BYTE: {
                if (collectionMode == PRIMITIVE_ARRAY) {
                    for (byte element : byteElements) {
                        list.add((double) element);
                    }
                    return list;
                }
                return elements.stream()
                        .filter(Objects::nonNull)
                        .map(element -> ((Number) element).doubleValue())
                        .collect(toList());
            }
        }
        throw new ValueMappingException(format(REQUEST_LIST_ELEMENTS_TYPE_INVALID, DOUBLE.toString(), elementsType.toString()));
    }

    public List<Float> getFloatList() {
        if (isEmpty()) return emptyList();
        List<Float> list = dynamicArrayOf();
        switch (elementsType) {
            case VALUE:
                return elements.stream()
                        .filter(element -> isPrimitive(cast(element)))
                        .map(element -> asPrimitive(cast(element)).getFloat())
                        .collect(toList());
            case STRING:
                return elements.stream()
                        .filter(Objects::nonNull)
                        .map(element -> parseFloat(cast(element)))
                        .collect(toList());
            case LONG: {
                if (collectionMode == PRIMITIVE_ARRAY) {
                    for (long element : longElements) {
                        list.add((float) element);
                    }
                    return list;
                }
                return elements.stream()
                        .filter(Objects::nonNull)
                        .map(element -> ((Number) element).floatValue())
                        .collect(toList());
            }
            case DOUBLE: {
                if (collectionMode == PRIMITIVE_ARRAY) {
                    for (double element : doubleElements) {
                        list.add((float) element);
                    }
                    return list;
                }
                return elements.stream()
                        .filter(Objects::nonNull)
                        .map(element -> ((Number) element).floatValue())
                        .collect(toList());
            }
            case FLOAT: {
                if (collectionMode == PRIMITIVE_ARRAY) {
                    for (float element : floatElements) {
                        list.add(element);
                    }
                    return list;
                }
                return elements.stream()
                        .filter(Objects::nonNull)
                        .map(element -> ((Number) element).floatValue())
                        .collect(toList());
            }
            case INT: {
                if (collectionMode == PRIMITIVE_ARRAY) {
                    for (int element : intElements) {
                        list.add((float) element);
                    }
                    return list;
                }
                return elements.stream()
                        .filter(Objects::nonNull)
                        .map(element -> ((Number) element).floatValue())
                        .collect(toList());
            }
            case BYTE: {
                if (collectionMode == PRIMITIVE_ARRAY) {
                    for (byte element : byteElements) {
                        list.add((float) element);
                    }
                    return list;
                }
                return elements.stream()
                        .filter(Objects::nonNull)
                        .map(element -> ((Number) element).floatValue())
                        .collect(toList());
            }
        }
        throw new ValueMappingException(format(REQUEST_LIST_ELEMENTS_TYPE_INVALID, FLOAT.toString(), elementsType.toString()));
    }


    public Set<Value> getValueSet() {
        if (isEmpty()) return emptySet();
        Set<Value> set = setOf();
        switch (elementsType) {
            case STRING:
                return elements
                        .stream()
                        .filter(Objects::nonNull)
                        .map(element -> stringPrimitive(cast(element)))
                        .collect(toSet());
            case LONG: {
                if (collectionMode == PRIMITIVE_ARRAY) {
                    for (long element : longElements) {
                        set.add(longPrimitive(element));
                    }
                    return set;
                }
                return elements.stream()
                        .filter(Objects::nonNull)
                        .map(element -> longPrimitive(((Number) element).longValue()))
                        .collect(toSet());
            }
            case DOUBLE: {
                if (collectionMode == PRIMITIVE_ARRAY) {
                    for (double element : doubleElements) {
                        set.add(doublePrimitive(element));
                    }
                    return set;
                }
                return elements.stream()
                        .filter(Objects::nonNull)
                        .map(element -> doublePrimitive(((Number) element).doubleValue()))
                        .collect(toSet());
            }
            case FLOAT: {
                if (collectionMode == PRIMITIVE_ARRAY) {
                    for (float element : floatElements) {
                        set.add(floatPrimitive(element));
                    }
                    return set;
                }
                return elements.stream()
                        .filter(Objects::nonNull)
                        .map(element -> floatPrimitive(((Number) element).floatValue()))
                        .collect(toSet());
            }
            case INT: {
                if (collectionMode == PRIMITIVE_ARRAY) {
                    for (int element : intElements) {
                        set.add(intPrimitive(element));
                    }
                    return set;
                }
                return elements.stream()
                        .filter(Objects::nonNull)
                        .map(element -> intPrimitive(((Number) element).intValue()))
                        .collect(toSet());
            }
            case BOOL: {
                if (collectionMode == PRIMITIVE_ARRAY) {
                    for (boolean element : boolElements) {
                        set.add(boolPrimitive(element));
                    }
                    return set;
                }
                return elements.stream()
                        .filter(Objects::nonNull)
                        .map(element -> boolPrimitive(cast(element)))
                        .collect(toSet());
            }
            case BYTE: {
                if (collectionMode == PRIMITIVE_ARRAY) {
                    for (byte element : byteElements) {
                        set.add(bytePrimitive(element));
                    }
                    return set;
                }
                return elements.stream()
                        .filter(Objects::nonNull)
                        .map(element -> bytePrimitive(((Number) element).byteValue()))
                        .collect(toSet());
            }
            case ENTITY:
                return cast(getEntitySet());
            case COLLECTION:
                return cast(getCollectionsSet());
            case MAP:
                return cast(getMapValueSet());
            case STRING_PARAMETERS_MAP:
                return cast(getStringParametersSet());
            case VALUE:
                return cast(getSet());
        }
        return cast(getSet());
    }

    public Set<MapValue> getMapValueSet() {
        if (isEmpty()) return emptySet();
        if (elementsType != MAP && elementsType != VALUE) {
            throw new ValueMappingException(format(REQUEST_SET_ELEMENTS_TYPE_INVALID, MAP.toString(), elementsType.toString()));
        }
        return cast(getSet());
    }

    public Set<CollectionValue<?>> getCollectionsSet() {
        if (isEmpty()) return emptySet();
        if (elementsType != CollectionElementsType.COLLECTION && elementsType != VALUE) {
            throw new ValueMappingException(format(REQUEST_SET_ELEMENTS_TYPE_INVALID, COLLECTION.toString(), elementsType.toString()));
        }
        return cast(getSet());
    }

    public Set<StringParametersMap> getStringParametersSet() {
        if (isEmpty()) return emptySet();
        if (elementsType != STRING_PARAMETERS_MAP && elementsType != VALUE) {
            throw new ValueMappingException(format(REQUEST_SET_ELEMENTS_TYPE_INVALID, STRING_PARAMETERS_MAP.toString(), elementsType.toString()));
        }
        return cast(getSet());
    }

    public Set<Entity> getEntitySet() {
        if (isEmpty()) return emptySet();
        if (elementsType != ENTITY && elementsType != VALUE) {
            throw new ValueMappingException(format(REQUEST_SET_ELEMENTS_TYPE_INVALID, ENTITY.toString(), elementsType.toString()));
        }
        return cast(getSet());
    }

    public Set<Long> getLongSet() {
        if (isEmpty()) return emptySet();
        Set<Long> set = setOf();
        switch (elementsType) {
            case VALUE:
                return elements.stream()
                        .filter(element -> isPrimitive(cast(element)))
                        .map(element -> asPrimitive(cast(element)).getLong())
                        .collect(toSet());
            case STRING:
                return elements.stream()
                        .filter(Objects::nonNull)
                        .map(element -> parseLong(cast(element)))
                        .collect(toSet());
            case LONG: {
                if (collectionMode == PRIMITIVE_ARRAY) {
                    for (long element : longElements) {
                        set.add(element);
                    }
                    return set;
                }
                return elements.stream()
                        .filter(Objects::nonNull)
                        .map(element -> ((Number) element).longValue())
                        .collect(toSet());
            }
            case DOUBLE: {
                if (collectionMode == PRIMITIVE_ARRAY) {
                    for (double element : doubleElements) {
                        set.add((long) element);
                    }
                    return set;
                }
                return elements.stream()
                        .filter(Objects::nonNull)
                        .map(element -> ((Number) element).longValue())
                        .collect(toSet());
            }
            case FLOAT: {
                if (collectionMode == PRIMITIVE_ARRAY) {
                    for (float element : floatElements) {
                        set.add((long) element);
                    }
                    return set;
                }
                return elements.stream()
                        .filter(Objects::nonNull)
                        .map(element -> ((Number) element).longValue())
                        .collect(toSet());
            }
            case INT: {
                if (collectionMode == PRIMITIVE_ARRAY) {
                    for (int element : intElements) {
                        set.add((long) element);
                    }
                    return set;
                }
                return elements.stream()
                        .filter(Objects::nonNull)
                        .map(element -> ((Number) element).longValue())
                        .collect(toSet());
            }
            case BYTE: {
                if (collectionMode == PRIMITIVE_ARRAY) {
                    for (byte element : byteElements) {
                        set.add((long) element);
                    }
                    return set;
                }
                return elements.stream()
                        .filter(Objects::nonNull)
                        .map(element -> ((Number) element).longValue())
                        .collect(toSet());
            }
        }
        throw new ValueMappingException(format(REQUEST_SET_ELEMENTS_TYPE_INVALID, LONG.toString(), elementsType.toString()));
    }

    public Set<String> getStringSet() {
        if (isEmpty()) return emptySet();
        Set<String> set = setOf();
        if (collectionMode == PRIMITIVE_ARRAY) {
            switch (elementsType) {
                case LONG: {
                    for (long element : longElements) {
                        set.add(EMPTY_STRING + element);
                    }
                    return set;
                }
                case DOUBLE: {
                    for (double element : doubleElements) {
                        set.add(EMPTY_STRING + element);
                    }
                    return set;
                }
                case FLOAT: {
                    for (float element : floatElements) {
                        set.add(EMPTY_STRING + element);
                    }
                    return set;
                }
                case INT: {
                    for (int element : intElements) {
                        set.add(EMPTY_STRING + element);
                    }
                    return set;
                }
                case BOOL: {
                    for (boolean element : boolElements) {
                        set.add(EMPTY_STRING + element);
                    }
                    return set;
                }
                case BYTE: {
                    for (byte element : byteElements) {
                        set.add(EMPTY_STRING + element);
                    }
                    return set;
                }
            }
        }
        if (elementsType == STRING) {
            return cast(getSet());
        }
        return getSet().stream().filter(Objects::nonNull).map(Object::toString).collect(toSet());
    }

    public Set<Boolean> getBoolSet() {
        if (isEmpty()) return emptySet();
        if (collectionMode == PRIMITIVE_ARRAY) {
            if (elementsType != BOOL) {
                throw new ValueMappingException(format(REQUEST_SET_ELEMENTS_TYPE_INVALID, BOOL.toString(), elementsType.toString()));
            }
            Set<Boolean> set = setOf();
            for (boolean element : boolElements) {
                set.add(element);
            }
            return set;
        }
        switch (elementsType) {
            case VALUE:
                return getSet()
                        .stream()
                        .filter(element -> isPrimitive(cast(element)))
                        .map(element -> asPrimitive(cast(element)).getBool())
                        .collect(toSet());
            case STRING:
                return elements.stream()
                        .filter(Objects::nonNull)
                        .map(element -> parseBoolean((cast(element))))
                        .collect(toSet());
            case BOOL:
                return cast(getSet());
        }
        throw new ValueMappingException(format(REQUEST_SET_ELEMENTS_TYPE_INVALID, BOOL.toString(), elementsType.toString()));
    }

    public Set<Integer> getIntSet() {
        if (isEmpty()) return emptySet();
        Set<Integer> set = setOf();
        switch (elementsType) {
            case VALUE:
                return elements.stream()
                        .filter(element -> isPrimitive(cast(element)))
                        .map(element -> asPrimitive(cast(element)).getInt())
                        .collect(toSet());
            case STRING:
                return elements.stream()
                        .filter(Objects::nonNull)
                        .map(element -> parseInt(cast(element)))
                        .collect(toSet());
            case LONG: {
                if (collectionMode == PRIMITIVE_ARRAY) {
                    for (long element : longElements) {
                        set.add((int) element);
                    }
                    return set;
                }
                return elements.stream()
                        .filter(Objects::nonNull)
                        .map(element -> ((Number) element).intValue())
                        .collect(toSet());
            }
            case DOUBLE: {
                if (collectionMode == PRIMITIVE_ARRAY) {
                    for (double element : doubleElements) {
                        set.add((int) element);
                    }
                    return set;
                }
                return elements.stream()
                        .filter(Objects::nonNull)
                        .map(element -> ((Number) element).intValue())
                        .collect(toSet());
            }
            case FLOAT: {
                if (collectionMode == PRIMITIVE_ARRAY) {
                    for (float element : floatElements) {
                        set.add((int) element);
                    }
                    return set;
                }
                return elements.stream()
                        .filter(Objects::nonNull)
                        .map(element -> ((Number) element).intValue())
                        .collect(toSet());
            }
            case INT: {
                if (collectionMode == PRIMITIVE_ARRAY) {
                    for (int element : intElements) {
                        set.add(element);
                    }
                    return set;
                }
                return elements.stream()
                        .filter(Objects::nonNull)
                        .map(element -> ((Number) element).intValue())
                        .collect(toSet());
            }
            case BYTE: {
                if (collectionMode == PRIMITIVE_ARRAY) {
                    for (byte element : byteElements) {
                        set.add((int) element);
                    }
                    return set;
                }
                return elements.stream()
                        .filter(Objects::nonNull)
                        .map(element -> ((Number) element).intValue())
                        .collect(toSet());
            }
        }
        throw new ValueMappingException(format(REQUEST_SET_ELEMENTS_TYPE_INVALID, INT.toString(), elementsType.toString()));
    }

    public Set<Byte> getByteSet() {
        if (isEmpty()) return emptySet();
        Set<Byte> set = setOf();
        switch (elementsType) {
            case VALUE:
                return elements.stream()
                        .filter(element -> isPrimitive(cast(element)))
                        .map(element -> asPrimitive(cast(element)).getByte())
                        .collect(toSet());
            case STRING:
                return elements.stream()
                        .filter(Objects::nonNull)
                        .map(element -> parseByte(cast(element)))
                        .collect(toSet());
            case LONG: {
                if (collectionMode == PRIMITIVE_ARRAY) {
                    for (long element : longElements) {
                        set.add((byte) element);
                    }
                    return set;
                }
                return elements.stream()
                        .filter(Objects::nonNull)
                        .map(element -> ((Number) element).byteValue())
                        .collect(toSet());
            }
            case DOUBLE: {
                if (collectionMode == PRIMITIVE_ARRAY) {
                    for (double element : doubleElements) {
                        set.add((byte) element);
                    }
                    return set;
                }
                return elements.stream()
                        .filter(Objects::nonNull)
                        .map(element -> ((Number) element).byteValue())
                        .collect(toSet());
            }
            case FLOAT: {
                if (collectionMode == PRIMITIVE_ARRAY) {
                    for (float element : floatElements) {
                        set.add((byte) element);
                    }
                    return set;
                }
                return elements.stream()
                        .filter(Objects::nonNull)
                        .map(element -> ((Number) element).byteValue())
                        .collect(toSet());
            }
            case INT: {
                if (collectionMode == PRIMITIVE_ARRAY) {
                    for (int element : intElements) {
                        set.add((byte) element);
                    }
                    return set;
                }
                return elements.stream()
                        .filter(Objects::nonNull)
                        .map(element -> ((Number) element).byteValue())
                        .collect(toSet());
            }
            case BYTE: {
                if (collectionMode == PRIMITIVE_ARRAY) {
                    for (byte element : byteElements) {
                        set.add(element);
                    }
                    return set;
                }
                return elements.stream()
                        .filter(Objects::nonNull)
                        .map(element -> ((Number) element).byteValue())
                        .collect(toSet());
            }
        }
        throw new ValueMappingException(format(REQUEST_SET_ELEMENTS_TYPE_INVALID, BYTE.toString(), elementsType.toString()));
    }

    public Set<Double> getDoubleSet() {
        if (isEmpty()) return emptySet();
        Set<Double> set = setOf();
        switch (elementsType) {
            case VALUE:
                return elements.stream()
                        .filter(element -> isPrimitive(cast(element)))
                        .map(element -> asPrimitive(cast(element)).getDouble())
                        .collect(toSet());
            case STRING:
                return elements.stream()
                        .filter(Objects::nonNull)
                        .map(element -> parseDouble(cast(element)))
                        .collect(toSet());
            case LONG: {
                if (collectionMode == PRIMITIVE_ARRAY) {
                    for (long element : longElements) {
                        set.add((double) element);
                    }
                    return set;
                }
                return elements.stream()
                        .filter(Objects::nonNull)
                        .map(element -> ((Number) element).doubleValue())
                        .collect(toSet());
            }
            case DOUBLE: {
                if (collectionMode == PRIMITIVE_ARRAY) {
                    for (double element : doubleElements) {
                        set.add(element);
                    }
                    return set;
                }
                return elements.stream()
                        .filter(Objects::nonNull)
                        .map(element -> ((Number) element).doubleValue())
                        .collect(toSet());
            }
            case FLOAT: {
                if (collectionMode == PRIMITIVE_ARRAY) {
                    for (float element : floatElements) {
                        set.add((double) element);
                    }
                    return set;
                }
                return elements.stream()
                        .filter(Objects::nonNull)
                        .map(element -> ((Number) element).doubleValue())
                        .collect(toSet());
            }
            case INT: {
                if (collectionMode == PRIMITIVE_ARRAY) {
                    for (int element : intElements) {
                        set.add((double) element);
                    }
                    return set;
                }
                return elements.stream()
                        .filter(Objects::nonNull)
                        .map(element -> ((Number) element).doubleValue())
                        .collect(toSet());
            }
            case BYTE: {
                if (collectionMode == PRIMITIVE_ARRAY) {
                    for (byte element : byteElements) {
                        set.add((double) element);
                    }
                    return set;
                }
                return elements.stream()
                        .filter(Objects::nonNull)
                        .map(element -> ((Number) element).doubleValue())
                        .collect(toSet());
            }
        }
        throw new ValueMappingException(format(REQUEST_SET_ELEMENTS_TYPE_INVALID, DOUBLE.toString(), elementsType.toString()));
    }

    public Set<Float> getFloatSet() {
        if (isEmpty()) return emptySet();
        Set<Float> set = setOf();
        switch (elementsType) {
            case VALUE:
                return elements.stream()
                        .filter(element -> isPrimitive(cast(element)))
                        .map(element -> asPrimitive(cast(element)).getFloat())
                        .collect(toSet());
            case STRING:
                return elements.stream()
                        .filter(Objects::nonNull)
                        .map(element -> parseFloat(cast(element)))
                        .collect(toSet());
            case LONG: {
                if (collectionMode == PRIMITIVE_ARRAY) {
                    for (long element : longElements) {
                        set.add((float) element);
                    }
                    return set;
                }
                return elements.stream()
                        .filter(Objects::nonNull)
                        .map(element -> ((Number) element).floatValue())
                        .collect(toSet());
            }
            case DOUBLE: {
                if (collectionMode == PRIMITIVE_ARRAY) {
                    for (double element : doubleElements) {
                        set.add((float) element);
                    }
                    return set;
                }
                return elements.stream()
                        .filter(Objects::nonNull)
                        .map(element -> ((Number) element).floatValue())
                        .collect(toSet());
            }
            case FLOAT: {
                if (collectionMode == PRIMITIVE_ARRAY) {
                    for (float element : floatElements) {
                        set.add(element);
                    }
                    return set;
                }
                return elements.stream()
                        .filter(Objects::nonNull)
                        .map(element -> ((Number) element).floatValue())
                        .collect(toSet());
            }
            case INT: {
                if (collectionMode == PRIMITIVE_ARRAY) {
                    for (int element : intElements) {
                        set.add((float) element);
                    }
                    return set;
                }
                return elements.stream()
                        .filter(Objects::nonNull)
                        .map(element -> ((Number) element).floatValue())
                        .collect(toSet());
            }
            case BYTE: {
                if (collectionMode == PRIMITIVE_ARRAY) {
                    for (byte element : byteElements) {
                        set.add((float) element);
                    }
                    return set;
                }
                return elements.stream()
                        .filter(Objects::nonNull)
                        .map(element -> ((Number) element).floatValue())
                        .collect(toSet());
            }
        }
        throw new ValueMappingException(format(REQUEST_SET_ELEMENTS_TYPE_INVALID, FLOAT.toString(), elementsType.toString()));
    }


    public Queue<Value> getValueQueue() {
        if (isEmpty()) return queueOf();
        Queue<Value> queue = queueOf();
        switch (elementsType) {
            case STRING:
                return elements
                        .stream()
                        .filter(Objects::nonNull)
                        .map(element -> stringPrimitive(cast(element)))
                        .collect(toCollection(CollectionsFactory::queueOf));
            case LONG: {
                if (collectionMode == PRIMITIVE_ARRAY) {
                    for (long element : longElements) {
                        queue.add(longPrimitive(element));
                    }
                    return queue;
                }
                return elements.stream()
                        .filter(Objects::nonNull)
                        .map(element -> longPrimitive(((Number) element).longValue()))
                        .collect(toCollection(CollectionsFactory::queueOf));
            }
            case DOUBLE: {
                if (collectionMode == PRIMITIVE_ARRAY) {
                    for (double element : doubleElements) {
                        queue.add(doublePrimitive(element));
                    }
                    return queue;
                }
                return elements.stream()
                        .filter(Objects::nonNull)
                        .map(element -> doublePrimitive(((Number) element).doubleValue()))
                        .collect(toCollection(CollectionsFactory::queueOf));
            }
            case FLOAT: {
                if (collectionMode == PRIMITIVE_ARRAY) {
                    for (float element : floatElements) {
                        queue.add(floatPrimitive(element));
                    }
                    return queue;
                }
                return elements.stream()
                        .filter(Objects::nonNull)
                        .map(element -> floatPrimitive(((Number) element).floatValue()))
                        .collect(toCollection(CollectionsFactory::queueOf));
            }
            case INT: {
                if (collectionMode == PRIMITIVE_ARRAY) {
                    for (int element : intElements) {
                        queue.add(intPrimitive(element));
                    }
                    return queue;
                }
                return elements.stream()
                        .filter(Objects::nonNull)
                        .map(element -> intPrimitive(((Number) element).intValue()))
                        .collect(toCollection(CollectionsFactory::queueOf));
            }
            case BOOL: {
                if (collectionMode == PRIMITIVE_ARRAY) {
                    for (boolean element : boolElements) {
                        queue.add(boolPrimitive(element));
                    }
                    return queue;
                }
                return elements.stream()
                        .filter(Objects::nonNull)
                        .map(element -> boolPrimitive(cast(element)))
                        .collect(toCollection(CollectionsFactory::queueOf));
            }
            case BYTE: {
                if (collectionMode == PRIMITIVE_ARRAY) {
                    for (byte element : byteElements) {
                        queue.add(bytePrimitive(element));
                    }
                    return queue;
                }
                return elements.stream()
                        .filter(Objects::nonNull)
                        .map(element -> bytePrimitive(((Number) element).byteValue()))
                        .collect(toCollection(CollectionsFactory::queueOf));
            }
            case ENTITY:
                return cast(getEntityQueue());
            case COLLECTION:
                return cast(getCollectionsQueue());
            case MAP:
                return cast(getMapValueQueue());
            case STRING_PARAMETERS_MAP:
                return cast(getStringParametersQueue());
            case VALUE:
                return cast(getQueue());
        }
        return cast(getQueue());
    }

    public Queue<MapValue> getMapValueQueue() {
        if (isEmpty()) return queueOf();
        if (elementsType != MAP && elementsType != VALUE) {
            throw new ValueMappingException(format(REQUEST_QUEUE_ELEMENTS_TYPE_INVALID, MAP.toString(), elementsType.toString()));
        }
        return cast(getQueue());
    }

    public Queue<CollectionValue<?>> getCollectionsQueue() {
        if (isEmpty()) return queueOf();
        if (elementsType != CollectionElementsType.COLLECTION && elementsType != VALUE) {
            throw new ValueMappingException(format(REQUEST_QUEUE_ELEMENTS_TYPE_INVALID, COLLECTION.toString(), elementsType.toString()));
        }
        return cast(getQueue());
    }

    public Queue<StringParametersMap> getStringParametersQueue() {
        if (isEmpty()) return queueOf();
        if (elementsType != STRING_PARAMETERS_MAP && elementsType != VALUE) {
            throw new ValueMappingException(format(REQUEST_QUEUE_ELEMENTS_TYPE_INVALID, STRING_PARAMETERS_MAP.toString(), elementsType.toString()));
        }
        return cast(getQueue());
    }

    public Queue<Entity> getEntityQueue() {
        if (isEmpty()) return queueOf();
        if (elementsType != ENTITY && elementsType != VALUE) {
            throw new ValueMappingException(format(REQUEST_QUEUE_ELEMENTS_TYPE_INVALID, ENTITY.toString(), elementsType.toString()));
        }
        return cast(getQueue());
    }

    public Queue<Long> getLongQueue() {
        if (isEmpty()) return queueOf();
        Queue<Long> queue = queueOf();
        switch (elementsType) {
            case VALUE:
                return elements.stream()
                        .filter(element -> isPrimitive(cast(element)))
                        .map(element -> asPrimitive(cast(element)).getLong())
                        .collect(toCollection(CollectionsFactory::queueOf));
            case STRING:
                return elements.stream()
                        .filter(Objects::nonNull)
                        .map(element -> parseLong(cast(element)))
                        .collect(toCollection(CollectionsFactory::queueOf));
            case LONG: {
                if (collectionMode == PRIMITIVE_ARRAY) {
                    for (long element : longElements) {
                        queue.add(element);
                    }
                    return queue;
                }
                return elements.stream()
                        .filter(Objects::nonNull)
                        .map(element -> ((Number) element).longValue())
                        .collect(toCollection(CollectionsFactory::queueOf));
            }
            case DOUBLE: {
                if (collectionMode == PRIMITIVE_ARRAY) {
                    for (double element : doubleElements) {
                        queue.add((long) element);
                    }
                    return queue;
                }
                return elements.stream()
                        .filter(Objects::nonNull)
                        .map(element -> ((Number) element).longValue())
                        .collect(toCollection(CollectionsFactory::queueOf));
            }
            case FLOAT: {
                if (collectionMode == PRIMITIVE_ARRAY) {
                    for (float element : floatElements) {
                        queue.add((long) element);
                    }
                    return queue;
                }
                return elements.stream()
                        .filter(Objects::nonNull)
                        .map(element -> ((Number) element).longValue())
                        .collect(toCollection(CollectionsFactory::queueOf));
            }
            case INT: {
                if (collectionMode == PRIMITIVE_ARRAY) {
                    for (int element : intElements) {
                        queue.add((long) element);
                    }
                    return queue;
                }
                return elements.stream()
                        .filter(Objects::nonNull)
                        .map(element -> ((Number) element).longValue())
                        .collect(toCollection(CollectionsFactory::queueOf));
            }
            case BYTE: {
                if (collectionMode == PRIMITIVE_ARRAY) {
                    for (byte element : byteElements) {
                        queue.add((long) element);
                    }
                    return queue;
                }
                return elements.stream()
                        .filter(Objects::nonNull)
                        .map(element -> ((Number) element).longValue())
                        .collect(toCollection(CollectionsFactory::queueOf));
            }
        }
        throw new ValueMappingException(format(REQUEST_QUEUE_ELEMENTS_TYPE_INVALID, LONG.toString(), elementsType.toString()));
    }

    public Queue<String> getStringQueue() {
        if (isEmpty()) return queueOf();
        Queue<String> queue = queueOf();
        if (collectionMode == PRIMITIVE_ARRAY) {
            switch (elementsType) {
                case LONG: {
                    for (long element : longElements) {
                        queue.add(EMPTY_STRING + element);
                    }
                    return queue;
                }
                case DOUBLE: {
                    for (double element : doubleElements) {
                        queue.add(EMPTY_STRING + element);
                    }
                    return queue;
                }
                case FLOAT: {
                    for (float element : floatElements) {
                        queue.add(EMPTY_STRING + element);
                    }
                    return queue;
                }
                case INT: {
                    for (int element : intElements) {
                        queue.add(EMPTY_STRING + element);
                    }
                    return queue;
                }
                case BOOL: {
                    for (boolean element : boolElements) {
                        queue.add(EMPTY_STRING + element);
                    }
                    return queue;
                }
                case BYTE: {
                    for (byte element : byteElements) {
                        queue.add(EMPTY_STRING + element);
                    }
                    return queue;
                }
            }
        }
        if (elementsType == STRING) {
            return cast(getQueue());
        }
        return getQueue().stream().filter(Objects::nonNull).map(Object::toString).collect(toCollection(CollectionsFactory::queueOf));
    }

    public Queue<Boolean> getBoolQueue() {
        if (isEmpty()) return queueOf();
        if (collectionMode == PRIMITIVE_ARRAY) {
            if (elementsType != BOOL) {
                throw new ValueMappingException(format(REQUEST_QUEUE_ELEMENTS_TYPE_INVALID, BOOL.toString(), elementsType.toString()));
            }
            Queue<Boolean> queue = queueOf();
            for (boolean element : boolElements) {
                queue.add(element);
            }
            return queue;
        }
        switch (elementsType) {
            case VALUE:
                return getQueue()
                        .stream()
                        .filter(element -> isPrimitive(cast(element)))
                        .map(element -> asPrimitive(cast(element)).getBool())
                        .collect(toCollection(CollectionsFactory::queueOf));
            case STRING:
                return elements.stream()
                        .filter(Objects::nonNull)
                        .map(element -> parseBoolean((cast(element))))
                        .collect(toCollection(CollectionsFactory::queueOf));
            case BOOL:
                return cast(getQueue());
        }
        throw new ValueMappingException(format(REQUEST_QUEUE_ELEMENTS_TYPE_INVALID, BOOL.toString(), elementsType.toString()));
    }

    public Queue<Integer> getIntQueue() {
        if (isEmpty()) return queueOf();
        Queue<Integer> queue = queueOf();
        switch (elementsType) {
            case VALUE:
                return elements.stream()
                        .filter(element -> isPrimitive(cast(element)))
                        .map(element -> asPrimitive(cast(element)).getInt())
                        .collect(toCollection(CollectionsFactory::queueOf));
            case STRING:
                return elements.stream()
                        .filter(Objects::nonNull)
                        .map(element -> parseInt(cast(element)))
                        .collect(toCollection(CollectionsFactory::queueOf));
            case LONG: {
                if (collectionMode == PRIMITIVE_ARRAY) {
                    for (long element : longElements) {
                        queue.add((int) element);
                    }
                    return queue;
                }
                return elements.stream()
                        .filter(Objects::nonNull)
                        .map(element -> ((Number) element).intValue())
                        .collect(toCollection(CollectionsFactory::queueOf));
            }
            case DOUBLE: {
                if (collectionMode == PRIMITIVE_ARRAY) {
                    for (double element : doubleElements) {
                        queue.add((int) element);
                    }
                    return queue;
                }
                return elements.stream()
                        .filter(Objects::nonNull)
                        .map(element -> ((Number) element).intValue())
                        .collect(toCollection(CollectionsFactory::queueOf));
            }
            case FLOAT: {
                if (collectionMode == PRIMITIVE_ARRAY) {
                    for (float element : floatElements) {
                        queue.add((int) element);
                    }
                    return queue;
                }
                return elements.stream()
                        .filter(Objects::nonNull)
                        .map(element -> ((Number) element).intValue())
                        .collect(toCollection(CollectionsFactory::queueOf));
            }
            case INT: {
                if (collectionMode == PRIMITIVE_ARRAY) {
                    for (int element : intElements) {
                        queue.add(element);
                    }
                    return queue;
                }
                return elements.stream()
                        .filter(Objects::nonNull)
                        .map(element -> ((Number) element).intValue())
                        .collect(toCollection(CollectionsFactory::queueOf));
            }
            case BYTE: {
                if (collectionMode == PRIMITIVE_ARRAY) {
                    for (byte element : byteElements) {
                        queue.add((int) element);
                    }
                    return queue;
                }
                return elements.stream()
                        .filter(Objects::nonNull)
                        .map(element -> ((Number) element).intValue())
                        .collect(toCollection(CollectionsFactory::queueOf));
            }
        }
        throw new ValueMappingException(format(REQUEST_QUEUE_ELEMENTS_TYPE_INVALID, INT.toString(), elementsType.toString()));
    }

    public Queue<Byte> getByteQueue() {
        if (isEmpty()) return queueOf();
        Queue<Byte> queue = queueOf();
        switch (elementsType) {
            case VALUE:
                return elements.stream()
                        .filter(element -> isPrimitive(cast(element)))
                        .map(element -> asPrimitive(cast(element)).getByte())
                        .collect(toCollection(CollectionsFactory::queueOf));
            case STRING:
                return elements.stream()
                        .filter(Objects::nonNull)
                        .map(element -> parseByte(cast(element)))
                        .collect(toCollection(CollectionsFactory::queueOf));
            case LONG: {
                if (collectionMode == PRIMITIVE_ARRAY) {
                    for (long element : longElements) {
                        queue.add((byte) element);
                    }
                    return queue;
                }
                return elements.stream()
                        .filter(Objects::nonNull)
                        .map(element -> ((Number) element).byteValue())
                        .collect(toCollection(CollectionsFactory::queueOf));
            }
            case DOUBLE: {
                if (collectionMode == PRIMITIVE_ARRAY) {
                    for (double element : doubleElements) {
                        queue.add((byte) element);
                    }
                    return queue;
                }
                return elements.stream()
                        .filter(Objects::nonNull)
                        .map(element -> ((Number) element).byteValue())
                        .collect(toCollection(CollectionsFactory::queueOf));
            }
            case FLOAT: {
                if (collectionMode == PRIMITIVE_ARRAY) {
                    for (float element : floatElements) {
                        queue.add((byte) element);
                    }
                    return queue;
                }
                return elements.stream()
                        .filter(Objects::nonNull)
                        .map(element -> ((Number) element).byteValue())
                        .collect(toCollection(CollectionsFactory::queueOf));
            }
            case INT: {
                if (collectionMode == PRIMITIVE_ARRAY) {
                    for (int element : intElements) {
                        queue.add((byte) element);
                    }
                    return queue;
                }
                return elements.stream()
                        .filter(Objects::nonNull)
                        .map(element -> ((Number) element).byteValue())
                        .collect(toCollection(CollectionsFactory::queueOf));
            }
            case BYTE: {
                if (collectionMode == PRIMITIVE_ARRAY) {
                    for (byte element : byteElements) {
                        queue.add(element);
                    }
                    return queue;
                }
                return elements.stream()
                        .filter(Objects::nonNull)
                        .map(element -> ((Number) element).byteValue())
                        .collect(toCollection(CollectionsFactory::queueOf));
            }
        }
        throw new ValueMappingException(format(REQUEST_QUEUE_ELEMENTS_TYPE_INVALID, BYTE.toString(), elementsType.toString()));
    }

    public Queue<Double> getDoubleQueue() {
        if (isEmpty()) return queueOf();
        Queue<Double> queue = queueOf();
        switch (elementsType) {
            case VALUE:
                return elements.stream()
                        .filter(element -> isPrimitive(cast(element)))
                        .map(element -> asPrimitive(cast(element)).getDouble())
                        .collect(toCollection(CollectionsFactory::queueOf));
            case STRING:
                return elements.stream()
                        .filter(Objects::nonNull)
                        .map(element -> parseDouble(cast(element)))
                        .collect(toCollection(CollectionsFactory::queueOf));
            case LONG: {
                if (collectionMode == PRIMITIVE_ARRAY) {
                    for (long element : longElements) {
                        queue.add((double) element);
                    }
                    return queue;
                }
                return elements.stream()
                        .filter(Objects::nonNull)
                        .map(element -> ((Number) element).doubleValue())
                        .collect(toCollection(CollectionsFactory::queueOf));
            }
            case DOUBLE: {
                if (collectionMode == PRIMITIVE_ARRAY) {
                    for (double element : doubleElements) {
                        queue.add(element);
                    }
                    return queue;
                }
                return elements.stream()
                        .filter(Objects::nonNull)
                        .map(element -> ((Number) element).doubleValue())
                        .collect(toCollection(CollectionsFactory::queueOf));
            }
            case FLOAT: {
                if (collectionMode == PRIMITIVE_ARRAY) {
                    for (float element : floatElements) {
                        queue.add((double) element);
                    }
                    return queue;
                }
                return elements.stream()
                        .filter(Objects::nonNull)
                        .map(element -> ((Number) element).doubleValue())
                        .collect(toCollection(CollectionsFactory::queueOf));
            }
            case INT: {
                if (collectionMode == PRIMITIVE_ARRAY) {
                    for (int element : intElements) {
                        queue.add((double) element);
                    }
                    return queue;
                }
                return elements.stream()
                        .filter(Objects::nonNull)
                        .map(element -> ((Number) element).doubleValue())
                        .collect(toCollection(CollectionsFactory::queueOf));
            }
            case BYTE: {
                if (collectionMode == PRIMITIVE_ARRAY) {
                    for (byte element : byteElements) {
                        queue.add((double) element);
                    }
                    return queue;
                }
                return elements.stream()
                        .filter(Objects::nonNull)
                        .map(element -> ((Number) element).doubleValue())
                        .collect(toCollection(CollectionsFactory::queueOf));
            }
        }
        throw new ValueMappingException(format(REQUEST_QUEUE_ELEMENTS_TYPE_INVALID, DOUBLE.toString(), elementsType.toString()));
    }

    public Queue<Float> getFloatQueue() {
        if (isEmpty()) return queueOf();
        Queue<Float> queue = queueOf();
        switch (elementsType) {
            case VALUE:
                return elements.stream()
                        .filter(element -> isPrimitive(cast(element)))
                        .map(element -> asPrimitive(cast(element)).getFloat())
                        .collect(toCollection(CollectionsFactory::queueOf));
            case STRING:
                return elements.stream()
                        .filter(Objects::nonNull)
                        .map(element -> parseFloat(cast(element)))
                        .collect(toCollection(CollectionsFactory::queueOf));
            case LONG: {
                if (collectionMode == PRIMITIVE_ARRAY) {
                    for (long element : longElements) {
                        queue.add((float) element);
                    }
                    return queue;
                }
                return elements.stream()
                        .filter(Objects::nonNull)
                        .map(element -> ((Number) element).floatValue())
                        .collect(toCollection(CollectionsFactory::queueOf));
            }
            case DOUBLE: {
                if (collectionMode == PRIMITIVE_ARRAY) {
                    for (double element : doubleElements) {
                        queue.add((float) element);
                    }
                    return queue;
                }
                return elements.stream()
                        .filter(Objects::nonNull)
                        .map(element -> ((Number) element).floatValue())
                        .collect(toCollection(CollectionsFactory::queueOf));
            }
            case FLOAT: {
                if (collectionMode == PRIMITIVE_ARRAY) {
                    for (float element : floatElements) {
                        queue.add(element);
                    }
                    return queue;
                }
                return elements.stream()
                        .filter(Objects::nonNull)
                        .map(element -> ((Number) element).floatValue())
                        .collect(toCollection(CollectionsFactory::queueOf));
            }
            case INT: {
                if (collectionMode == PRIMITIVE_ARRAY) {
                    for (int element : intElements) {
                        queue.add((float) element);
                    }
                    return queue;
                }
                return elements.stream()
                        .filter(Objects::nonNull)
                        .map(element -> ((Number) element).floatValue())
                        .collect(toCollection(CollectionsFactory::queueOf));
            }
            case BYTE: {
                if (collectionMode == PRIMITIVE_ARRAY) {
                    for (byte element : byteElements) {
                        queue.add((float) element);
                    }
                    return queue;
                }
                return elements.stream()
                        .filter(Objects::nonNull)
                        .map(element -> ((Number) element).floatValue())
                        .collect(toCollection(CollectionsFactory::queueOf));
            }
        }
        throw new ValueMappingException(format(REQUEST_QUEUE_ELEMENTS_TYPE_INVALID, FLOAT.toString(), elementsType.toString()));
    }


    public long[] getLongArray() {
        if (isEmpty()) return EMPTY_LONGS;
        long[] values = new long[elements.size()];
        int i = 0;
        if (collectionMode == PRIMITIVE_ARRAY) {
            switch (elementsType) {
                case LONG:
                    return longElements;
                case DOUBLE: {
                    for (double element : doubleElements) {
                        values[i] = (long) element;
                        i++;
                    }
                    break;
                }
                case FLOAT: {
                    for (float element : floatElements) {
                        values[i] = (long) element;
                        i++;
                    }
                    break;
                }
                case INT: {
                    for (int element : intElements) {
                        values[i] = element;
                        i++;
                    }
                    break;
                }
                case BYTE: {
                    for (byte element : byteElements) {
                        values[i] = element;
                        i++;
                    }
                    break;
                }
            }
            if (i == 0) return EMPTY_LONGS;
            return copyOf(values, i);
        }
        for (Object element : elements) {
            switch (elementsType) {
                case STRING:
                    if (nonNull(element)) {
                        values[i] = parseLong(cast(element));
                        i++;
                    }
                    break;
                case LONG:
                case DOUBLE:
                case FLOAT:
                case INT:
                case BYTE:
                    if (nonNull(element)) {
                        values[i] = ((Number) element).longValue();
                        i++;
                    }
                    break;
                case VALUE:
                    if (isPrimitive(cast(element))) {
                        values[i] = asPrimitive(cast(element)).getLong();
                        i++;
                    }
                    break;
            }
        }
        if (i == 0) return EMPTY_LONGS;
        return copyOf(values, i);
    }

    public boolean[] getBoolArray() {
        if (isEmpty()) return EMPTY_BOOLEANS;
        boolean[] values = new boolean[elements.size()];
        int i = 0;
        if (collectionMode == PRIMITIVE_ARRAY && elementsType == BOOL) {
            return boolElements;
        }
        for (Object element : elements) {
            switch (elementsType) {
                case STRING:
                    if (nonNull(element)) {
                        values[i] = parseBoolean(cast(element));
                        i++;
                    }
                    break;
                case VALUE:
                    if (isPrimitive(cast(element))) {
                        values[i] = asPrimitive(cast(element)).getBool();
                        i++;
                    }
                    break;
            }
        }
        if (i == 0) return EMPTY_BOOLEANS;
        return copyOf(values, i);
    }

    public int[] getIntArray() {
        if (isEmpty()) return EMPTY_INTS;
        int[] values = new int[elements.size()];
        int i = 0;
        if (collectionMode == PRIMITIVE_ARRAY) {
            switch (elementsType) {
                case LONG: {
                    for (long element : longElements) {
                        values[i] = (int) element;
                        i++;
                    }
                    break;
                }
                case DOUBLE: {
                    for (double element : doubleElements) {
                        values[i] = (int) element;
                        i++;
                    }
                    break;
                }
                case FLOAT: {
                    for (float element : floatElements) {
                        values[i] = (int) element;
                        i++;
                    }
                    break;
                }
                case INT:
                    return intElements;
                case BYTE: {
                    for (byte element : byteElements) {
                        values[i] = element;
                        i++;
                    }
                    break;
                }
            }
            if (i == 0) return EMPTY_INTS;
            return copyOf(values, i);
        }
        for (Object element : elements) {
            switch (elementsType) {
                case STRING:
                    if (nonNull(element)) {
                        values[i] = parseInt(cast(element));
                        i++;
                    }
                    break;
                case LONG:
                case DOUBLE:
                case FLOAT:
                case INT:
                case BYTE:
                    if (nonNull(element)) {
                        values[i] = ((Number) element).intValue();
                        i++;
                    }
                    break;
                case VALUE:
                    if (isPrimitive(cast(element))) {
                        values[i] = asPrimitive(cast(element)).getInt();
                        i++;
                    }
                    break;
            }
        }
        if (i == 0) return EMPTY_INTS;
        return copyOf(values, i);
    }

    public byte[] getByteArray() {
        if (isEmpty()) return EMPTY_BYTES;
        byte[] values = new byte[elements.size()];
        int i = 0;
        if (collectionMode == PRIMITIVE_ARRAY) {
            switch (elementsType) {
                case LONG: {
                    for (long element : longElements) {
                        values[i] = (byte) element;
                        i++;
                    }
                    break;
                }
                case DOUBLE: {
                    for (double element : doubleElements) {
                        values[i] = (byte) element;
                        i++;
                    }
                    break;
                }
                case FLOAT: {
                    for (float element : floatElements) {
                        values[i] = (byte) element;
                        i++;
                    }
                    break;
                }
                case INT: {
                    for (int element : intElements) {
                        values[i] = (byte) element;
                        i++;
                    }
                    break;
                }
                case BYTE:
                    return byteElements;
            }
            if (i == 0) return EMPTY_BYTES;
            return copyOf(values, i);
        }
        for (Object element : elements) {
            switch (elementsType) {
                case STRING:
                    if (nonNull(element)) {
                        values[i] = parseByte(cast(element));
                        i++;
                    }
                    break;
                case LONG:
                case DOUBLE:
                case FLOAT:
                case INT:
                case BYTE:
                    if (nonNull(element)) {
                        values[i] = ((Number) element).byteValue();
                        i++;
                    }
                    break;
                case VALUE:
                    if (isPrimitive(cast(element))) {
                        values[i] = asPrimitive(cast(element)).getByte();
                        i++;
                    }
                    break;
            }
        }
        if (i == 0) return EMPTY_BYTES;
        return copyOf(values, i);
    }

    public double[] getDoubleArray() {
        if (isEmpty()) return EMPTY_DOUBLES;
        double[] values = new double[elements.size()];
        int i = 0;
        if (collectionMode == PRIMITIVE_ARRAY) {
            switch (elementsType) {
                case LONG: {
                    for (long element : longElements) {
                        values[i] = (double) element;
                        i++;
                    }
                    break;
                }
                case DOUBLE:
                    return doubleElements;
                case FLOAT: {
                    for (float element : floatElements) {
                        values[i] = element;
                        i++;
                    }
                    break;
                }
                case INT: {
                    for (int element : intElements) {
                        values[i] = element;
                        i++;
                    }
                    break;
                }
                case BYTE: {
                    for (byte element : byteElements) {
                        values[i] = element;
                        i++;
                    }
                    break;
                }
            }
            if (i == 0) return EMPTY_DOUBLES;
            return copyOf(values, i);
        }
        for (Object element : elements) {
            switch (elementsType) {
                case STRING:
                    if (nonNull(element)) {
                        values[i] = parseDouble(cast(element));
                        i++;
                    }
                    break;
                case LONG:
                case DOUBLE:
                case FLOAT:
                case INT:
                case BYTE:
                    if (nonNull(element)) {
                        values[i] = ((Number) element).doubleValue();
                        i++;
                    }
                    break;
                case VALUE:
                    if (isPrimitive(cast(element))) {
                        values[i] = asPrimitive(cast(element)).getDouble();
                        i++;
                    }
                    break;
            }
        }
        if (i == 0) return EMPTY_DOUBLES;
        return copyOf(values, i);
    }

    public float[] getFloatArray() {
        if (isEmpty()) return EMPTY_FLOATS;
        float[] values = new float[elements.size()];
        int i = 0;
        if (collectionMode == PRIMITIVE_ARRAY) {
            switch (elementsType) {
                case LONG: {
                    for (long element : longElements) {
                        values[i] = (float) element;
                        i++;
                    }
                    break;
                }
                case DOUBLE: {
                    for (double element : doubleElements) {
                        values[i] = (float) element;
                        i++;
                    }
                    break;
                }
                case FLOAT:
                    return floatElements;
                case INT: {
                    for (int element : intElements) {
                        values[i] = element;
                        i++;
                    }
                    break;
                }
                case BYTE: {
                    for (byte element : byteElements) {
                        values[i] = element;
                        i++;
                    }
                    break;
                }
            }
            if (i == 0) return EMPTY_FLOATS;
            return copyOf(values, i);
        }
        for (Object element : elements) {
            switch (elementsType) {
                case STRING:
                    if (nonNull(element)) {
                        values[i] = parseFloat(cast(element));
                        i++;
                    }
                    break;
                case LONG:
                case DOUBLE:
                case FLOAT:
                case INT:
                case BYTE:
                    if (nonNull(element)) {
                        values[i] = ((Number) element).floatValue();
                        i++;
                    }
                    break;
                case VALUE:
                    if (isPrimitive(cast(element))) {
                        values[i] = asPrimitive(cast(element)).getFloat();
                        i++;
                    }
                    break;
            }
        }
        if (i == 0) return EMPTY_FLOATS;
        return copyOf(values, i);
    }

    public Map<String, String> dump() {
        if (isEmpty()) {
            return emptyMap();
        }
        Map<String, String> dump = mapOf();
        List<Value> valueList = getValueList();
        for (int i = 0; i < valueList.size(); i++) {
            Value value = valueList.get(i);
            if (isPrimitive(value)) {
                dump.put(OPENING_SQUARE_BRACES + i + CLOSING_SQUARE_BRACES, value.toString());
                continue;
            }
            if (isEntity(value)) {
                int index = i;
                asEntity(value).dump().forEach((innerKey, innerField) -> dump.put(OPENING_SQUARE_BRACES + index + CLOSING_SQUARE_BRACES + DOT + innerKey, innerField));
                continue;
            }
            if (isCollection(value)) {
                int index = i;
                asCollection(value).dump().forEach((innerKey, innerField) -> dump.put(OPENING_SQUARE_BRACES + index + CLOSING_SQUARE_BRACES + innerKey, innerField));
            }
        }
        return dump;
    }

    @Override
    public String toString() {
        switch (collectionMode) {
            case PRIMITIVE_ARRAY:
                switch (elementsType) {
                    case LONG:
                        return emptyIfNull(Arrays.toString(longElements));
                    case DOUBLE:
                        return emptyIfNull(Arrays.toString(doubleElements));
                    case INT:
                        return emptyIfNull(Arrays.toString(intElements));
                    case BOOL:
                        return emptyIfNull(Arrays.toString(boolElements));
                    case BYTE:
                        return emptyIfNull(Arrays.toString(byteElements));
                }
            case COLLECTION:
                return emptyIfNull(Arrays.toString(elements.toArray()));
        }
        return EMPTY_STRING;
    }

    @Override
    public boolean isEmpty() {
        if (isNull(elementsType)) return true;
        switch (collectionMode) {
            case PRIMITIVE_ARRAY:
                switch (elementsType) {
                    case LONG:
                        return CheckerForEmptiness.isEmpty(longElements);
                    case DOUBLE:
                        return CheckerForEmptiness.isEmpty(doubleElements);
                    case INT:
                        return CheckerForEmptiness.isEmpty(intElements);
                    case BOOL:
                        return CheckerForEmptiness.isEmpty(boolElements);
                    case BYTE:
                        return CheckerForEmptiness.isEmpty(byteElements);
                    case FLOAT:
                        return CheckerForEmptiness.isEmpty(floatElements);
                }
            case COLLECTION:
                return CheckerForEmptiness.isEmpty(elements);
        }
        return true;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;
        CollectionValue<?> otherValue = (CollectionValue<?>) other;
        if (otherValue.elementsType == VALUE
                || otherValue.elementsType == CollectionElementsType.COLLECTION
                || otherValue.elementsType == MAP
                || otherValue.elementsType == STRING_PARAMETERS_MAP
                || otherValue.elementsType == ENTITY) {
            return Objects.equals(otherValue.elements, elements);
        }
        if (elementsType == VALUE
                || elementsType == CollectionElementsType.COLLECTION
                || elementsType == MAP
                || elementsType == STRING_PARAMETERS_MAP
                || elementsType == ENTITY) {
            return Objects.equals(elements, otherValue.elements);
        }
        if (elementsType != otherValue.elementsType) {
            return false;
        }
        switch (collectionMode) {
            case PRIMITIVE_ARRAY:
                switch (elementsType) {
                    case LONG:
                        return Arrays.equals(longElements, otherValue.longElements);
                    case DOUBLE:
                        return Arrays.equals(doubleElements, otherValue.doubleElements);
                    case FLOAT:
                        return Arrays.equals(floatElements, otherValue.floatElements);
                    case INT:
                        return Arrays.equals(intElements, otherValue.intElements);
                    case BOOL:
                        return Arrays.equals(boolElements, otherValue.boolElements);
                    case BYTE:
                        return Arrays.equals(byteElements, otherValue.byteElements);
                }
            case COLLECTION:
                return Objects.equals(otherValue.elements, elements);
        }
        return false;
    }

    @Override
    public int hashCode() {
        List<T> elements = getList();
        return elements != null ? elements.hashCode() : 0;
    }
}