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

package io.art.protobuf.module;

import io.art.core.module.*;
import io.art.protobuf.configuration.*;
import lombok.*;
import static io.art.core.context.Context.*;
import static lombok.AccessLevel.*;

@Getter
public class ProtobufModule implements StatelessModule<ProtobufModuleConfiguration, ProtobufModuleConfiguration.Configurator> {
    @Getter(lazy = true, value = PRIVATE)
    private static final StatelessModuleProxy<ProtobufModuleConfiguration> protobufModule = context().getStatelessModule(ProtobufModule.class.getSimpleName());
    private final String id = ProtobufModule.class.getSimpleName();
    private final ProtobufModuleConfiguration configuration = new ProtobufModuleConfiguration();
    private final ProtobufModuleConfiguration.Configurator configurator = new ProtobufModuleConfiguration.Configurator(configuration);

    public static StatelessModuleProxy<ProtobufModuleConfiguration> protobufModule() {
        return getProtobufModule();
    }
}
