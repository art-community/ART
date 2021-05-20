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
import io.art.core.collection.*;
import static io.art.core.caster.Caster.*;
import static io.art.core.factory.MapFactory.*;
import java.util.*;

@UsedByGenerator
public class MetaTypeRegistry {
    private final static Map<Class<?>, MetaType<?>> META_TYPES = map();

    public static void register(MetaType<?> type) {
        META_TYPES.put(type.getType(), type);
    }

    public static ImmutableMap<Class<?>, MetaType<?>> metas() {
        return immutableMapOf(META_TYPES);
    }

    public static <T> MetaType<T> metaOf(Class<?> type) {
        return cast(META_TYPES.get(type));
    }
}
