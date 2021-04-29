package io.art.model.modeling.server;

import io.art.core.collection.*;
import io.art.http.authentication.*;
import io.art.value.constants.ValueModuleConstants.*;
import io.netty.channel.*;
import java.util.*;
import java.util.function.*;
import lombok.*;
import reactor.netty.http.*;
import reactor.netty.http.server.*;
import reactor.netty.http.server.logging.*;
import reactor.netty.tcp.*;

@Builder
@Getter
public class HttpServerModel {
    private final ImmutableMap<String, HttpServiceModel> services;
    private final String host;
    private final Integer port;
    private final HttpProtocol protocol;
    private final boolean compression;
    private final boolean logging;
    private final boolean wiretap;
    private final boolean accessLogging;
    private final Predicate<AccessLogArgProvider> accessLogFilter;
    private final AccessLogFactory accessLogFormatFunction;
    private final int fragmentationMtu;
    private final DataFormat defaultDataFormat;
    private final UnaryOperator<HttpRequestDecoderSpec> requestDecoderConfigurator;
    private final boolean redirectToHttps;
    private final Consumer<? super SslProvider.SslContextSpec> sslConfigurator;
    private final Function<? extends Throwable, ?> exceptionsMapper;
    private final Map<ChannelOption<?>, Object> tcpOptions;
    private final HttpAuthenticatorRegistry authentication;
}
