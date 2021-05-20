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

package io.art.rocks.db.storage;

import io.art.core.collection.*;
import io.art.rocks.db.bucket.*;
import io.art.rocks.db.exception.*;
import io.art.value.immutable.*;
import lombok.experimental.*;
import static io.art.core.checker.EmptinessChecker.*;
import static io.art.core.collection.ImmutableMap.*;
import static io.art.core.context.Context.*;
import static io.art.core.factory.MapFactory.*;
import static io.art.message.pack.module.MessagePackModule.*;
import static io.art.rocks.db.constants.RocksDbModuleConstants.ExceptionMessages.*;
import static io.art.rocks.db.constants.RocksDbModuleConstants.*;
import static io.art.rocks.db.storage.RocksDbCollectionStorage.*;
import static io.art.rocks.db.storage.RocksDbPrimitiveStorage.*;
import static io.art.value.immutable.Value.*;
import static io.art.value.module.ValueModule.*;
import static java.util.Objects.*;
import static java.util.Optional.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.Map.*;
import java.util.function.*;

@UtilityClass
public class RocksDbStorage {
    public void putObject(String name, String id, Object value) {
        putBinary(name, id, value(value));
    }

    public void putObject(String entityKey, Object value) {
        putBinary(entityKey, value(value));
    }

    public <T> Optional<T> getObject(Type type, String entityKey) {
        return getBinary(entityKey).map(value -> model(type, value));
    }


    public void putBinary(String name, String id, Value value) {
        if (isEmpty(name)) return;
        if (isEmpty(id)) return;
        if (isEmpty(value)) return;
        if (getValueIdentifiers(name).contains(id)) {
            putBinary(name + ROCKS_DB_KEY_DELIMITER + id, value);
            return;
        }
        add(name, id);
        putBinary(name + ROCKS_DB_KEY_DELIMITER + id, value);
    }

    public void putBinary(String entityKey, Value value) {
        if (isEmpty(entityKey)) return;
        if (isEmpty(value)) return;
        byte[] keyBytes = entityKey.getBytes(context().configuration().getCharset());
        byte[] valueBytes = messagePackModule().configuration().getWriter().writeToBytes(value);
        RocksDbPrimitiveStorage.put(keyBytes, valueBytes);
    }

    public Optional<Value> getBinary(String entityKey) {
        if (isEmpty(entityKey)) return empty();
        byte[] bytes = get(entityKey);
        if (isEmpty(bytes)) return empty();
        try {
            return ofNullable(messagePackModule().configuration().getReader().read(bytes));
        } catch (Throwable throwable) {
            throw new RocksDbException(MESSAGE_PACK_PARSING_ERROR, throwable);
        }
    }


    public void put(String name, String id, Value value) {
        if (isEmpty(name)) return;
        if (isEmpty(id)) return;
        if (isEmpty(value)) return;
        if (getValueIdentifiers(name).contains(id)) {
            put(name + ROCKS_DB_KEY_DELIMITER + id, value);
            return;
        }
        add(name, id);
        put(name + ROCKS_DB_KEY_DELIMITER + id, value);
    }

    public void put(String entityKey, Value value) {
        if (isEmpty(entityKey)) return;
        if (isEmpty(value)) return;
        switch (value.getType()) {
            case ENTITY:
                put(entityKey, asEntity(value));
                break;
            case ARRAY:
                put(entityKey, asArray(value));
                break;
            case STRING:
            case LONG:
            case DOUBLE:
            case INT:
            case BOOL:
            case BYTE:
                put(entityKey, asPrimitive(value));
        }
    }

    public void put(String entityKey, Primitive primitive) {
        if (isEmpty(entityKey)) return;
        if (isEmpty(primitive)) return;
        switch (primitive.getPrimitiveType()) {
            case STRING:
                RocksDbPrimitiveStorage.put(entityKey, primitive.getString());
                break;
            case LONG:
                RocksDbPrimitiveStorage.put(entityKey, primitive.getLong());
                break;
            case DOUBLE:
                RocksDbPrimitiveStorage.put(entityKey, primitive.getDouble());
                break;
            case INT:
                RocksDbPrimitiveStorage.put(entityKey, primitive.getInt());
                break;
            case BOOL:
                RocksDbPrimitiveStorage.put(entityKey, primitive.getBool());
                break;
            case BYTE:
                RocksDbPrimitiveStorage.put(entityKey, primitive.getByte());
                break;
        }
    }

