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

package io.art.transport.payload;

import io.art.core.exception.*;
import io.art.json.descriptor.*;
import io.art.message.pack.descriptor.*;
import io.art.protobuf.descriptor.*;
import io.art.value.immutable.Value;
import io.art.xml.descriptor.*;
import io.art.yaml.descriptor.*;
import io.netty.buffer.*;
import lombok.*;
import static io.art.json.module.JsonModule.*;
import static io.art.message.pack.module.MessagePackModule.*;
import static io.art.protobuf.module.ProtobufModule.*;
import static io.art.value.constants.ValueModuleConstants.*;
import static io.art.value.constants.ValueModuleConstants.ValueType.*;
import static io.art.value.immutable.Value.*;
import static io.art.value.xml.XmlEntityFromEntityConverter.*;
import static io.art.xml.module.XmlModule.*;
import static io.art.yaml.module.YamlModule.*;
import static io.netty.buffer.ByteBufAllocator.*;
import static lombok.AccessLevel.*;
import java.util.function.*;

@RequiredArgsConstructor
public class TransportPayloadWriter {
    private final DataFormat dataFormat;

    @Getter(lazy = true, value = PRIVATE)
    private final Function<Value, ByteBuf> writer = writer(dataFormat);

    @Getter(lazy = true, value = PRIVATE)
    private static final ProtobufWriter protobufWriter = protobufModule().configuration().getWriter();

    @Getter(lazy = true, value = PRIVATE)
    private static final JsonWriter jsonWriter = jsonModule().configuration().getWriter();

    @Getter(lazy = true, value = PRIVATE)
    private static final XmlWriter xmlWriter = xmlModule().configuration().getWriter();

    @Getter(lazy = true, value = PRIVATE)
    private static final MessagePackWriter messagePackWriter = messagePackModule().configuration().getWriter();

    @Getter(lazy = true, value = PRIVATE)
    private static final YamlWriter yamlWriter = yamlModule().configuration().getWriter();

    public ByteBuf write(Value value) {
        return getWriter().apply(value);
    }

    private static Function<Value, ByteBuf> writer(DataFormat dataFormat) {
        switch (dataFormat) {
            case PROTOBUF:
                return value -> {
                    ByteBuf buffer = DEFAULT.ioBuffer();
                    getProtobufWriter().write(value, buffer);
                    return buffer;
                };
            case JSON:
                return value -> {
                    ByteBuf buffer = DEFAULT.ioBuffer();
                    getJsonWriter().write(value, buffer);
                    return buffer;
                };
            case XML:
                return value -> {
                    ByteBuf buffer = DEFAULT.ioBuffer();
                    getXmlWriter().write(value.getType() == XML ? asXml(value) : fromEntityAsTags(asEntity(value)), buffer);
                    return buffer;
                };
            case MESSAGE_PACK:
                return value -> {
                    ByteBuf buffer = DEFAULT.ioBuffer();
                    getMessagePackWriter().write(value, buffer);
                    return buffer;
                };
            case YAML:
                return value -> {
                    ByteBuf buffer = DEFAULT.ioBuffer();
                    getYamlWriter().write(value, buffer);
                    return buffer;
                };
        }
        throw new ImpossibleSituationException();
    }
}
