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

package io.art.value.tuple;

import io.art.value.immutable.Value;
import io.art.value.immutable.*;
import io.art.value.tuple.schema.*;
import lombok.*;
import lombok.experimental.*;
import static io.art.core.caster.Caster.*;
import static io.art.core.factory.ArrayFactory.*;
import static io.art.value.constants.ValueModuleConstants.ValueType.*;
import static io.art.value.immutable.Value.*;
import static io.art.value.tuple.schema.ValueSchema.*;
import static java.util.Objects.*;
import java.util.*;

@UtilityClass
public class PlainTupleWriter {
    public static PlainTupleWriterResult writeTuple(Value value) {
        ValueSchema schema = fromValue(value);
        if (isNull(schema) || valueIsNull(value)) return null;
        if (isPrimitive(value)) {
            return new PlainTupleWriterResult(fixedArrayOf(asPrimitive(value).getValue()), schema);
        }
        if (value.getType() == BINARY) {
            return new PlainTupleWriterResult(fixedArrayOf(asBinary(value).getContent()), schema);

        }
        return new PlainTupleWriterResult(writeComplexValue(value), schema);
    }


    private static List<?> writeComplexValue(Value value) {
        if (valueIsNull(value)) return null;

        switch (value.getType()) {
            case ENTITY:
                return writeEntity(asEntity(value));
            case ARRAY:
                return writeArray(asArray(value));
        }

        return null;
    }

    private static List<?> writeEntity(Entity entity) {
        List<?> tuple = dynamicArrayOf();
        Set<Primitive> keys = entity.asMap().keySet();
        for (Primitive key : keys) {
            if (valueIsNull(key)) continue;
            Value value = entity.get(key);
            if (valueIsNull(value)) {
                tuple.add(null);
                continue;
            }
            if (isPrimitive(value)) {
                tuple.add(cast(asPrimitive(value).getValue()));
                continue;
            }
            if (value.getType() == BINARY) {
                tuple.add(cast(asBinary(value).getContent()));
                continue;
            }
            tuple.add(cast(writeComplexValue(value)));
        }
        return tuple;
    }

    private static List<?> writeArray(ArrayValue array) {
        List<?> tuple = dynamicArrayOf();
        List<Value> valueList = array.asList();
        for (Value value : valueList) {
            if (valueIsNull(value)) {
                tuple.add(null);
                continue;
            }
            if (isPrimitive(value)) {
                tuple.add(cast(asPrimitive(value).getValue()));
                continue;
            }
            if (value.getType() == BINARY) {
                tuple.add(cast(asBinary(value).getContent()));
                continue;
            }
            tuple.add(cast(writeComplexValue(value)));
        }
        return tuple;
    }

    @Getter
    @AllArgsConstructor
    public static class PlainTupleWriterResult {
        private final List<?> tuple;
        private final ValueSchema schema;
    }
}
