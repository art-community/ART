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

package io.art.soap.client.communicator;

import lombok.*;
import io.art.value.mapper.ValueFromModelMapper.*;
import io.art.value.mapper.ValueToModelMapper.*;
import static java.util.Objects.*;
import static lombok.AccessLevel.*;
import static io.art.core.caster.Caster.*;
import static io.art.soap.client.communicator.SoapEnvelopWrappingManager.*;

@NoArgsConstructor(access = PACKAGE)
class SoapEntityMapping {
    static <T> XmlToModelMapper<T> soapResponseToModel(SoapCommunicationConfiguration configuration) {
        ValueToModelMapper<?, XmlEntity> responseMapper = configuration.getResponseMapper();
        if (isNull(responseMapper)) {
            return null;
        }
        return entity -> cast(responseMapper.map(unwrapFromSoapEnvelope(entity)));
    }

    static <T> XmlFromModelMapper<T> soapRequestFromModel(SoapCommunicationConfiguration configuration) {
        ValueFromModelMapper<?, XmlEntity> requestMapper = configuration.getRequestMapper();
        if (isNull(requestMapper)) {
            return null;
        }
        return model -> wrapToSoapEnvelop(requestMapper.map(cast(model)), configuration);
    }
}
