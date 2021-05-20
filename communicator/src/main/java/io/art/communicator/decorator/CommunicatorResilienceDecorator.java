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
import io.art.core.model.*;
import io.art.core.property.*;
import io.art.logging.logger.*;
import io.art.resilience.configuration.*;
import io.github.resilience4j.bulkhead.*;
import io.github.resilience4j.circuitbreaker.*;
import io.github.resilience4j.ratelimiter.*;
import io.github.resilience4j.reactor.bulkhead.operator.*;
import io.github.resilience4j.reactor.circuitbreaker.operator.*;
import io.github.resilience4j.reactor.ratelimiter.operator.*;
import io.github.resilience4j.reactor.retry.*;
import io.github.resilience4j.reactor.timelimiter.*;
import io.github.resilience4j.retry.*;
import io.github.resilience4j.timelimiter.*;
import lombok.*;
import org.reactivestreams.*;
import reactor.core.publisher.*;
import static io.art.communicator.module.CommunicatorModule.*;
import static io.art.core.model.CommunicatorActionIdentifier.*;
import static io.art.core.property.Property.*;
import static io.art.logging.module.LoggingModule.*;
import static io.art.resilience.module.ResilienceModule.*;
import static java.util.Objects.*;
import static lombok.AccessLevel.*;
import static reactor.core.publisher.Flux.*;
import java.util.function.*;

public class CommunicatorResilienceDecorator implements UnaryOperator<Flux<Object>> {
    @Getter(lazy = true, value = PRIVATE)
    private static final Logger logger = logger(CommunicatorResilienceDecorator.class);
    private final Property<ResilienceConfiguration> resilienceConfiguration;
    private final CommunicatorActionIdentifier communicatorAction;

    public CommunicatorResilienceDecorator(CommunicatorAction action) {
        communicatorAction = communicatorAction(action.getCommunicatorId(), action.getActionId());
        this.resilienceConfiguration = property(resilienceConfiguration(communicatorAction)).listenConsumer(() -> configuration()
                .getConsumer()
                .resilienceConsumer());
    }

    @Override
    public Flux<Object> apply(Flux<Object> input) {
        ResilienceConfiguration configuration = resilienceConfiguration.get();

        RetryConfig retry = configuration.getRetry();
        RateLimiterConfig rateLimiter = configuration.getRateLimiter();
        TimeLimiterConfig timeLimiter = configuration.getTimeLimiter();
        CircuitBreakerConfig circuitBreaker = configuration.getCircuitBreaker();
        BulkheadConfig bulkhead = configuration.getBulkhead();

        Publisher<Object> decorated = input;

        if (nonNull(rateLimiter)) {
            decorated = RateLimiterOperator.of(rateLimiter(communicatorAction.toString(), rateLimiter)).apply(input);
        }

        if (nonNull(timeLimiter)) {
            decorated = TimeLimiterOperator.of(timeLimiter(communicatorAction.toString(), timeLimiter)).apply(input);
        }

        if (nonNull(bulkhead)) {
            decorated = BulkheadOperator.of(bulkhead(communicatorAction.toString(), bulkhead)).apply(input);
        }

        if (nonNull(circuitBreaker)) {
            decorated = CircuitBreakerOperator.of(circuitBreaker(communicatorAction.toString(), circuitBreaker)).apply(input);
        }

        if (nonNull(retry)) {
            decorated = RetryOperator.of(retry(communicatorAction.toString(), retry)).apply(input);
        }

        return from(decorated);
    }

    private CommunicatorModuleConfiguration configuration() {
        return communicatorModule().configuration();
    }

    private Supplier<ResilienceConfiguration> resilienceConfiguration(CommunicatorActionIdentifier communicatorAction) {
        return () -> configuration().getResilienceConfiguration(communicatorAction);
    }
}
