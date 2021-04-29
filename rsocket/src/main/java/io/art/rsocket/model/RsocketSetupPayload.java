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

package io.art.rsocket.model;

import io.art.core.model.*;
import io.art.value.builder.*;
import io.art.value.constants.ValueModuleConstants.*;
import io.art.value.immutable.*;
import lombok.*;
import static io.art.core.checker.NullityChecker.*;
import static io.art.rsocket.constants.RsocketModuleConstants.Fields.*;
import static io.art.value.constants.ValueModuleConstants.Fields.*;
import static io.art.value.factory.PrimitivesFactory.*;
import static io.art.value.immutable.Entity.*;
import static io.art.value.mapping.ServiceMethodMapping.*;

@ToString
@Builder(toBuilder = true)
public class RsocketSetupPayload {
    private final DataFormat dataFormat;
    private final DataFormat metadataFormat;
    private final ServiceMethodIdentifier serviceMethod;

    public Entity toEntity() {
        EntityBuilder entityBuilder = entityBuilder()
                .put(SETUP_PAYLOAD_DATA_FORMAT_FIELD, stringPrimitive(dataFormat.getFormat()))
                .put(SETUP_PAYLOAD_META_DATA_FORMAT_FIELD, stringPrimitive(metadataFormat.getFormat()));
        apply(serviceMethod, () -> entityBuilder.put(stringPrimitive(SERVICE_METHOD_IDENTIFIERS_KEY), fromServiceMethod(serviceMethod)));
        return entityBuilder.build();
    }
}
