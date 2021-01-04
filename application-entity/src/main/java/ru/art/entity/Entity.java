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
import ru.art.entity.constants.*;
import ru.art.entity.exception.*;
import ru.art.entity.mapper.*;
import static java.util.Collections.*;
import static java.util.Objects.*;
import static java.util.stream.Collectors.*;
import static ru.art.core.caster.Caster.*;
import static ru.art.core.checker.CheckerForEmptiness.*;
import static ru.art.core.constants.DateConstants.*;
import static ru.art.core.constants.StringConstants.*;
import static ru.art.core.factory.CollectionsFactory.*;
import static ru.art.entity.CollectionValuesFactory.*;
import static ru.art.entity.PrimitivesFactory.*;
import static ru.art.entity.Value.*;
import static ru.art.entity.constants.ValueMappingExceptionMessages.*;
import static ru.art.entity.constants.ValueType.*;
import java.text.*;
import java.util.*;
import java.util.stream.*;

@Getter
@ToString
@EqualsAndHashCode
public class Entity implements Value {
    private final Map<String, ? extends Value> fields;
    private final Set<String> fieldNames;

    private final ValueType type = ENTITY;

    public Entity(Map<String, ? extends Value> fields) {
        this.fields = fields;
        fieldNames = fields.keySet();
    }

    public static EntityBuilder entityBuilder() {
        return new EntityBuilder();
    }

    public static Entity merge(Entity... entities) {
        if (CheckerForEmptiness.isEmpty(entities)) {
            return null;
        }
        EntityBuilder entityBuilder = entityBuilder();
        for (Entity entity : entities) {
            if (nonNull(entity)) {
                entityBuilder.fields.putAll(entity.fields);
            }
        }
        return entityBuilder.build();
    }

    public <T, V extends Value> T getValue(String name, ValueToModelMapper<T, V> mapper) {
        if (isNull(mapper)) throw new ValueMappingException(MAPPER_IS_NULL);
        return mapper.map(cast(fields.get(name)));
    }


    public Value getValue(String name) {
        return fields.get(name);
    }

    public Entity getEntity(String name) {
        return asEntity(fields.get(name));
    }

    public String getString(String name) {
        Primitive primitive = asPrimitive(fields.get(name));
        return isNull(primitive) ? null : primitive.getString();
    }

    public Date getDate(String name) {
        Primitive primitive = asPrimitive(fields.get(name));
        try {
            return isNull(primitive) ? null : YYYY_MM_DD_T_HH_MM_SS_24H_SSS_Z_DASH_FORMAT.get().parse(primitive.getString());
        } catch (ParseException throwable) {
            throw new ValueMappingException(UNABLE_TO_PARSE_DATE);
        }
    }

    public Long getLong(String name) {
        Primitive primitive = asPrimitive(fields.get(name));
        return isNull(primitive) ? null : primitive.getLong();
    }

    public Byte getByte(String name) {
        Primitive primitive = asPrimitive(fields.get(name));
        return isNull(primitive) ? null : primitive.getByte();
    }

    public Double getDouble(String name) {
        Primitive primitive = asPrimitive(fields.get(name));
        return isNull(primitive) ? null : primitive.getDouble();
    }

    public Boolean getBool(String name) {
        Primitive primitive = asPrimitive(fields.get(name));
        return isNull(primitive) ? null : primitive.getBool();
    }

    public Integer getInt(String name) {
        Primitive primitive = asPrimitive(fields.get(name));
        return isNull(primitive) ? null : primitive.getInt();
    }

    public Float getFloat(String name) {
        Primitive primitive = asPrimitive(fields.get(name));
        return isNull(primitive) ? null : primitive.getFloat();
    }

    public StringParametersMap getStringParametersMap(String name) {
        Value value = fields.get(name);
        if (isEntity(value)) {
            return asEntity(value).toStringParameters();
        }
        return asStringParametersMap(value);
    }

    public Map<String, String> getStringParameters(String name) {
        StringParametersMap map = getStringParametersMap(name);
        return isNull(map) ? null : map.getParameters();
    }

