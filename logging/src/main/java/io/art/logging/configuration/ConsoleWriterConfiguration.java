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

package io.art.logging.configuration;

import io.art.core.source.*;
import lombok.*;
import static io.art.core.context.Context.*;
import java.nio.charset.*;

@Getter
@Builder
public class ConsoleWriterConfiguration {
    @Builder.Default
    private final Boolean colored = false;

    @Builder.Default
    private final Charset charset = context().configuration().getCharset();

    public static ConsoleWriterConfiguration from(ConfigurationSource source) {
        return ConsoleWriterConfiguration.builder().build();
    }
}
