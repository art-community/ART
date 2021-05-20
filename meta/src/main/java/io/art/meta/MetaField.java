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

package io.art.meta;

import io.art.core.annotation.*;
import lombok.*;
import static io.art.core.caster.Caster.*;
import static io.art.core.extensions.CollectionExtensions.*;
import static io.art.core.factory.MapFactory.*;
import java.util.*;

@Getter
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(staticName = "metaField")
@AllArgsConstructor(staticName = "metaField")
@UsedByGenerator
public class MetaField<T> {
    private final String name;
    private Class<T> type;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private final Map<Class<?>, MetaField<?>> GENERIC_CACHE = map();

    public <R> MetaField<R> reified(Class<R> generic) {
        return cast(putIfAbsent(GENERIC_CACHE, generic, () -> metaField(name, generic)));
    }
}
