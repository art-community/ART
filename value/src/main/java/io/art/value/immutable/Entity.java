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

package io.art.value.immutable;

import io.art.core.collection.*;
import io.art.core.exception.*;
import io.art.core.factory.*;
import io.art.core.property.*;
import io.art.value.builder.*;
import io.art.value.constants.ValueModuleConstants.*;
import io.art.value.exception.*;
import io.art.value.mapper.ValueFromModelMapper.*;
import io.art.value.mapper.*;
import lombok.*;
import static io.art.core.caster.Caster.*;
import static io.art.core.checker.EmptinessChecker.*;
import static io.art.core.checker.NullityChecker.*;
import static io.art.core.constants.StringConstants.*;
import static io.art.core.factory.QueueFactory.*;
import static io.art.core.factory.SetFactory.*;
import static io.art.core.property.LazyProperty.*;
import static io.art.value.constants.ValueModuleConstants.ExceptionMessages.*;
import static io.art.value.constants.ValueModuleConstants.ValueType.*;
import static io.art.value.factory.PrimitivesFactory.*;
import static io.art.value.immutable.Value.*;
import static io.art.value.mapper.ValueToModelMapper.*;
import static java.text.MessageFormat.*;
import static java.util.Objects.*;
import javax.annotation.*;
import java.util.*;
import java.util.function.*;

public class Entity implements Value {
    @Getter
    private final ValueType type = ENTITY;
    private final Set<Primitive> keys;
    private final Function<Primitive, ? extends Value> valueProvider;
    private final Map<Primitive, Value> asMap;
    private final ImmutableMap<Primitive, Value> asImmutableMap;

    public Entity(Set<Primitive> keys, Function<Primitive, ? extends Value> valueProvider) {
        this.keys = keys;
        this.valueProvider = valueProvider;
        asMap = asMap(key -> key, key -> key, value -> value);
        asImmutableMap = asImmutableMap(key -> key, key -> key, value -> value);
    }

    public static EntityBuilder entityBuilder() {
        return new EntityBuilder();
    }

    public int size() {
        return keys.size();
    }

    public Value get(String key) {
        return get(stringPrimitive(key));
    }

    public Value get(Long key) {
        return get(longPrimitive(key));
    }

    public Value get(Integer key) {
        return get(intPrimitive(key));
    }

    public Value get(Boolean key) {
        return get(boolPrimitive(key));
    }

    public Value get(Double key) {
        return get(doublePrimitive(key));
    }

    public Value get(Float key) {
        return get(floatPrimitive(key));
    }

    public Value get(Byte key) {
        return get(bytePrimitive(key));
    }

    public Value get(Short key) {
        return get(shortPrimitive(key));
    }

    public Value get(Character key) {
        return get(charPrimitive(key));
    }

    public Value get(Primitive primitive) {
        if (valueIsNull(primitive)) {
            return null;
        }
        return valueProvider.apply(primitive);
    }


    public boolean has(String key) {
        return has(stringPrimitive(key));

    }

    public boolean has(Long key) {
        return has(longPrimitive(key));
    }

    public boolean has(Integer key) {
        return has(intPrimitive(key));
    }

    public boolean has(Boolean key) {
        return has(boolPrimitive(key));
    }

    public boolean has(Double key) {
        return has(doublePrimitive(key));

    }

    public boolean has(Float key) {
        return has(floatPrimitive(key));
    }

    public boolean has(Byte key) {
        return has(bytePrimitive(key));
    }

    public boolean has(Short key) {
        return has(shortPrimitive(key));
    }

    public boolean has(Character key) {
        return has(charPrimitive(key));

    }

    public boolean has(Primitive key) {
        return keys.contains(key);
    }


    public Value find(String key, String delimiter) {
        if (isEmpty(key)) return null;
        Value value;
        if (nonNull(value = get(key))) return value;
        Queue<String> sections = queueOf(key.split(delimiter));
        Entity entity = this;
        String section;
        while (nonNull(section = sections.poll())) {
            value = entity.get(section);
            if (valueIsNull(value)) return null;
            if (!isEntity(value)) {
                if (sections.size() > 1) return null;
                return value;
            }
            entity = asEntity(value);
        }
        return value;
    }

    public Value find(String key) {
        return find(key, ESCAPED_DOT);
    }


    public <T, V extends Value> T map(Primitive primitive, ValueToModelMapper<T, V> mapper) {
        try {
            return let(cast(get(primitive)), mapper::map);
        } catch (Throwable throwable) {
            throw new ValueMappingException(format(FIELD_MAPPING_EXCEPTION, primitive), throwable);
        }
    }

    public <T, V extends Value> T mapOrDefault(Primitive key, PrimitiveType type, ValueToModelMapper<T, V> mapper) {
        return mapper.map(cast(orElse(get(key), type::defaultValue)));
    }


    public Map<Primitive, ? extends Value> toMap() {
        return toMap(key -> key, value -> value);
    }

    public <K, V> Map<K, V> toMap(PrimitiveToModelMapper<K> keyMapper, ValueToModelMapper<V, ? extends Value> valueMapper) {
        Map<K, V> newMap = MapFactory.map();
        for (Primitive key : keys) apply(map(key, valueMapper), value -> newMap.put(keyMapper.map(key), value));
        return newMap;
    }


    public Map<Primitive, ? extends Value> asMap() {
        return asMap;
    }

