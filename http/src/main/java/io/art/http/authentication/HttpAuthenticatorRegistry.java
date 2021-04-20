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
import java.util.Map.*;
import java.util.regex.*;
import reactor.netty.http.server.*;
import static io.art.core.factory.MapFactory.*;
import static io.art.http.authentication.HttpAuthenticator.*;

public class HttpAuthenticatorRegistry {
    private final Map<UriPathTemplate, Authenticator<HttpServerRequest, HttpServerResponse>> routes = map();
    private Authenticator<HttpServerRequest, HttpServerResponse> defaultAuthenticator = alwaysAllow();

    public static HttpAuthenticatorRegistry httpAuthenticatorRegistry(){
        return new HttpAuthenticatorRegistry();
    }

    public HttpAuthenticatorRegistry add(String pathPattern, Authenticator<HttpServerRequest, HttpServerResponse> authenticator){
        routes.put(new UriPathTemplate(pathPattern.startsWith("/") ? pathPattern.substring(1) : pathPattern), authenticator);
        return this;
    }

    public HttpAuthenticatorRegistry defaultAuthenticator(Authenticator<HttpServerRequest, HttpServerResponse> authenticator){
        defaultAuthenticator = authenticator;
        return this;
    }

    public Authenticator<HttpServerRequest, HttpServerResponse> get(String path){
        return routes.entrySet().stream()
                .filter(entry -> entry.getKey().matches(path))
                .findFirst()
                .map(Entry::getValue)
                .orElse(defaultAuthenticator);
    }

    private static final class UriPathTemplate {

        private static final Pattern FULL_SPLAT_PATTERN     =
                Pattern.compile("[*][*]");
        private static final String  FULL_SPLAT_REPLACEMENT = ".*";

        private static final Pattern NAME_SPLAT_PATTERN     =
                Pattern.compile("\\{([^/]+?)}[*][*]");

        private static final Pattern NAME_PATTERN           = Pattern.compile("\\{([^/]+?)}");

        private static final Pattern URL_PATTERN            =
                Pattern.compile("(?:(\\w+)://)?((?:\\[.+?])|(?<!\\[)(?:[^/?]+?))(?::(\\d{2,5}))?([/?].*)?");

        private final Pattern uriPattern;

        private static String getNameSplatReplacement(String name) {
            return "(?<" + name + ">.*)";
        }

        private static String getNameReplacement(String name) {
            return "(?<" + name + ">[^\\/]*)";
        }

        static String filterQueryParams(String uri) {
            int hasQuery = uri.lastIndexOf('?');
            if (hasQuery != -1) {
                return uri.substring(0, hasQuery);
            }
            else {
                return uri;
            }
        }

        static String filterHostAndPort(String uri) {
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

        UriPathTemplate(String uriPattern) {
            String s = "^" + filterQueryParams(filterHostAndPort(uriPattern));

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
            uri = filterQueryParams(filterHostAndPort(uri));
            return uriPattern.matcher(uri);
        }

    }
}
