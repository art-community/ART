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

import io.art.rocks.db.exception.*;
import lombok.experimental.*;
import org.rocksdb.*;
import static io.art.core.checker.EmptinessChecker.*;
import static io.art.core.constants.ArrayConstants.*;
import static io.art.core.constants.StringConstants.*;
import static io.art.core.context.Context.*;
import static io.art.logging.module.LoggingModule.*;
import static io.art.rocks.db.constants.RocksDbModuleConstants.ExceptionMessages.*;
import static io.art.rocks.db.constants.RocksDbModuleConstants.LoggingMessages.*;
import static io.art.rocks.db.module.RocksDbModule.*;
import static java.text.MessageFormat.*;
import static java.util.Objects.*;
import static java.util.Optional.*;
import java.util.*;
import java.util.function.*;

@UtilityClass
public class RocksDbPrimitiveStorage {
    public <T> void put(String key, T value, Function<T, byte[]> mapper) {
        if (isEmpty(mapper)) return;
        if (isEmpty(key)) return;
        if (isEmpty(value)) return;
        RocksDbPrimitiveStorage.put(key.getBytes(context().configuration().getCharset()), mapper.apply(value));
    }

    public void put(byte[] key, byte[] value) {
        if (isEmpty(key)) return;
        if (isEmpty(value)) return;
        try {
            if (rocksDbModule().configuration().isLogging()) {
                logger(RocksDbPrimitiveStorage.class).trace(format(PUT_OPERATION, new String(key, context().configuration().getCharset()), new String(value, context().configuration().getCharset())));
            }
            rocksDbModule().state().db().put(key, value);
        } catch (RocksDBException throwable) {
            throw new RocksDbException(PUT_ERROR, throwable);
        }
    }

    public void put(String key, String value) {
        if (isEmpty(key)) return;
        if (isEmpty(value)) return;
        byte[] keyBytes = key.getBytes(context().configuration().getCharset());
        byte[] valueBytes = value.getBytes(context().configuration().getCharset());
        RocksDbPrimitiveStorage.put(keyBytes, valueBytes);
    }

    public void put(String key, int value) {
        if (isEmpty(key)) return;
        byte[] keyBytes = key.getBytes(context().configuration().getCharset());
        byte[] valueBytes = ((Integer) value).toString().getBytes(context().configuration().getCharset());
        RocksDbPrimitiveStorage.put(keyBytes, valueBytes);
    }

    public void put(String key, char value) {
        if (isEmpty(key)) return;
        byte[] keyBytes = key.getBytes(context().configuration().getCharset());
        byte[] valueBytes = new byte[]{(byte) value};
        RocksDbPrimitiveStorage.put(keyBytes, valueBytes);
    }

    public void put(String key, double value) {
        if (isEmpty(key)) return;
        byte[] keyBytes = key.getBytes(context().configuration().getCharset());
        byte[] valueBytes = ((Double) value).toString().getBytes(context().configuration().getCharset());
        RocksDbPrimitiveStorage.put(keyBytes, valueBytes);
    }

    public void put(String key, long value) {
        if (isEmpty(key)) return;
        byte[] keyBytes = key.getBytes(context().configuration().getCharset());
        byte[] valueBytes = ((Long) value).toString().getBytes(context().configuration().getCharset());
        RocksDbPrimitiveStorage.put(keyBytes, valueBytes);
    }

    public void put(String key, byte value) {
        if (isEmpty(key)) return;
        byte[] keyBytes = key.getBytes(context().configuration().getCharset());
        byte[] valueBytes = ((Byte) value).toString().getBytes(context().configuration().getCharset());
        RocksDbPrimitiveStorage.put(keyBytes, valueBytes);
    }

    public void put(String key, boolean value) {
        if (isEmpty(key)) return;
        byte[] keyBytes = key.getBytes(context().configuration().getCharset());
        byte[] valueBytes = ((Boolean) value).toString().getBytes(context().configuration().getCharset());
        RocksDbPrimitiveStorage.put(keyBytes, valueBytes);
    }


    public void put(String key, Integer value) {
        if (isEmpty(key)) return;
        byte[] keyBytes = key.getBytes(context().configuration().getCharset());
        byte[] valueBytes = value.toString().getBytes(context().configuration().getCharset());
        RocksDbPrimitiveStorage.put(keyBytes, valueBytes);
    }

    public void put(String key, Character value) {
        if (isEmpty(key)) return;
        byte[] keyBytes = key.getBytes(context().configuration().getCharset());
        byte[] valueBytes = value.toString().getBytes(context().configuration().getCharset());
        RocksDbPrimitiveStorage.put(keyBytes, valueBytes);
    }

