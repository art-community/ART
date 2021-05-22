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
import io.art.core.exception.*;
import lombok.*;
import static io.art.core.caster.Caster.*;
import static io.art.core.factory.MapFactory.*;
import java.util.*;

@ForGenerator
@EqualsAndHashCode
public abstract class MetaConstructor<C> {
    private final MetaClass<C> owner;
    private final Map<String, MetaParameter<?>> parameters = map();

    protected MetaConstructor(MetaClass<C> owner) {
        this.owner = owner;
    }

    protected <T> MetaParameter<T> register(MetaParameter<T> parameter) {
        parameters.put(parameter.name(), parameter);
        return parameter;
    }

    public MetaClass<C> type() {
        return owner;
    }

    public <T> MetaParameter<T> parameter(String name) {
        return cast(parameters.get(name));
    }

    public ImmutableMap<String, MetaParameter<?>> parameters() {
        return immutableMapOf(parameters);
    }

    public C invoke() {
        throw new NotImplementedException("");
    }

    public C invoke(Object argument) {
        throw new NotImplementedException("");
    }

    public abstract C invoke(Object... arguments);
}
