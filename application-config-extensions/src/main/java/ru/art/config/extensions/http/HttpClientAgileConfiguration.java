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

package ru.art.config.extensions.http;

import lombok.*;
import org.apache.http.client.config.*;
import org.apache.http.config.*;
import org.apache.http.entity.*;
import org.apache.http.impl.nio.reactor.*;
import ru.art.core.mime.*;
import ru.art.http.client.configuration.HttpClientModuleConfiguration.*;
import ru.art.http.client.model.*;
import ru.art.http.constants.*;
import ru.art.http.mapper.*;
import static org.apache.http.entity.ContentType.*;
import static ru.art.config.extensions.ConfigExtensions.*;
import static ru.art.config.extensions.common.CommonConfigKeys.*;
import static ru.art.config.extensions.http.HttpConfigKeys.*;
import static ru.art.config.extensions.http.HttpContentMappersConfigurator.*;
import static ru.art.core.checker.CheckerForEmptiness.*;
import static ru.art.core.constants.StringConstants.*;
import static ru.art.core.constants.ThreadConstants.*;
import static ru.art.core.extension.NullCheckingExtensions.*;
import static ru.art.http.client.constants.HttpClientModuleConstants.*;
import static ru.art.http.client.model.HttpCommunicationTargetConfiguration.httpCommunicationTarget;
import static ru.art.http.constants.HttpCommonConstants.*;
import java.util.*;

@Getter
public class HttpClientAgileConfiguration extends HttpClientModuleDefaultConfiguration {
    private final Map<MimeType, HttpContentMapper> contentMappers = configureHttpContentMappers(super.getContentMappers());
    private RequestConfig requestConfig;
    private SocketConfig socketConfig;
    private IOReactorConfig ioReactorConfig;
    private String balancerHost;
    private int balancerPort;
    private Map<String, HttpCommunicationTargetConfiguration> communicationTargets;
    private boolean ssl;
    private boolean disableSslHostNameVerification;
    private String sslKeyStoreType;
    private String sslKeyStoreFilePath;
    private String sslKeyStorePassword;
    private boolean enableRawDataTracing;
    private boolean enableValueTracing;
    private MimeToContentTypeMapper consumesMimeTypeMapper;
    private MimeToContentTypeMapper producesMimeTypeMapper;
    private int maxConnectionsPerRoute;
    private int maxConnectionsTotal;
    private int validateAfterInactivityMillis;

    public HttpClientAgileConfiguration() {
        refresh();
    }

