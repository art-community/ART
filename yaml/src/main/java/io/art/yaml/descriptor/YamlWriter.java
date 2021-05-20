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

package io.art.yaml.descriptor;

import com.fasterxml.jackson.dataformat.yaml.*;
import io.art.value.descriptor.Writer;
import io.art.value.immutable.Value;
import io.art.value.immutable.*;
import io.art.yaml.exception.*;
import io.netty.buffer.*;
import lombok.*;
import static io.art.core.caster.Caster.*;
import static io.art.value.immutable.Value.*;
import java.io.*;
import java.nio.*;
import java.nio.charset.*;
import java.util.*;

@AllArgsConstructor
public class YamlWriter implements Writer<Value> {
    private final YAMLFactory yamlFactory;

    @Override
    public void write(Value value, ByteBuffer buffer) {
        write(value, buffer, YamlException::new);
    }

    @Override
    public void write(Value value, ByteBuf buffer) {
        write(value, buffer, YamlException::new);
    }

    @Override
    public void write(Value value, OutputStream outputStream, Charset charset) {
        if (valueIsNull(value)) {
            return;
        }
        try (YAMLGenerator generator = yamlFactory.createGenerator(new OutputStreamWriter(outputStream, charset))) {
            switch (value.getType()) {
                case ENTITY:
                    writeYamlEntity(generator, asEntity(value));
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
            throw new YamlException(ioException);
        }
    }


    private static void writeYamlEntity(YAMLGenerator generator, Entity entity) throws IOException {
        if (valueIsNull(entity)) return;
        generator.writeStartObject();
        writeYamlFields(generator, entity);
        generator.writeEndObject();
    }

    private static void writeYamlEntity(YAMLGenerator yamlGenerator, String name, Entity entity) throws IOException {
        if (valueIsNull(entity)) return;
        yamlGenerator.writeObjectFieldStart(name);
        writeYamlFields(yamlGenerator, entity);
        yamlGenerator.writeEndObject();
    }

    private static void writeYamlFields(YAMLGenerator generator, Entity entity) throws IOException {
        Set<Primitive> keys = entity.asMap().keySet();
        for (Primitive key : keys) {
            if (valueIsNull(key)) continue;
            Value value = entity.get(key);
            if (valueIsNull(value)) continue;
            writeField(generator, key.getString(), value);
        }
    }

    private static void writeArray(YAMLGenerator yamlGenerator, String fieldName, ArrayValue array) throws IOException {
        if (valueIsNull(array)) return;
        yamlGenerator.writeArrayFieldStart(fieldName);
        for (int index = 0; index < array.size(); index++) {
            Value value = array.get(index);
            if (valueIsNull(value)) continue;
            writeArrayElement(yamlGenerator, value);
        }
        yamlGenerator.writeEndArray();
    }

    private static void writeArray(YAMLGenerator yamlGenerator, ArrayValue array) throws IOException {
        if (valueIsNull(array)) return;
        yamlGenerator.writeStartArray();
        for (int index = 0; index < array.size(); index++) {
            Value value = array.get(index);
            if (valueIsNull(value)) continue;
            writeArrayElement(yamlGenerator, value);
        }
        yamlGenerator.writeEndArray();
    }


    private static void writeField(YAMLGenerator yamlGenerator, String name, Value value) throws IOException {
        if (valueIsNull(value)) return;
        switch (value.getType()) {
            case ENTITY:
                writeYamlEntity(yamlGenerator, name, asEntity(value));
                return;
            case ARRAY:
                writeArray(yamlGenerator, name, asArray(value));
                return;
            case BINARY:
                yamlGenerator.writeBinaryField(name, asBinary(value).getContent());
                return;
            case STRING:
            case INT:
            case DOUBLE:
            case LONG:
            case BOOL:
            case BYTE:
            case FLOAT:
                writeField(yamlGenerator, name, asPrimitive(value));
        }
    }

    private static void writeField(YAMLGenerator yamlGenerator, String name, Primitive value) throws IOException {
        if (valueIsNull(value)) return;
        switch (value.getType()) {
            case STRING:
                yamlGenerator.writeStringField(name, value.getString());
                return;
            case INT:
                yamlGenerator.writeNumberField(name, value.getInt());
                return;
            case DOUBLE:
                yamlGenerator.writeNumberField(name, value.getDouble());
                return;
            case LONG:
                yamlGenerator.writeNumberField(name, value.getLong());
                return;
            case BOOL:
                yamlGenerator.writeBooleanField(name, value.getBool());
                return;
            case BYTE:
                yamlGenerator.writeNumberField(name, value.getByte());
                return;
            case FLOAT:
                yamlGenerator.writeNumberField(name, value.getFloat());
        }
    }


    private static void writeArrayElement(YAMLGenerator yamlGenerator, Value value) throws IOException {
        if (valueIsNull(value)) return;
        switch (value.getType()) {
            case ARRAY:
                writeArray(yamlGenerator, asArray(cast(value)));
                return;
            case ENTITY:
                writeYamlEntity(yamlGenerator, asEntity(cast(value)));
                return;
            case BINARY:
                yamlGenerator.writeBinary(asBinary(value).getContent());
                return;
            case STRING:
                yamlGenerator.writeString(asPrimitive(value).getString());
                return;
            case INT:
                yamlGenerator.writeNumber(asPrimitive(value).getInt());
                return;
            case BOOL:
                yamlGenerator.writeBoolean(asPrimitive(value).getBool());
                return;
            case DOUBLE:
                yamlGenerator.writeNumber(asPrimitive(value).getDouble());
                return;
            case LONG:
                yamlGenerator.writeNumber(asPrimitive(value).getLong());
                return;
            case BYTE:
                yamlGenerator.writeNumber(asPrimitive(value).getByte());
                return;
            case FLOAT:
                yamlGenerator.writeNumber(asPrimitive(value).getFloat());
        }
    }
}
