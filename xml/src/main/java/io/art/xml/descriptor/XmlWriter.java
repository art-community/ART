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

package io.art.xml.descriptor;

import io.art.core.collection.*;
import io.art.value.descriptor.Writer;
import io.art.value.immutable.*;
import io.art.xml.exception.*;
import io.netty.buffer.*;
import lombok.*;
import org.apache.logging.log4j.*;
import static io.art.core.checker.EmptinessChecker.*;
import static io.art.core.context.Context.*;
import static io.art.logging.LoggingModule.*;
import static io.art.value.constants.ValueModuleConstants.ValueType.XmlValueType.*;
import static io.art.xml.constants.XmlDocumentConstants.*;
import static java.util.Objects.*;
import static lombok.AccessLevel.*;
import javax.xml.stream.*;
import java.io.*;
import java.nio.*;
import java.util.*;

@AllArgsConstructor
public class XmlWriter implements Writer<XmlEntity> {
    @Getter(lazy = true, value = PRIVATE)
    private final Logger logger = logger(XmlWriter.class);
    private final XMLOutputFactory outputFactory;

    @Override
    public void write(XmlEntity entity, OutputStream outputStream) {
        if (isNull(entity)) return;
        XMLStreamWriter writer = null;
        try {
            writer = outputFactory.createXMLStreamWriter(outputStream, context().configuration().getCharset().name());
            writeAllElements(writer, entity);
        } catch (Throwable throwable) {
            throw new XmlException(throwable);
        } finally {
            if (nonNull(writer)) {
                try {
                    writer.close();
                } catch (Throwable throwable) {
                    getLogger().error(throwable.getMessage(), throwable);
                }
            }
        }
    }

    @Override
    public void write(XmlEntity value, ByteBuffer buffer) {
        write(value, buffer, XmlException::new);
    }

    @Override
    public void write(XmlEntity value, ByteBuf buffer) {
        write(value, buffer, XmlException::new);
    }


    private void writeAllElements(XMLStreamWriter xmlStreamWriter, XmlEntity xmlEntity) throws XMLStreamException {
        writeStartDocument(xmlStreamWriter);
        writeXmlEntity(xmlStreamWriter, xmlEntity);
        writeEndDocument(xmlStreamWriter);
    }

    private void writeXmlEntity(XMLStreamWriter xmlStreamWriter, XmlEntity entity) throws XMLStreamException {
        ImmutableArray<XmlEntity> children = entity.getChildren();

        if (isEmpty(entity.getTag())) {
            for (XmlEntity xmlEntity : children) {
                if (isNull(xmlEntity)) continue;
                writeXmlEntity(xmlStreamWriter, xmlEntity);
            }
            return;
        }

        writeStartElement(xmlStreamWriter, entity);
        writeNamespaces(xmlStreamWriter, entity);
        writeAttributes(xmlStreamWriter, entity);

        for (XmlEntity xmlEntity : children) {
            if (isNull(xmlEntity)) continue;
            writeXmlEntity(xmlStreamWriter, xmlEntity);
        }

        writeValue(xmlStreamWriter, entity);
        writeEndElement(xmlStreamWriter);
    }

    private void writeValue(XMLStreamWriter xmlStreamWriter, XmlEntity entity) throws XMLStreamException {
        if (entity.isCData()) {
            writeCData(xmlStreamWriter, entity);
            return;
        }
        writeCharacters(xmlStreamWriter, entity);
    }

    private void writeStartElement(XMLStreamWriter xmlStreamWriter, XmlEntity entity) throws XMLStreamException {
        String namespace = entity.getNamespace();
        String prefix = entity.getPrefix();

        if (isNotEmpty(prefix) && isNotEmpty(namespace)) {
            xmlStreamWriter.writeStartElement(prefix, entity.getTag(), namespace);
            return;
        }
        xmlStreamWriter.writeStartElement(entity.getTag());
    }

    private void writeNamespaces(XMLStreamWriter xmlStreamWriter, XmlEntity entity) throws XMLStreamException {
        ImmutableMap<String, String> namespaces = entity.getNamespaces();
        for (Map.Entry<String, String> entry : namespaces.entrySet()) {
            xmlStreamWriter.writeNamespace(entry.getKey(), entry.getValue());
        }
    }

    private void writeAttributes(XMLStreamWriter xmlStreamWriter, XmlEntity entity) throws XMLStreamException {
        ImmutableMap<String, String> attributes = entity.getAttributes();
        for (Map.Entry<String, String> entry : attributes.entrySet()) {
            xmlStreamWriter.writeAttribute(entry.getKey(), entry.getValue());
        }
    }

    private void writeCharacters(XMLStreamWriter xmlStreamWriter, XmlEntity entity) throws XMLStreamException {
        String value = entity.getValue();
        if (nonNull(value)) {
            xmlStreamWriter.writeCharacters(value);
        }
    }

    private void writeCData(XMLStreamWriter xmlStreamWriter, XmlEntity entity) throws XMLStreamException {
        XmlValue<?> xmlValue = entity.getXmlValue();
        if (CDATA.equals(xmlValue.getType())) {
            xmlStreamWriter.writeCData(writeToString((XmlEntity) xmlValue.getValue()));
        }
    }

    private void writeEndElement(XMLStreamWriter xmlStreamWriter) throws XMLStreamException {
        xmlStreamWriter.writeEndElement();
    }

    private void writeStartDocument(XMLStreamWriter xmlStreamWriter) throws XMLStreamException {
        xmlStreamWriter.writeStartDocument(context().configuration().getCharset().name(), XML_VERSION);
    }

    private void writeEndDocument(XMLStreamWriter xmlStreamWriter) throws XMLStreamException {
        xmlStreamWriter.writeEndDocument();
    }
}
