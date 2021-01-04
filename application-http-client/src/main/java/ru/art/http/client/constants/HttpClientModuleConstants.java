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

package ru.art.http.client.constants;

public interface HttpClientModuleConstants {
    String HTTP_CLIENT_CLOSED = "HTTP client closed";
    String HTTP_COMMUNICATION_SERVICE_TYPE = "HTTP_COMMUNICATION";
    String HTTP_CLIENT_MODULE_ID = "HTTP_CLIENT_MODULE";
    String TRACE_ID_HEADER = "X-Trace-Id";
    String PROFILE_HEADER = "X-Profile";
    int DEFAULT_HTTP_CLIENT_TIMEOUT = 10000;
    int RESPONSE_BUFFER_DEFAULT_SIZE = 4096;
    int DEFAULT_MAX_CONNECTIONS_PER_ROUTE = 2;
    int DEFAULT_MAX_CONNECTIONS_TOTAL = 20;
    int DEFAULT_VALIDATE_AFTER_INACTIVITY_MILLIS = 4000;
    String HTTP_HEADER_CONNECTION_CLOSE = "Close";
    String HTTP_HEADER_CONNECTION_KEEP_ALIVE = "Keep-Alive";

    enum ConnectionClosingPolicy {
        CLOSE_AFTER_RESPONSE,
        CLOSE_IF_NOT_KEEP_ALIVE_HEADER_PRESENTS,
        ALWAYS_KEEP_OPEN_AFTER_RESPONSE
    }
}
