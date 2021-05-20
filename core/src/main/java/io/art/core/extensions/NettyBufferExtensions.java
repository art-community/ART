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

import io.netty.buffer.*;
import lombok.experimental.*;
import static io.art.core.checker.EmptinessChecker.*;
import static io.art.core.constants.ArrayConstants.*;
import static io.art.core.context.Context.*;
import static io.art.core.extensions.NioBufferExtensions.*;
import static io.netty.buffer.ByteBufAllocator.*;
import static io.netty.buffer.Unpooled.*;
import static java.nio.ByteBuffer.*;
import java.io.*;
import java.nio.*;
import java.nio.charset.*;

@UtilityClass
public class NettyBufferExtensions {
    public static ByteBuf from(InputStream value) {
        return from(InputStreamExtensions.toByteArray(value));
    }

    public static ByteBuf from(String value) {
        return from(value.getBytes());
    }

    public static ByteBuf from(String value, Charset charset) {
        return from(value.getBytes(charset));
    }

    public static ByteBuf from(byte[] array) {
        if (isEmpty(array)) {
            return EMPTY_BUFFER;
        }
        ByteBuf buffer = DEFAULT.buffer(array.length);
        buffer.writeBytes(array);
        return buffer;
    }

    public static ByteBuf from(ByteBuffer buffer) {
        return toNettyBuffer(buffer);
    }

    public static byte[] toByteArray(ByteBuf buffer) {
        if (isEmpty(buffer)) {
            return EMPTY_BYTES;
        }
        byte[] bytes = new byte[buffer.readableBytes()];
        buffer.readBytes(bytes);
        buffer.resetReaderIndex();
        return bytes;
    }

    public static ByteBuffer toNioBuffer(ByteBuf buffer) {
        if (buffer.isDirect()) {
            return buffer.nioBuffer();
        }
        return wrap(toByteArray(buffer));
    }

    public static String toString(ByteBuf buffer) {
        return new String(toByteArray(buffer), context().configuration().getCharset());
    }

    public static String toString(ByteBuf buffer, Charset charset) {
        return new String(toByteArray(buffer), charset);
    }
}
