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
import ru.art.entity.*;
import ru.art.json.exception.*;
import static com.fasterxml.jackson.core.JsonToken.*;
import static java.util.Collections.*;
import static java.util.Objects.*;
import static ru.art.core.checker.CheckerForEmptiness.isEmpty;
import static ru.art.core.context.Context.*;
import static ru.art.core.extension.FileExtensions.*;
import static ru.art.core.extension.InputStreamExtensions.*;
import static ru.art.core.extension.StringExtensions.*;
import static ru.art.core.factory.CollectionsFactory.*;
import static ru.art.entity.CollectionValuesFactory.*;
import static ru.art.entity.Entity.*;
import static ru.art.entity.PrimitivesFactory.*;
import static ru.art.json.constants.JsonMappingExceptionMessages.*;
import static ru.art.json.module.JsonModule.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;


@UtilityClass
public class JsonEntityReader {
    public static Value readJson(byte[] jsonBytes) {
        return readJson(jsonModule().getObjectMapper().getFactory(), new String(jsonBytes, contextConfiguration().getCharset()));
    }

    public static Value readJson(InputStream inputStream) {
        return readJson(toByteArray(inputStream));
    }

    public static Value readJson(Path path) {
        return readJson(readFile(path));
    }

    public static Value readJson(String json) {
        return readJson(jsonModule().getObjectMapper().getFactory(), json);
    }

    public static Value readJson(JsonFactory jsonFactory, String json) {
        if (isNull(jsonFactory)) throw new JsonMappingException(JSON_FACTORY_IS_NULL);
        if (isEmpty(json)) return null;
        try {
            JsonParser parser = jsonFactory.createParser(json);
            JsonToken nextToken = parser.nextToken();
            if (isNull(nextToken)) return null;
            switch (nextToken) {
                case START_OBJECT:
                    return parseJsonEntity(parser);
                case START_ARRAY:
                    return parseArray(parser);
                case VALUE_STRING:
                    return stringPrimitive(parser.getText());
                case VALUE_NUMBER_INT:
                    return longPrimitive(parser.getLongValue());
                case VALUE_NUMBER_FLOAT:
                    return floatPrimitive(parser.getFloatValue());
                case VALUE_TRUE:
                    return boolPrimitive(true);
                case VALUE_FALSE:
                    return boolPrimitive(false);
                case FIELD_NAME:
                case VALUE_NULL:
                case NOT_AVAILABLE:
                case END_OBJECT:
                case VALUE_EMBEDDED_OBJECT:
                case END_ARRAY:
                    return null;
            }
            return null;
        } catch (IOException ioException) {
            throw new JsonMappingException(ioException);
        }
    }


    private static Entity parseJsonEntity(JsonParser parser) throws IOException {
        EntityBuilder entityBuilder = entityBuilder();
        JsonToken currentToken = parser.nextToken();
        do {
            String currentName = parser.getCurrentName();
            if (isNull(currentToken)) {
                currentToken = parser.nextToken();
                continue;
            }
            currentName = emptyIfNull(currentName);
            switch (currentToken) {
                case NOT_AVAILABLE:
                case END_OBJECT:
                    return entityBuilder.build();
                case START_OBJECT:
                    entityBuilder.entityField(currentName, parseJsonEntity(parser));
                    break;
                case START_ARRAY:
                    parseArray(entityBuilder, parser);
                    break;
                case VALUE_STRING:
                    entityBuilder.stringField(currentName, parser.getText());
                    break;
                case VALUE_NUMBER_INT:
                    entityBuilder.longField(currentName, parser.getLongValue());
                    break;
                case VALUE_NUMBER_FLOAT:
                    entityBuilder.floatField(currentName, parser.getFloatValue());
                    break;
                case VALUE_TRUE:
                    entityBuilder.boolField(currentName, true);
                    break;
                case VALUE_FALSE:
                    entityBuilder.boolField(currentName, false);
                    break;
                case VALUE_NULL:
                    break;
            }
            currentToken = parser.nextToken();
        } while (!parser.isClosed());
        return entityBuilder.build();
    }

    private static void parseArray(EntityBuilder entityBuilder, JsonParser parser) throws IOException {
        String currentName = emptyIfNull(parser.getCurrentName());
        JsonToken currentToken = parser.nextToken();
        switch (currentToken) {
            case END_ARRAY:
                entityBuilder.valueCollectionField(currentName, emptyList());
                return;
            case NOT_AVAILABLE:
            case END_OBJECT:
            case FIELD_NAME:
            case VALUE_EMBEDDED_OBJECT:
            case VALUE_NULL:
                return;
            case START_OBJECT:
                entityBuilder.entityCollectionField(currentName, parseEntityArray(parser));
                return;
            case START_ARRAY:
                entityBuilder.collectionValueCollectionField(currentName, parseArraysArray(parser));
                return;
            case VALUE_STRING:
                entityBuilder.stringCollectionField(currentName, parseStringArray(parser));
                return;
            case VALUE_NUMBER_INT:
                entityBuilder.longCollectionField(currentName, parseLongArray(parser));
                return;
            case VALUE_NUMBER_FLOAT:
                entityBuilder.floatCollectionField(currentName, parseFloatArray(parser));
                return;
            case VALUE_TRUE:
                entityBuilder.boolCollectionField(currentName, parseBooleanArray(parser));
                return;
            case VALUE_FALSE:
                entityBuilder.boolCollectionField(currentName, parseBooleanArray(parser));
        }
    }


