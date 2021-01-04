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

package ru.art.grpc.client.interceptor;

import io.grpc.*;
import static io.grpc.Metadata.*;
import static io.grpc.Metadata.Key.*;
import static org.apache.logging.log4j.ThreadContext.*;
import static ru.art.core.checker.CheckerForEmptiness.*;
import static ru.art.grpc.client.constants.GrpcClientModuleConstants.*;
import static ru.art.logging.LoggingModuleConstants.LoggingParameters.*;

public class GrpcClientTracingIdentifiersInterceptor implements ClientInterceptor {
    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> methodDescriptor, CallOptions callOptions, Channel channel) {
        return new ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(channel.newCall(methodDescriptor, callOptions)) {
            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {
                String traceId = get(TRACE_ID_KEY);
                String profile = get(PROFILE_KEY);
                if (isNotEmpty(traceId)) {
                    headers.put(of(TRACE_ID_HEADER, ASCII_STRING_MARSHALLER), traceId);
                }
                if (isNotEmpty(profile)) {
                    headers.put(of(PROFILE_HEADER, ASCII_STRING_MARSHALLER), traceId);
                }
                super.start(responseListener, headers);
            }
        };
    }
}
