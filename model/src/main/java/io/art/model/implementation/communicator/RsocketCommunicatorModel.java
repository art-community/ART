/*
 * ART
 *
 * Copyright 2020 ART
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

package io.art.model.implementation.communicator;

import io.art.communicator.constants.CommunicatorModuleConstants.*;
import io.art.communicator.specification.CommunicatorSpecification.*;
import lombok.*;
import static io.art.rsocket.constants.RsocketModuleConstants.RsocketProtocol.*;
import java.util.function.*;

@Getter
@Builder
public class RsocketCommunicatorModel implements CommunicatorModel {
    private final String id;
    private final Class<?> proxyClass;
    private final String targetServiceId;
    private final String targetMethodId;
    private final CommunicationProtocol protocol = RSOCKET;
    private final Function<CommunicatorSpecificationBuilder, CommunicatorSpecificationBuilder> decorator;
}