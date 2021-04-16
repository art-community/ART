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

package io.art.metrics.http.configurator;

import io.art.core.factory.*;
import io.art.core.mime.*;
import io.art.http.mapper.*;
import static io.art.core.factory.CollectionsFactory.*;
import static io.art.metrics.http.constants.MetricsModuleHttpConstants.*;
import java.util.*;

public interface HttpMetricsMapperConfigurator {
    static Map<MimeType, HttpContentMapper> configureMetricsContentMapper(Map<MimeType, HttpContentMapper> currentMappers) {
        HttpTextPlainMapper textPlainMapper = new HttpTextPlainMapper();
        CollectionsFactory.MapBuilder<MimeType, HttpContentMapper> mappers = mapOf(currentMappers);
        mappers.put(METRICS_CONTENT_TYPE.getMimeType(), new HttpContentMapper(textPlainMapper, textPlainMapper));
        return mappers;
    }
}