    public void put(String entityKey, Entity entity) {
        if (isEmpty(entityKey)) return;
        if (isEmpty(entity)) return;
        for (Entry<? extends Value, ? extends Value> entry : entity.asMap().entrySet()) {
            if (!isPrimitive(entry.getKey())) continue;
            put(entityKey + ROCKS_DB_KEY_DELIMITER + entry.getKey(), entry.getValue());
        }
    }

    public void put(String entityKey, ArrayValue array) {
        if (isEmpty(entityKey)) return;
        if (isEmpty(array)) return;
        int index = 0;
        for (Value element : array.asImmutableArray()) {
            put(entityKey + ROCKS_DB_KEY_DELIMITER + index, element);
        }
    }


    public Optional<String> getEntityStringField(String entityKey, String fieldName) {
        return getString(entityKey + ROCKS_DB_KEY_DELIMITER + fieldName);
    }

    public Optional<Integer> getEntityIntField(String entityKey, String fieldName) {
        return getInt(entityKey + ROCKS_DB_KEY_DELIMITER + fieldName);
    }

    public Optional<Double> getEntityDoubleField(String entityKey, String fieldName) {
        return getDouble(entityKey + ROCKS_DB_KEY_DELIMITER + fieldName);
    }

    public Optional<Long> getEntityLongField(String entityKey, String fieldName) {
        return getLong(entityKey + ROCKS_DB_KEY_DELIMITER + fieldName);
    }

    public Optional<Character> getEntityCharField(String entityKey, String fieldName) {
        return getChar(entityKey + ROCKS_DB_KEY_DELIMITER + fieldName);
    }

    public Optional<Boolean> getEntityBoolField(String entityKey, String fieldName) {
        return getBoolean(entityKey + ROCKS_DB_KEY_DELIMITER + fieldName);
    }

    public Optional<Byte> getEntityByteField(String entityKey, String fieldName) {
        return getByte(entityKey + ROCKS_DB_KEY_DELIMITER + fieldName);
    }


    public void putEntityField(String entityKey, String fieldName, String value) {
        if (isEmpty(value)) return;
        RocksDbPrimitiveStorage.put(entityKey + ROCKS_DB_KEY_DELIMITER + fieldName, value);
    }

    public void putEntityField(String entityKey, String fieldName, int value) {
        if (isEmpty(value)) return;
        RocksDbPrimitiveStorage.put(entityKey + ROCKS_DB_KEY_DELIMITER + fieldName, value);
    }

    public void putEntityField(String entityKey, String fieldName, double value) {
        if (isEmpty(value)) return;
        RocksDbPrimitiveStorage.put(entityKey + ROCKS_DB_KEY_DELIMITER + fieldName, value);
    }

    public void putEntityField(String entityKey, String fieldName, char value) {
        if (isEmpty(value)) return;
        RocksDbPrimitiveStorage.put(entityKey + ROCKS_DB_KEY_DELIMITER + fieldName, value);
    }

    public void putEntityField(String entityKey, String fieldName, long value) {
        if (isEmpty(value)) return;
        RocksDbPrimitiveStorage.put(entityKey + ROCKS_DB_KEY_DELIMITER + fieldName, value);
    }

    public void putEntityField(String entityKey, String fieldName, boolean value) {
        if (isEmpty(value)) return;
        RocksDbPrimitiveStorage.put(entityKey + ROCKS_DB_KEY_DELIMITER + fieldName, value);
    }


    public List<String> getEntityStringList(String entityKey, String fieldName) {
        return getStringList(entityKey + ROCKS_DB_KEY_DELIMITER + fieldName);
    }

    public List<Integer> getEntityIntStringList(String entityKey, String fieldName) {
        return getIntList(entityKey + ROCKS_DB_KEY_DELIMITER + fieldName);
    }

