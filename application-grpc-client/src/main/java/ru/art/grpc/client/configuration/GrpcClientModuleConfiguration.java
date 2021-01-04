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

package ru.art.grpc.client.configuration;

import io.grpc.ClientInterceptor;
import lombok.Getter;
import ru.art.core.module.ModuleConfiguration;
import ru.art.entity.Entity;
import ru.art.entity.interceptor.ValueInterceptor;
import ru.art.grpc.client.exception.GrpcClientException;
import ru.art.grpc.client.interceptor.GrpcClientLoggingInterceptor;
import ru.art.grpc.client.interceptor.GrpcClientTracingIdentifiersInterceptor;
import ru.art.grpc.client.model.GrpcCommunicationTargetConfiguration;
import ru.art.logging.LoggingValueInterceptor;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;

import static io.grpc.internal.GrpcUtil.DEFAULT_KEEPALIVE_TIMEOUT_NANOS;
import static java.lang.Long.MAX_VALUE;
import static java.text.MessageFormat.format;
import static java.util.Collections.emptyMap;
import static java.util.concurrent.ForkJoinPool.commonPool;
import static ru.art.core.constants.NetworkConstants.LOCALHOST;
import static ru.art.core.constants.ThreadConstants.DEFAULT_THREAD_POOL_SIZE;
import static ru.art.core.extension.ExceptionExtensions.exceptionIfNull;
import static ru.art.core.factory.CollectionsFactory.linkedListOf;
import static ru.art.grpc.client.constants.GrpcClientExceptionMessages.GRPC_COMMUNICATION_TARGET_CONFIGURATION_NOT_FOUND;
import static ru.art.grpc.client.constants.GrpcClientModuleConstants.*;

public interface GrpcClientModuleConfiguration extends ModuleConfiguration {
    List<ClientInterceptor> getInterceptors();

    long getTimeout();

    long getKeepAliveTimeNanos();

    long getKeepAliveTimeOutNanos();

    boolean isKeepAliveWithoutCalls();

    long getIdleTimeOutNanos();

    boolean isWaitForReady();

    Executor getOverridingExecutor();

    String getBalancerHost();

    int getBalancerPort();

    Executor getAsynchronousFuturesExecutor();

    Map<String, GrpcCommunicationTargetConfiguration> getCommunicationTargets();

    boolean isEnableRawDataTracing();

    boolean isEnableValueTracing();

    List<ValueInterceptor<Entity, Entity>> getRequestValueInterceptors();

    List<ValueInterceptor<Entity, Entity>> getResponseValueInterceptors();

    default GrpcCommunicationTargetConfiguration getCommunicationTargetConfiguration(String serviceId) {
        return exceptionIfNull(getCommunicationTargets().get(serviceId),
                new GrpcClientException(format(GRPC_COMMUNICATION_TARGET_CONFIGURATION_NOT_FOUND, serviceId))).toBuilder().build();
    }

    GrpcClientModuleDefaultConfiguration DEFAULT_CONFIGURATION = new GrpcClientModuleDefaultConfiguration();

    @Getter
    class GrpcClientModuleDefaultConfiguration implements GrpcClientModuleConfiguration {
        private final boolean enableRawDataTracing = false;
        private final boolean enableValueTracing = false;
        @Getter(lazy = true, onMethod = @__({@SuppressWarnings("unchecked")}))
        private final List<ClientInterceptor> interceptors = initializeInterceptors();
        private final Executor asynchronousFuturesExecutor = commonPool();
        private final long timeout = DEFAULT_GRPC_DEADLINE;
        private final Executor overridingExecutor = new ForkJoinPool(DEFAULT_THREAD_POOL_SIZE);
        private final String balancerHost = LOCALHOST;
        private final int balancerPort = DEFAULT_GRPC_PORT;
        private final Map<String, GrpcCommunicationTargetConfiguration> communicationTargets = emptyMap();
        @Getter(lazy = true, onMethod = @__({@SuppressWarnings("unchecked")}))
        private final List<ValueInterceptor<Entity, Entity>> requestValueInterceptors = initializeValueInterceptors();
        @Getter(lazy = true, onMethod = @__({@SuppressWarnings("unchecked")}))
        private final List<ValueInterceptor<Entity, Entity>> responseValueInterceptors = initializeValueInterceptors();
        private long keepAliveTimeNanos = MAX_VALUE;
        private long keepAliveTimeOutNanos = DEFAULT_KEEPALIVE_TIMEOUT_NANOS;
        private boolean keepAliveWithoutCalls = false;
        private long idleTimeOutNanos = IDLE_DEFAULT_TIMEOUT;
        private boolean waitForReady = false;

        private List<ClientInterceptor> initializeInterceptors() {
            return linkedListOf(new GrpcClientTracingIdentifiersInterceptor(), new GrpcClientLoggingInterceptor());
        }

        private List<ValueInterceptor<Entity, Entity>> initializeValueInterceptors() {
            return linkedListOf(new LoggingValueInterceptor<>(this::isEnableValueTracing));
        }
    }
}
