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
import io.art.core.exception.*;
import lombok.*;
import static io.art.core.constants.ExceptionMessages.*;
import static io.art.core.extensions.CollectionExtensions.*;
import static java.text.MessageFormat.*;
import static java.util.Arrays.*;
import java.util.*;

@Value
@AllArgsConstructor
public class ProcessorArchitecture {
    String canonicalName;
    Set<String> aliases;

    private boolean isKnown() {
        return tryAsKnown().isPresent();
    }

    private KnownProcessorArchitecture asKnown() {
        return tryAsKnown().orElseThrow(() -> new InternalRuntimeException(format(UNKNOWN_PROCESSOR_ARCHITECTURE, this)));
    }

    private Optional<KnownProcessorArchitecture> tryAsKnown() {
        return stream(KnownProcessorArchitecture.values())
                .filter((value -> value.getArchitecture().names().stream().anyMatch(names()::contains)))
                .findFirst();
    }

    public Set<String> names() {
        return addFirstToSet(canonicalName, aliases);
    }
}
