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

package io.art.transport.payload;

import io.art.core.property.*;
import io.art.value.immutable.Value;
import io.netty.buffer.*;
import lombok.*;

@AllArgsConstructor
public class TransportPayload {
    @Getter
    private final ByteBuf data;

    private final LazyProperty<Value> valueProvider;

    private static final TransportPayload EMPTY = new TransportPayload(null, null);

    public boolean isEmpty() {
        return this == EMPTY;
    }

    public static TransportPayload emptyTransportPayload() {
        return EMPTY;
    }

    public Value getValue() {
        return valueProvider.get();
    }
}
