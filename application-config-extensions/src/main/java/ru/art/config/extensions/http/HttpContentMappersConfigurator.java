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

package ru.art.config.extensions.http;

import ru.art.core.factory.CollectionsFactory.*;
import ru.art.core.mime.*;
import ru.art.http.json.*;
import ru.art.http.mapper.*;
import ru.art.http.xml.*;
import static ru.art.core.factory.CollectionsFactory.*;
import static ru.art.http.constants.HttpMimeTypes.*;
import static ru.art.metrics.http.constants.MetricsModuleHttpConstants.*;
import java.util.*;

public interface HttpContentMappersConfigurator {
    static Map<MimeType, HttpContentMapper> configureHttpContentMappers(Map<MimeType, HttpContentMapper> parentMappers) {
        MapBuilder<MimeType, HttpContentMapper> mappers = mapOf();
        mappers.putAll(parentMappers);
        HttpTextPlainMapper textPlainMapper = new HttpTextPlainMapper();
        HttpJsonMapper jsonMapper = new HttpJsonMapper();
        HttpXmlMapper xmlMapper = new HttpXmlMapper();
        HttpBytesMapper bytesMapper = new HttpBytesMapper();
        HttpContentMapper bytesContentMapper = new HttpContentMapper(bytesMapper, bytesMapper);
        HttpContentMapper textContentMapper = new HttpContentMapper(textPlainMapper, textPlainMapper);
        HttpContentMapper jsonContentMapper = new HttpContentMapper(jsonMapper, jsonMapper);
        HttpContentMapper xmlContentMapper = new HttpContentMapper(xmlMapper, xmlMapper);
        mappers.add(TEXT_HTML, textContentMapper)
                .add(TEXT_HTML_UTF_8, textContentMapper)
                .add(TEXT_HTML_WIN_1251, textContentMapper)
                .add(IMAGE_JPEG, bytesContentMapper)
                .add(IMAGE_JPG, bytesContentMapper)
                .add(IMAGE_ICO, bytesContentMapper)
                .add(IMAGE_PNG, bytesContentMapper)
                .add(IMAGE_WEBP, bytesContentMapper)
                .add(IMAGE_SVG, bytesContentMapper)
                .add(IMAGE_SVG_XML, bytesContentMapper)
                .add(IMAGE_GIF, bytesContentMapper)
                .add(TEXT_CSS, textContentMapper)
                .add(TEXT_JS, textContentMapper)
                .add(TEXT_CSV, bytesContentMapper)
                .add(TEXT_CSV_UTF_8, bytesContentMapper)
                .add(TEXT_CSV_WIN_1251, bytesContentMapper)
                .add(APPLICATION_OCTET_STREAM, bytesContentMapper)
                .add(APPLICATION_JSON, jsonContentMapper)
                .add(APPLICATION_JSON_UTF8, jsonContentMapper)
                .add(APPLICATION_JSON_WIN_1251, jsonContentMapper)
                .add(TEXT_PLAIN, textContentMapper)
                .add(TEXT_XML, xmlContentMapper)
                .add(TEXT_XML_UTF_8, xmlContentMapper)
                .add(TEXT_XML_WIN_1251, xmlContentMapper)
                .add(APPLICATION_XML, xmlContentMapper)
                .add(APPLICATION_SOAP_XML, xmlContentMapper)
                .add(METRICS_CONTENT_TYPE.getMimeType(), textContentMapper)
                .add(ALL, textContentMapper);
        return mappers;
    }
}
