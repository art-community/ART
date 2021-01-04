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

package ru.art.grpc.client.communicator;

import io.grpc.*;
import lombok.*;
import org.apache.logging.log4j.*;
import ru.art.core.lazy.*;
import ru.art.core.runnable.*;
import ru.art.core.validator.*;
import ru.art.entity.*;
import ru.art.entity.Value;
import ru.art.entity.interceptor.*;
import ru.art.entity.mapper.*;
import ru.art.grpc.client.handler.*;
import ru.art.grpc.client.model.*;
import ru.art.service.model.*;
import static java.util.Objects.*;
import static java.util.concurrent.TimeUnit.*;
import static lombok.AccessLevel.PRIVATE;
import static ru.art.core.caster.Caster.*;
import static ru.art.core.checker.CheckerForEmptiness.*;
import static ru.art.core.constants.StringConstants.*;
import static ru.art.core.lazy.LazyLoadingValue.*;
import static ru.art.core.wrapper.ExceptionWrapper.*;
import static ru.art.grpc.client.communicator.GrpcCommunicatorChannelFactory.*;
import static ru.art.grpc.client.constants.GrpcClientModuleConstants.*;
import static ru.art.logging.LoggingModule.*;
import java.util.concurrent.*;

public class GrpcCommunicatorImplementation implements GrpcCommunicator, GrpcCommunicator.GrpcAsynchronousCommunicator {
    private final GrpcCommunicationConfiguration configuration = new GrpcCommunicationConfiguration();
    private final BuilderValidator validator = new BuilderValidator(GrpcCommunicator.class.getName());
    private final LazyLoadingValue<ManagedChannel> channel = lazyValue(() -> createChannel(configuration));
    @Getter(lazy = true, value = PRIVATE)
    private static final Logger logger = loggingModule().getLogger(GrpcCommunicator.class);

    GrpcCommunicatorImplementation(String host, int port, String path) {
        configuration.setUrl(validator.notEmptyField(host, "host") + COLON + validator.notNullField(port, "port"));
        configuration.setPath(validator.notEmptyField(path, "path"));
    }

    GrpcCommunicatorImplementation(String url, String path) {
        configuration.setUrl(validator.notEmptyField(url, "url"));
        configuration.setPath(validator.notEmptyField(path, "path"));
    }

    GrpcCommunicatorImplementation(String url) {
        configuration.setUrl(validator.notEmptyField(url, "url"));
    }

    GrpcCommunicatorImplementation(GrpcCommunicationTargetConfiguration targetConfiguration) {
        configuration.setPath(validator.notEmptyField(targetConfiguration.path(), "path"));
        deadlineTimeout(targetConfiguration.timeout());
        keepAliveTimeNanos(targetConfiguration.keepAliveTimeNanos());
        keepAliveTimeNanos(targetConfiguration.keepAliveTimeOutNanos());
        keepAliveWithoutCalls(targetConfiguration.keepAliveWithoutCalls());
        if (targetConfiguration.secured()) {
            secured();
        }
        if (targetConfiguration.waitForReady()) {
            waitForReady();
        }
        if (isNotEmpty(targetConfiguration.url())) {
            configuration.setUrl(targetConfiguration.url());
            return;
        }
        configuration.setUrl(validator.notEmptyField(targetConfiguration.host(), "host")
                + COLON
                + validator.notNullField(targetConfiguration.port(), "port"));
    }

    @Override
    public GrpcCommunicator serviceId(String id) {
        configuration.setServiceId(validator.notEmptyField(id, "serviceId"));
        return this;
    }

    @Override
    public GrpcCommunicator methodId(String id) {
        configuration.setMethodId(validator.notEmptyField(id, "methodId"));
        return this;
    }

    @Override
    public GrpcCommunicator functionId(String id) {
        configuration.setServiceId(GRPC_FUNCTION_SERVICE);
        configuration.setMethodId(validator.notEmptyField(id, "functionId"));
        return this;
    }

    @Override
    public <RequestType> GrpcCommunicator requestMapper(ValueFromModelMapper<RequestType, ? extends Value> mapper) {
        configuration.setRequestMapper(validator.notNullField(cast(mapper), "requestMapper"));
        return this;
    }

    @Override
    public <ResponseType> GrpcCommunicator responseMapper(ValueToModelMapper<ResponseType, ? extends Value> mapper) {
        configuration.setResponseMapper(validator.notNullField(cast(mapper), "responseMapper"));
        return this;
    }

