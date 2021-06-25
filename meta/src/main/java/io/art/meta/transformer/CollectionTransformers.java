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

package io.art.meta.transformer;

import lombok.experimental.*;
import static io.art.core.caster.Caster.*;
import static io.art.core.factory.ArrayFactory.*;
import java.util.*;

@UtilityClass
public class CollectionTransformers {
    public static MetaTransformer<Collection<?>> collectionTransformer(MetaTransformer<Object[]> parameterTransformer) {
        return new MetaTransformer<Collection<?>>() {
            public Collection<?> transform(Collection<?> value) {
                return value;
            }

            public Collection<?> transform(List<?> value) {
                Collection<?> collection = dynamicArray(value.size());
                for (Object element : value) {
                    collection.add(cast(parameterTransformer.transform(element)));
                }
                return collection;
            }
        };
    }
}
