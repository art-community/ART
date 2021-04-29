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

package io.art.server.specification;

import io.art.core.annotation.*;
import io.art.core.caster.*;
import io.art.core.collection.*;
import io.art.core.constants.*;
import io.art.core.exception.*;
import io.art.server.configuration.*;
import io.art.server.decorator.*;
import io.art.server.implementation.*;
import io.art.value.immutable.Value;
import io.art.value.mapper.*;
import lombok.*;
import org.reactivestreams.*;
import reactor.core.publisher.*;
import reactor.core.scheduler.*;
import reactor.util.context.*;
import static io.art.core.caster.Caster.*;
import static io.art.core.constants.MethodDecoratorScope.*;
import static io.art.core.constants.MethodProcessingMode.*;
import static io.art.core.factory.ArrayFactory.*;
import static io.art.core.model.ServiceMethodIdentifier.*;
import static io.art.server.constants.ServerModuleConstants.StateKeys.*;
import static io.art.server.module.ServerModule.*;
import static io.art.server.state.ServerModuleState.ServerThreadLocalState.*;
import static java.util.Objects.*;
import static java.util.function.UnaryOperator.*;
import static lombok.AccessLevel.*;
import java.util.*;
import java.util.function.*;

@Builder(toBuilder = true)
@UsedByGenerator
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ServiceMethodSpecification {
    @Getter
    @EqualsAndHashCode.Include
    private final String methodId;

    @Getter
    @EqualsAndHashCode.Include
    private final String serviceId;

    @Getter
    private final MethodProcessingMode inputMode;

    @Getter
    private final MethodProcessingMode outputMode;

    @Getter
    private final ValueToModelMapper<?, ? extends Value> inputMapper;

    @Getter
    private final ValueFromModelMapper<?, ? extends Value> outputMapper;

    private final ServiceMethodImplementation implementation;

    private final ImmutableArray<UnaryOperator<Flux<Object>>> beforeInputDecorators = immutableArrayOf(
    );

    private final ImmutableArray<UnaryOperator<Flux<Object>>> afterInputDecorators = immutableArrayOf(
            new ServiceLoggingDecorator(this, INPUT),
            new ServiceDeactivationDecorator(this),
            new ServiceStateDecorator(this)
    );

    private final ImmutableArray<UnaryOperator<Flux<Object>>> beforeOutputDecorators = immutableArrayOf(
    );

    private final ImmutableArray<UnaryOperator<Flux<Object>>> afterOutputDecorators = immutableArrayOf(
            new ServiceLoggingDecorator(this, OUTPUT),
            new ServiceDeactivationDecorator(this)
    );

    @Singular("inputDecorator")
    private final List<UnaryOperator<Flux<Object>>> inputDecorators;

    @Singular("outputDecorator")
    private final List<UnaryOperator<Flux<Object>>> outputDecorators;

    @Getter(lazy = true)
    private final ServerModuleConfiguration configuration = serverModule().configuration();

    @Getter(lazy = true, value = PRIVATE)
    private final Function<Flux<Object>, Flux<Object>> adoptInput = adoptInput();

    @Getter(lazy = true, value = PRIVATE)
    private final Function<Flux<Object>, Flux<Object>> adoptOutput = adoptOutput();

    @Getter(lazy = true, value = PRIVATE)
    private final Function<Flux<Object>, Flux<Object>> adoptServe = adoptServe();

    @Getter(lazy = true, value = PRIVATE)
    private final Scheduler blockingScheduler = getConfiguration().getBlockingScheduler(serviceMethod(serviceId, methodId));


    public Flux<Value> serve(Flux<Value> input) {
        return input
                .transform(this::transformInput)
                .transform(getAdoptServe())
                .transform(this::transformOutput);
    }


    private Flux<Object> transformInput(Flux<Value> input) {
        return input
                .mapNotNull(value -> inputMapper.map(cast(value)))
                .transform(flux -> decorateInput(cast(flux)))
                .transform(getAdoptInput());
    }

    private Flux<Value> transformOutput(Flux<Object> output) {
        return output.transform(getAdoptOutput())
                .transform(this::decorateOutput)
                .mapNotNull(value -> (Value) outputMapper.map(cast(value)))
                .onErrorResume(Throwable.class, this::transformException);
    }


    private Flux<Value> transformException(Throwable exception) {
        return Flux
                .error(exception)
                .transform(this::decorateOutput)
                .map(Caster::cast);
    }


    private Flux<Object> decorateInput(Flux<Object> input) {
        Flux<Object> result = input;
        for (UnaryOperator<Flux<Object>> decorator : beforeInputDecorators) {
            result = result.transform(decorator);
        }
        for (UnaryOperator<Flux<Object>> decorator : inputDecorators) {
            result = result.transform(decorator);
        }
        for (UnaryOperator<Flux<Object>> decorator : afterInputDecorators) {
            result = result.transform(decorator);
        }
        return result;
    }

    private Flux<Object> decorateOutput(Flux<Object> output) {
        Flux<Object> result = output;
        for (UnaryOperator<Flux<Object>> decorator : beforeOutputDecorators) {
            result = result.transform(decorator);
        }
        for (UnaryOperator<Flux<Object>> decorator : outputDecorators) {
            result = result.transform(decorator);
        }
        for (UnaryOperator<Flux<Object>> decorator : afterOutputDecorators) {
            result = result.transform(decorator);
        }
        return result;
    }

    private Function<Flux<Object>, Flux<Object>> adoptInput() {
        if (isNull(inputMode)) {
            throw new ImpossibleSituationException();
        }
        switch (inputMode) {
            case BLOCKING:
                return identity();
            case MONO:
                return input -> input.map(Mono::just);
            case FLUX:
                return Flux::just;
            default:
                throw new ImpossibleSituationException();
        }
    }

    private Function<Flux<Object>, Flux<Object>> adoptOutput() {
        if (isNull(outputMode)) {
            throw new ImpossibleSituationException();
        }
        switch (outputMode) {
            case BLOCKING:
                return identity();
            case MONO:
            case FLUX:
                return input -> {
                    Flux<Publisher<Object>> casted = cast(input);
                    return Flux.concat(casted);
                };
            default:
                throw new ImpossibleSituationException();
        }
    }

    private Function<Flux<Object>, Flux<Object>> adoptServe() {
        if (inputMode == BLOCKING || outputMode == BLOCKING) {
            return input -> input
                    .publishOn(getBlockingScheduler())
                    .transformDeferredContextual((flux, ctx) -> flux
                            .mapNotNull(entry -> processServing(entry, ctx))
                            .defaultIfEmpty(processServing(null, ctx))
                    );
        }

        return input -> input
                .transformDeferredContextual((flux, ctx) -> flux.mapNotNull(entry -> processServing(entry, ctx)));
    }

    private Object processServing(Object input, ContextView context) {
        if (context.hasKey(SPECIFICATION_KEY)) serverModule().state().localState(fromContext(context));
        return implementation.serve(input);
    }
}
