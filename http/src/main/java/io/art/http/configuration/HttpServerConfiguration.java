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

package io.art.http.configuration;

import io.art.core.changes.*;
import io.art.core.collection.*;
import io.art.core.source.*;
import io.art.http.authentication.*;
import io.art.http.refresher.*;
import io.art.value.constants.ValueModuleConstants.*;
import java.util.function.*;
import lombok.*;
import reactor.netty.http.server.*;
import static io.art.core.checker.NullityChecker.*;
import static io.art.core.constants.NetworkConstants.*;
import static io.art.http.constants.HttpModuleConstants.ConfigurationKeys.*;
import static io.art.http.constants.HttpModuleConstants.Defaults.*;
import static io.art.http.authentication.HttpAuthenticationRouter.*;
import static io.art.value.constants.ValueModuleConstants.DataFormat.*;

@Getter
@Builder(toBuilder = true)
public class HttpServerConfiguration {
    private HttpServer httpServer;
    private ImmutableMap<String, HttpServiceConfiguration> services;
    private boolean logging;
    private int fragmentationMtu;
    private DataFormat defaultDataFormat;
    private final Function<? extends Throwable, ?> exceptionMapper;
    @Builder.Default
    private final HttpAuthenticationRouter authentication = httpAuthenticationRouter();

    public static HttpServerConfiguration defaults() {
        HttpServerConfiguration configuration = HttpServerConfiguration.builder().build();
        configuration.defaultDataFormat = JSON;
        configuration.logging = false;
        configuration.fragmentationMtu = 0;
        configuration.httpServer = HttpServer.create().port(DEFAULT_PORT);
        return configuration;
    }

    public static HttpServerConfiguration from(HttpModuleRefresher refresher, ConfigurationSource source) {
        HttpServerConfiguration configuration = HttpServerConfiguration.builder().build();

        ChangesListener serverListener = refresher.serverListener();
        ChangesListener serverLoggingListener = refresher.serverLoggingListener();

        configuration.logging = serverLoggingListener.emit(orElse(source.getBool(LOGGING_KEY), false));

        configuration.defaultDataFormat = serverListener.emit(dataFormat(source.getString(DATA_FORMAT_KEY), JSON));
        configuration.fragmentationMtu = serverListener.emit(orElse(source.getInt(FRAGMENTATION_MTU_KEY), 0));

        int port = serverListener.emit(orElse(source.getInt(TRANSPORT_PORT_KEY), DEFAULT_PORT));
        String host = serverListener.emit(orElse(source.getString(TRANSPORT_HOST_KEY), BROADCAST_IP_ADDRESS));

        configuration.httpServer = HttpServer.create().port(serverListener.emit(port)).host(serverListener.emit(host));
        configuration.services = source.getNestedMap(SERVICES_KEY, HttpServiceConfiguration::from);

        return configuration;
    }

}