    public MapValue getMapValue(String name) {
        Value value = fields.get(name);
        if (isNull(value)) {
            return null;
        }
        if (isEntity(value)) {
            return asEntity(value).toMap();
        }
        return asMap(value);
    }

    public <K extends Value, V extends Value> Map<K, V> getMap(String name) {
        MapValue map = getMapValue(name);
        return isNull(map) ? null : cast(map.getElements());
    }

    public <K, V> Map<K, V> getMap(String name, ValueToModelMapper<K, ? extends Value> keyMapper, ValueToModelMapper<V, ? extends Value> valueMapper) {
        Map<Value, Value> mapValue = getMap(name);
        if (isNull(mapValue)) return null;
        return mapValue
                .entrySet()
                .stream()
                .collect(Collectors.toMap(entry -> keyMapper.map(cast(entry.getKey())), entry -> valueMapper.map(cast(entry.getValue()))));
    }

    public <C> CollectionValue<C> getCollectionValue(String name) {
        CollectionValue<C> collection = asCollection(fields.get(name));
        return isNull(collection) ? null : collection;
    }


    public Value find(String key) {
        if (CheckerForEmptiness.isEmpty(key)) {
            return null;
        }
        Value value;
        if (nonNull(value = getValue(key))) {
            return value;
        }
        Queue<String> sections = queueOf(key.split(ESCAPED_DOT));
        Entity entity = this;
        String section;
        while ((section = sections.poll()) != null) {
            value = entity.getValue(section);
            if (Value.isEmpty(value)) return null;
            if (!isEntity(value)) {
                if (sections.size() > 1) return null;
                return value;
            }
            entity = asEntity(value);
        }
        return value;
    }

    public String findString(String key) {
        Value value = find(key);
        if (Value.isEmpty(value)) return null;
        return asPrimitive(value).getString();
    }

    public Integer findInt(String name) {
        Primitive primitive = asPrimitive(find(name));
        return isNull(primitive) ? null : primitive.getInt();
    }

    public Long findLong(String name) {
        Primitive primitive = asPrimitive(find(name));
        return isNull(primitive) ? null : primitive.getLong();
    }

    public Double findDouble(String name) {
        Primitive primitive = asPrimitive(find(name));
        return isNull(primitive) ? null : primitive.getDouble();
    }

    public Boolean findBool(String name) {
        Primitive primitive = asPrimitive(find(name));
        return isNull(primitive) ? null : primitive.getBool();
    }

    public Float findFloat(String name) {
        Primitive primitive = asPrimitive(find(name));
        return isNull(primitive) ? null : primitive.getFloat();
    }

    public Entity findEntity(String name) {
        return asEntity(find(name));
    }

    public <T> CollectionValue<T> findCollectionValue(String name) {
        return asCollection(find(name));
    }

    public MapValue findMapValue(String name) {
        Value value = find(name);
        if (isNull(value)) {
            return null;
        }
        if (isEntity(value)) {
            return asEntity(value).toMap();
        }
        return asMap(value);
    }

    public StringParametersMap findStringParametersMap(String name) {
        Value value = find(name);
        if (isNull(value)) {
            return null;
        }
        if (isEntity(value)) {
            return asEntity(value).toStringParameters();
        }
        return asStringParametersMap(value);
    }


    public List<String> findStringList(String name) {
        CollectionValue<String> collection = asCollection(find(name));
        return isNull(collection) ? null : collection.getStringList();
    }

    public List<Integer> findIntList(String name) {
        CollectionValue<Integer> collection = asCollection(find(name));
        return isNull(collection) ? null : collection.getIntList();
    }

    public List<Double> findDoubleList(String name) {
        CollectionValue<Double> collection = asCollection(find(name));
        return isNull(collection) ? null : collection.getDoubleList();
    }

    public List<Long> findLongList(String name) {
        CollectionValue<Long> collection = asCollection(find(name));
        return isNull(collection) ? null : collection.getLongList();
    }

    public List<Boolean> findBoolList(String name) {
        CollectionValue<Integer> collection = asCollection(find(name));
        return isNull(collection) ? null : collection.getBoolList();
    }

