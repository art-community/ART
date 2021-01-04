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

package ru.art.state.api.communication.grpc;

import lombok.*;
import lombok.experimental.*;
import ru.art.grpc.client.communicator.*;
import ru.art.grpc.client.specification.*;
import ru.art.service.exception.*;
import static lombok.AccessLevel.PRIVATE;
import static ru.art.core.caster.Caster.*;
import static ru.art.entity.PrimitiveMapping.*;
import static ru.art.grpc.client.communicator.GrpcCommunicator.*;
import static ru.art.state.api.constants.StateApiConstants.NetworkServiceConstants.Methods.*;
import static ru.art.state.api.constants.StateApiConstants.NetworkServiceConstants.*;
import static ru.art.state.api.mapping.ClusterProfileRequestResponseMapper.ClusterProfileRequestMapper.*;
import static ru.art.state.api.mapping.ClusterProfileRequestResponseMapper.ClusterProfileResponseMapper.*;
import static ru.art.state.api.mapping.ModuleConnectionRequestMapper.*;

@Getter
@RequiredArgsConstructor
public class NetworkServiceProxySpecification implements GrpcCommunicationSpecification {
    private final String serviceId = NETWORK_COMMUNICATION_SERVICE_ID;
    private final String path;
    private final String host;
    private final Integer port;

    @Getter(lazy = true, value = PRIVATE)
    @Accessors(fluent = true)
    private final GrpcCommunicator getClusterProfile = grpcCommunicator(host, port, path)
            .serviceId(NETWORK_SERVICE_ID)
            .methodId(GET_CLUSTER_PROFILE)
            .requestMapper(fromClusterProfileRequest)
            .responseMapper(toClusterProfileResponse);

    @Getter(lazy = true, value = PRIVATE)
    @Accessors(fluent = true)
    private final GrpcCommunicator connect = grpcCommunicator(host, port, path)
            .serviceId(NETWORK_SERVICE_ID)
            .requestMapper(fromModuleConnectionRequest)
            .methodId(CONNECT);

    @Getter(lazy = true, value = PRIVATE)
    @Accessors(fluent = true)
    private final GrpcCommunicator incrementSession = grpcCommunicator(host, port, path)
            .serviceId(NETWORK_SERVICE_ID)
            .methodId(INCREMENT_SESSION)
            .requestMapper(fromModuleConnectionRequest);

    @Getter(lazy = true, value = PRIVATE)
    @Accessors(fluent = true)
    private final GrpcCommunicator decrementSession = grpcCommunicator(host, port, path)
            .serviceId(NETWORK_SERVICE_ID)
            .methodId(DECREMENT_SESSION)
            .requestMapper(fromModuleConnectionRequest);

    @Getter(lazy = true, value = PRIVATE)
    @Accessors(fluent = true)
    private final GrpcCommunicator getSessions = grpcCommunicator(host, port, path)
            .serviceId(NETWORK_SERVICE_ID)
            .methodId(GET_SESSIONS)
            .requestMapper(fromModuleConnectionRequest)
            .responseMapper(intMapper.getToModel());

    @Override
    public <P, R> R executeMethod(String methodId, P request) {
        switch (methodId) {
            case GET_CLUSTER_PROFILE:
                return cast(getClusterProfile().execute(cast(request)).getResponseData());
            case CONNECT:
                connect().asynchronous().executeAsynchronous(cast(request));
                return null;
            case INCREMENT_SESSION:
                incrementSession().asynchronous().executeAsynchronous(cast(request));
                return null;
            case DECREMENT_SESSION:
                decrementSession().asynchronous().executeAsynchronous(cast(request));
                return null;
            case GET_SESSIONS:
                return cast(getSessions().execute(cast(request)).getResponseData());
        }
        throw new UnknownServiceMethodException(getServiceId(), methodId);
    }
}
