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

package ru.art.http.server.body.descriptor;

import static ru.art.core.constants.ArrayConstants.*;
import static ru.art.core.extension.InputStreamExtensions.*;
import static ru.art.http.server.constants.HttpServerExceptionMessages.*;
import static ru.art.http.server.module.HttpServerModule.*;
import static ru.art.logging.LoggingModule.*;
import javax.servlet.http.*;

public interface HttpBodyDescriptor {
    static byte[] readRequestBody(HttpServletRequest request) {
        try {
            return toByteArray(request.getInputStream(), httpServerModule().getRequestBodyBufferSize());
        } catch (Throwable throwable) {
            loggingModule().getLogger(HttpBodyDescriptor.class).error(REQUEST_BODY_READING_EXCEPTION, throwable);
            return EMPTY_BYTES;
        }
    }

    static void writeResponseBody(HttpServletResponse response, byte[] body) {
        try {
            response.getOutputStream().write(body);
        } catch (Throwable throwable) {
            loggingModule()
                    .getLogger(HttpBodyDescriptor.class)
                    .error(REQUEST_BODY_WRITING_EXCEPTION, throwable);
        }
    }
}
