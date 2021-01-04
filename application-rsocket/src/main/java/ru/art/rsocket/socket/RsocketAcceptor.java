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

package ru.art.rsocket.socket;

import io.rsocket.*;
import org.reactivestreams.*;
import reactor.core.publisher.*;
import ru.art.rsocket.flux.*;
import ru.art.rsocket.model.*;
import ru.art.rsocket.service.*;
import ru.art.rsocket.state.*;
import ru.art.service.model.*;
import static reactor.core.publisher.Mono.*;
import static ru.art.core.caster.Caster.*;
import static ru.art.core.extension.NullCheckingExtensions.*;
import static ru.art.reactive.service.constants.ReactiveServiceModuleConstants.ReactiveMethodProcessingMode.*;
import static ru.art.rsocket.constants.RsocketModuleConstants.*;
import static ru.art.rsocket.model.RsocketRequestContext.*;
import static ru.art.rsocket.module.RsocketModule.*;
import static ru.art.rsocket.selector.RsocketDataFormatMimeTypeConverter.*;
import static ru.art.rsocket.state.RsocketModuleState.*;
import static ru.art.rsocket.writer.RsocketPayloadWriter.*;
import static ru.art.rsocket.writer.ServiceResponsePayloadWriter.*;
import static ru.art.service.ServiceController.*;

public class RsocketAcceptor implements RSocket {
    private final CurrentRsocketState state;

    public RsocketAcceptor(RSocket socket, ConnectionSetupPayload setupPayload) {
        state = CurrentRsocketState
                .builder()
                .dataMimeType(setupPayload.dataMimeType())
                .metadataMimeType(setupPayload.metadataMimeType())
                .rsocket(socket)
                .dataFormat(fromMimeType(setupPayload.dataMimeType()))
                .build();
        updateState();
    }

    @Override
    public Mono<Void> fireAndForget(Payload payload) {
        RsocketDataFormat dataFormat = updateState().getDataFormat();
        RsocketRequestContext context = fromPayload(payload, dataFormat);
        if (context.isStopHandling()) {
            return never();
        }
        executeServiceMethodUnchecked(context.getRequest());
        return never();
    }

    @Override
    public Mono<Payload> requestResponse(Payload payload) {
        RsocketDataFormat dataFormat = updateState().getDataFormat();
        RsocketRequestContext context = fromPayload(payload, dataFormat);
        if (context.isStopHandling()) {
            return just(writePayloadData(context.getAlternativeResponse(), dataFormat));
        }
        ServiceResponse<?> serviceResponse = executeServiceMethodUnchecked(context.getRequest());
        RsocketService.RsocketMethod rsocketMethod = context.getRsocketReactiveMethods().getRsocketMethod();
        return context.getRsocketReactiveMethods().getReactiveMethod().responseProcessingMode() == STRAIGHT
                ? just(writeServiceResponse(rsocketMethod, serviceResponse, getOrElse(rsocketMethod.overrideResponseDataFormat(), dataFormat)))
                : writeResponseReactive(context.getRsocketReactiveMethods(), cast(serviceResponse), dataFormat).next();
    }

    @Override
    public Flux<Payload> requestStream(Payload payload) {
        RsocketDataFormat dataFormat = updateState().getDataFormat();
        RsocketRequestContext context = fromPayload(payload, fromMimeType(state.getDataMimeType()));
        if (context.isStopHandling()) {
            return Flux.just(writePayloadData(context.getAlternativeResponse(), dataFormat));
        }
        if (context.getRsocketReactiveMethods().getReactiveMethod().responseProcessingMode() == STRAIGHT) {
            return Flux.empty();
        }
        ServiceResponse<?> serviceResponse = executeServiceMethodUnchecked(context.getRequest());
        return writeResponseReactive(context.getRsocketReactiveMethods(), cast(serviceResponse), fromMimeType(state.getDataMimeType()));
    }

    @Override
    public Flux<Payload> requestChannel(Publisher<Payload> payloads) {
        return Flux.create(emitter -> new RsocketRequestChannelEmitter(emitter, payloads, updateState().getDataFormat()));
    }

    @Override
    public Mono<Void> metadataPush(Payload payload) {
        RsocketDataFormat dataFormat = updateState().getDataFormat();
        executeServiceMethodUnchecked(fromPayload(payload, dataFormat).getRequest());
        return never();
    }

    @Override
    public void dispose() {
        rsocketModuleState().clearCurrentRocketState();
    }

    private CurrentRsocketState updateState() {
        RsocketModuleState state = rsocketModuleState();
        state.setCurrentRocketState(this.state);
        return this.state;
    }
}
