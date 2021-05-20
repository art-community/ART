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
import io.art.value.descriptor.Writer;
import io.art.value.immutable.*;
import io.netty.buffer.*;
import org.msgpack.core.*;
import static io.art.message.pack.constants.MessagePackConstants.ExceptionMessages.*;
import static io.art.value.immutable.Value.*;
import static java.nio.channels.Channels.*;
import static java.text.MessageFormat.*;
import static java.util.Objects.*;
import static org.msgpack.core.MessagePack.*;
import static org.msgpack.value.ValueFactory.*;
import java.io.*;
import java.nio.*;
import java.nio.charset.*;
import java.util.*;

public class MessagePackWriter implements Writer<Value> {
    @Override
    public void write(io.art.value.immutable.Value value, ByteBuffer buffer) {
        write(value, buffer, MessagePackException::new);
    }

    @Override
    public void write(Value value, ByteBuf buffer) {
        write(value, buffer, MessagePackException::new);
    }

    @Override
    public void write(Value value, OutputStream outputStream, Charset charset) {
        if (Value.valueIsNull(value)) {
            return;
        }
        try (MessagePacker packer = newDefaultPacker(newChannel(outputStream))) {
            packer.packValue(write(value));
        } catch (Throwable throwable) {
            throw new MessagePackException(throwable);
        }
    }

    public org.msgpack.value.Value write(Value value) {
        if (valueIsNull(value)) return null;
        if (isPrimitive(value)) return writePrimitive(asPrimitive(value));
        switch (value.getType()) {
            case ENTITY:
                return writeEntity(asEntity(value));
            case BINARY:
                return newBinary(asBinary(value).getContent());
            case ARRAY:
                return writeArray(asArray(value));
        }
        return null;
    }


    private org.msgpack.value.Value writePrimitive(Primitive primitive) {
        if (valueIsNull(primitive)) return null;
        switch (primitive.getPrimitiveType()) {
            case STRING:
                return newString(primitive.getString());
            case LONG:
                return newInteger(primitive.getLong());
            case INT:
                return newInteger(primitive.getInt());
            case BYTE:
                return newInteger(primitive.getByte());
            case DOUBLE:
                return newFloat(primitive.getDouble());
            case FLOAT:
                return newFloat(primitive.getFloat());
            case BOOL:
                return newBoolean(primitive.getBool());
        }
        throw new MessagePackException(format(VALUE_TYPE_NOT_SUPPORTED, primitive.getType()));
    }

    private org.msgpack.value.Value writeArray(ArrayValue array) {
        if (valueIsNull(array)) return null;
        org.msgpack.value.Value[] values = new org.msgpack.value.Value[array.size()];
        List<Value> list = array.asList();
        int newArrayIndex = 0;
        for (Value value : list) {
            org.msgpack.value.Value element = write(value);
            if (isNull(element)) continue;
            values[newArrayIndex++] = element;
        }
        return newArray(Arrays.copyOf(values, newArrayIndex), true);
    }

    private org.msgpack.value.Value writeEntity(Entity entity) {
        if (valueIsNull(entity)) return null;
        MapBuilder mapBuilder = newMapBuilder();
        Set<Primitive> keys = entity.asMap().keySet();
        for (Primitive key : keys) {
            if (valueIsNull(key)) continue;
            Value value = entity.get(key);
            if (valueIsNull(value)) continue;
            writeEntityField(mapBuilder, key, value);
        }
        return mapBuilder.build();
    }

    private void writeEntityField(MapBuilder mapBuilder, Primitive key, Value value) {
        switch (key.getType()) {
            case STRING:
                mapBuilder.put(newString(key.getString()), write(value));
                return;
            case INT:
                mapBuilder.put(newInteger(key.getInt()), write(value));
                return;
            case BOOL:
                mapBuilder.put(newBoolean(key.getBool()), write(value));
                return;
            case LONG:
                mapBuilder.put(newInteger(key.getLong()), write(value));
                return;
            case BYTE:
                mapBuilder.put(newInteger(key.getByte()), write(value));
                return;
            case DOUBLE:
                mapBuilder.put(newFloat(key.getDouble()), write(value));
                return;
            case FLOAT:
                mapBuilder.put(newFloat(key.getFloat()), write(value));
        }
    }
}
