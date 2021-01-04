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

package ru.art.http.client.model;

import lombok.*;
import org.apache.http.client.config.*;
import org.apache.http.config.*;
import org.apache.http.impl.nio.reactor.*;
import static ru.art.http.client.module.HttpClientModule.*;

@Getter
@Builder(toBuilder = true)
public class HttpClientConfiguration {
    @Builder.Default
    private final RequestConfig requestConfig = httpClientModule().getRequestConfig();
    @Builder.Default
    private final SocketConfig socketConfig = httpClientModule().getSocketConfig();
    @Builder.Default
    private final IOReactorConfig ioReactorConfig = httpClientModule().getIoReactorConfig();
    @Builder.Default
    private final ConnectionConfig connectionConfig = httpClientModule().getConnectionConfig();
    @Builder.Default
    private final boolean ssl = httpClientModule().isSsl();
    @Builder.Default
    private final boolean disableSslHostNameVerification = httpClientModule().isDisableSslHostNameVerification();
    @Builder.Default
    private final String sslKeyStoreType = httpClientModule().getSslKeyStoreType();
    @Builder.Default
    private final String sslKeyStoreFilePath = httpClientModule().getSslKeyStoreFilePath();
    @Builder.Default
    private final String sslKeyStorePassword = httpClientModule().getSslKeyStorePassword();
    @Builder.Default
    private final boolean enableRawDataTracing = httpClientModule().isEnableRawDataTracing();
    @Builder.Default
    private final int maxConnectionsPerRoute = httpClientModule().getMaxConnectionsPerRoute();
    @Builder.Default
    private final int maxConnectionsTotal = httpClientModule().getMaxConnectionsTotal();
    @Builder.Default
    private final int validateAfterInactivityMillis = httpClientModule().getValidateAfterInactivityMillis();
}