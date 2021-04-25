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

package io.art.http.payload;

import io.art.core.exception.*;
import io.art.json.descriptor.*;
import io.art.message.pack.descriptor.*;
import io.art.protobuf.descriptor.*;
import io.art.value.immutable.*;
import io.art.xml.descriptor.*;
import io.art.yaml.descriptor.*;
import io.netty.buffer.*;
import lombok.experimental.*;
import static io.art.json.module.JsonModule.*;
import static io.art.message.pack.module.MessagePackModule.*;
import static io.art.protobuf.module.ProtobufModule.*;
import static io.art.value.constants.ValueModuleConstants.*;
import static io.art.value.constants.ValueModuleConstants.ValueType.*;
import static io.art.value.immutable.Value.*;
import static io.art.value.xml.XmlEntityFromEntityConverter.*;
import static io.art.xml.module.XmlModule.*;
import static io.art.yaml.module.YamlModule.*;

@UtilityClass
public class HttpPayloadWriter {
    private static final ProtobufWriter protobufWriter = protobufModule().configuration().getWriter();
    private static final JsonWriter jsonWriter = jsonModule().configuration().getWriter();
    private static final XmlWriter xmlWriter = xmlModule().configuration().getWriter();
    private static final MessagePackWriter messagePackWriter = messagePackModule().configuration().getWriter();
    private static final YamlWriter yamlWriter = yamlModule().configuration().getWriter();

    public ByteBuf writePayloadData(DataFormat dataFormat, Value value, ByteBuf buffer) {
        switch (dataFormat) {
            case PROTOBUF:
                protobufWriter.write(value, buffer);
                return buffer;
            case JSON:
                jsonWriter.write(value, buffer);
                return buffer;
            case XML:
                xmlWriter.write(value.getType() == XML ? asXml(value) : fromEntityAsTags(asEntity(value)), buffer);
                return buffer;
            case MESSAGE_PACK:
                messagePackWriter.write(value, buffer);
                return buffer;
            case YAML:
                yamlWriter.write(value, buffer);
                return buffer;
        }
        throw new ImpossibleSituationException();
    }
}