    public List<Float> findFloatList(String name) {
        CollectionValue<Float> collection = asCollection(find(name));
        return isNull(collection) ? null : collection.getFloatList();
    }

    public List<Entity> findEntityList(String name) {
        CollectionValue<?> collectionValue = findCollectionValue(name);
        return isNull(collectionValue) ? emptyList() : collectionValue.getEntityList();
    }

    public <T> List<CollectionValue<T>> findCollectionValueList(String name) {
        CollectionValue<?> collectionValue = findCollectionValue(name);
        return isNull(collectionValue) ? emptyList() : cast(collectionValue.getCollectionsList());
    }

    public List<MapValue> findMapValueList(String name) {
        CollectionValue<?> collectionValue = findCollectionValue(name);
        return isNull(collectionValue) ? emptyList() : collectionValue.getMapValueList();
    }

    public List<StringParametersMap> findStringParametersMapList(String name) {
        CollectionValue<?> collectionValue = findCollectionValue(name);
        return isNull(collectionValue) ? emptyList() : collectionValue.getStringParametersList();
    }


    public List<String> findStringSet(String name) {
        CollectionValue<String> collection = asCollection(find(name));
        return isNull(collection) ? null : collection.getStringList();
    }

    public List<Integer> findIntSet(String name) {
        CollectionValue<Integer> collection = asCollection(find(name));
        return isNull(collection) ? null : collection.getIntList();
    }

    public List<Double> findDoubleSet(String name) {
        CollectionValue<Double> collection = asCollection(find(name));
        return isNull(collection) ? null : collection.getDoubleList();
    }

    public Set<Long> findLongSet(String name) {
        CollectionValue<Long> collection = asCollection(find(name));
        return isNull(collection) ? null : collection.getLongSet();
    }

    public Set<Boolean> findBoolSet(String name) {
        CollectionValue<Integer> collection = asCollection(find(name));
        return isNull(collection) ? null : collection.getBoolSet();
    }

    public Set<Float> findFloatSet(String name) {
        CollectionValue<Float> collection = asCollection(find(name));
        return isNull(collection) ? null : collection.getFloatSet();
    }

    public Set<Entity> findEntitySet(String name) {
        CollectionValue<?> collectionValue = findCollectionValue(name);
        return isNull(collectionValue) ? emptySet() : collectionValue.getEntitySet();
    }

    public <T> Set<CollectionValue<T>> findCollectionValueSet(String name) {
        CollectionValue<?> collectionValue = findCollectionValue(name);
        return isNull(collectionValue) ? emptySet() : cast(collectionValue.getCollectionsSet());
    }

    public Set<MapValue> findMapValueSet(String name) {
        CollectionValue<?> collectionValue = findCollectionValue(name);
        return isNull(collectionValue) ? emptySet() : collectionValue.getMapValueSet();
    }

    public Set<StringParametersMap> findStringParametersMapSet(String name) {
        CollectionValue<?> collectionValue = findCollectionValue(name);
        return isNull(collectionValue) ? emptySet() : collectionValue.getStringParametersSet();
    }


    public <T> List<T> getEntityList(String name, ValueToModelMapper<T, Entity> mapper) {
        CollectionValue<Entity> collectionValue = asCollection(fields.get(name));
        return isNull(collectionValue) ? emptyList() : collectionValue.getEntityList().stream().filter(Objects::nonNull)
                .map(mapper::map)
                .collect(toList());
    }

    public List<Entity> getEntityList(String name) {
        CollectionValue<Entity> collectionValue = asCollection(fields.get(name));
        return isNull(collectionValue) ? emptyList() : collectionValue.getEntityList();
    }

    public List<Value> getValueList(String name) {
        CollectionValue<Value> collectionValue = asCollection(fields.get(name));
        return isNull(collectionValue) ? emptyList() : collectionValue.getValueList();
    }

    public <C> List<CollectionValue<C>> getCollectionValueList(String name) {
        CollectionValue<CollectionValue<C>> collectionValue = asCollection(fields.get(name));
        return isNull(collectionValue) ? emptyList() : cast(collectionValue.getCollectionsList());
    }

