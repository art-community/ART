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

public interface HttpConfigKeys {
    String HTTP_SERVER_SECTION_ID = "http.server";
    String HTTP_COMMUNICATION_SECTION_ID = "http.communication";
    String HTTP_BALANCER_SECTION_ID = "http.balancer";
    String MAX_THREADS_COUNT = "maxThreadsCount";
    String MIN_SPARE_THREADS_COUNT = "minSpareThreadsCount";
    String CONNECTION_TIMEOUT = "connectionTimeout";
    String SO_TIMEOUT = "soTimeout";
    String VALIDATE_AFTER_INACTIVITY_MILLIS = "validateAfterInactivityMillis";
    String CONNECTION_REQUEST_TIMEOUT = "connectionRequestTimeout";
    String SOCKET_TIMEOUT = "socketTimeout";
    String SCHEME = "scheme";
    String IS_DISABLE_SSL_HOST_NAME_VERIFICATION = "disableSslHostNameVerification";
    String SSL = "ssl";
    String SSL_KEY_STORE_FILE_PATH = "sslKeyStoreFilePath";
    String SSL_KEY_STORE_PASSWORD = "sslKeyStorePassword";
    String MAX_CONNECTIONS_PER_ROUTE = "maxConnectionsPerRoute";
    String MAX_CONNECTIONS_TOTAL = "maxConnectionsTotal";
    String SSL_KEY_STORE_TYPE = "sslKeyStoreType";
    String CONSUMES_MIME_TYPE = "consumesMimeType";
    String PRODUCES_MIME_TYPE = "producesMimeType";
    String WEB = "web";

}
