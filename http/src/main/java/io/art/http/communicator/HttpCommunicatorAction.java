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

package io.art.http.communicator;

import io.art.communicator.action.*;
import io.art.communicator.implementation.*;
import io.art.core.exception.*;
import io.art.core.mime.*;
import io.art.core.model.*;
import io.art.core.property.*;
import io.art.http.configuration.*;
import io.art.http.refresher.*;
import io.art.transport.payload.*;
import io.art.value.immutable.Value;
import io.netty.handler.codec.http.*;
import lombok.*;
import org.apache.logging.log4j.*;
import reactor.core.publisher.*;
import reactor.netty.http.client.*;
import static io.art.communicator.module.CommunicatorModule.*;
import static io.art.core.constants.CompilerSuppressingWarnings.*;
import static io.art.core.property.Property.*;
import static io.art.http.constants.HttpModuleConstants.HttpProtocol.*;
import static io.art.http.module.HttpModule.*;
import static io.art.logging.LoggingModule.*;
import static io.art.value.mime.MimeTypeDataFormatMapper.*;
import static io.netty.handler.codec.http.HttpHeaderNames.*;
import static io.netty.handler.codec.http.HttpMethod.*;
import static lombok.AccessLevel.*;
import java.util.function.*;

@Builder(toBuilder = true)
public class HttpCommunicatorAction implements CommunicatorActionImplementation {
    private final CommunicatorActionIdentifier communicatorActionId;

    @Getter(lazy = true, value = PRIVATE)
    private final Logger logger = logger(HttpCommunicatorAction.class);

    @Getter(lazy = true, value = PRIVATE)
    private final CommunicatorAction communicatorAction = communicatorAction();

    private final Property<HttpClient> client = property(this::createClient).listenConsumer(() -> consumer()
            .connectorConsumers()
            .consumerFor(connectorConfiguration().getConnectorId()));

    private final Property<Function<Flux<Value>, Flux<Value>>> communicate = property(this::adoptCommunicate)
            .listenProperties(client);

    @Override
    public void initialize() {
        client.initialize();
    }

    @Override
    public void dispose() {
        client.dispose();
    }

    @Override
    public Flux<Value> communicate(Flux<Value> input) {
        return communicate.get().apply(input);
    }

    private HttpClient createClient() {
        HttpConnectorConfiguration connectorConfiguration = connectorConfiguration();
        HttpClient client = connectorConfiguration.getHttpClient();
        return client;
    }

    @SuppressWarnings(CONSTANT_CONDITIONS)
    private CommunicatorAction communicatorAction() {
        return communicatorModule()
                .configuration()
                .getRegistry()
                .findActionById(communicatorActionId)
                .orElseThrow(ImpossibleSituationException::new);
    }

    private HttpConnectorConfiguration connectorConfiguration() {
        HttpCommunicatorConfiguration communicatorConfiguration = httpModule().configuration().getCommunicatorConfiguration();
        return communicatorModule()
                .configuration()
                .findConnectorId(HTTP.getProtocol(), communicatorActionId)
                .map(communicatorConfiguration.getConnectorConfigurations()::get)
                .orElseGet(() -> communicatorConfiguration.getConnectorConfigurations().get(communicatorActionId.getCommunicatorId()));
    }

    private Function<Flux<Value>, Flux<Value>> adoptCommunicate() {
        HttpClient client = this.client.get();
        HttpMethod httpMethod = communicationMethod();
        if (GET.equals(httpMethod)) {
            return input -> client.get().response((response, data) -> data
                    .map(payload -> communicatorModule().configuration().getReader(communicatorActionId, fromMimeType(MimeType.valueOf(response.responseHeaders().get(CONTENT_TYPE)))).read(payload))
                    .map(TransportPayload::getValue));
        }
        throw new ImpossibleSituationException();
    }

    private HttpMethod communicationMethod() {
        CommunicatorAction communicatorAction = getCommunicatorAction();
        return GET;
    }

    private HttpModuleRefresher.Consumer consumer() {
        return httpModule().configuration().getConsumer();
    }
}
