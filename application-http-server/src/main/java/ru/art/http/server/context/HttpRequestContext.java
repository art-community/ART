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

package ru.art.http.server.context;

import lombok.*;
import ru.art.core.mime.*;
import javax.servlet.http.*;
import java.nio.charset.*;
import java.util.*;

@Getter
@Builder
public class HttpRequestContext {
    @Singular("header")
    private final Map<String, String> headers;
    @Singular("cookie")
    private final List<Cookie> cookies;
    @Singular("part")
    private final Map<String, MultiPartContext> parts;

    private final String acceptLanguages;
    private final String acceptEncodings;
    private final Charset acceptCharset;
    private final String requestUrl;
    private final MimeType contentType;
    private final MimeType acceptType;
    private final boolean hasContent;
    private final int contentLength;
    private final MimeType interceptedResponseContentType;
    private final HttpServletRequest request;
    private final HttpServletResponse response;
}
