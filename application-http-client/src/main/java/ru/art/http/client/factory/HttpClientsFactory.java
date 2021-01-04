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

package ru.art.http.client.factory;

import lombok.experimental.*;
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
import ru.art.http.client.model.*;
import static java.security.KeyStore.*;
import static java.util.Objects.*;
import static org.apache.http.ssl.SSLContexts.*;
import static ru.art.http.client.constants.HttpClientExceptionMessages.*;
import static ru.art.http.client.module.HttpClientModule.*;
import static ru.art.http.constants.HttpCommonConstants.*;
import javax.net.ssl.*;
import java.io.*;
import java.security.*;

@UtilityClass
public class HttpClientsFactory {
    private static HostnameVerifier ALLOW_ALL = (hostName, session) -> true;

    @SuppressWarnings("Duplicates")
    public static CloseableHttpAsyncClient createAsyncHttpClient(HttpClientConfiguration configuration) {
        HttpAsyncClientBuilder clientBuilder = HttpAsyncClients.custom()
                .setConnectionManager(createAsyncConnectionManager(configuration))
                .addInterceptorFirst(new LogbookHttpRequestInterceptor(httpClientModule().getLogbook()))
                .addInterceptorLast(new LogbookHttpResponseInterceptor());
        CloseableHttpAsyncClient client = httpClientModuleState().registerClient(clientBuilder.build());
        client.start();
        return client;
    }

    @SuppressWarnings("Duplicates")
    public static CloseableHttpClient createHttpClient(HttpClientConfiguration configuration) {
        HttpClientBuilder clientBuilder = HttpClients.custom()
                .setConnectionManager(createConnectionManager(configuration))
                .addInterceptorFirst(new LogbookHttpRequestInterceptor(httpClientModule().getLogbook()))
                .addInterceptorLast(new LogbookHttpResponseInterceptor());
        return httpClientModuleState().registerClient(clientBuilder.build());
    }

    private static PoolingHttpClientConnectionManager createConnectionManager(HttpClientConfiguration configuration) {
        HostnameVerifier hostnameVerifier = new DefaultHostnameVerifier(PublicSuffixMatcherLoader.getDefault());
        SSLContext sslContext = null;
        if (configuration.isSsl()) {
            try {
                sslContext = custom().loadKeyMaterial(loadKeyStore(configuration), configuration.getSslKeyStorePassword().toCharArray()).build();
                if (configuration.isDisableSslHostNameVerification()) {
                    hostnameVerifier = ALLOW_ALL;
                }
            } catch (Throwable throwable) {
                throw new HttpClientException(HTTP_SSL_CONFIGURATION_FAILED, throwable);
            }
        }

        SSLConnectionSocketFactory sslSocketFactory = isNull(sslContext)
                ? new SSLConnectionSocketFactory(SSLContexts.createDefault(), hostnameVerifier)
                : new SSLConnectionSocketFactory(sslContext, null, null, hostnameVerifier);

        Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register(HTTP_SCHEME, PlainConnectionSocketFactory.getSocketFactory())
                .register(HTTPS_SCHEME, sslSocketFactory)
                .build();
        PoolingHttpClientConnectionManager manager = new PoolingHttpClientConnectionManager(registry);
        manager.setDefaultSocketConfig(configuration.getSocketConfig());
        manager.setDefaultConnectionConfig(configuration.getConnectionConfig());
        manager.setMaxTotal(configuration.getMaxConnectionsTotal());
        manager.setDefaultMaxPerRoute(configuration.getMaxConnectionsPerRoute());
        manager.setValidateAfterInactivity(configuration.getValidateAfterInactivityMillis());
        return manager;
    }

    private static PoolingNHttpClientConnectionManager createAsyncConnectionManager(HttpClientConfiguration configuration) {
        HostnameVerifier hostnameVerifier = new DefaultHostnameVerifier(PublicSuffixMatcherLoader.getDefault());
        SSLContext sslContext = null;
        if (configuration.isSsl()) {
            try {
                sslContext = custom().loadKeyMaterial(loadKeyStore(configuration), configuration.getSslKeyStorePassword().toCharArray()).build();
                if (configuration.isDisableSslHostNameVerification()) {
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
            manager = new PoolingNHttpClientConnectionManager(new DefaultConnectingIOReactor(configuration.getIoReactorConfig()), registry);
        } catch (Throwable throwable) {
            throw new HttpClientException(HTTP_ASYNC_CONFIGURATION_FAILED, throwable);
        }
        manager.setDefaultConnectionConfig(configuration.getConnectionConfig());
        manager.setMaxTotal(configuration.getMaxConnectionsTotal());
        manager.setDefaultMaxPerRoute(configuration.getMaxConnectionsPerRoute());
        return manager;
    }

    private static KeyStore loadKeyStore(HttpClientConfiguration configuration) {
        try (FileInputStream keyStoreInputStream = new FileInputStream(new File(configuration.getSslKeyStoreFilePath()))) {
            KeyStore keyStore = getInstance(configuration.getSslKeyStoreType());
            keyStore.load(keyStoreInputStream, configuration.getSslKeyStorePassword().toCharArray());
            return keyStore;
        } catch (Throwable throwable) {
            throw new HttpClientException(HTTP_SSL_CONFIGURATION_FAILED, throwable);
        }
    }
}
