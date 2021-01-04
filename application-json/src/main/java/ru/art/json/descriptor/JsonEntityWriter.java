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

package ru.art.json.descriptor;

import com.fasterxml.jackson.core.*;
import lombok.experimental.*;
import ru.art.core.checker.*;
import ru.art.entity.*;
import ru.art.entity.constants.*;
import ru.art.entity.constants.ValueType.*;
import ru.art.json.exception.*;
import static java.util.Objects.*;
import static ru.art.core.caster.Caster.*;
import static ru.art.core.constants.StringConstants.*;
import static ru.art.core.context.Context.*;
import static ru.art.core.extension.FileExtensions.*;
import static ru.art.core.extension.StringExtensions.*;
import static ru.art.entity.Value.*;
import static ru.art.json.constants.JsonMappingExceptionMessages.*;
import static ru.art.json.module.JsonModule.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;

@UtilityClass
public class JsonEntityWriter {
    public static byte[] writeJsonToBytes(Value value) {
        return writeJson(value).getBytes(contextConfiguration().getCharset());
    }

    public static void writeJson(Value value, OutputStream outputStream) {
        if (isNull(outputStream)) {
            return;
        }
        try {
            outputStream.write(writeJson(value).getBytes(contextConfiguration().getCharset()));
        } catch (IOException ioException) {
            throw new JsonMappingException(ioException);
        }
    }

    public static void writeJson(Value value, Path path) {
        writeFileQuietly(path, writeJson(value));
    }

    public static String writeJson(Value value) {
        return writeJson(jsonModule().getObjectMapper().getFactory(), value, false);
    }

    public static String writeJson(JsonFactory jsonFactory, Value value, boolean prettyOutput) {
        if (isNull(jsonFactory)) throw new JsonMappingException(JSON_FACTORY_IS_NULL);
        if (isNull(value)) {
            return BRACES;
        }
        if (isEmpty(value)) {
            if (isCollection(value)) {
                return SQUARE_BRACES;
            }
            return BRACES;
        }
        StringWriter stringWriter = new StringWriter();
        try (JsonGenerator generator = jsonFactory.createGenerator(stringWriter)) {
            if (prettyOutput) {
                generator.useDefaultPrettyPrinter();
            }
            switch (value.getType()) {
                case ENTITY:
                    writeJsonEntity(generator, asEntity(value));
                    break;
                case COLLECTION:
                    writeArray(generator, asCollection(value));
                    break;
                case STRING:
                    return emptyIfNull(asPrimitive(value).getString());
                case LONG:
                    return emptyIfNull(asPrimitive(value).getLong());
                case DOUBLE:
                    return emptyIfNull(asPrimitive(value).getDouble());
                case FLOAT:
                    return emptyIfNull(asPrimitive(value).getFloat());
                case INT:
                    return emptyIfNull(asPrimitive(value).getInt());
                case BOOL:
                    return emptyIfNull(asPrimitive(value).getBool());
                case BYTE:
                    return emptyIfNull(asPrimitive(value).getByte());
                case STRING_PARAMETERS_MAP:
                    writeStringParameters(generator, asStringParametersMap(value));
                    break;
                case MAP:
                    writeJsonMap(generator, asMap(value));
            }
        } catch (IOException ioException) {
            throw new JsonMappingException(ioException);
        }
        return stringWriter.toString();
    }


    private static void writeJsonMap(JsonGenerator generator, MapValue map) {
        if (isNull(map)) return;
        map.getElements()
                .entrySet()
                .stream()
                .filter(entry -> isPrimitive(entry.getKey()))
                .forEach(entry -> writeJsonMapEntry(generator, entry));
    }

    private static void writeJsonMap(JsonGenerator jsonGenerator, String name, MapValue mapValue) {
        if (isNull(mapValue)) return;
        try {
            jsonGenerator.writeObjectFieldStart(name);
            writeJsonMap(jsonGenerator, mapValue);
            jsonGenerator.writeEndObject();
        } catch (IOException ioException) {
            throw new JsonMappingException(ioException);
        }
    }


    private static void writeJsonMapEntry(JsonGenerator generator, Map.Entry<? extends Value, ? extends Value> entry) {
        try {
            writeField(generator, emptyIfNull(entry.getKey()), entry.getValue());
        } catch (IOException ioException) {
            throw new JsonMappingException(ioException);
        }
    }

    private static void writeJsonEntity(JsonGenerator generator, Entity entity) throws IOException {
        if (isNull(entity)) return;
        generator.writeStartObject();
        Map<String, ? extends Value> fields = getEntityFields(entity);
        for (String field : fields.keySet()) {
            writeField(generator, field, fields.get(field));
        }
        generator.writeEndObject();
    }

    private static void writeJsonEntity(JsonGenerator jsonGenerator, String name, Entity entity) throws IOException {
        if (isNull(entity)) return;
        jsonGenerator.writeObjectFieldStart(name);
        Map<String, ? extends Value> fields = getEntityFields(entity);
        for (String field : fields.keySet()) {
            writeField(jsonGenerator, field, fields.get(field));
        }
        jsonGenerator.writeEndObject();
    }

    private static void writeArray(JsonGenerator jsonGenerator, String fieldName, CollectionValue<?> array) throws IOException {
        if (isNull(array)) return;
        jsonGenerator.writeArrayFieldStart(fieldName);
        switch (array.getCollectionMode()) {
            case PRIMITIVE_ARRAY:
                writePrimitiveElements(jsonGenerator, array);
                break;
            case COLLECTION:
                writeCollectionElements(jsonGenerator, array);
                break;
        }
        jsonGenerator.writeEndArray();
    }

