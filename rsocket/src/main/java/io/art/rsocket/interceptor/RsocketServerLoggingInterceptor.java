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

package io.art.rsocket.interceptor;

import io.art.core.property.*;
import io.art.logging.logger.*;
import io.art.rsocket.configuration.*;
import io.rsocket.*;
import io.rsocket.plugins.*;
import lombok.*;
import static io.art.core.checker.NullityChecker.*;
import static io.art.core.property.Property.*;
import static io.art.logging.module.LoggingModule.*;
import static io.art.rsocket.module.RsocketModule.*;
import static lombok.AccessLevel.*;

public class RsocketServerLoggingInterceptor implements RSocketInterceptor {
    @Getter(lazy = true, value = PRIVATE)
    private static final Logger logger = logger(RsocketServerLoggingInterceptor.class);

    private final Property<Boolean> enabled = property(this::enabled).listenConsumer(() -> configuration()
            .getConsumer()
            .serverLoggingConsumer());

    @Override
    public RSocket apply(RSocket rsocket) {
        return new RsocketLoggingProxy(getLogger(), rsocket, enabled);
    }

    private boolean enabled() {
        return let(configuration().getServerConfiguration(), RsocketServerConfiguration::isLogging, false);
    }

    private RsocketModuleConfiguration configuration() {
        return rsocketModule().configuration();
    }
}
