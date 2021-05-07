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

package io.art.http.authentication;

import java.util.*;
import java.util.function.*;
import java.util.regex.*;
import lombok.*;
import reactor.netty.http.server.*;
import static io.art.core.collector.SetCollector.*;
import static io.art.core.factory.MapFactory.*;
import static io.art.core.factory.SetFactory.*;
import static io.art.http.authentication.HttpAuthenticatorFactory.*;

@NoArgsConstructor(staticName = "httpAuthenticationRouter")
public class HttpAuthenticationRouter {
    private final Set<AuthenticationMethod> authenticationMethods = set();
    private Authenticator<HttpServerRequest, HttpServerResponse> defaultAuthenticator = alwaysAllow();
    private final Map<String, Authenticator<HttpServerRequest, HttpServerResponse>> cachedPaths = map();


    public HttpAuthenticationRouter add(Authenticator<HttpServerRequest, HttpServerResponse> authenticator,
                                        UnaryOperator<AuthenticationMethod> configurator) {

        AuthenticationMethod newRoute = configurator.apply(new AuthenticationMethod(authenticator));
        authenticationMethods.add(newRoute);
        newRoute.cacheablePaths().forEach(path -> cachedPaths.put(path, newRoute.authenticator));
        return this;
    }

    public HttpAuthenticationRouter defaultAuthenticator(Authenticator<HttpServerRequest, HttpServerResponse> authenticator) {
        defaultAuthenticator = authenticator;
        return this;
    }

    public Authenticator<HttpServerRequest, HttpServerResponse> get(String path) {
        return cachedPaths.containsKey(UriPathTemplate.filter(path)) ?
                cachedPaths.get(path) :
                authenticationMethods.stream()
                        .filter(method -> method.matches(path))
                        .findFirst()
                        .map(AuthenticationMethod::getAuthenticator)
                        .orElse(defaultAuthenticator);
    }

    @RequiredArgsConstructor
    public static final class AuthenticationMethod {
        private final Set<UriPathTemplate> paths = set();
        private final Set<String> ignoredPaths = set();
        @Getter(value = AccessLevel.PROTECTED)
        private final Authenticator<HttpServerRequest, HttpServerResponse> authenticator;

        private Boolean matches(String path) {
            return !ignoredPaths.contains(path) &&
                    paths.stream().anyMatch(template -> template.matches(path));
        }

        public AuthenticationMethod on(String... templates){
            Arrays.stream(templates).forEach(pathPattern ->
                    paths.add(new UriPathTemplate(pathPattern.startsWith("/") ? pathPattern.substring(1) : pathPattern)));
            return this;
        }

        public AuthenticationMethod ignore(String... paths){
            Arrays.stream(paths).forEach(path -> ignoredPaths.add(path.startsWith("/") ? path.substring(1) : path));
            return this;
        }

        private Set<String> cacheablePaths() {
            return paths.stream()
                    .map(UriPathTemplate::getPlainString)
                    .filter(Objects::nonNull)
                    .filter(path -> !ignoredPaths.contains(path))
                    .collect(setCollector());

        }
    }

    private static final class UriPathTemplate {

        private static final Pattern FULL_SPLAT_PATTERN     = Pattern.compile("[*][*]");
        private static final String  FULL_SPLAT_REPLACEMENT = ".*";

        private static final Pattern NAME_SPLAT_PATTERN     = Pattern.compile("\\{([^/]+?)}[*][*]");

        private static final Pattern NAME_PATTERN           = Pattern.compile("\\{([^/]+?)}");

        private static final Pattern URL_PATTERN            =
                Pattern.compile("(?:(\\w+)://)?((?:\\[.+?])|(?<!\\[)(?:[^/?]+?))(?::(\\d{2,5}))?([/?].*)?");

        private final Pattern uriPattern;
        @Getter
        private String plainString = null;

        private static String getNameSplatReplacement(String name) {
            return "(?<" + name + ">.*)";
        }

        private static String getNameReplacement(String name) {
            return "(?<" + name + ">[^\\/]*)";
        }

        private static String filterQueryParams(String uri) {
            int hasQuery = uri.lastIndexOf('?');
            if (hasQuery != -1) {
                return uri.substring(0, hasQuery);
            }
            else {
                return uri;
            }
        }

        private static String filterHostAndPort(String uri) {
            if (uri.startsWith("/")) {
                return uri;
            }
            else {
                Matcher matcher = URL_PATTERN.matcher(uri);
                if (matcher.matches()) {
                    String path = matcher.group(4);
                    return path == null ? "/" : path;
                }
                else {
                    throw new IllegalArgumentException("Unable to parse url [" + uri + "]");
                }
            }
        }

        static String filter(String uri) {
            return filterQueryParams(filterHostAndPort(uri));
        }


        UriPathTemplate(String uriPattern) {
            String s = "^" + filter(uriPattern);
            if (!NAME_PATTERN.matcher(s).find() && !FULL_SPLAT_PATTERN.matcher(s).find())
                plainString = s;

            Matcher m = NAME_SPLAT_PATTERN.matcher(s);
            while (m.find()) {
                for (int i = 1; i <= m.groupCount(); i++) {
                    String name = m.group(i);
                    s = m.replaceFirst(getNameSplatReplacement(name));
                    m.reset(s);
                }
            }

            m = NAME_PATTERN.matcher(s);
            while (m.find()) {
                for (int i = 1; i <= m.groupCount(); i++) {
                    String name = m.group(i);
                    s = m.replaceFirst(getNameReplacement(name));
                    m.reset(s);
                }
            }

            m = FULL_SPLAT_PATTERN.matcher(s);
            while (m.find()) {
                s = m.replaceAll(FULL_SPLAT_REPLACEMENT);
                m.reset(s);
            }

            this.uriPattern = Pattern.compile(s + "$");
        }

        public boolean matches(String uri) {
            return matcher(uri).matches();
        }

        private Matcher matcher(String uri) {
            uri = filter(uri);
            return uriPattern.matcher(uri);
        }

    }
}
