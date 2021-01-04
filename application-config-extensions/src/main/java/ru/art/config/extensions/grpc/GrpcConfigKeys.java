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

package ru.art.config.extensions.grpc;

public interface GrpcConfigKeys {
    String GRPC_SERVER_CONFIG_SECTION_ID = "grpc.server";
    String GRPC_COMMUNICATION_SECTION_ID = "grpc.communication";
    String GRPC_BALANCER_SECTION_ID = "grpc.balancer";
    String HANDSHAKE_TIMEOUT = "handshakeTimeout";
    String SECURED = "secured";
    String PERMIT_KEEP_ALIVE_WITHOUT_CALLS = "permitKeepAliveWithoutCalls";
    String PERMIT_KEEP_ALIVE_TIME_MILLIS = "permitKeepAliveTimeMillis";
}
