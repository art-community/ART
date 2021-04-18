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

package io.art.value.mapper;

import lombok.*;
import io.art.value.immutable.Value;
import static lombok.AccessLevel.*;

@Getter
@Builder(toBuilder = true)
@AllArgsConstructor(access = PRIVATE)
public class ValueMapper<T, V extends Value> {
    private final ValueFromModelMapper<T, V> fromModel;
    private final ValueToModelMapper<T, V> toModel;

    public static <T, V extends Value> ValueMapper<T, V> mapper(ValueFromModelMapper<T, V> fromModel, ValueToModelMapper<T, V> toModel) {
        return new ValueMapper<>(fromModel, toModel);
    }

    public T toModel(V value) {
        return toModel.map(value);
    }

    public V fromModel(T model) {
        return fromModel.map(model);
    }
}
