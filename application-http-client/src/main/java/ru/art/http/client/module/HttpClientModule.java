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

package ru.art.http.client.module;

import lombok.*;
import org.apache.http.impl.client.*;
import org.apache.http.impl.nio.client.*;
import org.apache.logging.log4j.*;
import ru.art.core.module.Module;
import ru.art.http.client.configuration.*;
import ru.art.http.client.state.*;
import static lombok.AccessLevel.*;
import static ru.art.core.context.Context.*;
import static ru.art.core.wrapper.ExceptionWrapper.*;
import static ru.art.http.client.configuration.HttpClientModuleConfiguration.*;
import static ru.art.http.client.constants.HttpClientModuleConstants.*;
import static ru.art.logging.LoggingModule.*;

@Getter
public class HttpClientModule implements Module<HttpClientModuleConfiguration, HttpClientModuleState> {
    @Getter(lazy = true, value = PRIVATE)
    private static final HttpClientModuleConfiguration httpClientModule = context().getModule(HTTP_CLIENT_MODULE_ID, HttpClientModule::new);
    @Getter(lazy = true, value = PRIVATE)
    private static final HttpClientModuleState httpClientModuleState = context().getModuleState(HTTP_CLIENT_MODULE_ID, HttpClientModule::new);
    private final String id = HTTP_CLIENT_MODULE_ID;
    private final HttpClientModuleConfiguration defaultConfiguration = DEFAULT_CONFIGURATION;
    private final HttpClientModuleState state = new HttpClientModuleState();
    @Getter(lazy = true, value = PRIVATE)
    private final static Logger logger = loggingModule().getLogger(HttpClientModule.class);

    public static HttpClientModuleConfiguration httpClientModule() {
        if (contextIsNotReady()) {
            return DEFAULT_CONFIGURATION;
        }
        return getHttpClientModule();
    }

    public static HttpClientModuleState httpClientModuleState() {
        return getHttpClientModuleState();
    }

    @Override
    public void onUnload() {
        httpClientModuleState().getClients().forEach(this::closeClient);
        httpClientModuleState().getAsynchronousClients().forEach(this::closeClient);
    }

    private void closeClient(CloseableHttpClient client) {
        ignoreException(() -> {
            client.close();
            getLogger().info(HTTP_CLIENT_CLOSED);
        }, getLogger()::error);
    }

    private void closeClient(CloseableHttpAsyncClient client) {
        ignoreException(() -> {
            client.close();
            getLogger().info(HTTP_CLIENT_CLOSED);
        }, getLogger()::error);
    }
}
