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

package io.art.rsocket.socket;

import io.art.core.exception.*;
import io.art.core.mime.*;
import io.art.core.model.*;
import io.art.rsocket.configuration.*;
import io.art.rsocket.exception.*;
import io.art.rsocket.model.*;
import io.art.rsocket.state.*;
import io.art.server.configuration.*;
import io.art.server.specification.*;
import io.art.transport.payload.*;
import io.art.value.immutable.*;
import io.rsocket.*;
import io.rsocket.util.*;
import org.reactivestreams.*;
import reactor.core.publisher.*;
import reactor.util.context.*;
import static io.art.rsocket.constants.RsocketModuleConstants.ContextKeys.*;
import static io.art.rsocket.constants.RsocketModuleConstants.*;
import static io.art.rsocket.constants.RsocketModuleConstants.ExceptionMessages.*;
import static io.art.rsocket.model.RsocketSetupPayload.*;
import static io.art.rsocket.module.RsocketModule.*;
import static io.art.rsocket.reader.RsocketPayloadReader.*;
import static io.art.rsocket.state.RsocketModuleState.RsocketThreadLocalState.*;
import static io.art.server.module.ServerModule.*;
import static io.art.value.constants.ValueModuleConstants.*;
import static io.art.value.constants.ValueModuleConstants.Fields.*;
import static io.art.value.immutable.Value.*;
import static io.art.value.mapping.ServiceMethodMapping.*;
import static io.art.value.mime.MimeTypeDataFormatMapper.*;
import static java.text.MessageFormat.*;
import static java.util.Objects.*;
import static reactor.core.publisher.Flux.*;
import java.util.function.*;

public class ServingRsocket implements RSocket {
    private final TransportPayloadReader dataReader;
    private final TransportPayloadWriter dataWriter;
    private final RSocket requesterSocket;
    private final RsocketModuleState moduleState = rsocketModule().state();
    private final RsocketSetupPayload setupPayload;
    private final ServiceMethodSpecification specification;

    public ServingRsocket(ConnectionSetupPayload payload, RSocket requesterSocket, RsocketServerConfiguration serverConfiguration) {
        moduleState.registerRequester(this.requesterSocket = requesterSocket);
        DataFormat dataFormat = fromMimeType(MimeType.valueOf(payload.dataMimeType()), serverConfiguration.getDefaultDataFormat());
        DataFormat metaDataFormat = fromMimeType(MimeType.valueOf(payload.metadataMimeType()), serverConfiguration.getDefaultMetaDataFormat());
        Function<DataFormat, TransportPayloadReader> setupReader = serverModule().configuration().getReader();
        TransportPayload setupPayloadData = setupReader.apply(dataFormat).read(payload.sliceData());
        RsocketSetupPayloadBuilder setupPayloadBuilder = RsocketSetupPayload.builder()
                .dataFormat(dataFormat)
                .metadataFormat(metaDataFormat);
        Entity serviceIdentifiers;
        if (!setupPayloadData.isEmpty() && isEntity(setupPayloadData.getValue()) && nonNull(serviceIdentifiers = asEntity(asEntity(setupPayloadData.getValue()).get(SERVICE_METHOD_IDENTIFIERS_KEY)))) {
            ServiceMethodIdentifier serviceMethodId = toServiceMethod(serviceIdentifiers);
            if (nonNull(serviceMethodId)) {
                setupPayload = setupPayloadBuilder.serviceMethod(serviceMethodId).build();
                specification = findSpecification(serviceMethodId);
                ServerModuleConfiguration configuration = specification.getConfiguration();
                dataReader = configuration.getReader(serviceMethodId, dataFormat);
                dataWriter = configuration.getWriter(serviceMethodId, dataFormat);
                return;
            }
        }
        ServiceMethodIdentifier defaultServiceMethod = serverConfiguration.getDefaultServiceMethod();
        if (nonNull(defaultServiceMethod)) {
            setupPayload = setupPayloadBuilder.serviceMethod(defaultServiceMethod).build();
            specification = findSpecification(defaultServiceMethod);
            ServerModuleConfiguration configuration = specification.getConfiguration();
            dataReader = configuration.getReader(defaultServiceMethod, dataFormat);
            dataWriter = configuration.getWriter(defaultServiceMethod, dataFormat);
            return;
        }
        throw new ImpossibleSituationException();
    }

    @Override
    public Mono<Void> fireAndForget(Payload payload) {
        TransportPayload payloadValue = readRsocketPayload(dataReader, payload);
        Flux<Value> input = addContext(payloadValue.isEmpty() ? empty() : just(payloadValue.getValue()));
        return specification.serve(input).then();
    }

    @Override
    public Mono<Payload> requestResponse(Payload payload) {
        TransportPayload payloadValue = readRsocketPayload(dataReader, payload);
        Flux<Value> input = addContext(payloadValue.isEmpty() ? empty() : just(payloadValue.getValue()));
        return specification.serve(input).map(value -> ByteBufPayload.create(dataWriter.write(value))).last(EMPTY_PAYLOAD);
    }

    @Override
    public Flux<Payload> requestStream(Payload payload) {
        TransportPayload payloadValue = readRsocketPayload(dataReader, payload);
        Flux<Value> input = addContext(payloadValue.isEmpty() ? empty() : just(payloadValue.getValue()));
        return specification.serve(input).map(value -> ByteBufPayload.create(dataWriter.write(value)));
    }

    @Override
    public Flux<Payload> requestChannel(Publisher<Payload> payloads) {
        Flux<Value> input = addContext(from(payloads)
                .map(data -> readRsocketPayload(dataReader, data))
                .filter(data -> !data.isEmpty())
                .map(TransportPayload::getValue));
        return specification.serve(input).map(value -> ByteBufPayload.create(dataWriter.write(value)));
    }

    @Override
    public Mono<Void> metadataPush(Payload payload) {
        TransportPayload payloadValue = readRsocketPayload(dataReader, payload);
        Flux<Value> input = addContext(payloadValue.isEmpty() ? empty() : just(payloadValue.getValue()));
        return specification.serve(input).then();
    }

    @Override
    public void dispose() {
        moduleState.removeRequester(this);
    }

    private ServiceMethodSpecification findSpecification(ServiceMethodIdentifier serviceMethodId) {
        return specifications()
                .findMethodById(serviceMethodId)
                .orElseThrow(() -> new RsocketException(format(SPECIFICATION_NOT_FOUND, serviceMethodId)));
    }

    private <T> Flux<T> addContext(Flux<T> flux) {
        return flux.doOnEach(signal -> loadContext(signal.getContextView())).contextWrite(this::saveContext);
    }

    private void loadContext(ContextView context) {
        moduleState.localState(fromContext(context));
    }

    private Context saveContext(Context context) {
        return context
                .putNonNull(REQUESTER_RSOCKET_KEY, requesterSocket)
                .putNonNull(SETUP_PAYLOAD_KEY, setupPayload);
    }
}