    public void put(String key, Double value) {
        if (isEmpty(key)) return;
        byte[] keyBytes = key.getBytes(context().configuration().getCharset());
        byte[] valueBytes = value.toString().getBytes(context().configuration().getCharset());
        RocksDbPrimitiveStorage.put(keyBytes, valueBytes);
    }

    public void put(String key, Long value) {
        if (isEmpty(key)) return;
        byte[] keyBytes = key.getBytes(context().configuration().getCharset());
        byte[] valueBytes = value.toString().getBytes(context().configuration().getCharset());
        RocksDbPrimitiveStorage.put(keyBytes, valueBytes);
    }

    public void put(String key, Byte value) {
        if (isEmpty(key)) return;
        byte[] keyBytes = key.getBytes(context().configuration().getCharset());
        byte[] valueBytes = value.toString().getBytes(context().configuration().getCharset());
        RocksDbPrimitiveStorage.put(keyBytes, valueBytes);
    }

    public void put(String key, Boolean value) {
        if (isEmpty(key)) return;
        byte[] keyBytes = key.getBytes(context().configuration().getCharset());
        byte[] valueBytes = value.toString().getBytes(context().configuration().getCharset());
        RocksDbPrimitiveStorage.put(keyBytes, valueBytes);
    }


    public <T> void add(String key, T value, Function<T, byte[]> mapper) {
        if (isEmpty(mapper)) return;
        if (isEmpty(key)) return;
        if (isNull(value)) return;
        RocksDbPrimitiveStorage.add(key.getBytes(context().configuration().getCharset()), mapper.apply(value));
    }

    public void add(byte[] key, byte[] value) {
        if (isEmpty(key)) return;
        if (isNull(value)) return;
        try {
            if (rocksDbModule().configuration().isLogging()) {
                String loggingValue = isEmpty(value) ? EMPTY_STRING : new String(value, context().configuration().getCharset());
                logger(RocksDbPrimitiveStorage.class)
                        .trace(format(MERGE_OPERATION, new String(key, context().configuration().getCharset()), loggingValue));
            }
            rocksDbModule().state().db().merge(key, value);
        } catch (RocksDBException throwable) {
            throw new RocksDbException(MERGE_ERROR, throwable);
        }
    }

    public void add(String key, String value) {
        if (isEmpty(key)) return;
        if (isNull(value)) return;
        byte[] keyBytes = key.getBytes(context().configuration().getCharset());
        byte[] valueBytes = value.getBytes(context().configuration().getCharset());
        add(keyBytes, valueBytes);
    }

    public void add(String key, int value) {
        byte[] keyBytes = key.getBytes(context().configuration().getCharset());
        byte[] valueBytes = ((Integer) value).toString().getBytes(context().configuration().getCharset());
        add(keyBytes, valueBytes);
    }

    public void add(String key, char value) {
        byte[] keyBytes = key.getBytes(context().configuration().getCharset());
        byte[] valueBytes = new byte[]{(byte) value};
        add(keyBytes, valueBytes);
    }

    public void add(String key, double value) {
        byte[] keyBytes = key.getBytes(context().configuration().getCharset());
        byte[] valueBytes = ((Double) value).toString().getBytes(context().configuration().getCharset());
        add(keyBytes, valueBytes);
    }

    public void add(String key, long value) {
        byte[] keyBytes = key.getBytes(context().configuration().getCharset());
        byte[] valueBytes = ((Long) value).toString().getBytes(context().configuration().getCharset());
        add(keyBytes, valueBytes);
    }

    public void add(String key, byte value) {
        byte[] keyBytes = key.getBytes(context().configuration().getCharset());
        byte[] valueBytes = new byte[]{value};
        add(keyBytes, valueBytes);
    }

    public void add(String key, boolean value) {
        byte[] keyBytes = key.getBytes(context().configuration().getCharset());
        byte[] valueBytes = value ? new byte[]{(byte) 1} : new byte[]{(byte) 0};
        add(keyBytes, valueBytes);
    }

    public void add(String key, Integer value) {
        if (isEmpty(key)) return;
        if (isNull(value)) return;
        byte[] keyBytes = key.getBytes(context().configuration().getCharset());
        byte[] valueBytes = value.toString().getBytes(context().configuration().getCharset());
        add(keyBytes, valueBytes);
    }

    public void add(String key, Character value) {
        if (isEmpty(key)) return;
        if (isNull(value)) return;
        byte[] keyBytes = key.getBytes(context().configuration().getCharset());
        byte[] valueBytes = value.toString().getBytes(context().configuration().getCharset());
        add(keyBytes, valueBytes);
    }

    public void add(String key, Double value) {
        if (isEmpty(key)) return;
        if (isNull(value)) return;
        byte[] keyBytes = key.getBytes(context().configuration().getCharset());
        byte[] valueBytes = value.toString().getBytes(context().configuration().getCharset());
        add(keyBytes, valueBytes);
    }