    public List<Double> getEntityDoubleList(String entityKey, String fieldName) {
        return getDoubleList(entityKey + ROCKS_DB_KEY_DELIMITER + fieldName);
    }

    public List<Long> getEntityLongList(String entityKey, String fieldName) {
        return getLongList(entityKey + ROCKS_DB_KEY_DELIMITER + fieldName);
    }

    public List<Character> getEntityCharList(String entityKey, String fieldName) {
        return getCharList(entityKey + ROCKS_DB_KEY_DELIMITER + fieldName);
    }

    public List<Boolean> getEntityBoolList(String entityKey, String fieldName) {
        return getBoolList(entityKey + ROCKS_DB_KEY_DELIMITER + fieldName);
    }


    public Optional<String> getEntityStringField(Bucket bucket) {
        return getString(bucket.dbKey());
    }

    public Optional<Integer> getEntityIntField(Bucket bucket) {
        return getInt(bucket.dbKey());
    }

    public Optional<Double> getEntityDoubleField(Bucket bucket) {
        return getDouble(bucket.dbKey());
    }

    public Optional<Long> getEntityLongField(Bucket bucket) {
        return getLong(bucket.dbKey());
    }

    public Optional<Character> getEntityCharField(Bucket bucket) {
        return getChar(bucket.dbKey());
    }

    public Optional<Boolean> getEntityBoolField(Bucket bucket) {
        return getBoolean(bucket.dbKey());
    }

    public Optional<Byte> getEntityByteField(Bucket bucket) {
        return getByte(bucket.dbKey());
    }


    public void putEntityField(Bucket bucket, String value) {
        if (isEmpty(value)) return;
        RocksDbPrimitiveStorage.put(bucket.dbKey(), value);
    }

    public void putEntityField(Bucket bucket, int value) {
        if (isEmpty(value)) return;
        RocksDbPrimitiveStorage.put(bucket.dbKey(), value);
    }

    public void putEntityField(Bucket bucket, double value) {
        if (isEmpty(value)) return;
        RocksDbPrimitiveStorage.put(bucket.dbKey(), value);
    }

    public void putEntityField(Bucket bucket, char value) {
        if (isEmpty(value)) return;
        RocksDbPrimitiveStorage.put(bucket.dbKey(), value);
    }

    public void putEntityField(Bucket bucket, long value) {
        if (isEmpty(value)) return;
        RocksDbPrimitiveStorage.put(bucket.dbKey(), value);
    }

    public void putEntityField(Bucket bucket, boolean value) {
        if (isEmpty(value)) return;
        RocksDbPrimitiveStorage.put(bucket.dbKey(), value);
    }


    public List<String> getEntityStringList(Bucket bucket) {
        return getStringList(bucket.dbKey());
    }

    public List<Integer> getEntityIntStringList(Bucket bucket) {
        return getIntList(bucket.dbKey());
    }

    public List<Double> getEntityDoubleList(Bucket bucket) {
        return getDoubleList(bucket.dbKey());
    }

    public List<Character> getEntityCharList(Bucket bucket) {
        return getCharList(bucket.dbKey());
    }

    public List<Boolean> getEntityBoolList(Bucket bucket) {
        return getBoolList(bucket.dbKey());
    }

    public void deleteValue(String name, String id) {
        String dbKey = name + ROCKS_DB_KEY_DELIMITER + id;
        deleteByPrefix(dbKey);
        delete(dbKey);
        Set<String> valueIdentifiers = getValueIdentifiers(name);
        valueIdentifiers.remove(id);
        putStrings(name, valueIdentifiers);
    }


    public void deleteValues(String name) {
        deleteByPrefix(name);
        delete(name);
    }

    public Set<String> getValueIdentifiers(String name) {
        return getStringSet(name);
    }

    public <T> ImmutableMap<String, T> getValues(String name, Function<String, T> getter) {
        if (isEmpty(name)) return emptyImmutableMap();
        if (isNull(getter)) return emptyImmutableMap();
        Map<String, T> values = map();
        Set<String> valueIdentifiers = getValueIdentifiers(name);
        for (String identifier : valueIdentifiers) {
            values.put(identifier, getter.apply(identifier));
        }
        return immutableMapOf(values);
    }
}