    public <K, V> Map<K, V> asMap(PrimitiveToModelMapper<K> toKeyMapper, PrimitiveFromModelMapper<K> fromKeyMapper, ValueToModelMapper<V, ? extends Value> valueMapper) {
        return new ProxyMap<>(toKeyMapper, fromKeyMapper, valueMapper);
    }


    public ImmutableMap<Primitive, ? extends Value> asImmutableMap() {
        return asImmutableMap;
    }

    public <K, V> ImmutableMap<K, V> asImmutableMap(PrimitiveToModelMapper<K> toKeyMapper, PrimitiveFromModelMapper<K> fromKeyMapper, ValueToModelMapper<V, ? extends Value> valueMapper) {
        return new ProxyMap<>(toKeyMapper, fromKeyMapper, valueMapper);
    }

    public EntityBuilder toBuilder() {
        EntityBuilder entityBuilder = entityBuilder();
        keys.forEach(key -> entityBuilder.lazyPut(key, () -> valueProvider.apply(key)));
        return entityBuilder;
    }

    @Override
    public boolean equals(Object object) {
        if (object == this) return true;
        if (!(object instanceof Entity)) return false;

        Entity another = (Entity) object;
        Set<Primitive> keySet = this.asMap().keySet();

        if (!(keySet.equals(another.asMap().keySet()))) return false;

        Value entry;
        for (Primitive key : keySet) {
            entry = this.get(key);
            if (isNull(entry)) {
                if (isNull(another.get(key))) continue;
                return false;
            }
            if (!(entry.equals(another.get(key)))) return false;
        }
        return true;
    }


    private class ProxyMap<K, V> implements Map<K, V>, ImmutableMap<K, V> {
        private final ValueToModelMapper<V, ? extends Value> valueMapper;
        private final PrimitiveFromModelMapper<K> fromKeyMapper;
        private final LazyProperty<Map<K, V>> evaluated;
        private final Set<K> fields;

        public ProxyMap(PrimitiveToModelMapper<K> toKeyMapper, PrimitiveFromModelMapper<K> fromKeyMapper, ValueToModelMapper<V, ? extends Value> valueMapper) {
            this.valueMapper = valueMapper;
            this.fromKeyMapper = fromKeyMapper;
            this.evaluated = lazy(() -> Entity.this.toMap(toKeyMapper, valueMapper));
            this.fields = set();
            for (Primitive key : keys) {
                fields.add(toKeyMapper.map(key));
            }
        }

        @Override
        public int size() {
            return keys.size();
        }

        @Override
        public boolean isEmpty() {
            return keys.isEmpty();
        }

        @Override
        public boolean containsKey(Object key) {
            if (isNull(key)) {
                return false;
            }
            return keys.contains(fromKeyMapper.map(cast(key)));
        }

        @Override
        public boolean containsValue(Object value) {
            if (isNull(value)) {
                return false;
            }
            return evaluated.get().containsValue(value);
        }

        @Override
        public V get(Object key) {
            if (isNull(key)) {
                return null;
            }
            return map(fromKeyMapper.map(cast(key)), valueMapper);
        }

        @Override
        public V put(K key, V value) {
            throw new NotImplementedException("put");
        }

        @Override
        public V remove(Object key) {
            throw new NotImplementedException("remove");
        }

        @Override
        public void putAll(@Nonnull Map<? extends K, ? extends V> map) {
            throw new NotImplementedException("putAll");
        }

        @Override
        public void clear() {
            throw new NotImplementedException("clear");
        }

        @Override
        @Nonnull
        public Set<K> keySet() {
            return fields;
        }

        @Override
        @Nonnull
        public Collection<V> values() {
            return evaluated.get().values();
        }

        @Override
        @Nonnull
        public Set<Entry<K, V>> entrySet() {
            return evaluated.get().entrySet();
        }

        @Override
        public V getOrDefault(Object key, V defaultValue) {
            return orElse(get(key), defaultValue);
        }

        @Override
        public void forEach(BiConsumer<? super K, ? super V> action) {
            entrySet().forEach(entry -> action.accept(entry.getKey(), entry.getValue()));
        }

        @Override
        public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
            throw new NotImplementedException("replaceAll");
        }

        @Override
        public V putIfAbsent(K key, V value) {
            throw new NotImplementedException("putIfAbsent");
        }

        @Override
        public boolean remove(Object key, Object value) {
            throw new NotImplementedException("remove");
        }

        @Override
        public boolean replace(K key, V oldValue, V newValue) {
            throw new NotImplementedException("replace");
        }

        @Override
        public V replace(K key, V value) {
            throw new NotImplementedException("replace");
        }

        @Override
        public V computeIfAbsent(K key, @Nonnull Function<? super K, ? extends V> mappingFunction) {
            throw new NotImplementedException("computeIfAbsent");
        }

        @Override
        public V computeIfPresent(K key, @Nonnull BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
            throw new NotImplementedException("computeIfPresent");
        }

        @Override
        public V compute(K key, @Nonnull BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
            throw new NotImplementedException("compute");
        }

        @Override
        public V merge(K key, @Nonnull V value, @Nonnull BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
            throw new NotImplementedException("merge");
        }

        @Override
        public Map<K, V> toMutable() {
            return evaluated.get();
        }
    }

    public static Entity EMPTY = entityBuilder().build();
}