    public void add(String key, Long value) {
        if (isEmpty(key)) return;
        if (isNull(value)) return;
        byte[] keyBytes = key.getBytes(context().configuration().getCharset());
        byte[] valueBytes = value.toString().getBytes(context().configuration().getCharset());
        add(keyBytes, valueBytes);
    }

    public void add(String key, Byte value) {
        if (isEmpty(key)) return;
        if (isNull(value)) return;
        byte[] keyBytes = key.getBytes(context().configuration().getCharset());
        byte[] valueBytes = value.toString().getBytes(context().configuration().getCharset());
        add(keyBytes, valueBytes);
    }

    public void add(String key, Boolean value) {
        if (isEmpty(key)) return;
        if (isNull(value)) return;
        byte[] keyBytes = key.getBytes(context().configuration().getCharset());
        byte[] valueBytes = value ? new byte[]{(byte) 1} : new byte[]{(byte) 0};
        add(keyBytes, valueBytes);
    }


    public <T> Optional<T> get(String key, Function<byte[], T> mapper) {
        if (isEmpty(key)) return empty();
        if (isEmpty(mapper)) return empty();
        return ofNullable(mapper.apply(RocksDbPrimitiveStorage.get(key)));
    }

    public byte[] get(String key) {
        if (isEmpty(key)) return EMPTY_BYTES;
        return get(key.getBytes(context().configuration().getCharset()));
    }

    public byte[] get(byte[] key) {
        if (isEmpty(key)) return EMPTY_BYTES;
        try {
            if (rocksDbModule().configuration().isLogging()) {
                logger(RocksDbPrimitiveStorage.class)
                        .trace(format(GET_START_OPERATION, new String(key, context().configuration().getCharset())));
            }
            byte[] valueBytes = rocksDbModule().state().db().get(key);
            if (rocksDbModule().configuration().isLogging()) {
                String value = isEmpty(valueBytes) ? EMPTY_STRING : new String(valueBytes, context().configuration().getCharset());
                logger(RocksDbPrimitiveStorage.class)
                        .trace(format(GET_END_OPERATION, value, new String(key, context().configuration().getCharset())));
            }
            return valueBytes;
        } catch (RocksDBException throwable) {
            throw new RocksDbException(GET_ERROR, throwable);
        }
    }

    public Optional<String> getString(String key) {
        if (isEmpty(key)) return empty();
        byte[] keyBytes = key.getBytes(context().configuration().getCharset());
        byte[] bytes = RocksDbPrimitiveStorage.get(keyBytes);
        if (isEmpty(bytes)) return empty();
        return of(new String(bytes, context().configuration().getCharset()));
    }

    public Optional<Integer> getInt(String key) {
        return getString(key).map(Integer::valueOf);
    }

    public Optional<Character> getChar(String key) {
        return getString(key).map(value -> value.charAt(0));
    }

    public Optional<Double> getDouble(String key) {
        return getString(key).map(Double::valueOf);
    }

    public Optional<Long> getLong(String key) {
        return getString(key).map(Long::valueOf);
    }

    public Optional<Byte> getByte(String key) {
        return getString(key).map(Byte::valueOf);
    }

    public Optional<Boolean> getBoolean(String key) {
        return getString(key).map(Boolean::valueOf);
    }


    public void delete(String key) {
        if (isEmpty(key)) return;
        byte[] keyBytes = key.getBytes(context().configuration().getCharset());
        RocksDbPrimitiveStorage.delete(keyBytes);
    }

    public void delete(byte[] key) {
        if (isEmpty(key)) return;
        try {
            if (rocksDbModule().configuration().isLogging()) {
                logger(RocksDbPrimitiveStorage.class)
                        .trace(format(DELETE_OPERATION, new String(key, context().configuration().getCharset())));
            }
            rocksDbModule().state().db().delete(key);
        } catch (RocksDBException throwable) {
            throw new RocksDbException(DELETE_ERROR, throwable);
        }
    }

    public void deleteByPrefix(String prefix) {
        if (rocksDbModule().configuration().isLogging()) {
            logger(RocksDbPrimitiveStorage.class)
                    .trace(format(DELETE_BY_PREFIX_OPERATION, prefix));
        }

        RocksIterator iterator = rocksDbModule().state().db().newIterator();
        iterator.seek(prefix.getBytes(context().configuration().getCharset()));
        while (iterator.isValid()) {
            byte[] keyBytes = iterator.key();
            iterator.next();
            if (isEmpty(keyBytes)) continue;
            if (!new String(keyBytes, context().configuration().getCharset()).startsWith(prefix)) continue;
            delete(keyBytes);
        }
    }


    public boolean exists(String key) {
        return isNotEmpty(get(key));
    }

    public boolean exists(byte[] key) {
        return isNotEmpty(get(key));
    }
}
