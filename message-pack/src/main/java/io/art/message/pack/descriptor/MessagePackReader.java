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

package io.art.message.pack.descriptor;

import io.art.message.pack.exception.MessagePackException;
import io.art.value.builder.*;
import io.art.value.descriptor.Reader;
import io.art.value.immutable.ArrayValue;
import io.art.value.immutable.Value;
import org.msgpack.core.*;
import org.msgpack.value.*;
import static io.art.message.pack.constants.MessagePackConstants.ExceptionMessages.*;
import static io.art.value.factory.ArrayValueFactory.*;
import static io.art.value.factory.PrimitivesFactory.*;
import static io.art.value.immutable.BinaryValue.*;
import static io.art.value.immutable.Entity.*;
import static java.text.MessageFormat.*;
import static java.util.Objects.*;
import static org.msgpack.core.MessagePack.*;
import java.io.*;
import java.nio.*;
import java.util.*;


public class MessagePackReader implements Reader<Value> {
    public Value read(InputStream inputStream) {
        try (MessageUnpacker unpacker = newDefaultUnpacker(inputStream)) {
            return read(unpacker.unpackValue());
        } catch (Throwable throwable) {
            throw new MessagePackException(throwable);
        }
    }

    @Override
    public Value read(ByteBuffer nioBuffer) {
        try (MessageUnpacker unpacker = newDefaultUnpacker(nioBuffer)) {
            return read(unpacker.unpackValue());
        } catch (Throwable throwable) {
            throw new MessagePackException(throwable);
        }
    }

    public Value read(org.msgpack.value.Value value) {
        if (isNull(value) || value.isNilValue()) return null;
        switch (value.getValueType()) {
            case NIL:
            case EXTENSION:
                return null;
            case BOOLEAN:
                return boolPrimitive(value.asBooleanValue().getBoolean());
            case INTEGER:
                IntegerValue integerValue = value.asIntegerValue();
                if (integerValue.isInByteRange()) {
                    return bytePrimitive(integerValue.toByte());
                }
                if (integerValue.isInIntRange()) {
                    return intPrimitive(integerValue.toInt());
                }
                if (integerValue.isInLongRange()) {
                    return longPrimitive(integerValue.toLong());
                }
            case FLOAT:
                return doublePrimitive(value.asFloatValue().toDouble());
            case STRING:
                return stringPrimitive(value.asStringValue().toString());
            case BINARY:
                return binary(value.asBinaryValue().asByteArray());
            case ARRAY:
                return readArray(value.asArrayValue());
            case MAP:
                return readMap(value.asMapValue());
        }
        return null;
    }


    private Value readMap(org.msgpack.value.MapValue map) {
        if (isNull(map) || map.isNilValue()) return null;
        EntityBuilder entityBuilder = entityBuilder();
        for (Map.Entry<org.msgpack.value.Value, org.msgpack.value.Value> entry : map.entrySet()) {
            org.msgpack.value.Value key = entry.getKey();
            if (isNull(key) || key.isNilValue()) continue;
            org.msgpack.value.Value value = entry.getValue();
            if (isNull(value) || value.isNilValue()) continue;
            switch (key.getValueType()) {
                case NIL:
                case BINARY:
                case ARRAY:
                case MAP:
                case EXTENSION:
                    break;
                case BOOLEAN:
                    entityBuilder.lazyPut(key.asBooleanValue().getBoolean(), () -> readEntityField(value));
                    break;
                case INTEGER:
                    IntegerValue integerValue = key.asIntegerValue();
                    if (integerValue.isInByteRange()) {
                        entityBuilder.lazyPut(bytePrimitive(integerValue.toByte()), () -> readEntityField(value));
                        break;
                    }
                    if (integerValue.isInIntRange()) {
                        entityBuilder.lazyPut(intPrimitive(integerValue.toInt()), () -> readEntityField(value));
                        break;
                    }
                    if (integerValue.isInLongRange()) {
                        entityBuilder.lazyPut(longPrimitive(integerValue.toLong()), () -> readEntityField(value));
                        break;
                    }
                case FLOAT:
                    entityBuilder.lazyPut(key.asFloatValue().toFloat(), () -> readEntityField(value));
                    break;
                case STRING:
                    entityBuilder.lazyPut(key.asStringValue().toString(), () -> readEntityField(value));
                    break;
            }
        }
        return entityBuilder.build();
    }

    private Value readEntityField(org.msgpack.value.Value value) {
        switch (value.getValueType()) {
            case BOOLEAN:
                return boolPrimitive(value.asBooleanValue().getBoolean());
            case INTEGER:
                IntegerValue integerValue = value.asIntegerValue();
                if (integerValue.isInByteRange()) {
                    return bytePrimitive(integerValue.asByte());
                }
                if (integerValue.isInIntRange()) {
                    return intPrimitive(integerValue.asInt());
                }
                if (integerValue.isInLongRange()) {
                    return longPrimitive(integerValue.asLong());
                }
            case FLOAT:
                return floatPrimitive(value.asFloatValue().toFloat());
            case STRING:
                return stringPrimitive(value.asStringValue().asString());
            case BINARY:
                return binary(value.asBinaryValue().asByteArray());
            case ARRAY:
                return readArray(value.asArrayValue());
            case MAP:
                return readMap(value.asMapValue());
            case EXTENSION:
            case NIL:
                return null;
        }
        throw new MessagePackException(format(VALUE_TYPE_NOT_SUPPORTED, value.getValueType()));
    }

    private ArrayValue readArray(org.msgpack.value.ArrayValue array) {
        if (isNull(array) || array.isNilValue()) return null;
        return array(index -> read(array.get(index)), array::size);
    }
}
