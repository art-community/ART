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
import static io.art.core.factory.MapFactory.*;
import static lombok.AccessLevel.*;
import java.util.*;

@Getter
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(staticName = "metaType")
@UsedByGenerator
public class MetaType<T> {
    private final Class<T> type;
    private final Map<String, MetaField<?>> fields;
    private final Map<String, MetaMethod<?>> methods;

    public <F> MetaField<F> field(String name) {
        return cast(fields.get(name));
    }

    public <M> MetaMethod<M> method(String name) {
        return cast(methods.get(name));
    }

    @RequiredArgsConstructor(access = PRIVATE)
    public static class MetaTypeBuilder {
        private final Class<?> type;
        private final Map<String, MetaField<?>> fields = map();
        private final Map<String, MetaMethod<?>> methods = map();
    }

    public abstract static class MetaTypeProvider {
        protected abstract MetaTypeBuilder builder();

        protected MetaTypeBuilder metaTypeOf(Class<?> type) {
            return new MetaTypeBuilder(type);
        }

        protected <F> MetaField<F> add(MetaField<F> field) {
            builder().fields.put(field.getName(), field);
            return field;
        }

        protected <M> MetaMethod<M> add(MetaMethod<M> method) {
            builder().methods.put(method.getName(), method);
            return method;
        }

        protected <T> MetaType<T> provide() {
            MetaTypeBuilder builder = builder();
            return cast(metaType(builder.type, builder.fields, builder.methods));
        }
    }
}
