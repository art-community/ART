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

package ru.art.information.specification;

import lombok.experimental.*;
import ru.art.http.server.specification.*;
import ru.art.information.service.*;
import static ru.art.core.context.Context.*;
import static ru.art.http.constants.HttpCommonConstants.*;
import static ru.art.http.constants.MimeToContentTypeMapper.*;
import static ru.art.http.server.HttpServerModuleConfiguration.*;
import static ru.art.http.server.constants.HttpServerModuleConstants.HttpResourceServiceConstants.HttpResourceType.*;
import static ru.art.http.server.function.HttpServiceFunction.*;
import static ru.art.http.server.module.HttpServerModule.*;
import static ru.art.information.constants.InformationModuleConstants.*;
import static ru.art.information.mapping.InformationResponseMapper.*;
import static ru.art.information.mapping.StatusResponseMapper.*;
import static ru.art.service.ServiceModule.*;

@UtilityClass
public class InformationServiceSpecification {
    public static void registerInformationService(String modulePath) {
        String webPath = modulePath + INFORMATION_PATH + WEB_UI_PATH;
        serviceModuleState()
                .getServiceRegistry()
                .registerService(new HttpResourceServiceSpecification(webPath,
                        httpServerModule()
                                .getResourceConfiguration()
                                .toBuilder()
                                .templateResourceVariable(WEB_UI_PATH_VARIABLE, webPath)
                                .templateResourceVariable(MAIN_MODULE_ID_VARIABLE, contextConfiguration().getMainModuleId())
                                .defaultResource(new HttpResource(INFORMATION_INDEX_HTML, STRING, contextConfiguration().getCharset()))
                                .build()));
        httpGet(modulePath + GET_INFORMATION_PATH)
                .producesMimeType(applicationJsonUtf8())
                .consumesMimeType(applicationJsonUtf8())
                .ignoreRequestContentType()
                .ignoreRequestAcceptType()
                .responseMapper(fromInformationResponse)
                .produce(InformationService::getInformation);
        httpGet(modulePath + STATUS_PATH)
                .producesMimeType(applicationJsonUtf8())
                .consumesMimeType(applicationJsonUtf8())
                .ignoreRequestContentType()
                .ignoreRequestAcceptType()
                .responseMapper(fromStatusResponse)
                .produce(InformationService::getStatus);
    }
}
