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

package io.art.communicator.decorator;

import io.art.communicator.action.*;
import io.art.communicator.configuration.*;
import io.art.core.constants.*;
import io.art.core.model.*;
import io.art.core.property.*;
import lombok.*;
import org.apache.logging.log4j.*;
import reactor.core.publisher.*;
import static io.art.communicator.constants.CommunicatorModuleConstants.LoggingMessages.*;
import static io.art.communicator.module.CommunicatorModule.*;
import static io.art.core.constants.CompilerSuppressingWarnings.*;
import static io.art.core.constants.StringConstants.*;
import static io.art.core.model.CommunicatorActionIdentifier.*;
import static io.art.core.property.Property.*;
import static io.art.logging.LoggingModule.*;
import static java.text.MessageFormat.*;
import static lombok.AccessLevel.*;
import java.util.*;
import java.util.function.*;

public class CommunicatorLoggingDecorator implements UnaryOperator<Flux<Object>> {
    private final MethodDecoratorScope scope;
    private final CommunicatorAction action;
    private final Property<Boolean> enabled;

    @Getter(lazy = true, value = PRIVATE)
    private final UnaryOperator<Flux<Object>> decorator = createDecorator();

    @Getter(lazy = true, value = PRIVATE)
    private final Logger logger = logger(CommunicatorLoggingDecorator.class.getName() + SPACE + OPENING_SQUARE_BRACES + scope + CLOSING_SQUARE_BRACES);

    public CommunicatorLoggingDecorator(CommunicatorAction action, MethodDecoratorScope scope) {
        this.scope = scope;
        this.action = action;
        CommunicatorActionIdentifier communicatorAction = communicatorAction(action.getCommunicatorId(), action.getActionId());
        enabled = property(() -> configuration().isLogging(communicatorAction)).listenConsumer(() -> configuration()
                .getConsumer()
                .loggingConsumer());
    }

    @Override
    public Flux<Object> apply(Flux<Object> input) {
        return getDecorator().apply(input);
    }

    private void logSubscribe(CommunicatorAction action) {
        if (!enabled.get()) return;
        getLogger().info(format(COMMUNICATOR_SUBSCRIBED_MESSAGE, action.getCommunicatorId(), action.getActionId()));
    }

    private void logComplete(CommunicatorAction action) {
        if (!enabled.get()) return;
        getLogger().info(format(COMMUNICATOR_COMPLETED_MESSAGE, action.getCommunicatorId(), action.getActionId()));
    }

    private void logInput(Object data, CommunicatorAction action) {
        if (!enabled.get()) return;
        getLogger().info(format(COMMUNICATOR_INPUT_DATA, action.getCommunicatorId(), action.getActionId(), data));
    }

    private void logOutput(Object data, CommunicatorAction action) {
        if (!enabled.get()) return;
        getLogger().info(format(COMMUNICATOR_OUTPUT_DATA, action.getCommunicatorId(), action.getActionId(), data));
    }

    private void logException(Throwable exception, CommunicatorAction action) {
        if (!enabled.get()) return;
        getLogger().error(format(COMMUNICATOR_FAILED_MESSAGE, action.getCommunicatorId(), action.getActionId()), exception);
    }

    @SuppressWarnings(CONSTANT_CONDITIONS)
    private UnaryOperator<Flux<Object>> createDecorator() {
        switch (scope) {
            case INPUT:
                return input -> input
                        .doOnSubscribe(subscription -> logSubscribe(action))
                        .doOnNext(data -> logInput(data, action))
                        .doOnError(exception -> logException(exception, action));
            case OUTPUT:
                return output -> output
                        .doOnNext(data -> logOutput(data, action))
                        .doOnError(exception -> logException(exception, action))
                        .doOnComplete(() -> logComplete(action));
        }
        return UnaryOperator.identity();
    }

    private CommunicatorModuleConfiguration configuration() {
        return communicatorModule().configuration();
    }
}
