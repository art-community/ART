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
import io.art.value.constants.ValueModuleConstants.*;
import io.art.xml.descriptor.*;
import io.art.yaml.descriptor.*;
import io.netty.buffer.*;
import lombok.*;
import static io.art.core.property.LazyProperty.*;
import static io.art.json.module.JsonModule.*;
import static io.art.message.pack.module.MessagePackModule.*;
import static io.art.protobuf.module.ProtobufModule.*;
import static io.art.transport.payload.TransportPayload.*;
import static io.art.xml.module.XmlModule.*;
import static io.art.yaml.module.YamlModule.*;
import static lombok.AccessLevel.*;
import java.util.function.*;

@RequiredArgsConstructor
public class TransportPayloadReader {
    private final DataFormat dataFormat;

    @Getter(lazy = true, value = PRIVATE)
    private final Function<ByteBuf, TransportPayload> reader = reader(dataFormat);

    @Getter(lazy = true, value = PRIVATE)
    private static final ProtobufReader protobufReader = protobufModule().configuration().getReader();

    @Getter(lazy = true, value = PRIVATE)
    private static final JsonReader jsonReader = jsonModule().configuration().getReader();

    @Getter(lazy = true, value = PRIVATE)
    private static final XmlReader xmlReader = xmlModule().configuration().getReader();

    @Getter(lazy = true, value = PRIVATE)
    private static final MessagePackReader messagePackReader = messagePackModule().configuration().getReader();

    @Getter(lazy = true, value = PRIVATE)
    private static final YamlReader yamlReader = yamlModule().configuration().getReader();

    public TransportPayload read(ByteBuf buffer) {
        return getReader().apply(buffer);
    }

    private static Function<ByteBuf, TransportPayload> reader(DataFormat dataFormat) {
        switch (dataFormat) {
            case PROTOBUF:
                return buffer -> buffer.capacity() == 0 ? emptyTransportPayload() : new TransportPayload(buffer, lazy(() -> getProtobufReader().read(buffer)));
            case JSON:
                return buffer -> buffer.capacity() == 0 ? emptyTransportPayload() : new TransportPayload(buffer, lazy(() -> getJsonReader().read(buffer)));
            case XML:
                return buffer -> buffer.capacity() == 0 ? emptyTransportPayload() : new TransportPayload(buffer, lazy(() -> getXmlReader().read(buffer)));
            case MESSAGE_PACK:
                return buffer -> buffer.capacity() == 0 ? emptyTransportPayload() : new TransportPayload(buffer, lazy(() -> getMessagePackReader().read(buffer)));
            case YAML:
                return buffer -> buffer.capacity() == 0 ? emptyTransportPayload() : new TransportPayload(buffer, lazy(() -> getYamlReader().read(buffer)));
        }
        throw new ImpossibleSituationException();
    }
}
