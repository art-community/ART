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

package ru.art.config.remote.api.specification;

import lombok.*;
import ru.art.grpc.client.communicator.GrpcCommunicator.*;
import ru.art.grpc.client.specification.*;
import ru.art.service.exception.*;
import static lombok.AccessLevel.PRIVATE;
import static ru.art.config.remote.api.constants.RemoteConfigApiConstants.Methods.*;
import static ru.art.config.remote.api.constants.RemoteConfigApiConstants.*;
import static ru.art.grpc.client.communicator.GrpcCommunicator.*;

@Getter
public class RemoteConfigCommunicationSpecification implements GrpcCommunicationSpecification {
    private final String host;
    private final Integer port;
    private final String path;
    private final String serviceId = REMOTE_CONFIG_SERVICE_ID;

    public RemoteConfigCommunicationSpecification(String host, Integer port, String path) {
        this.host = host;
        this.port = port;
        this.path = path;
    }

    @Getter(lazy = true, value = PRIVATE)
    private final GrpcAsynchronousCommunicator applyConfiguration = grpcCommunicator(host, port, path)
            .serviceId(REMOTE_CONFIG_SERVICE_ID)
            .methodId(APPLY_CONFIGURATION_METHOD_ID)
            .asynchronous();

    public void applyConfiguration() {
        executeMethod(APPLY_CONFIGURATION_METHOD_ID, null);
    }

    @Override
    public <P, R> R executeMethod(String methodId, P request) {
        if (APPLY_CONFIGURATION_METHOD_ID.equals(methodId)) {
            getApplyConfiguration().executeAsynchronous();
            return null;
        }
        throw new UnknownServiceMethodException(getServiceId(), methodId);
    }
}