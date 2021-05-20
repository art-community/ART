/*
 * ART
 *
 * Copyright 2019-2021 ART
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

package io.art.server.decorator;

import io.art.core.constants.*;
import io.art.core.model.*;
import io.art.core.property.*;
import io.art.logging.logger.*;
import io.art.server.configuration.*;
import io.art.server.specification.*;
import lombok.*;
import reactor.core.publisher.*;
import static io.art.core.constants.CompilerSuppressingWarnings.*;
import static io.art.core.constants.StringConstants.*;
import static io.art.core.model.ServiceMethodIdentifier.*;
import static io.art.core.property.Property.*;
import static io.art.logging.module.LoggingModule.*;
import static io.art.server.constants.ServerModuleConstants.LoggingMessages.*;
import static io.art.server.module.ServerModule.*;
import static java.text.MessageFormat.*;
import static lombok.AccessLevel.*;
import java.util.function.*;

public class ServiceLoggingDecorator implements UnaryOperator<Flux<Object>> {
    private final MethodDecoratorScope scope;
    private final ServiceMethodSpecification specification;
    private final Property<Boolean> enabled;

    @Getter(lazy = true, value = PRIVATE)
    private final UnaryOperator<Flux<Object>> decorator = createDecorator();

    @Getter(lazy = true, value = PRIVATE)
    private final Logger logger = logger(ServiceLoggingDecorator.class.getName() + SPACE + OPENING_SQUARE_BRACES + scope + CLOSING_SQUARE_BRACES);

    public ServiceLoggingDecorator(ServiceMethodSpecification specification, MethodDecoratorScope scope) {
        this.scope = scope;
        this.specification = specification;
        ServiceMethodIdentifier serviceMethod = serviceMethod(specification.getServiceId(), specification.getMethodId());
        enabled = property(() -> configuration().isLogging(serviceMethod)).listenConsumer(() -> configuration()
                .getConsumer()
                .loggingConsumer());
    }

    @Override
    public Flux<Object> apply(Flux<Object> input) {
        return getDecorator().apply(input);
    }

    private void logSubscribe(ServiceMethodSpecification specification) {
        if (!enabled.get()) return;
        getLogger().info(format(SERVICE_SUBSCRIBED_MESSAGE, specification.getServiceId(), specification.getMethodId()));
    }

    private void logComplete(ServiceMethodSpecification specification) {
        if (!enabled.get()) return;
        getLogger().info(format(SERVICE_COMPLETED_MESSAGE, specification.getServiceId(), specification.getMethodId()));
    }

    private void logInput(Object data, ServiceMethodSpecification specification) {
        if (!enabled.get()) return;
        getLogger().info(format(SERVICE_INPUT_DATA, specification.getServiceId(), specification.getMethodId(), data));
    }

    private void logOutput(Object data, ServiceMethodSpecification specification) {
        if (!enabled.get()) return;
        getLogger().info(format(SERVICE_OUTPUT_DATA, specification.getServiceId(), specification.getMethodId(), data));
    }

    private void logException(Throwable exception, ServiceMethodSpecification specification) {
        if (!enabled.get()) return;
        getLogger().error(format(SERVICE_FAILED_MESSAGE, specification.getServiceId(), specification.getMethodId()), exception);
    }

    @SuppressWarnings(CONSTANT_CONDITIONS)
    private UnaryOperator<Flux<Object>> createDecorator() {
        switch (scope) {
            case INPUT:
                return input -> input
                        .doOnSubscribe(subscription -> logSubscribe(specification))
                        .doOnNext(data -> logInput(data, specification))
                        .doOnError(exception -> logException(exception, specification));
            case OUTPUT:
                return output -> output
                        .doOnNext(data -> logOutput(data, specification))
                        .doOnError(exception -> logException(exception, specification))
                        .doOnComplete(() -> logComplete(specification));
        }
        return UnaryOperator.identity();
    }

    private ServerModuleConfiguration configuration() {
        return serverModule().configuration();
    }
}
