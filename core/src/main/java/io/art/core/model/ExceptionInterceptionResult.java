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

package io.art.core.model;

import io.art.core.constants.*;
import lombok.*;
import static io.art.core.constants.ExceptionInterceptionStrategy.*;

@Getter
@Builder(builderMethodName = "exceptionInterceptionResult")
public class ExceptionInterceptionResult {
    @Builder.Default
    private final ExceptionInterceptionStrategy strategy = NEXT;
    private final Throwable inException;
    private final Throwable outException;
    private final Object fallback;

    public static ExceptionInterceptionResult next(Throwable in) {
        return exceptionInterceptionResult()
                .inException(in)
                .outException(in)
                .build();
    }

    public static ExceptionInterceptionResult next(Throwable in, Throwable out) {
        return exceptionInterceptionResult()
                .inException(in)
                .outException(out)
                .build();
    }

    public static ExceptionInterceptionResult throwException(Throwable in) {
        return exceptionInterceptionResult()
                .inException(in)
                .outException(in)
                .strategy(THROW_EXCEPTION)
                .build();
    }

    public static ExceptionInterceptionResult throwException(Throwable in, Throwable out) {
        return exceptionInterceptionResult()
                .inException(in)
                .outException(out)
                .strategy(THROW_EXCEPTION)
                .build();
    }

    public static ExceptionInterceptionResult returnFallback(Throwable in, Object fallback) {
        return exceptionInterceptionResult()
                .inException(in)
                .fallback(fallback)
                .strategy(RETURN_FALLBACK)
                .build();
    }
}
