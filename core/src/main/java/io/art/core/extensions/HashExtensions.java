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

package io.art.core.extensions;

import io.art.core.exception.*;
import io.art.core.property.*;
import lombok.experimental.*;
import static io.art.core.constants.AlgorithmConstants.*;
import static io.art.core.context.Context.*;
import static io.art.core.extensions.FileExtensions.*;
import static io.art.core.property.LazyProperty.*;
import static io.art.core.wrapper.ExceptionWrapper.*;
import java.io.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.security.*;

@UtilityClass
public class HashExtensions {
    private final LazyProperty<MessageDigest> md5 = lazy(() -> wrapExceptionCall(() -> MessageDigest.getInstance(MD5), InternalRuntimeException::new));

    public static byte[] md5(byte[] content) {
        return md5.get().digest(content);
    }

    public static byte[] md5(String content) {
        return md5(content, context().configuration().getCharset());
    }

    public static byte[] md5(String content, Charset charset) {
        return md5(content.getBytes(charset));
    }

    public static byte[] md5(File file) {
        return md5(file.toPath());
    }

    public static byte[] md5(Path file) {
        return md5(readFileBytes(file));
    }
}
