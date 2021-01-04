/*
 * ART Java
 *
 * Copyright 2019 ART
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

package ru.art.core.extension;

import lombok.experimental.*;
import ru.art.core.exception.*;
import static java.lang.System.*;
import static java.nio.ByteBuffer.*;
import static java.nio.channels.Channels.*;
import static ru.art.core.constants.ArrayConstants.*;
import static ru.art.core.constants.BufferConstants.*;
import static ru.art.core.constants.StreamConstants.*;
import static ru.art.core.constants.StringConstants.*;
import static ru.art.core.context.Context.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.charset.*;

@UtilityClass
public class InputStreamExtensions {
    public static byte[] toByteArray(InputStream inputStream) {
        return toByteArray(inputStream, DEFAULT_BUFFER_SIZE);
    }

    public static byte[] toByteArray(InputStream inputStream, int bufferSize) {
        if (bufferSize <= 0) {
            return EMPTY_BYTES;
        }
        ByteBuffer buffer = allocateDirect(bufferSize);
        byte[] result = EMPTY_BYTES;
        try {
            ReadableByteChannel channel = newChannel(inputStream);
            while (channel.read(buffer) != EOF) {
                buffer.flip();
                byte[] bufferBytes = new byte[buffer.limit()];
                buffer.get(bufferBytes);
                byte[] newResult = new byte[result.length + bufferBytes.length];
                arraycopy(result, 0, newResult, 0, result.length);
                arraycopy(bufferBytes, 0, newResult, result.length, bufferBytes.length);
                result = newResult;
                buffer.clear();
            }
        } catch (IOException ioException) {
            throw new InternalRuntimeException(ioException);
        }
        return result;
    }

    public static byte[] toByteArraySafety(InputStream inputStream) {
        return toByteArraySafety(inputStream, DEFAULT_BUFFER_SIZE);
    }

    public static byte[] toByteArraySafety(InputStream inputStream, int bufferSize) {
        try {
            return toByteArray(inputStream, bufferSize);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            return EMPTY_BYTES;
        }
    }

    public static String toString(InputStream inputStream) {
        return toString(inputStream, DEFAULT_BUFFER_SIZE);
    }

    public static String toString(InputStream inputStream, int bufferSize) {
        return toString(inputStream, bufferSize, contextConfiguration().getCharset());
    }

    public static String toString(InputStream inputStream, Charset charset) {
        return toString(inputStream, DEFAULT_BUFFER_SIZE, charset);
    }

    public static String toString(InputStream inputStream, int bufferSize, Charset charset) {
        return new String(toByteArray(inputStream, bufferSize), charset);
    }
}
