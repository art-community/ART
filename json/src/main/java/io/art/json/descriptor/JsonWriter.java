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

package io.art.json.descriptor;

import com.fasterxml.jackson.core.*;
import io.art.json.exception.*;
import io.art.value.descriptor.Writer;
import io.art.value.immutable.Value;
import io.art.value.immutable.*;
import io.netty.buffer.*;
import lombok.*;
import static io.art.core.caster.Caster.*;
import static io.art.value.immutable.Value.*;
import java.io.*;
import java.nio.*;
import java.nio.charset.*;
import java.util.*;

@AllArgsConstructor
public class JsonWriter implements Writer<Value> {
    private final JsonFactory jsonFactory;

    @Override
    public void write(Value value, ByteBuffer buffer) {
        write(value, buffer, JsonException::new);
    }

    @Override
    public void write(Value value, ByteBuf buffer) {
        write(value, buffer, JsonException::new);
    }

    @Override
    public void write(Value value, OutputStream outputStream, Charset charset) {
        if (valueIsNull(value)) {
            return;
        }

        try (JsonGenerator generator = jsonFactory.createGenerator(new OutputStreamWriter(outputStream, charset))) {
            switch (value.getType()) {
                case ENTITY:
                    writeJsonEntity(generator, asEntity(value));
                    return;
                case ARRAY:
                    writeArray(generator, asArray(value));
                    return;
                case BINARY:
                    generator.writeBinary(asBinary(value).getContent());
                    return;
                case STRING:
                    generator.writeString(asPrimitive(value).getString());
                    return;
                case LONG:
                    generator.writeNumber(asPrimitive(value).getLong());
                    return;
                case DOUBLE:
                    generator.writeNumber(asPrimitive(value).getDouble());
                    return;
                case FLOAT:
                    generator.writeNumber(asPrimitive(value).getFloat());
                    return;
                case INT:
                    generator.writeNumber(asPrimitive(value).getInt());
                    return;
                case BOOL:
                    generator.writeBoolean(asPrimitive(value).getBool());
                    return;
                case BYTE:
                    generator.writeNumber(asPrimitive(value).getByte());
            }
        } catch (IOException ioException) {
            throw new JsonException(ioException);
        }
    }

    private static void writeJsonEntity(JsonGenerator generator, Entity entity) throws IOException {
        if (valueIsNull(entity)) return;
        generator.writeStartObject();
        writeJsonFields(generator, entity);
        generator.writeEndObject();
    }

    private static void writeJsonEntity(JsonGenerator jsonGenerator, String name, Entity entity) throws IOException {
        if (valueIsNull(entity)) return;
        jsonGenerator.writeObjectFieldStart(name);
        writeJsonFields(jsonGenerator, entity);
        jsonGenerator.writeEndObject();
    }

    private static void writeJsonFields(JsonGenerator generator, Entity entity) throws IOException {
        Set<Primitive> keys = entity.asMap().keySet();
        for (Primitive key : keys) {
            if (valueIsNull(key)) continue;
            Value value = entity.get(key);
            if (valueIsNull(value)) continue;
            writeField(generator, key.getString(), value);
        }
    }

    private static void writeArray(JsonGenerator jsonGenerator, String fieldName, ArrayValue array) throws IOException {
        if (valueIsNull(array)) return;
        jsonGenerator.writeArrayFieldStart(fieldName);
        for (int index = 0; index < array.size(); index++) {
            Value value = array.get(index);
            if (valueIsNull(value)) continue;
            writeArrayElement(jsonGenerator, value);
        }
        jsonGenerator.writeEndArray();
    }

    private static void writeArray(JsonGenerator jsonGenerator, ArrayValue array) throws IOException {
        if (valueIsNull(array)) return;
        jsonGenerator.writeStartArray();
        for (int index = 0; index < array.size(); index++) {
            Value value = array.get(index);
            if (valueIsNull(value)) continue;
            writeArrayElement(jsonGenerator, value);
        }
        jsonGenerator.writeEndArray();
    }


    private static void writeField(JsonGenerator jsonGenerator, String name, Value value) throws IOException {
        if (valueIsNull(value)) return;
        switch (value.getType()) {
            case ENTITY:
                writeJsonEntity(jsonGenerator, name, asEntity(value));
                return;
            case ARRAY:
                writeArray(jsonGenerator, name, asArray(value));
                return;
            case BINARY:
                jsonGenerator.writeBinaryField(name, asBinary(value).getContent());
                return;
            case STRING:
            case INT:
            case DOUBLE:
            case LONG:
            case BOOL:
            case BYTE:
            case FLOAT:
                writeField(jsonGenerator, name, asPrimitive(value));
        }
    }

    private static void writeField(JsonGenerator jsonGenerator, String name, Primitive value) throws IOException {
        if (valueIsNull(value)) return;
        switch (value.getType()) {
            case STRING:
                jsonGenerator.writeStringField(name, value.getString());
                return;
            case INT:
                jsonGenerator.writeNumberField(name, value.getInt());
                return;
            case DOUBLE:
                jsonGenerator.writeNumberField(name, value.getDouble());
                return;
            case LONG:
                jsonGenerator.writeNumberField(name, value.getLong());
                return;
            case BOOL:
                jsonGenerator.writeBooleanField(name, value.getBool());
                return;
            case BYTE:
                jsonGenerator.writeNumberField(name, value.getByte());
                return;
            case FLOAT:
                jsonGenerator.writeNumberField(name, value.getFloat());
        }
    }


    private static void writeArrayElement(JsonGenerator jsonGenerator, Value value) throws IOException {
        if (valueIsNull(value)) return;
        switch (value.getType()) {
            case ARRAY:
                writeArray(jsonGenerator, asArray(cast(value)));
                return;
            case ENTITY:
                writeJsonEntity(jsonGenerator, asEntity(cast(value)));
                return;
            case BINARY:
                jsonGenerator.writeBinary(asBinary(value).getContent());
                return;
            case STRING:
                jsonGenerator.writeString(asPrimitive(value).getString());
                return;
            case INT:
                jsonGenerator.writeNumber(asPrimitive(value).getInt());
                return;
            case BOOL:
                jsonGenerator.writeBoolean(asPrimitive(value).getBool());
                return;
            case DOUBLE:
                jsonGenerator.writeNumber(asPrimitive(value).getDouble());
                return;
            case LONG:
                jsonGenerator.writeNumber(asPrimitive(value).getLong());
                return;
            case BYTE:
                jsonGenerator.writeNumber(asPrimitive(value).getByte());
                return;
            case FLOAT:
                jsonGenerator.writeNumber(asPrimitive(value).getFloat());
        }
    }
}