    private static CollectionValue<?> parseArray(JsonParser parser) throws IOException {
        JsonToken currentToken = parser.nextToken();
        switch (currentToken) {
            case NOT_AVAILABLE:
            case END_OBJECT:
            case END_ARRAY:
            case FIELD_NAME:
            case VALUE_EMBEDDED_OBJECT:
            case VALUE_NULL:
                return emptyCollection();
            case START_OBJECT:
                return entityCollection(parseEntityArray(parser));
            case START_ARRAY:
                return collectionOfCollections(parseArraysArray(parser));
            case VALUE_STRING:
                return stringCollection(parseStringArray(parser));
            case VALUE_NUMBER_INT:
                return longCollection(parseLongArray(parser));
            case VALUE_NUMBER_FLOAT:
                return doubleCollection(parseDoubleArray(parser));
            case VALUE_TRUE:
            case VALUE_FALSE:
                return boolCollection(parseBooleanArray(parser));
        }
        return emptyCollection();
    }


    private static Collection<String> parseStringArray(JsonParser parser) throws IOException {
        List<String> array = dynamicArrayOf();
        JsonToken currentToken = parser.currentToken();
        do {
            if (currentToken != VALUE_STRING) return array;
            array.add(parser.getText());
            currentToken = parser.nextToken();
        } while (!parser.isClosed() && currentToken != END_ARRAY);
        return array;
    }

    private static Collection<Double> parseDoubleArray(JsonParser parser) throws IOException {
        List<Double> array = dynamicArrayOf();
        JsonToken currentToken = parser.currentToken();
        do {
            if (currentToken != VALUE_NUMBER_FLOAT) return array;
            array.add(parser.getDoubleValue());
            currentToken = parser.nextToken();
        } while (!parser.isClosed() && currentToken != END_ARRAY);
        return array;
    }

    private static Collection<Boolean> parseBooleanArray(JsonParser parser) throws IOException {
        List<Boolean> array = dynamicArrayOf();
        JsonToken currentToken = parser.currentToken();
        do {
            if (currentToken != VALUE_FALSE && currentToken != VALUE_TRUE) return array;
            array.add(parser.getBooleanValue());
            currentToken = parser.nextToken();
        } while (!parser.isClosed() && currentToken != END_ARRAY);
        return array;
    }

    private static Collection<Long> parseLongArray(JsonParser parser) throws IOException {
        List<Long> array = dynamicArrayOf();
        JsonToken currentToken = parser.currentToken();
        do {
            if (currentToken != VALUE_NUMBER_INT) return array;
            array.add(parser.getLongValue());
            currentToken = parser.nextToken();
        } while (!parser.isClosed() && currentToken != END_ARRAY);
        return array;
    }

    private static Collection<Float> parseFloatArray(JsonParser parser) throws IOException {
        List<Float> array = dynamicArrayOf();
        JsonToken currentToken = parser.currentToken();
        do {
            if (currentToken != VALUE_NUMBER_FLOAT) return array;
            array.add(parser.getFloatValue());
            currentToken = parser.nextToken();
        } while (!parser.isClosed() && currentToken != END_ARRAY);
        return array;
    }

    private static Collection<Entity> parseEntityArray(JsonParser parser) throws IOException {
        List<Entity> array = dynamicArrayOf();
        JsonToken currentToken = parser.currentToken();
        do {
            if (currentToken != START_OBJECT) return array;
            array.add(parseJsonEntity(parser));
            currentToken = parser.nextToken();
        } while (!parser.isClosed() && currentToken != END_ARRAY);
        return array;
    }

    private Collection<CollectionValue<Entity>> parseArraysArray(JsonParser parser) throws IOException {
        List<CollectionValue<Entity>> array = dynamicArrayOf();
        JsonToken currentToken = parser.currentToken();
        do {
            if (currentToken != START_ARRAY) {
                array.add(entityCollection(parseEntityArray(parser)));
            }
            currentToken = parser.nextToken();
            if (currentToken == END_ARRAY && isEmpty(array)) {
                array.add(entityCollection(dynamicArrayOf()));
            }
        } while (!parser.isClosed() && currentToken != END_ARRAY);
        return array;
    }
}
