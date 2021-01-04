/*
 * ART Java
 *
 * Copyright 2019 ART
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

package ru.art.tarantool.configuration;

import lombok.*;
import ru.art.tarantool.configuration.lua.*;
import ru.art.tarantool.model.*;
import static ru.art.tarantool.constants.TarantoolModuleConstants.*;
import static ru.art.tarantool.constants.TarantoolModuleConstants.TarantoolInstanceMode.*;
import java.util.*;

@Getter
@Builder
public class TarantoolConfiguration {
    @Builder.Default
    private final TarantoolConnectionConfiguration connectionConfiguration = TarantoolConnectionConfiguration.builder().build();
    @Builder.Default
    private final TarantoolInitialConfiguration initialConfiguration = TarantoolInitialConfiguration.builder().build();
    @Builder.Default
    private final TarantoolInstanceMode instanceMode = LOCAL;
    @Singular("entityFieldsMapping")
    private final Map<String, TarantoolEntityFieldsMapping> entityFieldsMappings;
    @Singular("replica")
    private final Set<String> replicas;
}
