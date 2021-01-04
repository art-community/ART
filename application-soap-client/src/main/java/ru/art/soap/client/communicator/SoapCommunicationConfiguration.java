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

package ru.art.soap.client.communicator;

import lombok.*;
import org.apache.http.*;
import org.apache.http.client.config.*;
import org.apache.http.impl.client.*;
import org.apache.http.impl.nio.client.*;
import ru.art.entity.*;
import ru.art.entity.interceptor.*;
import ru.art.entity.mapper.*;
import ru.art.http.client.constants.*;
import ru.art.http.client.constants.HttpClientModuleConstants.*;
import ru.art.http.client.handler.*;
import ru.art.http.client.interceptor.*;
import ru.art.soap.client.exception.*;
import ru.art.soap.content.mapper.*;
import static lombok.AccessLevel.*;
import static ru.art.core.checker.CheckerForEmptiness.*;
import static ru.art.core.factory.CollectionsFactory.*;
import static ru.art.http.client.constants.HttpClientModuleConstants.ConnectionClosingPolicy.CLOSE_AFTER_RESPONSE;
import static ru.art.http.client.module.HttpClientModule.*;
import static ru.art.soap.client.constants.SoapClientModuleConstants.*;
import static ru.art.soap.client.constants.SoapClientModuleConstants.OperationIdSource.*;
import static ru.art.soap.client.constants.SoapClientModuleExceptionMessages.*;
import static ru.art.soap.client.module.SoapClientModule.*;
import static ru.art.soap.content.mapper.SoapMimeToContentTypeMapper.*;
import java.nio.charset.*;
import java.util.*;

@Getter
@Setter(value = PACKAGE)
class SoapCommunicationConfiguration {
    private CloseableHttpClient httpClient;
    private CloseableHttpAsyncClient asynchronousHttpClient;
    private String url;
    private String operationId;
    private RequestConfig requestConfig = httpClientModule().getRequestConfig();
    private ValueFromModelMapper<?, XmlEntity> requestMapper;
    private ValueToModelMapper<?, XmlEntity> responseMapper;
    private String operationPrefix;
    private String operationNamespace;
    private String bodyPrefix = soapClientModule().getBodyPrefix();
    private String bodyNamespace = soapClientModule().getBodyNamespace();
    private String envelopePrefix = soapClientModule().getEnvelopePrefix();
    private String envelopeNamespace = soapClientModule().getEnvelopeNamespace();
    private SoapMimeToContentTypeMapper consumesMimeType = textXml();
    private SoapMimeToContentTypeMapper producesMimeType = textXml();
    private Charset requestCharset;
    private String requestBodyEncoding;
    private List<HttpClientInterceptor> requestInterceptors = linkedListOf();
    private List<HttpClientInterceptor> responseInterceptors = linkedListOf();
    private HttpVersion httpVersion;
    private HttpCommunicationResponseHandler<?, ?> responseHandler;
    private HttpCommunicationExceptionHandler<?> exceptionHandler;
    private HttpCommunicationCancellationHandler<?> cancellationHandler;
    private List<ValueInterceptor<XmlEntity, XmlEntity>> requestValueInterceptors = linkedListOf();
    private List<ValueInterceptor<XmlEntity, XmlEntity>> responseValueInterceptors = linkedListOf();
    private OperationIdSource operationIdSource = REQUEST;
    private ConnectionClosingPolicy connectionClosingPolicy = CLOSE_AFTER_RESPONSE;
    private boolean enableKeepAlive;

    void validateRequiredFields() {
        boolean urlIsEmpty = isEmpty(url);
        boolean operationIdIsEmpty = operationIdSource != REQUEST && isEmpty(operationId);
        boolean operationNamespaceIsEmpty = operationIdSource != REQUEST && isEmpty(operationNamespace);
        if (!urlIsEmpty && !operationIdIsEmpty && !operationNamespaceIsEmpty) {
            return;
        }
        if (urlIsEmpty && operationIdIsEmpty && operationNamespaceIsEmpty) {
            throw new SoapClientModuleException(INVALID_SOAP_COMMUNICATION_CONFIGURATION + "url, operationId, operationNamespace");
        }
        if (urlIsEmpty && operationIdIsEmpty) {
            throw new SoapClientModuleException(INVALID_SOAP_COMMUNICATION_CONFIGURATION + "url, operationId");
        }
        if (urlIsEmpty && operationNamespaceIsEmpty) {
            throw new SoapClientModuleException(INVALID_SOAP_COMMUNICATION_CONFIGURATION + "url, operationNamespace");
        }
        if (urlIsEmpty) {
            throw new SoapClientModuleException(INVALID_SOAP_COMMUNICATION_CONFIGURATION + "url");
        }
        if (operationIdIsEmpty && operationNamespaceIsEmpty) {
            throw new SoapClientModuleException(INVALID_SOAP_COMMUNICATION_CONFIGURATION + "operationId, operationNamespace");
        }
        if (operationIdIsEmpty) {
            throw new SoapClientModuleException(INVALID_SOAP_COMMUNICATION_CONFIGURATION + "operationId");
        }
        throw new SoapClientModuleException("operationNamespace");
    }
}
