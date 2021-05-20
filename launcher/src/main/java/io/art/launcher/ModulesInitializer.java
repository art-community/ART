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

package io.art.launcher;

import io.art.communicator.module.*;
import io.art.configurator.module.*;
import io.art.core.annotation.*;
import io.art.http.module.*;
import io.art.logging.module.*;
import io.art.rsocket.module.*;
import io.art.server.module.*;
import io.art.storage.module.*;
import io.art.value.module.*;
import lombok.*;
import lombok.experimental.*;
import static java.util.function.UnaryOperator.*;
import java.util.function.*;

@Getter
@Setter
@UsedByGenerator
@Accessors(fluent = true)
public class ModulesInitializer {
    private UnaryOperator<ConfiguratorInitializer> configurator = identity();
    private UnaryOperator<LoggingInitializer> logging = identity();
    private UnaryOperator<ServerInitializer> server = identity();
    private UnaryOperator<CommunicatorInitializer> communicator = identity();
    private UnaryOperator<ValueInitializer> value = identity();
    private UnaryOperator<RsocketInitializer> rsocket = identity();
    private UnaryOperator<HttpInitializer> http = identity();
    private UnaryOperator<StorageInitializer> storage = identity();

    public static ModulesInitializer modules() {
        return new ModulesInitializer();
    }
}
