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

package ru.art.http.client.configuration;

import lombok.*;
import org.apache.http.*;
import org.apache.http.client.config.*;
import org.apache.http.config.*;
import org.apache.http.conn.socket.*;
import org.apache.http.conn.ssl.*;
import org.apache.http.conn.util.*;
import org.apache.http.impl.client.*;
import org.apache.http.impl.conn.*;
import org.apache.http.impl.nio.client.*;
import org.apache.http.impl.nio.conn.*;
import org.apache.http.impl.nio.reactor.*;
import org.apache.http.nio.conn.*;
import org.apache.http.nio.conn.ssl.*;
import org.apache.http.ssl.SSLContexts;
import org.zalando.logbook.httpclient.*;
import ru.art.http.client.exception.*;
import ru.art.http.client.interceptor.*;
import ru.art.http.client.model.*;
import ru.art.http.configuration.*;
import static java.security.KeyStore.*;
import static java.text.MessageFormat.*;
import static java.util.Collections.*;
import static java.util.Objects.*;
import static org.apache.http.HttpVersion.*;
import static org.apache.http.ssl.SSLContexts.*;
import static ru.art.core.constants.NetworkConstants.*;
import static ru.art.core.constants.StringConstants.*;
import static ru.art.core.extension.ExceptionExtensions.*;
import static ru.art.core.factory.CollectionsFactory.*;
import static ru.art.http.client.constants.HttpClientExceptionMessages.*;
import static ru.art.http.client.constants.HttpClientModuleConstants.*;
import static ru.art.http.client.interceptor.HttpClientInterceptor.*;
import static ru.art.http.client.module.HttpClientModule.*;
import static ru.art.http.constants.HttpCommonConstants.*;
import javax.net.ssl.*;
import java.io.*;
import java.security.*;
import java.util.*;
import ru.art.http.configuration.HttpCommonDefaultConfiguration;

public interface HttpClientModuleConfiguration extends HttpModuleConfiguration {
    int getMaxConnectionsPerRoute();

    int getMaxConnectionsTotal();

    int getValidateAfterInactivityMillis();

    CloseableHttpClient getClient();

    CloseableHttpAsyncClient getAsynchronousClient();

    RequestConfig getRequestConfig();

    SocketConfig getSocketConfig();

    IOReactorConfig getIoReactorConfig();

    ConnectionConfig getConnectionConfig();

    HttpVersion getHttpVersion();

    int getResponseBodyBufferSize();

    List<HttpClientInterceptor> getRequestInterceptors();

    List<HttpClientInterceptor> getResponseInterceptors();

    boolean isSsl();

    boolean isDisableSslHostNameVerification();

    String getSslKeyStoreType();

    String getSslKeyStoreFilePath();

    String getSslKeyStorePassword();

    String getBalancerHost();

    int getBalancerPort();

    Map<String, HttpCommunicationTargetConfiguration> getCommunicationTargets();

    default HttpCommunicationTargetConfiguration getCommunicationTargetConfiguration(String serviceId) {
        return exceptionIfNull(getCommunicationTargets().get(serviceId),
                new HttpClientException(format(HTTP_COMMUNICATION_TARGET_NOT_FOUND, serviceId))).toBuilder().build();
    }

    HttpClientModuleDefaultConfiguration DEFAULT_CONFIGURATION = new HttpClientModuleDefaultConfiguration();

    class HttpClientModuleDefaultConfiguration extends HttpCommonDefaultConfiguration implements HttpClientModuleConfiguration {
        private static HostnameVerifier ALLOW_ALL = (hostName, session) -> true;
        @Getter
        int maxConnectionsPerRoute = DEFAULT_MAX_CONNECTIONS_PER_ROUTE;
        @Getter
        int maxConnectionsTotal = DEFAULT_MAX_CONNECTIONS_TOTAL;
        @Getter
        int validateAfterInactivityMillis = DEFAULT_VALIDATE_AFTER_INACTIVITY_MILLIS;
        @Getter
        private final RequestConfig requestConfig = RequestConfig.DEFAULT;
        @Getter
        private final SocketConfig socketConfig = SocketConfig.DEFAULT;
        @Getter
        private final ConnectionConfig connectionConfig = ConnectionConfig.DEFAULT;
        @Getter
        private final IOReactorConfig ioReactorConfig = IOReactorConfig.DEFAULT;
        @Getter
        private final HttpVersion httpVersion = HTTP_1_1;
        @Getter
        private final int responseBodyBufferSize = RESPONSE_BUFFER_DEFAULT_SIZE;
        @Getter
        private final boolean ssl = false;
        @Getter
        private final boolean disableSslHostNameVerification = true;
        @Getter
        private final String sslKeyStoreType = EMPTY_STRING;
        @Getter
        private final String sslKeyStoreFilePath = EMPTY_STRING;
        @Getter
        private final String sslKeyStorePassword = EMPTY_STRING;
        @Getter
        private final String balancerHost = LOCALHOST;
        @Getter
        private final int balancerPort = DEFAULT_HTTP_PORT;
        @Getter
        private final Map<String, HttpCommunicationTargetConfiguration> communicationTargets = emptyMap();

        @Getter(lazy = true)
        private final CloseableHttpClient client = createHttpClient();

        @Getter(lazy = true)
        private final List<HttpClientInterceptor> requestInterceptors = getHttpClientInterceptors();

        @Getter(lazy = true)
        private final List<HttpClientInterceptor> responseInterceptors = linkedListOf();

        @Getter(lazy = true)
        private final CloseableHttpAsyncClient asynchronousClient = createAsyncHttpClient();