    private static void writeArray(JsonGenerator jsonGenerator, CollectionValue<?> array) throws IOException {
        if (isNull(array)) return;
        jsonGenerator.writeStartArray();
        switch (array.getCollectionMode()) {
            case PRIMITIVE_ARRAY:
                writePrimitiveElements(jsonGenerator, array);
                break;
            case COLLECTION:
                writeCollectionElements(jsonGenerator, array);
                break;
        }
        jsonGenerator.writeEndArray();
    }


    private static void writeField(JsonGenerator jsonGenerator, String name, Value value) throws IOException {
        if (isNull(value)) return;
        switch (value.getType()) {
            case ENTITY:
                writeJsonEntity(jsonGenerator, name, asEntity(value));
                return;
            case COLLECTION:
                writeArray(jsonGenerator, name, asCollection(value));
                return;
            case MAP:
                writeJsonMap(jsonGenerator, name, asMap(value));
                return;
            case STRING_PARAMETERS_MAP:
                writeStringParameters(jsonGenerator, name, asStringParametersMap(value));
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
        if (isEmpty(value) || CheckerForEmptiness.isEmpty(value.getValue())) return;
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


    private static void writeCollectionValue(JsonGenerator jsonGenerator, CollectionElementsType type, Object value) throws IOException {
        if (isNull(value)) return;
        switch (type) {
            case COLLECTION:
                writeArray(jsonGenerator, asCollection(cast(value)));
                return;
            case ENTITY:
                writeJsonEntity(jsonGenerator, asEntity(cast(value)));
                return;
            case STRING:
                jsonGenerator.writeString((String) value);
                return;
            case INT:
                jsonGenerator.writeNumber((Integer) value);
                return;
            case BOOL:
                jsonGenerator.writeBoolean((Boolean) value);
                return;
            case DOUBLE:
                jsonGenerator.writeNumber((Double) value);
                return;
            case LONG:
                jsonGenerator.writeNumber((Long) value);
                return;
            case BYTE:
                jsonGenerator.writeNumber((Byte) value);
                return;
            case FLOAT:
                jsonGenerator.writeNumber((Float) value);
                return;
            case VALUE:
                Value valueObject = (Value) value;
                writeCollectionValue(jsonGenerator, valueObject.getType(), valueObject);
                return;
            case STRING_PARAMETERS_MAP:
                writeStringParameters(jsonGenerator, asStringParametersMap(cast(value)));
        }
    }


    private static void writeCollectionValue(JsonGenerator jsonGenerator, ValueType type, Value value) throws IOException {
        if (isNull(value)) return;
        switch (type) {
            case COLLECTION:
                writeArray(jsonGenerator, asCollection(cast(value)));
                return;
            case ENTITY:
                writeJsonEntity(jsonGenerator, asEntity(cast(value)));
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
            case STRING_PARAMETERS_MAP:
                writeStringParameters(jsonGenerator, asStringParametersMap(cast(value)));
        }
    }

    private static Map<String, ? extends Value> getEntityFields(Entity entity) {
        return entity.getFields();
    }


    private static void writeCollectionElements(JsonGenerator jsonGenerator, CollectionValue<?> array) throws IOException {
        CollectionElementsType valueType = array.getElementsType();
        for (Object value : array.getElements()) {
            writeCollectionValue(jsonGenerator, valueType, value);
        }
    }

    private static void writePrimitiveElements(JsonGenerator jsonGenerator, CollectionValue<?> array) throws IOException {
        CollectionElementsType valueType = array.getElementsType();
        switch (valueType) {
            case INT:
                for (int value : array.getIntArray()) {
                    jsonGenerator.writeNumber(value);
                }
                return;
            case BOOL:
                for (boolean value : array.getBoolArray()) {
                    jsonGenerator.writeBoolean(value);
                }
                return;
            case DOUBLE:
                for (double value : array.getDoubleArray()) {
                    jsonGenerator.writeNumber(value);
                }
                return;
            case LONG:
                for (long value : array.getIntArray()) {
                    jsonGenerator.writeNumber(value);
                }
                return;
            case BYTE:
                for (byte value : array.getByteArray()) {
                    jsonGenerator.writeNumber(value);
                }
                return;
            case FLOAT:
                for (float value : array.getFloatArray()) {
                    jsonGenerator.writeNumber(value);
                }
        }
    }

    private static void writeStringParameters(JsonGenerator generator, StringParametersMap stringParametersMap) throws IOException {
        if (isNull(stringParametersMap)) return;
        generator.writeStartObject();
        Map<String, String> parameters = stringParametersMap.getParameters();
        for (String field : parameters.keySet()) {
            generator.writeStringField(field, parameters.get(field));
        }
        generator.writeEndObject();
    }

    private static void writeStringParameters(JsonGenerator generator, String name, StringParametersMap stringParametersMap) throws IOException {
        if (isNull(stringParametersMap)) return;
        generator.writeObjectFieldStart(name);
        Map<String, String> parameters = stringParametersMap.getParameters();
        for (String field : parameters.keySet()) {
            generator.writeStringField(field, parameters.get(field));
        }
        generator.writeEndObject();
    }

}
