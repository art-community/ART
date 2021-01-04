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

package ru.art.http.server.specification;

import lombok.*;
import ru.art.core.caster.*;
import ru.art.entity.*;
import ru.art.entity.mapper.ValueToModelMapper.*;
import ru.art.http.server.HttpServerModuleConfiguration.*;
import ru.art.http.server.model.*;
import ru.art.service.*;
import ru.art.service.exception.*;
import ru.art.service.interceptor.ServiceExecutionInterceptor.*;
import ru.art.service.model.*;
import static lombok.AccessLevel.*;
import static ru.art.core.caster.Caster.*;
import static ru.art.core.constants.StringConstants.*;
import static ru.art.core.extension.NullCheckingExtensions.*;
import static ru.art.core.factory.CollectionsFactory.*;
import static ru.art.http.server.constants.HttpServerModuleConstants.HttpResourceServiceConstants.*;
import static ru.art.http.server.constants.HttpServerModuleConstants.HttpResourceServiceConstants.HttpParameters.*;
import static ru.art.http.server.constants.HttpServerModuleConstants.HttpResourceServiceConstants.Methods.*;
import static ru.art.http.server.extractor.HttpResponseContentTypeExtractor.*;
import static ru.art.http.server.interceptor.HttpServerInterception.*;
import static ru.art.http.server.interceptor.HttpServerInterceptor.*;
import static ru.art.http.server.model.HttpService.*;
import static ru.art.http.server.module.HttpServerModule.*;
import static ru.art.http.server.service.HttpResourceService.*;
import static ru.art.service.interceptor.ServiceExecutionInterceptor.*;
import static ru.art.service.model.ServiceInterceptionResult.*;
import java.util.*;
import java.util.function.*;

@Getter
@AllArgsConstructor
@RequiredArgsConstructor
public class HttpResourceServiceSpecification implements HttpServiceSpecification {
    private final String resourcePath;
    private HttpResourceConfiguration resourceConfiguration;
    @Getter(lazy = true)
    private final String serviceId = resourcePath;
    @Getter(lazy = true)
    private final HttpService httpService = httpService()
            .get(GET_RESOURCE)
            .fromPathParameters(RESOURCE)
            .requestMapper((StringParametersMapToModelMapper<String>) resource -> doIfNotNull(resource,
                    (Function<StringParametersMap, String>) res -> resource.getParameter(RESOURCE)))
            .overrideResponseContentType()
            .responseMapper(Caster::cast)
            .addRequestInterceptor(intercept(interceptAndContinue(((request, response) -> response.setContentType(extractTypeByFile(request.getRequestURI()))))))
            .listen(resourcePath)

            .serve(EMPTY_STRING);

    @Override
    public List<ResponseInterceptor> getResponseInterceptors() {
        return linkedListOf(interceptResponse(new ServiceLoggingInterception() {
            @Override
            public ServiceInterceptionResult intercept(ServiceRequest<?> request, ServiceResponse<?> response) {
                super.intercept(request, ServiceResponse.builder()
                        .command(response.getCommand())
                        .serviceException(response.getServiceException())
                        .responseData(HTTP_RESOURCE_BODY_REPLACEMENT)
                        .build());
                return nextInterceptor(request, response);
            }
        }));
    }

    @Override
    @SuppressWarnings("All")
    public <P, R> R executeMethod(String methodId, P request) {
        if (GET_RESOURCE.equals(methodId)) {
            return cast(getHttpResource(cast(request), getOrElse(resourceConfiguration, resourceConfiguration = httpServerModule().getResourceConfiguration())));
        }
        throw new UnknownServiceMethodException(getServiceId(), methodId);
    }
}
