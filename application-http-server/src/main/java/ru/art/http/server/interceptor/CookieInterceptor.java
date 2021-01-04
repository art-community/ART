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

package ru.art.http.server.interceptor;

import lombok.*;
import lombok.experimental.*;
import ru.art.core.constants.*;
import ru.art.core.mime.*;
import ru.art.http.server.exception.*;
import static java.util.Arrays.*;
import static java.util.Objects.*;
import static ru.art.core.checker.CheckerForEmptiness.*;
import static ru.art.core.constants.InterceptionStrategy.*;
import static ru.art.core.context.Context.*;
import static ru.art.http.constants.HttpMethodType.*;
import static ru.art.http.constants.HttpMimeTypes.*;
import javax.servlet.http.*;
import java.nio.charset.*;
import java.util.*;
import java.util.function.*;

@Builder(toBuilder = true)
public class CookieInterceptor implements HttpServerInterception {
    private final Predicate<String> pathFilter;
    @Singular("cookieValidator")
    private final Map<String, Predicate<String>> cookieValidator;
    private final Function<String, Error> errorProvider;

    @Override
    public InterceptionStrategy intercept(HttpServletRequest request, HttpServletResponse response) {
        if (pathFilter.test(request.getRequestURI()) || request.getMethod().equals(OPTIONS.name()) || hasTokenCookie(request)) {
            return NEXT_INTERCEPTOR;
        }
        try {
            if (isNull(errorProvider)) {
                return STOP_HANDLING;
            }
            Error error = errorProvider.apply(request.getRequestURI());
            String charset = error.overrideRequestCharset ? error.charset.name() : ifEmpty(request.getCharacterEncoding(), error.charset.name());
            response.setCharacterEncoding(charset);
            response.setContentType(error.contentType.toString());
            response.setStatus(error.status);
            if (isNotEmpty(error.content)) {
                response.getOutputStream().write(error.content.getBytes(charset));
            }
            response.getOutputStream().close();
            return STOP_HANDLING;
        } catch (Throwable throwable) {
            throw new HttpServerException(throwable);
        }
    }

    private boolean hasTokenCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (isEmpty(cookies)) return false;
        return stream(cookies)
                .filter(Objects::nonNull)
                .filter(cookie -> nonNull(cookie.getName()))
                .filter(cookie -> nonNull(cookie.getValue()))
                .anyMatch(this::filterCookie);
    }

    private boolean filterCookie(Cookie cookie) {
        Predicate<String> predicate = cookieValidator.get(cookie.getName());
        if (isNull(predicate)) return false;
        return predicate.test(cookie.getValue());
    }

    @Getter
    @Setter
    @Accessors(fluent = true)
    @NoArgsConstructor(staticName = "cookieError")
    public static class Error {
        private int status;
        private String content;
        private MimeType contentType = TEXT_HTML_UTF_8;
        private Charset charset = contextConfiguration().getCharset();
        private boolean overrideRequestCharset = false;
    }
}
