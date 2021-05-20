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

import io.art.core.model.*;
import io.art.core.property.*;
import io.art.logging.logger.*;
import io.art.server.configuration.*;
import io.art.server.exception.*;
import io.art.server.refresher.*;
import io.art.server.specification.*;
import io.art.server.validation.*;
import lombok.*;
import reactor.core.publisher.*;
import static io.art.core.property.LazyProperty.*;
import static io.art.core.property.Property.*;
import static io.art.logging.module.LoggingModule.*;
import static io.art.server.constants.ServerModuleConstants.ValidationErrorPatterns.*;
import static io.art.server.module.ServerModule.*;
import static io.art.server.validation.Validators.*;
import static java.util.Objects.*;
import static lombok.AccessLevel.*;
import static reactor.core.publisher.Flux.*;
import java.util.function.*;

public class ServiceValidationDecorator implements UnaryOperator<Flux<Object>> {
    @Getter(lazy = true, value = PRIVATE)
    private static final Logger logger = logger(ServiceLoggingDecorator.class);
    private final static ValidationException NULL_EXCEPTION = new ValidationException(notNull(expression -> REQUEST_IS_NULL));
    private final Property<Boolean> enabled;
    private final Property<Boolean> deactivated;
    private final LazyProperty<Boolean> hasInput;

    public ServiceValidationDecorator(ServiceMethodIdentifier serviceMethod) {
        enabled = property(() -> configuration().isValidating(serviceMethod)).listenConsumer(() -> consumer().validationConsumer());
        deactivated = property(() -> configuration().isDeactivated(serviceMethod)).listenConsumer(() -> consumer().deactivationConsumer());
        hasInput = lazy(() -> hasInput(serviceMethod));
    }

    @Override
    public Flux<Object> apply(Flux<Object> input) {
        if (disabled()) return input;
        return input.switchIfEmpty(error(NULL_EXCEPTION.fillInStackTrace())).doOnNext(this::validateReactiveData);
    }

    private boolean hasInput(ServiceMethodIdentifier serviceMethodId) {
        return configuration()
                .getRegistry()
                .findMethodById(serviceMethodId)
                .map(ServiceMethodSpecification::getInputMapper)
                .isPresent();
    }

    private void validateReactiveData(Object data) {
        if (disabled()) return;
        if (isNull(data)) throw (ValidationException) NULL_EXCEPTION.fillInStackTrace();
        Validatable requestData = (Validatable) data;
        requestData.onValidating(new Validator(requestData));
    }

    private boolean disabled() {
        return !enabled.get() || deactivated.get() || !hasInput.get();
    }

    private ServerModuleRefresher.Consumer consumer() {
        return configuration().getConsumer();
    }

    private ServerModuleConfiguration configuration() {
        return serverModule().configuration();
    }
}
