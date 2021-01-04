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

package ru.art.grpc.server;

import io.grpc.*;
import io.grpc.netty.shaded.io.grpc.netty.*;
import lombok.*;
import org.apache.logging.log4j.Logger;
import ru.art.grpc.server.configuration.GrpcServerModuleConfiguration.*;
import ru.art.grpc.server.exception.*;
import ru.art.grpc.server.servlet.*;
import ru.art.grpc.server.specification.*;
import static io.grpc.ServerBuilder.*;
import static java.lang.System.*;
import static java.text.MessageFormat.*;
import static java.util.Objects.*;
import static java.util.concurrent.TimeUnit.*;
import static lombok.AccessLevel.PRIVATE;
import static ru.art.core.caster.Caster.*;
import static ru.art.core.context.Context.*;
import static ru.art.core.factory.CollectionsFactory.*;
import static ru.art.grpc.server.constants.GrpcServerExceptionMessages.*;
import static ru.art.grpc.server.constants.GrpcServerLoggingMessages.*;
import static ru.art.grpc.server.constants.GrpcServerModuleConstants.*;
import static ru.art.grpc.server.module.GrpcServerModule.*;
import static ru.art.logging.LoggingModule.*;
import static ru.art.service.ServiceModule.*;
import java.util.*;
import java.util.concurrent.*;

@AllArgsConstructor
public class GrpcServer {
    @Getter(lazy = true, value = PRIVATE)
    private final static Logger logger = loggingModule().getLogger(GrpcServer.class);
    private final Server server;

    public static GrpcServer grpcServer() {
        NettyServerBuilder serverBuilder = cast(forPort(grpcServerModule().getPort()));
        serverBuilder
                .keepAliveTime(grpcServerModule().getKeepAliveTimeNanos(), NANOSECONDS)
                .keepAliveTimeout(grpcServerModule().getKeepAliveTimeOutNanos(), NANOSECONDS)
                .permitKeepAliveTime(grpcServerModule().getPermitKeepAliveTimeNanos(), NANOSECONDS)
                .permitKeepAliveWithoutCalls(grpcServerModule().isPermitKeepAliveWithoutCalls())
                .maxInboundMessageSize(grpcServerModule().getMaxInboundMessageSize());
        if (grpcServerModule().isExecuteServiceInTransportThread()) {
            serverBuilder.directExecutor();
        }
        Executor overridingExecutor;
        if (nonNull(overridingExecutor = grpcServerModule().getOverridingExecutor())) {
            serverBuilder.executor(overridingExecutor);
        }
        GrpcServerSecurityConfiguration serverSecurityConfiguration;
        if (nonNull(serverSecurityConfiguration = grpcServerModule().getSecurityConfiguration())) {
            serverBuilder.useTransportSecurity(serverSecurityConfiguration.getCertificateFile(), serverSecurityConfiguration.getPrivateKeyFile());
        }
        grpcServerModule().getInterceptors().forEach(serverBuilder::intercept);
        Map<String, GrpcServiceSpecification> protobufServices = mapOf();
        serviceModuleState().getServiceRegistry().getServices().values().stream()
                .filter(serviceSpecification -> serviceSpecification.getServiceTypes().contains(GRPC_SERVICE_TYPE))
                .map(serviceSpecification -> (GrpcServiceSpecification) serviceSpecification)
                .peek(GrpcServer::logService)
                .forEach(service -> protobufServices.put(service.getServiceId(), service));
        serverBuilder.addService(new GrpcServletContainer(grpcServerModule().getPath(), protobufServices).getServlet());
        serverBuilder.handshakeTimeout(grpcServerModule().getHandshakeTimeout(), SECONDS);
        GrpcServer grpcServer = new GrpcServer(serverBuilder.build());
        grpcServerModuleState().setServer(grpcServer);
        return grpcServer;
    }

    public static GrpcServer startGrpcServer() {
        try {
            long timestamp = currentTimeMillis();
            GrpcServer grpcServer = grpcServer();
            grpcServer.server.start();
            getLogger().info(format(GRPC_STARTED_MESSAGE, currentTimeMillis() - timestamp));
            return grpcServer;
        } catch (Throwable throwable) {
            throw new GrpcServerException(GRPC_SERVER_INITIALIZATION_FAILED, throwable);
        }
    }

    private static String buildServiceLoadedMessage(GrpcServiceSpecification service) {
        return format(GRPC_LOADED_SERVICE_MESSAGE, contextConfiguration().getIpAddress(),
                grpcServerModule().getPort(),
                grpcServerModule().getPath(),
                service.getServiceId(),
                service.getGrpcService().getGrpcMethods().keySet().toString());
    }

    private static void logService(GrpcServiceSpecification specification) {
        getLogger().info(buildServiceLoadedMessage(specification));
    }

    public void await() {
        try {
            server.awaitTermination();
        } catch (Throwable throwable) {
            throw new GrpcServerException(GRPC_SERVER_AWAITING_FAILED, throwable);
        }
    }

    public boolean isWorking() {
        return !server.isTerminated();
    }

    public void stop() {
        long millis = currentTimeMillis();
        try {
            server.shutdownNow();
            server.awaitTermination();
            getLogger().info(format(GRPC_STOPPED_MESSAGE, currentTimeMillis() - millis));
        } catch (Throwable throwable) {
            getLogger().error(GRPC_SERVER_STOPPING_FAILED);
        }
    }

    public void restart() {
        long millis = currentTimeMillis();
        try {
            server.shutdownNow();
            server.awaitTermination();
            startGrpcServer();
            getLogger().info(format(GRPC_RESTARTED_MESSAGE, currentTimeMillis() - millis));
        } catch (Throwable throwable) {
            getLogger().error(GRPC_SERVER_RESTARTING_FAILED);
        }
    }
}