    @Override
    public GrpcCommunicator deadlineTimeout(long timeout) {
        configuration.setDeadlineTimeout(timeout);
        return this;
    }

    @Override
    public GrpcCommunicator waitForReady() {
        configuration.setWaitForReady(true);
        return this;
    }

    @Override
    public GrpcCommunicator addInterceptor(ClientInterceptor interceptor) {
        configuration.getInterceptors().add(validator.notNullField(interceptor, "interceptor"));
        return this;
    }

    @Override
    public GrpcCommunicator executor(Executor executor) {
        configuration.setOverrideExecutor(validator.notNullField(executor, "executor"));
        return this;
    }

    @Override
    public GrpcCommunicator secured() {
        configuration.setUseSecuredTransport(true);
        return this;
    }

    @Override
    public GrpcCommunicator keepAliveTimeNanos(long time) {
        configuration.setKeepAliveTimeNanos(time);
        return this;
    }

    @Override
    public GrpcCommunicator keepAliveTimeOutNanos(long timeOut) {
        configuration.setKeepAliveTimeNanos(timeOut);
        return this;
    }

    @Override
    public GrpcCommunicator keepAliveWithoutCalls(boolean keepAliveWithoutCalls) {
        configuration.setKeepAliveWithoutCalls(keepAliveWithoutCalls);
        return null;
    }

    @Override
    public void shutdownChannel() {
        ManagedChannel channel = this.channel.safeValue();
        if (isNull(channel) || channel.isShutdown() || channel.isTerminated()) {
            return;
        }
        ignoreException((ExceptionRunnable) () -> channel
                .shutdownNow()
                .awaitTermination(GRPC_CHANNEL_SHUTDOWN_TIMEOUT, MILLISECONDS), getLogger()::error);
    }

    @Override
    public GrpcAsynchronousCommunicator asynchronous() {
        return this;
    }

    @Override
    public <ResponseType> ServiceResponse<ResponseType> execute() {
        validator.validate();
        configuration.validateRequiredFields();
        return GrpcCommunicationExecutor.execute(channel.safeValue(), configuration, null);
    }

    @Override
    public <RequestType, ResponseType> ServiceResponse<ResponseType> execute(RequestType request) {
        request = validator.notNullField(request, "request");
        validator.validate();
        configuration.validateRequiredFields();
        return GrpcCommunicationExecutor.execute(channel.safeValue(), configuration, request);
    }

    @Override
    public GrpcCommunicator addRequestValueInterceptor(ValueInterceptor<Entity, Entity> interceptor) {
        configuration.getRequestValueInterceptors().add(validator.notNullField(interceptor, "requestValueInterceptor"));
        return this;
    }

    @Override
    public GrpcCommunicator addResponseValueInterceptor(ValueInterceptor<Entity, Entity> interceptor) {
        configuration.getResponseValueInterceptors().add(validator.notNullField(interceptor, "responseValueInterceptor"));
        return this;
    }

    @Override
    public GrpcAsynchronousCommunicator asynchronousFuturesExecutor(Executor executor) {
        configuration.setAsynchronousFuturesExecutor(validator.notNullField(executor, "asynchronousFuturesExecutor"));
        return this;
    }

    @Override
    public <RequestType, ResponseType> GrpcAsynchronousCommunicator completionHandler(GrpcCommunicationCompletionHandler<RequestType, ResponseType> completionHandler) {
        configuration.setCompletionHandler(validator.notNullField(completionHandler, "completionHandler"));
        return this;
    }

    @Override
    public <RequestType> GrpcAsynchronousCommunicator exceptionHandler(GrpcCommunicationExceptionHandler<RequestType> exceptionHandler) {
        configuration.setExceptionHandler(validator.notNullField(exceptionHandler, "exceptionHandler"));
        return this;
    }

    @Override
    public <ResponseType> CompletableFuture<ServiceResponse<ResponseType>> executeAsynchronous() {
        validator.validate();
        configuration.validateRequiredFields();
        return GrpcCommunicationAsynchronousExecutor.execute(channel.safeValue(), configuration, null);
    }

    @Override
    public <RequestType, ResponseType> CompletableFuture<ServiceResponse<ResponseType>> executeAsynchronous(RequestType request) {
        request = validator.notNullField(request, "request");
        validator.validate();
        configuration.validateRequiredFields();
        return GrpcCommunicationAsynchronousExecutor.execute(channel.safeValue(), configuration, request);
    }
}
