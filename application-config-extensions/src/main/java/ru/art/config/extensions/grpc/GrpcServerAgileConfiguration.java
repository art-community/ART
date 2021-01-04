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

import lombok.*;
import ru.art.grpc.server.*;
import ru.art.grpc.server.configuration.GrpcServerModuleConfiguration.*;
import static java.util.Objects.*;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static ru.art.config.extensions.ConfigExtensions.*;
import static ru.art.config.extensions.common.CommonConfigKeys.*;
import static ru.art.config.extensions.grpc.GrpcConfigKeys.*;
import static ru.art.core.constants.ThreadConstants.*;
import static ru.art.core.context.Context.*;
import static ru.art.core.extension.ExceptionExtensions.*;
import static ru.art.grpc.server.constants.GrpcServerModuleConstants.*;
import static ru.art.grpc.server.module.GrpcServerModule.*;
import java.util.concurrent.*;

@Getter
public class GrpcServerAgileConfiguration extends GrpcServerModuleDefaultConfiguration {
    private int port;
    private String path;
    private int handshakeTimeout;
    private Executor overridingExecutor;
    private boolean enableRawDataTracing;
    private boolean enableValueTracing;
    private long keepAliveTimeNanos;
    private long keepAliveTimeOutNanos;
    private boolean keepAliveWithoutCalls;
    private boolean permitKeepAliveWithoutCalls;
    private long permitKeepAliveTimeNanos;

    public GrpcServerAgileConfiguration() {
        refresh();
    }

    @Override
    public void refresh() {
        int newPort = configInt(GRPC_SERVER_CONFIG_SECTION_ID, PORT, super.getPort());
        boolean restart = port != newPort;
        port = newPort;
        int newHandshakeTimeout = configInt(GRPC_SERVER_CONFIG_SECTION_ID, HANDSHAKE_TIMEOUT, super.getHandshakeTimeout());
        restart |= handshakeTimeout != newHandshakeTimeout;
        handshakeTimeout = newHandshakeTimeout;
        long newKeepAliveTimeNanos = ifException(() -> MILLISECONDS.toNanos(configLong(GRPC_SERVER_CONFIG_SECTION_ID, KEEP_ALIVE_TIME_MILLIS)), super.getKeepAliveTimeNanos());
        restart |= keepAliveTimeNanos != newKeepAliveTimeNanos;
        keepAliveTimeNanos = newKeepAliveTimeNanos;
        long newKeepAliveTimeOutNanos = ifException(() -> MILLISECONDS.toNanos(configLong(GRPC_SERVER_CONFIG_SECTION_ID, KEEP_ALIVE_TIME_OUT_MILLIS)), super.getKeepAliveTimeOutNanos());
        restart |= keepAliveTimeOutNanos != newKeepAliveTimeOutNanos;
        keepAliveTimeOutNanos = newKeepAliveTimeOutNanos;
        boolean newKeepAliveWithoutCalls = configBoolean(GRPC_SERVER_CONFIG_SECTION_ID, KEEP_ALIVE_WITHOUT_CALLS, super.isKeepAliveWithoutCalls());
        restart |= keepAliveWithoutCalls != newKeepAliveWithoutCalls;
        keepAliveWithoutCalls = newKeepAliveWithoutCalls;
        boolean newPermitKeepAliveWithoutCalls = configBoolean(GRPC_SERVER_CONFIG_SECTION_ID, PERMIT_KEEP_ALIVE_WITHOUT_CALLS, super.isPermitKeepAliveWithoutCalls());
        restart |= permitKeepAliveWithoutCalls != newPermitKeepAliveWithoutCalls;
        permitKeepAliveWithoutCalls = newPermitKeepAliveWithoutCalls;
        long newPermitKeepAliveTimeNanos = ifException(() -> MILLISECONDS.toNanos(configLong(GRPC_SERVER_CONFIG_SECTION_ID, PERMIT_KEEP_ALIVE_TIME_MILLIS)), super.getPermitKeepAliveTimeNanos());
        restart |= permitKeepAliveTimeNanos != newPermitKeepAliveTimeNanos;
        permitKeepAliveTimeNanos = newPermitKeepAliveTimeNanos;
        String newPath = configString(GRPC_SERVER_CONFIG_SECTION_ID, PATH, super.getPath());
        restart |= !newPath.equals(path);
        path = newPath;
        int newPoolSize = configInt(GRPC_SERVER_CONFIG_SECTION_ID, THREAD_POOL_SIZE, DEFAULT_THREAD_POOL_SIZE);
        restart |= isNull(overridingExecutor) || ((ForkJoinPool) overridingExecutor).getParallelism() != newPoolSize;
        overridingExecutor = new ForkJoinPool(newPoolSize);
        enableRawDataTracing = configBoolean(GRPC_SERVER_CONFIG_SECTION_ID, ENABLE_RAW_DATA_TRACING, super.isEnableRawDataTracing());
        enableValueTracing = configBoolean(GRPC_SERVER_CONFIG_SECTION_ID, ENABLE_VALUE_TRACING, super.isEnableValueTracing());
        if (restart && context().hasModule(GRPC_SERVER_MODULE_ID)) {
            GrpcServer server = grpcServerModuleState().getServer();
            if (nonNull(server) && server.isWorking()) {
                server.restart();
            }
        }
    }
}