    public List<MapValue> getMapValueList(String name) {
        CollectionValue<MapValue> collectionValue = asCollection(fields.get(name));
        return isNull(collectionValue) ? emptyList() : collectionValue.getMapValueList();
    }

    public List<StringParametersMap> getStringParameterMapList(String name) {
        CollectionValue<StringParametersMap> collectionValue = asCollection(fields.get(name));
        return isNull(collectionValue) ? emptyList() : collectionValue.getStringParametersList();
    }

    public List<Long> getLongList(String name) {
        CollectionValue<Long> collectionValue = asCollection(fields.get(name));
        return isNull(collectionValue) ? emptyList() : collectionValue.getLongList();
    }

    public List<String> getStringList(String name) {
        CollectionValue<String> collectionValue = asCollection(fields.get(name));
        return isNull(collectionValue) ? emptyList() : collectionValue.getStringList();
    }

    public List<Boolean> getBoolList(String name) {
        CollectionValue<Boolean> collectionValue = asCollection(fields.get(name));
        return isNull(collectionValue) ? emptyList() : collectionValue.getBoolList();
    }

    public List<Integer> getIntList(String name) {
        CollectionValue<Integer> collectionValue = asCollection(fields.get(name));
        return isNull(collectionValue) ? emptyList() : collectionValue.getIntList();
    }

    public List<Double> getDoubleList(String name) {
        CollectionValue<Double> collectionValue = asCollection(fields.get(name));
        return isNull(collectionValue) ? emptyList() : collectionValue.getDoubleList();
    }

    public List<Byte> getByteList(String name) {
        CollectionValue<Byte> collectionValue = asCollection(fields.get(name));
        return isNull(collectionValue) ? emptyList() : collectionValue.getByteList();
    }

    public List<Float> getFloatList(String name) {
        CollectionValue<Float> collectionValue = asCollection(fields.get(name));
        return isNull(collectionValue) ? emptyList() : collectionValue.getFloatList();
    }


    public <T> Set<T> getEntitySet(String name, ValueToModelMapper<T, Entity> mapper) {
        CollectionValue<Entity> collectionValue = asCollection(fields.get(name));
        return isNull(collectionValue) ? emptySet() : collectionValue.getEntitySet().stream().filter(Objects::nonNull)
                .map(mapper::map)
                .collect(toSet());
    }

    public Set<Entity> getEntitySet(String name) {
        CollectionValue<Entity> collectionValue = asCollection(fields.get(name));
        return isNull(collectionValue) ? emptySet() : collectionValue.getEntitySet();
    }

    public Set<Value> getValueSet(String name) {
        CollectionValue<Value> collectionValue = asCollection(fields.get(name));
        return isNull(collectionValue) ? emptySet() : collectionValue.getValueSet();
    }

    public <C> Set<CollectionValue<C>> getCollectionValueSet(String name) {
        CollectionValue<CollectionValue<C>> collectionValue = asCollection(fields.get(name));
        return isNull(collectionValue) ? emptySet() : cast(collectionValue.getCollectionsSet());
    }

    public Set<MapValue> getMapValueSet(String name) {
        CollectionValue<MapValue> collectionValue = asCollection(fields.get(name));
        return isNull(collectionValue) ? emptySet() : collectionValue.getMapValueSet();
    }

    public Set<StringParametersMap> getStringParameterMapSet(String name) {
        CollectionValue<StringParametersMap> collectionValue = asCollection(fields.get(name));
        return isNull(collectionValue) ? emptySet() : collectionValue.getStringParametersSet();
    }

    public Set<Long> getLongSet(String name) {
        CollectionValue<Long> collectionValue = asCollection(fields.get(name));
        return isNull(collectionValue) ? emptySet() : collectionValue.getLongSet();
    }

    public Set<String> getStringSet(String name) {
        CollectionValue<String> collectionValue = asCollection(fields.get(name));
        return isNull(collectionValue) ? emptySet() : collectionValue.getStringSet();
    }

    public Set<Boolean> getBoolSet(String name) {
        CollectionValue<Boolean> collectionValue = asCollection(fields.get(name));
        return isNull(collectionValue) ? emptySet() : collectionValue.getBoolSet();

    }

