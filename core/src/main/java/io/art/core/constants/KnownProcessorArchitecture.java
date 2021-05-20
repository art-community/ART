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

package io.art.core.constants;

import io.art.core.model.*;
import lombok.*;
import static io.art.core.factory.SetFactory.*;

@Getter
@AllArgsConstructor
public enum KnownProcessorArchitecture {
    X86(new ProcessorArchitecture("x86", setOf("i386", "ia-32", "i686"))),

    X86_64(new ProcessorArchitecture("x86-64", setOf("x86_64", "amd64", "x64"))),

    IA_64(new ProcessorArchitecture("ia-64", setOf("ia64"))),

    ARM_V7(new ProcessorArchitecture("arm-v7", setOf("armv7", "arm", "arm32"))),

    ARM_V8(new ProcessorArchitecture("arm-v8", setOf("aarch64")));

    private final ProcessorArchitecture architecture;
}
