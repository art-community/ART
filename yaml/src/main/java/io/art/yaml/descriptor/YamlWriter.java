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
import io.art.core.extensions.*;
import io.art.core.stream.*;
import io.art.value.immutable.*;
import io.art.yaml.exception.*;
import lombok.experimental.*;
import static io.art.core.caster.Caster.*;
import static io.art.core.constants.BufferConstants.*;
import static io.art.core.context.Context.*;
import static io.art.core.extensions.FileExtensions.*;
import static io.art.value.immutable.Value.*;
import static io.art.yaml.module.YamlModule.*;
import static java.nio.ByteBuffer.*;
import java.io.*;
import java.nio.*;
import java.nio.file.*;
import java.util.*;

@UtilityClass
public class YamlWriter {
    public static byte[] writeYamlToBytes(Value value) {
        ByteBuffer byteBuffer = allocate(DEFAULT_BUFFER_SIZE);
        try {
            try (NioByteBufferOutputStream outputStream = new NioByteBufferOutputStream(byteBuffer)) {
                writeYaml(value, outputStream);
            } catch (IOException ioException) {
                throw new YamlException(ioException);
            }
            return NioBufferExtensions.toByteArray(byteBuffer);
        } finally {
            byteBuffer.clear();
        }
    }

    public static void writeYaml(Value value, Path path) {
        try (OutputStream outputStream = fileOutputStream(path)) {
            writeYaml(value, outputStream);
        } catch (IOException ioException) {
            throw new YamlException(ioException);
        }
    }

    public static String writeYaml(Value value) {
        return new String(writeYamlToBytes(value), context().configuration().getCharset());
    }

    public static void writeYaml(Value value, OutputStream outputStream) {
        writeYaml(yamlModule().configuration().getObjectMapper().getFactory(), value, outputStream);
    }

    public static void writeYaml(YAMLFactory yamlFactory, Value value, OutputStream outputStream) {
        if (valueIsNull(value)) {
            return;
        }
        try (YAMLGenerator generator = yamlFactory.createGenerator(outputStream)) {
            switch (value.getType()) {
                case ENTITY:
                    writeYamlEntity(generator, asEntity(value));
                    return;
                case ARRAY:
                    writeArray(generator, asArray(value));
                    return;
                case BINARY:
                    outputStream.write(Arrays.toString(asBinary(value).getContent()).getBytes());
                    return;
                case STRING:
                    outputStream.write(asPrimitive(value).getString().getBytes());
                    return;
                case LONG:
                    outputStream.write(asPrimitive(value).getLong().toString().getBytes());
                    return;
                case DOUBLE:
                    outputStream.write(asPrimitive(value).getDouble().toString().getBytes());
                    return;
                case FLOAT:
                    outputStream.write(asPrimitive(value).getFloat().toString().getBytes());
                    return;
                case INT:
                    outputStream.write(asPrimitive(value).getInt().toString().getBytes());
                    return;
                case BOOL:
                    outputStream.write(asPrimitive(value).getBool().toString().getBytes());
                    return;
                case BYTE:
                    outputStream.write(asPrimitive(value).getByte().toString().getBytes());
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
