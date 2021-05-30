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
import static io.art.core.context.Context.*;
import java.io.*;
import java.nio.*;
import java.nio.charset.*;

public interface Reader {
    default Value read(byte[] bytes) {
        return read(new ByteArrayInputStream(bytes));
    }

    default Value read(ByteBuffer nioBuffer) {
        return read(new NioByteBufferInputStream(nioBuffer));
    }

    default Value read(ByteBuf nettyBuffer) {
        return read(new ByteBufInputStream(nettyBuffer));
    }

    default Value read(String string) {
        return read(string, context().configuration().getCharset());
    }

    default Value read(String string, Charset charset) {
        return read(string.getBytes(charset));
    }

    Value read(InputStream input);
}
