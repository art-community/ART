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
import static ru.art.grpc.client.communicator.GrpcCommunicator.*;
import static ru.art.state.api.constants.StateApiConstants.LockServiceConstants.*;
import static ru.art.state.api.constants.StateApiConstants.LockServiceConstants.Methods.*;
import static ru.art.state.api.mapping.LockRequestMapper.*;

@Getter
public class LockServiceProxySpecification implements GrpcCommunicationSpecification {
    private final String path;
    private final String host;
    private final Integer port;
    private final String serviceId = LOCK_SERVICE_ID;

    public LockServiceProxySpecification(String path, String host, Integer port) {
        this.path = path;
        this.host = host;
        this.port = port;
    }

    @Getter(lazy = true, value = PRIVATE)
    @Accessors(fluent = true)
    private final GrpcCommunicator lock = grpcCommunicator(host, port, path)
            .serviceId(LOCK_SERVICE_ID)
            .methodId(LOCK)
            .requestMapper(fromLockRequest);

    @Getter(lazy = true, value = PRIVATE)
    @Accessors(fluent = true)
    private final GrpcCommunicator unlock = grpcCommunicator(host, port, path)
            .serviceId(LOCK_SERVICE_ID)
            .methodId(UNLOCK)
            .requestMapper(fromLockRequest);

    @Override
    public <P, R> R executeMethod(String methodId, P request) {
        switch (methodId) {
            case LOCK:
                lock().execute(cast(request));
                return null;
            case UNLOCK:
                unlock().execute(cast(request));
                return null;
        }
        throw new UnknownServiceMethodException(getServiceId(), methodId);
    }
}
