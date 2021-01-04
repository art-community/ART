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

package ru.art.rsocket.writer;

import io.rsocket.*;
import lombok.experimental.*;
import reactor.core.publisher.*;
import ru.art.entity.*;
import ru.art.entity.interceptor.*;
import ru.art.entity.mapper.*;
import ru.art.reactive.service.wrapper.*;
import ru.art.rsocket.constants.RsocketModuleConstants.*;
import ru.art.rsocket.model.*;
import ru.art.rsocket.service.RsocketService.*;
import ru.art.service.model.*;
import static io.rsocket.util.ByteBufPayload.create;
import static io.rsocket.util.DefaultPayload.*;
import static java.util.Objects.*;
import static reactor.core.publisher.Flux.*;
import static ru.art.core.caster.Caster.*;
import static ru.art.core.constants.InterceptionStrategy.*;
import static ru.art.rsocket.processor.ResponseValueInterceptorProcessor.*;
import static ru.art.rsocket.writer.RsocketPayloadWriter.*;
import static ru.art.service.factory.ServiceResponseFactory.*;
import static ru.art.service.mapping.ServiceResponseMapping.*;
import java.util.*;

@UtilityClass
public class ServiceResponsePayloadWriter {
    public static Payload writeServiceResponse(RsocketMethod rsocketMethod, ServiceResponse<?> serviceResponse, RsocketDataFormat dataFormat) {
        ValueFromModelMapper<?, ?> responseMapper = rsocketMethod.responseMapper();
        Entity responseValue = fromServiceResponse(responseMapper).map(cast(serviceResponse));
        List<ValueInterceptor<Entity, Entity>> responseValueInterceptors = rsocketMethod.responseValueInterceptors();
        for (ValueInterceptor<Entity, Entity> responseValueInterceptor : responseValueInterceptors) {
            ValueInterceptionResult<Entity, Entity> result = responseValueInterceptor.intercept(responseValue);
            if (isNull(result)) {
                break;
            }
            responseValue = result.getOutValue();
            if (result.getNextInterceptionStrategy() == PROCESS_HANDLING) {
                break;
            }
            if (result.getNextInterceptionStrategy() == STOP_HANDLING) {
                if (isNull(result.getOutValue())) {
                    return create(EMPTY_BUFFER);
                }
                return writePayloadData(result.getOutValue(), dataFormat);
            }
        }
        return create(writePayloadData(responseValue, dataFormat));
    }

    public static Flux<Payload> writeResponseReactive(RsocketReactiveMethods rsocketReactiveMethods, ServiceResponse<?> serviceResponse, RsocketDataFormat dataFormat) {
        if (isNull(serviceResponse)) {
            return empty();
        }
        if (nonNull(serviceResponse.getServiceException())) {
            return error(serviceResponse.getServiceException());
        }
        ValueFromModelMapper<?, ?> responseMapper = rsocketReactiveMethods.getRsocketMethod().responseMapper();
        Flux<Payload> flux = from(cast(serviceResponse.getResponseData()))
                .map(response -> fromServiceResponse(responseMapper).map(cast(okResponse(serviceResponse.getCommand(), response))))
                .map(responseValue -> processResponseValueInterceptors(responseValue, rsocketReactiveMethods.getRsocketMethod().responseValueInterceptors()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(responseValue -> writePayloadData(responseValue, dataFormat));
        ReactiveServiceExceptionWrappers exceptionWrappers = rsocketReactiveMethods.getReactiveMethod().reactiveServiceExceptionWrappers();
        return exceptionWrappers
                .getReactiveServiceExceptionWrappers()
                .entrySet()
                .stream()
                .reduce(flux, (resultFlux, entry) -> resultFlux
                        .onErrorResume(entry.getKey(), exception -> cast(just(writePayloadData(fromServiceResponse(responseMapper).map(cast(ServiceResponse.builder()
                                .command(serviceResponse.getCommand())
                                .serviceException(entry.getValue().wrap(serviceResponse.getCommand(), cast(exception)))
                                .build())), dataFormat)))), (current, next) -> next)
                .onErrorResume(Throwable.class, exception -> just(writePayloadData(fromServiceResponse(responseMapper).map(cast(ServiceResponse.builder()
                        .command(serviceResponse.getCommand())
                        .serviceException(exceptionWrappers.getUndeclaredExceptionWrapper().wrap(serviceResponse.getCommand(), exception))
                        .build())), dataFormat)));
    }

}
