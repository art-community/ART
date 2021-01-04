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

package ru.art.rsocket.reader;

import io.netty.buffer.*;
import io.rsocket.*;
import lombok.experimental.*;
import ru.art.entity.*;
import ru.art.rsocket.exception.*;
import static java.text.MessageFormat.*;
import static ru.art.core.wrapper.ExceptionWrapper.*;
import static ru.art.json.descriptor.JsonEntityReader.*;
import static ru.art.message.pack.descriptor.MessagePackEntityReader.*;
import static ru.art.protobuf.descriptor.ProtobufEntityReader.*;
import static ru.art.rsocket.constants.RsocketModuleConstants.ExceptionMessages.*;
import static ru.art.rsocket.constants.RsocketModuleConstants.*;
import static ru.art.rsocket.module.RsocketModule.*;
import static ru.art.xml.descriptor.XmlEntityReader.*;
import java.nio.*;

@UtilityClass
public class RsocketPayloadReader {
    public static Value readPayloadData(Payload payload, RsocketDataFormat dataFormat) {
        switch (dataFormat) {
            case PROTOBUF:
                ByteBuffer data = payload.getData();
                if (data.capacity() == 0) {
                    return null;
                }
                return readProtobuf(wrapException(() -> com.google.protobuf.Value.parseFrom(data), RsocketServerException::new));
            case JSON:
                return readJson(wrapException(payload::getDataUtf8, RsocketServerException::new));
            case XML:
                return readXml(wrapException(payload::getDataUtf8, RsocketServerException::new));
            case MESSAGE_PACK:
                ByteBuf byteBuf = payload.sliceData();
                if (byteBuf.readableBytes() == 0) {
                    return null;
                }
                byte[] bytes = new byte[byteBuf.readableBytes()];
                byteBuf.readBytes(bytes);
                return readMessagePack(bytes);
        }
        throw new RsocketException(format(UNSUPPORTED_DATA_FORMAT, rsocketModule().getDataFormat()));
    }

    public static Value readPayloadMetaData(Payload payload, RsocketDataFormat dataFormat) {
        switch (dataFormat) {
            case PROTOBUF:
                ByteBuffer metadata = payload.getMetadata();
                if (metadata.capacity() == 0) {
                    return null;
                }
                return readProtobuf(wrapException(() -> com.google.protobuf.Value.parseFrom(metadata), RsocketServerException::new));
            case JSON:
                return readJson(wrapException(payload::getMetadataUtf8, RsocketServerException::new));
            case XML:
                return readXml(wrapException(payload::getMetadataUtf8, RsocketServerException::new));
            case MESSAGE_PACK:
                ByteBuf byteBuf = payload.sliceMetadata();
                if (byteBuf.readableBytes() == 0) {
                    return null;
                }
                byte[] bytes = new byte[byteBuf.readableBytes()];
                byteBuf.readBytes(bytes);
                return readMessagePack(bytes);
        }
        throw new RsocketException(format(UNSUPPORTED_DATA_FORMAT, rsocketModule().getDataFormat()));
    }
}
