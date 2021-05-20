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
import io.art.logging.logger.*;
import io.art.value.descriptor.Reader;
import io.art.value.immutable.*;
import io.art.xml.exception.*;
import lombok.*;
import static io.art.core.checker.EmptinessChecker.*;
import static io.art.core.collection.ImmutableMap.*;
import static io.art.logging.module.LoggingModule.*;
import static io.art.value.immutable.XmlEntity.*;
import static io.art.xml.constants.XmlMappingExceptionMessages.*;
import static java.util.Objects.*;
import static javax.xml.stream.XMLStreamConstants.*;
import static lombok.AccessLevel.*;
import javax.xml.stream.*;
import java.io.*;

@AllArgsConstructor
public class XmlReader implements Reader<XmlEntity> {
    @Getter(lazy = true, value = PRIVATE)
    private static final Logger logger = logger(XmlWriter.class);
    private final XMLInputFactory xmlInputFactory;

    @Override
    public XmlEntity read(InputStream inputStream) {
        if (isEmpty(inputStream)) return null;
        XMLStreamReader reader = null;
        try {
            reader = xmlInputFactory.createXMLStreamReader(inputStream);
            XmlEntityBuilder root = getRootElement(reader);
            return root.create();
        } catch (Throwable throwable) {
            throw new XmlException(throwable);
        } finally {
            if (nonNull(reader)) {
                try {
                    reader.close();
                } catch (Throwable throwable) {
                    getLogger().error(throwable.getMessage(), throwable);
                }
            }
        }
    }


    private static XmlEntityBuilder getRootElement(XMLStreamReader parser) throws XMLStreamException {
        if (parser.next() == START_ELEMENT) {
            String prefix = parser.getPrefix();
            ImmutableMap<String, String> attributes = getAttributes(parser);
            ImmutableMap<String, String> namespaces = getNamespaces(parser);
            XmlEntityBuilder rootElement = parseXml(parser);
            rootElement.stringAttributes(attributes);
            rootElement.namespaceFields(namespaces);
            rootElement.prefix(prefix);
            return rootElement;
        }
        throw new XmlException(INCORRECT_XML_STRUCTURE);
    }

    private static XmlEntityBuilder parseXml(XMLStreamReader parser) throws XMLStreamException {
        XmlEntityBuilder xmlEntityBuilder = xmlEntityBuilder();

        int eventType;

        while (parser.hasNext()) {

            eventType = parser.next();

            if (parser.isWhiteSpace()) {
                continue;
            }

            switch (eventType) {
                case START_ELEMENT:
                    ImmutableMap<String, String> attributes = getAttributes(parser);
                    ImmutableMap<String, String> namespaces = getNamespaces(parser);
                    XmlEntityBuilder child = parseXml(parser);
                    child.stringAttributes(attributes);
                    child.namespaceFields(namespaces);
                    xmlEntityBuilder.child(child.create());
                    break;

                case CDATA:
                case SPACE:
                case CHARACTERS:
                    xmlEntityBuilder.value(parser.getText());
                    break;

                case END_ELEMENT:
                    xmlEntityBuilder.tag(parser.getLocalName());
                    xmlEntityBuilder.prefix(parser.getPrefix());
                    xmlEntityBuilder.namespace(parser.getNamespaceURI());
                    return xmlEntityBuilder;

                case END_DOCUMENT:
                    return xmlEntityBuilder;
            }
        }

        throw new XmlException(XML_FILE_HAS_NOT_END_DOCUMENT_TAG);
    }

    private static ImmutableMap<String, String> getAttributes(XMLStreamReader parser) {
        int attributeCount = parser.getAttributeCount();
        if (attributeCount == 0) {
            return emptyImmutableMap();
        }

        ImmutableMap.Builder<String, String> attributes = immutableMapBuilder(attributeCount);
        for (int index = 0; index < attributeCount; index++) {
            attributes.put(parser.getAttributeLocalName(index), parser.getAttributeValue(index));
        }
        return attributes.build();
    }

    private static ImmutableMap<String, String> getNamespaces(XMLStreamReader parser) {
        int namespaceCount = parser.getNamespaceCount();
        if (namespaceCount == 0) {
            return emptyImmutableMap();
        }

        ImmutableMap.Builder<String, String> namespaces = immutableMapBuilder(namespaceCount);
        for (int index = 0; index < namespaceCount; index++) {
            namespaces.put(parser.getNamespacePrefix(index), parser.getNamespaceURI(index));
        }
        return namespaces.build();
    }
}
