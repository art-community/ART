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

package io.art.value.xml;

import io.art.core.collection.*;
import io.art.value.builder.*;
import io.art.value.immutable.*;
import lombok.experimental.*;
import static io.art.core.checker.EmptinessChecker.*;
import static io.art.core.collector.ArrayCollector.*;
import static io.art.core.collector.MapCollector.*;
import static io.art.core.extensions.CollectionExtensions.*;
import static io.art.value.factory.ArrayValueFactory.*;
import static io.art.value.factory.EntityFactory.*;
import static io.art.value.factory.PrimitivesFactory.*;
import static io.art.value.immutable.Entity.*;
import static java.util.Objects.*;
import java.util.*;

@UtilityClass
public final class XmlEntityToEntityConverter {
    public static Entity toEntityFromTags(XmlEntity xmlEntity) {
        if (Value.valueIsNull(xmlEntity)) return null;
        EntityBuilder entityBuilder = entityBuilder();
        String value = xmlEntity.getValue();
        if (isNotEmpty(value)) {
            entityBuilder.put(xmlEntity.getTag(), stringPrimitive(value));
        }
        ImmutableArray<XmlEntity> children = xmlEntity.getChildren();
        if (isEmpty(children)) return entityBuilder.build();
        if (areAllUnique(children.stream().map(XmlEntity::getTag).collect(arrayCollector()))) {
            EntityBuilder innerEntityBuilder = entityBuilder();
            for (XmlEntity child : children) {
                if (isEmpty(child.getChildren())) {
                    innerEntityBuilder.put(child.getTag(), stringPrimitive(child.getValue()));
                    continue;
                }
                Entity innerEntity = toEntityFromTags(child);
                if (isNull(innerEntity)) continue;
                innerEntityBuilder.put(child.getTag(), innerEntity.get(child.getTag()));
            }
            return entityBuilder.put(xmlEntity.getTag(), innerEntityBuilder.build()).build();
        }
        ImmutableArray.Builder<Value> collection = ImmutableArray.immutableArrayBuilder(children.size());
        for (XmlEntity child : children) {
            if (isEmpty(child.getChildren()) && isEmpty(child.getValue())) {
                collection.add(stringPrimitive(child.getTag()));
                continue;
            }
            if (isEmpty(child.getChildren())) {
                collection.add(entityBuilder().put(child.getTag(), stringPrimitive(child.getValue())).build());
                continue;
            }
            Entity entity = toEntityFromTags(child);
            if (nonNull(entity)) {
                collection.add(entity);
            }
        }
        return entityBuilder.put(xmlEntity.getTag(), array(collection.build())).build();
    }

    public static Entity toEntityFromAttributes(XmlEntity xmlEntity) {
        if (Value.valueIsEmpty(xmlEntity)) {
            return null;
        }
        Map<Primitive, Primitive> attributes = xmlEntity.getAttributes()
                .entrySet()
                .stream()
                .collect(mapCollector(entry -> stringPrimitive(entry.getKey()), entry -> stringPrimitive(entry.getValue())));
        return entityBuilder().put(xmlEntity.getTag(), entity(attributes.keySet(), attributes::get)).build();
    }
}
