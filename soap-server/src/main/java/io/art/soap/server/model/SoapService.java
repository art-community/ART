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

package io.art.soap.server.model;

import lombok.*;
import lombok.experimental.*;
import io.art.value.mapper.ValueFromModelMapper.*;
import io.art.value.mapper.ValueToModelMapper.*;
import io.art.http.server.interceptor.*;
import io.art.soap.content.mapper.*;
import static java.lang.Integer.*;
import static java.util.Collections.*;
import static io.art.core.factory.CollectionsFactory.*;
import static io.art.soap.content.mapper.SoapMimeToContentTypeMapper.*;
import java.util.*;

@Getter
@AllArgsConstructor
public class SoapService {
    private String path;
    private Map<@NonNull String, @NonNull SoapOperation> soapOperations;
    private String wsdlServiceUrl;
    private String wsdlResourcePath;
    private Object defaultFaultResponse;
    private boolean ignoreRequestAcceptType;
    private boolean ignoreRequestContentType;
    private XmlFromModelMapper<?> defaultFaultMapper;
    private final SoapMimeToContentTypeMapper consumes;
    private final SoapMimeToContentTypeMapper produces;
    private List<HttpServerInterceptor> requestInterceptors;
    private List<HttpServerInterceptor> responseInterceptors;


    public static SoapServiceBuilder soapService() {
        return new SoapServiceBuilder();
    }

    @Getter
    @Setter
    @Accessors(fluent = true)
    @NoArgsConstructor(staticName = "soapOperation")
    public static class SoapOperation {
        private XmlToModelMapper<?> requestMapper;
        private XmlFromModelMapper<?> responseMapper;
        private RequestValidationPolicy validationPolicy = RequestValidationPolicy.NON_VALIDATABLE;
        private List<HttpServerInterceptor> requestInterceptors = linkedListOf();
        private List<HttpServerInterceptor> responseInterceptors = linkedListOf();
        private List<ValueInterceptor<XmlEntity, XmlEntity>> requestValueInterceptors = linkedListOf();
        private List<ValueInterceptor<XmlEntity, XmlEntity>> responseValueInterceptors = linkedListOf();
        private Map<Class<? extends Throwable>, XmlFromModelMapper<?>> faultMapping = mapOf();
        private String methodId;

        public SoapOperation faultMapper(Class<? extends Throwable> exceptionClass, XmlFromModelMapper<?> mapper) {
            faultMapping.put(exceptionClass, mapper);
            return this;
        }

        public SoapOperation addRequestInterceptor(HttpServerInterceptor interceptor) {
            requestInterceptors.add(interceptor);
            return this;
        }

        public SoapOperation addResponseInterceptor(HttpServerInterceptor interceptor) {
            responseInterceptors.add(interceptor);
            return this;
        }

        public SoapOperation addRequestValueInterceptor(ValueInterceptor<XmlEntity, XmlEntity> interceptor) {
            requestValueInterceptors.add(interceptor);
            return this;
        }

        public SoapOperation addResponseValueInterceptor(ValueInterceptor<XmlEntity, XmlEntity> interceptor) {
            responseValueInterceptors.add(interceptor);
            return this;
        }

    }

    public static class SoapServiceBuilder {
        private List<@NonNull String> soapOperations$key;
        private List<@NonNull SoapOperation> soapOperations$value;
        private String wsdlServiceUrl;
        private String wsdlResourcePath;
        private Object defaultFaultResponse;
        private boolean ignoreRequestAcceptType;
        private boolean ignoreRequestContentType;
        private XmlFromModelMapper<?> defaultFaultMapper;
        private SoapMimeToContentTypeMapper consumes = textXml();
        private SoapMimeToContentTypeMapper produces = textXml();
        private List<HttpServerInterceptor> requestInterceptors = linkedListOf();
        private List<HttpServerInterceptor> responseInterceptors = linkedListOf();

        public SoapServiceBuilder operation(@NonNull String operationKey, @NonNull SoapOperation operationValue) {
            if (this.soapOperations$key == null) {
                this.soapOperations$key = dynamicArrayOf();
                this.soapOperations$value = dynamicArrayOf();
            }
            this.soapOperations$key.add(operationKey);
            this.soapOperations$value.add(operationValue);
            return this;
        }

        public SoapServiceBuilder addRequestInterceptor(HttpServerInterceptor interceptor) {
            requestInterceptors.add(interceptor);
            return this;
        }

        public SoapServiceBuilder addResponseInterceptor(HttpServerInterceptor interceptor) {
            responseInterceptors.add(interceptor);
            return this;
        }

        public SoapServiceBuilder wsdlServiceUrl(String wsdlServiceUrl) {
            this.wsdlServiceUrl = wsdlServiceUrl;
            return this;
        }

        public SoapServiceBuilder wsdlResourcePath(String wsdlResourcePath) {
            this.wsdlResourcePath = wsdlResourcePath;
            return this;
        }

        public SoapServiceBuilder defaultFaultResponse(Object defaultFaultResponse) {
            this.defaultFaultResponse = defaultFaultResponse;
            return this;
        }

        public SoapServiceBuilder ignoreRequestAcceptType(boolean ignoreRequestAcceptType) {
            this.ignoreRequestAcceptType = ignoreRequestAcceptType;
            return this;
        }

        public SoapServiceBuilder ignoreRequestContentType(boolean ignoreRequestContentType) {
            this.ignoreRequestContentType = ignoreRequestContentType;
            return this;
        }

        public SoapServiceBuilder defaultFaultMapper(XmlFromModelMapper<?> defaultFaultMapper) {
            this.defaultFaultMapper = defaultFaultMapper;
            return this;
        }

        public SoapServiceBuilder consumes(SoapMimeToContentTypeMapper consumes) {
            this.consumes = consumes;
            return this;
        }

        public SoapServiceBuilder produces(SoapMimeToContentTypeMapper produces) {
            this.produces = produces;
            return this;
        }

        public SoapService serve(String path) {
            Map<String, SoapOperation> soapOperations;
            switch (this.soapOperations$key == null ? 0 : this.soapOperations$key.size()) {
                case 0:
                    soapOperations = emptyMap();
                    break;
                case 1:
                    soapOperations = singletonMap(this.soapOperations$key.get(0), this.soapOperations$value.get(0));
                    break;
                default:
                    soapOperations = mapOf(this.soapOperations$key.size() < 1073741824 ?
                            1 + this.soapOperations$key.size() + (this.soapOperations$key.size() - 3) / 3
                            : MAX_VALUE);
                    for (int $i = 0; $i < this.soapOperations$key.size(); $i++) {
                        soapOperations.put(this.soapOperations$key.get($i), this.soapOperations$value.get($i));
                    }
                    soapOperations = unmodifiableMap(soapOperations);
            }

            return new SoapService(path,
                    soapOperations,
                    wsdlServiceUrl,
                    wsdlResourcePath,
                    defaultFaultResponse,
                    ignoreRequestAcceptType,
                    ignoreRequestContentType,
                    defaultFaultMapper,
                    consumes,
                    produces,
                    requestInterceptors,
                    responseInterceptors);
        }
    }
}