        @SuppressWarnings({"Duplicates", "WeakerAccess"})
        protected CloseableHttpAsyncClient createAsyncHttpClient() {
            HttpAsyncClientBuilder clientBuilder = HttpAsyncClients.custom()
                    .setConnectionManager(createAsyncConnectionManager())
                    .addInterceptorFirst(new LogbookHttpRequestInterceptor(getLogbook()));
            CloseableHttpAsyncClient client = httpClientModuleState().registerClient(clientBuilder.build());
            client.start();
            return client;
        }

        @SuppressWarnings({"Duplicates", "WeakerAccess"})
        protected CloseableHttpClient createHttpClient() {
            HttpClientBuilder clientBuilder = HttpClients.custom()
                    .setMaxConnPerRoute(getMaxConnectionsPerRoute())
                    .setMaxConnTotal(getMaxConnectionsTotal())
                    .setDefaultRequestConfig(getRequestConfig())
                    .setDefaultConnectionConfig(getConnectionConfig())
                    .setDefaultSocketConfig(getSocketConfig())
                    .addInterceptorFirst(new LogbookHttpRequestInterceptor(getLogbook()))
                    .addInterceptorLast(new LogbookHttpResponseInterceptor())
                    .setConnectionManager(createConnectionManager());
            return httpClientModuleState().registerClient(clientBuilder.build());
        }

        private PoolingHttpClientConnectionManager createConnectionManager() {
            HostnameVerifier hostnameVerifier = new DefaultHostnameVerifier(PublicSuffixMatcherLoader.getDefault());
            SSLContext sslContext = null;
            if (isSsl()) {
                try {
                    sslContext = custom().loadKeyMaterial(loadKeyStore(), getSslKeyStorePassword().toCharArray()).build();
                    if (isDisableSslHostNameVerification()) {
                        hostnameVerifier = ALLOW_ALL;
                    }
                } catch (Throwable throwable) {
                    throw new HttpClientException(HTTP_SSL_CONFIGURATION_FAILED, throwable);
                }
            }

            SSLConnectionSocketFactory sslSocketFactory = isNull(sslContext)
                    ? new SSLConnectionSocketFactory(org.apache.http.ssl.SSLContexts.createDefault(), hostnameVerifier)
                    : new SSLConnectionSocketFactory(sslContext, null, null, hostnameVerifier);

            Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
                    .register(HTTP_SCHEME, PlainConnectionSocketFactory.getSocketFactory())
                    .register(HTTPS_SCHEME, sslSocketFactory)
                    .build();
            PoolingHttpClientConnectionManager manager = new PoolingHttpClientConnectionManager(registry);
            manager.setDefaultSocketConfig(getSocketConfig());
            manager.setDefaultConnectionConfig(getConnectionConfig());
            manager.setMaxTotal(getMaxConnectionsTotal());
            manager.setDefaultMaxPerRoute(getMaxConnectionsPerRoute());
            manager.setValidateAfterInactivity(getValidateAfterInactivityMillis());
            return manager;
        }

        private PoolingNHttpClientConnectionManager createAsyncConnectionManager() {
            HostnameVerifier hostnameVerifier = new DefaultHostnameVerifier(PublicSuffixMatcherLoader.getDefault());
            SSLContext sslContext = null;
            if (isSsl()) {
                try {
                    sslContext = custom().loadKeyMaterial(loadKeyStore(), getSslKeyStorePassword().toCharArray()).build();
                    if (isDisableSslHostNameVerification()) {
                        hostnameVerifier = ALLOW_ALL;
                    }
                } catch (Throwable throwable) {
                    throw new HttpClientException(HTTP_SSL_CONFIGURATION_FAILED, throwable);
                }
            }

            SSLIOSessionStrategy sslSessionStrategy = isNull(sslContext)
                    ? new SSLIOSessionStrategy(SSLContexts.createDefault(), hostnameVerifier)
                    : new SSLIOSessionStrategy(sslContext, null, null, hostnameVerifier);

            Registry<SchemeIOSessionStrategy> registry = RegistryBuilder.<SchemeIOSessionStrategy>create()
                    .register(HTTP_SCHEME, NoopIOSessionStrategy.INSTANCE)
                    .register(HTTPS_SCHEME, sslSessionStrategy)
                    .build();
            PoolingNHttpClientConnectionManager manager = null;
            try {
                manager = new PoolingNHttpClientConnectionManager(new DefaultConnectingIOReactor(getIoReactorConfig()), registry);
            } catch (Throwable throwable) {
                throw new HttpClientException(HTTP_ASYNC_CONFIGURATION_FAILED, throwable);
            }
            manager.setDefaultConnectionConfig(getConnectionConfig());
            manager.setMaxTotal(getMaxConnectionsTotal());
            manager.setDefaultMaxPerRoute(getMaxConnectionsPerRoute());
            return manager;
        }

        private KeyStore loadKeyStore() {
            try (FileInputStream keyStoreInputStream = new FileInputStream(new File(getSslKeyStoreFilePath()))) {
                KeyStore keyStore = getInstance(getSslKeyStoreType());
                keyStore.load(keyStoreInputStream, getSslKeyStorePassword().toCharArray());
                return keyStore;
            } catch (Throwable throwable) {
                throw new HttpClientException(HTTP_SSL_CONFIGURATION_FAILED, throwable);
            }
        }

        private List<HttpClientInterceptor> getHttpClientInterceptors() {
            return linkedListOf(interceptRequest(new HttpClientTracingIdentifiersRequestInterception()));
        }
    }
}