    @Override
    public void refresh() {
        MimeToContentTypeMapper consumesMimeTypeMapper = super.getConsumesMimeTypeMapper();
        String consumesMimeTypeString = configString(HTTP_COMMUNICATION_SECTION_ID, CONSUMES_MIME_TYPE,
                consumesMimeTypeMapper.getMimeType().toString());
        MimeType consumesMimeType = MimeType.valueOf(consumesMimeTypeString);
        ContentType consumesContentType = getOrElse(getByMimeType(consumesMimeTypeString), consumesMimeTypeMapper.getContentType());
        this.consumesMimeTypeMapper = new MimeToContentTypeMapper(consumesMimeType, consumesContentType);

        MimeToContentTypeMapper producesMimeTypeMapper = super.getProducesMimeTypeMapper();
        String producesMimeTypeString = configString(HTTP_COMMUNICATION_SECTION_ID, PRODUCES_MIME_TYPE,
                producesMimeTypeMapper.getMimeType().toString());
        MimeType producesMimeType = MimeType.valueOf(producesMimeTypeString);
        ContentType producesContentType = getOrElse(getByMimeType(producesMimeTypeString), producesMimeTypeMapper.getContentType());
        this.producesMimeTypeMapper = new MimeToContentTypeMapper(producesMimeType, producesContentType);

        enableRawDataTracing = configBoolean(HTTP_COMMUNICATION_SECTION_ID, ENABLE_RAW_DATA_TRACING, super.isEnableRawDataTracing());
        enableValueTracing = configBoolean(HTTP_COMMUNICATION_SECTION_ID, ENABLE_VALUE_TRACING, super.isEnableValueTracing());
        balancerHost = configString(HTTP_BALANCER_SECTION_ID, HOST, super.getBalancerHost());
        balancerPort = configInt(HTTP_BALANCER_SECTION_ID, PORT, super.getBalancerPort());
        communicationTargets = configInnerMap(HTTP_COMMUNICATION_SECTION_ID, TARGETS, config -> httpCommunicationTarget()
                .host(ifEmpty(config.getString(HOST), balancerHost))
                .port(getOrElse(config.getInt(PORT), balancerPort))
                .path(getOrElse(config.getString(PATH), SLASH))
                .scheme(ifEmpty(config.getString(SCHEME), HTTP_SCHEME))
                .url(config.getString(URL))
                .requestConfig(RequestConfig.custom()
                        .setConnectTimeout(getOrElse(config.getInt(CONNECTION_TIMEOUT), super.getRequestConfig().getConnectTimeout()))
                        .setSocketTimeout(getOrElse(config.getInt(SOCKET_TIMEOUT), super.getRequestConfig().getSocketTimeout()))
                        .setConnectionRequestTimeout(getOrElse(config.getInt(CONNECTION_REQUEST_TIMEOUT), super.getRequestConfig().getConnectionRequestTimeout()))
                        .build())
                .build(), super.getCommunicationTargets());
        int socketTimeout = configInt(HTTP_COMMUNICATION_SECTION_ID, SOCKET_TIMEOUT, RequestConfig.DEFAULT.getSocketTimeout());
        int connectionTimeout = configInt(HTTP_COMMUNICATION_SECTION_ID, CONNECTION_TIMEOUT, DEFAULT_HTTP_CLIENT_TIMEOUT);
        int soTimeout = configInt(HTTP_COMMUNICATION_SECTION_ID, SO_TIMEOUT, SocketConfig.DEFAULT.getSoTimeout());
        int connectionRequestTimeout = configInt(HTTP_COMMUNICATION_SECTION_ID, CONNECTION_REQUEST_TIMEOUT, RequestConfig.DEFAULT.getConnectionRequestTimeout());
        int ioReactorThreadCount = configInt(HTTP_COMMUNICATION_SECTION_ID, THREAD_POOL_SIZE, DEFAULT_THREAD_POOL_SIZE);
        ioReactorConfig = IOReactorConfig.custom()
                .setIoThreadCount(ioReactorThreadCount)
                .build();
        requestConfig = RequestConfig.custom()
                .setConnectTimeout(connectionTimeout)
                .setSocketTimeout(socketTimeout)
                .setConnectionRequestTimeout(connectionRequestTimeout)
                .build();
        socketConfig = SocketConfig.custom()
                .setSoTimeout(soTimeout)
                .build();
        ssl = configBoolean(HTTP_COMMUNICATION_SECTION_ID, SSL, super.isSsl());
        disableSslHostNameVerification = configBoolean(HTTP_COMMUNICATION_SECTION_ID, IS_DISABLE_SSL_HOST_NAME_VERIFICATION, super.isDisableSslHostNameVerification());
        sslKeyStoreType = configString(HTTP_COMMUNICATION_SECTION_ID, SSL_KEY_STORE_TYPE, super.getSslKeyStoreType());
        sslKeyStoreFilePath = configString(HTTP_COMMUNICATION_SECTION_ID, SSL_KEY_STORE_FILE_PATH, super.getSslKeyStoreFilePath());
        sslKeyStorePassword = configString(HTTP_COMMUNICATION_SECTION_ID, SSL_KEY_STORE_PASSWORD, super.getSslKeyStorePassword());
        maxConnectionsPerRoute = configInt(HTTP_COMMUNICATION_SECTION_ID, MAX_CONNECTIONS_PER_ROUTE, super.getMaxConnectionsPerRoute());
        maxConnectionsTotal = configInt(HTTP_COMMUNICATION_SECTION_ID, MAX_CONNECTIONS_TOTAL, super.getMaxConnectionsTotal());
        validateAfterInactivityMillis = configInt(HTTP_COMMUNICATION_SECTION_ID, VALIDATE_AFTER_INACTIVITY_MILLIS, super.getValidateAfterInactivityMillis());
    }
}