    public Set<Integer> getIntSet(String name) {
        CollectionValue<Integer> collectionValue = asCollection(fields.get(name));
        return isNull(collectionValue) ? emptySet() : collectionValue.getIntSet();
    }

    public Set<Double> getDoubleSet(String name) {
        CollectionValue<Double> collectionValue = asCollection(fields.get(name));
        return isNull(collectionValue) ? emptySet() : collectionValue.getDoubleSet();
    }

    public Set<Byte> getByteSet(String name) {
        CollectionValue<Byte> collectionValue = asCollection(fields.get(name));
        return isNull(collectionValue) ? emptySet() : collectionValue.getByteSet();
    }

    public Set<Float> getFloatSet(String name) {
        CollectionValue<Float> collectionValue = asCollection(fields.get(name));
        return isNull(collectionValue) ? emptySet() : collectionValue.getFloatSet();
    }


    public <T> Queue<T> getEntityQueue(String name, ValueToModelMapper<T, Entity> mapper) {
        CollectionValue<Entity> collectionValue = asCollection(fields.get(name));
        return isNull(collectionValue) ? new LinkedList<>() : collectionValue.getEntityQueue().stream().filter(Objects::nonNull)
                .map(mapper::map)
                .collect(toCollection(LinkedList::new));
    }

    public Queue<Entity> getEntityQueue(String name) {
        CollectionValue<Entity> collectionValue = asCollection(fields.get(name));
        return isNull(collectionValue) ? new LinkedList<>() : collectionValue.getEntityQueue();
    }

    public Queue<Value> getValueQueue(String name) {
        CollectionValue<Value> collectionValue = asCollection(fields.get(name));
        return isNull(collectionValue) ? new LinkedList<>() : collectionValue.getValueQueue();
    }

    public <C> Queue<CollectionValue<C>> getCollectionValueQueue(String name) {
        CollectionValue<CollectionValue<C>> collectionValue = asCollection(fields.get(name));
        return isNull(collectionValue) ? new LinkedList<>() : cast(collectionValue.getCollectionsQueue());
    }

    public Queue<MapValue> getMapValueQueue(String name) {
        CollectionValue<MapValue> collectionValue = asCollection(fields.get(name));
        return isNull(collectionValue) ? new LinkedList<>() : collectionValue.getMapValueQueue();
    }

    public Queue<StringParametersMap> getStringParameterMapQueue(String name) {
        CollectionValue<StringParametersMap> collectionValue = asCollection(fields.get(name));
        return isNull(collectionValue) ? new LinkedList<>() : collectionValue.getStringParametersQueue();
    }

    public Queue<Long> getLongQueue(String name) {
        CollectionValue<Long> collectionValue = asCollection(fields.get(name));
        return isNull(collectionValue) ? new LinkedList<>() : collectionValue.getLongQueue();
    }

    public Queue<String> getStringQueue(String name) {
        CollectionValue<String> collectionValue = asCollection(fields.get(name));
        return isNull(collectionValue) ? new LinkedList<>() : collectionValue.getStringQueue();
    }

    public Queue<Boolean> getBoolQueue(String name) {
        CollectionValue<Boolean> collectionValue = asCollection(fields.get(name));
        return isNull(collectionValue) ? new LinkedList<>() : collectionValue.getBoolQueue();
    }

    public Queue<Integer> getIntQueue(String name) {
        CollectionValue<Integer> collectionValue = asCollection(fields.get(name));
        return isNull(collectionValue) ? new LinkedList<>() : collectionValue.getIntQueue();
    }

    public Queue<Double> getDoubleQueue(String name) {
        CollectionValue<Double> collectionValue = asCollection(fields.get(name));
        return isNull(collectionValue) ? new LinkedList<>() : collectionValue.getDoubleQueue();
    }

    public Queue<Byte> getByteQueue(String name) {
        CollectionValue<Byte> collectionValue = asCollection(fields.get(name));
        return isNull(collectionValue) ? new LinkedList<>() : collectionValue.getByteQueue();
    }

