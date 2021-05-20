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

package io.art.value.descriptor;

import io.art.core.stream.*;
import io.art.value.immutable.*;
import io.netty.buffer.*;
import static io.art.core.constants.BufferConstants.*;
import static io.art.core.context.Context.*;
import static io.art.core.extensions.NioBufferExtensions.*;
import static java.nio.ByteBuffer.*;
import java.io.*;
import java.nio.*;
import java.nio.charset.*;
import java.util.function.*;

public interface Writer<T extends Value> {
    default void write(T value, ByteBuffer buffer, Function<IOException, ? extends RuntimeException> catcher) {
        try (NioByteBufferOutputStream outputStream = new NioByteBufferOutputStream(buffer)) {
            write(value, outputStream);
        } catch (IOException ioException) {
            throw catcher.apply(ioException);
        }
    }

    default void write(T value, ByteBuf buffer, Function<IOException, ? extends RuntimeException> catcher) {
        try (ByteBufOutputStream outputStream = new ByteBufOutputStream(buffer)) {
            write(value, outputStream);
        } catch (IOException ioException) {
            throw catcher.apply(ioException);
        }
    }

    default byte[] writeToBytes(T value) {
        ByteBuffer buffer = allocate(DEFAULT_BUFFER_SIZE);
        try {
            return writeToBytes(value, buffer);
        } finally {
            buffer.clear();
        }
    }

    default byte[] writeToBytes(T value, ByteBuffer buffer) {
        write(value, buffer);
        return toByteArray(buffer);
    }

    default String writeToString(T value) {
        return writeToString(value, context().configuration().getCharset());
    }

    default String writeToString(T value, ByteBuffer buffer) {
        return writeToString(value, buffer, context().configuration().getCharset());
    }

    default String writeToString(T value, Charset charset) {
        return new String(writeToBytes(value), charset);
    }

    default String writeToString(T value, ByteBuffer buffer, Charset charset) {
        return new String(writeToBytes(value, buffer), charset);
    }

    default void write(T value, OutputStream outputStream) {
        write(value, outputStream, context().configuration().getCharset());
    }

    void write(T value, OutputStream outputStream, Charset charset);

    void write(T value, ByteBuffer buffer);

    void write(T value, ByteBuf buffer);
}
