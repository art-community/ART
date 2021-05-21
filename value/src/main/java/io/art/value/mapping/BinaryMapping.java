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

package io.art.value.mapping;

import io.art.core.annotation.*;
import io.art.value.immutable.*;
import io.art.value.mapper.ValueFromModelMapper.*;
import io.art.value.mapper.ValueToModelMapper.*;
import lombok.experimental.*;
import static io.art.core.checker.NullityChecker.let;

@UtilityClass
@ForGenerator
public class BinaryMapping {
    public static BinaryFromModelMapper<byte[]> fromBinary = binary -> let(binary, BinaryValue::new);
    public static BinaryToModelMapper<byte[]> toBinary = binary -> let(binary, BinaryValue::getContent);
}