    public Queue<Float> getFloatQueue(String name) {
        CollectionValue<Float> collectionValue = asCollection(fields.get(name));
        return isNull(collectionValue) ? new LinkedList<>() : collectionValue.getFloatQueue();
    }


    public <K, V> Map<K, V> getMap(String name, ValueToModelMapper<Map<K, V>, MapValue> mapper) {
        MapValue mapValue = getMapValue(name);
        return isNull(mapValue) ? emptyMap() : mapper.map(mapValue);
    }

    public MapValue toMap() {
        if (isEmpty()) {
            return MapValue.builder().build();
        }
        return MapValue.builder().elements(fields.entrySet()
                .stream()
                .collect(Collectors.toMap(entry -> stringPrimitive(entry.getKey()), Map.Entry::getValue)))
                .build();
    }

    public StringParametersMap toStringParameters() {
        if (isEmpty()) {
            return StringParametersMap.builder().build();
        }
        return StringParametersMap.builder().parameters(fields.entrySet()
                .stream()
                .filter(entry -> isPrimitive(entry.getValue()))
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> asPrimitive(entry.getValue()).getString())))
                .build();
    }

    public Map<String, String> dump() {
        if (isEmpty()) {
            return emptyMap();
        }
        Map<String, String> dump = mapOf();
        fields.forEach((key, value) -> {
            if (isPrimitive(value)) {
                dump.put(key, value.toString());
                return;
            }
            if (isEntity(value)) {
                asEntity(value).dump().forEach((innerKey, innerField) -> dump.put(key + DOT + innerKey, innerField));
                return;
            }
            if (isCollection(value)) {
                asCollection(value).dump().forEach((innerKey, innerField) -> dump.put(key + innerKey, innerField));
            }
        });
        return dump;
    }

    @Override
    public boolean isEmpty() {
        return CheckerForEmptiness.isEmpty(fields);
    }

    public static class EntityBuilder {
        private final Map<String, Value> fields = mapOf();

        public EntityBuilder valueField(String name, Value value) {
            fields.put(name, value);
            return this;
        }

        @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
        public EntityBuilder valueField(String name, Optional<Value> optionalValue) {
            optionalValue.ifPresent(value -> fields.put(name, value));
            return this;
        }

        public <V> EntityBuilder valueField(String name, V value, ValueFromModelMapper<V, Value> mapper) {
            fields.put(name, mapper.map(value));
            return this;
        }

        public EntityBuilder intField(String name, Integer value) {
            fields.put(name, intPrimitive(value));
            return this;
        }

        public EntityBuilder longField(String name, Long value) {
            fields.put(name, longPrimitive(value));
            return this;
        }

        public EntityBuilder stringField(String name, String value) {
            fields.put(name, stringPrimitive(value));
            return this;
        }

        public EntityBuilder dateField(String name, Date value) {
            if (isNotEmpty(value)) {
                fields.put(name, stringPrimitive(YYYY_MM_DD_T_HH_MM_SS_24H_SSS_Z_DASH_FORMAT.get().format(value)));
            }
            return this;
        }

        public EntityBuilder boolField(String name, Boolean value) {
            fields.put(name, boolPrimitive(value));
            return this;
        }

        public EntityBuilder doubleField(String name, Double value) {
            fields.put(name, doublePrimitive(value));
            return this;
        }

        public EntityBuilder floatField(String name, Float value) {
            fields.put(name, floatPrimitive(value));
            return this;
        }

        public EntityBuilder byteField(String name, Byte value) {
            fields.put(name, bytePrimitive(value));
            return this;
        }

        public EntityBuilder entityField(String name, Entity entity) {
            fields.put(name, entity);
            return this;
        }

        public <T> EntityBuilder entityField(String name, T object, ValueFromModelMapper<T, Entity> mapper) {
            if (isNull(object)) {
                fields.put(name, null);
                return this;
            }
            return entityField(name, mapper.map(object));
        }

        public EntityBuilder mapField(String name, Map<? extends Value, ? extends Value> map) {
            fields.put(name, MapValue.builder().elements(map).build());
            return this;
        }

        public EntityBuilder mapField(String name, Map<?, ?> map, ValueFromModelMapper<?, ? extends Value> keyMapper, ValueFromModelMapper<?, ? extends Value> valueMapper) {
            if (isNull(map)) {
                mapField(name, emptyMap());
            }
            Map<? extends Value, ? extends Value> elements = map.entrySet()
                    .stream()
                    .collect(Collectors.toMap(entry -> keyMapper.map(cast(entry.getKey())), entry -> valueMapper.map(cast(entry.getValue()))));
            mapField(name, elements);
            return this;
        }

        public EntityBuilder stringParametersField(String name, StringParametersMap stringParametersMap) {
            fields.put(name, stringParametersMap);
            return this;
        }

        public EntityBuilder stringParametersField(String name, Map<String, String> stringParametersMap) {
            fields.put(name, StringParametersMap.builder().parameters(stringParametersMap).build());
            return this;
        }


        public EntityBuilder boolCollectionField(String name, Collection<Boolean> value) {
            fields.put(name, boolCollection(value));
            return this;
        }

        public EntityBuilder intCollectionField(String name, Collection<Integer> value) {
            fields.put(name, intCollection(value));
            return this;
        }

        public EntityBuilder longCollectionField(String name, Collection<Long> value) {
            fields.put(name, longCollection(value));
            return this;
        }

        public EntityBuilder stringCollectionField(String name, Collection<String> value) {
            fields.put(name, stringCollection(value));
            return this;
        }

        public EntityBuilder doubleCollectionField(String name, Collection<Double> value) {
            fields.put(name, doubleCollection(value));
            return this;
        }

        public EntityBuilder byteCollectionField(String name, Collection<Byte> value) {
            fields.put(name, byteCollection(value));
            return this;
        }

        public EntityBuilder floatCollectionField(String name, Collection<Float> value) {
            fields.put(name, floatCollection(value));
            return this;
        }


        public EntityBuilder boolArrayField(String name, boolean[] value) {
            fields.put(name, boolCollection(value));
            return this;
        }

        public EntityBuilder intArrayField(String name, int[] value) {
            fields.put(name, intCollection(value));
            return this;
        }

        public EntityBuilder longArrayField(String name, long[] value) {
            fields.put(name, longCollection(value));
            return this;
        }

        public EntityBuilder doubleArrayField(String name, double[] value) {
            fields.put(name, doubleCollection(value));
            return this;
        }

        public EntityBuilder byteArrayField(String name, byte[] value) {
            fields.put(name, byteCollection(value));
            return this;
        }

        public EntityBuilder floatArrayField(String name, float[] value) {
            fields.put(name, floatCollection(value));
            return this;
        }


        public EntityBuilder entityCollectionField(String name, Collection<Entity> value) {
            fields.put(name, collectionValue(CollectionElementsType.ENTITY, value));
            return this;
        }

        public EntityBuilder valueCollectionField(String name, Collection<Value> value) {
            fields.put(name, collectionValue(CollectionElementsType.VALUE, value));
            return this;
        }

        public EntityBuilder collectionValueField(String name, CollectionValue<?> value) {
            fields.put(name, value);
            return this;
        }

        public EntityBuilder stringParametersCollectionField(String name, Collection<StringParametersMap> value) {
            fields.put(name, collectionValue(CollectionElementsType.STRING_PARAMETERS_MAP, value));
            return this;
        }

        public EntityBuilder collectionValueCollectionField(String name, Collection<CollectionValue<Entity>> value) {
            fields.put(name, collectionValue(CollectionElementsType.COLLECTION, value));
            return this;
        }

        public EntityBuilder mapValueCollectionField(String name, Collection<MapValue> value) {
            fields.put(name, collectionValue(CollectionElementsType.MAP, value));
            return this;
        }

        public <T> EntityBuilder entityCollectionField(String name, Collection<T> collection, ValueFromModelMapper<T, Entity> mapper) {
            if (isNull(collection)) {
                entityCollectionField(name, emptyList());
                return this;
            }
            return entityCollectionField(name, collection.stream()
                    .filter(Objects::nonNull)
                    .map(mapper::map)
                    .collect(toList()));
        }


        public Entity build() {
            return new Entity(cast(mapOf(fields)));
        }
    }
}
