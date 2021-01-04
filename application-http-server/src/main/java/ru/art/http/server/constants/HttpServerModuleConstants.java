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

package ru.art.http.server.constants;

public interface HttpServerModuleConstants {
    String HTTP_SERVICE_TYPE = "HTTP_SERVICE";
    String EXECUTE_HTTP_FUNCTION = "EXECUTE_HTTP_FUNCTION";
    String HTTP_SERVER_MODULE_ID = "HTTP_SERVER_MODULE";
    String DEFAULT_MODULE_PATH = "/module";
    int DEFAULT_REQUEST_BODY_BUFFER_SIZE = 4096;
    boolean DEFAULT_PRESTART_MIN_SPARE_THREADS = true;
    int DEFAULT_MAX_IDLE_TIME = 120000;
    String DEFAULT_SERVLET = "default";
    String ADDRESS_ATTRIBUTE = "address";
    String MAX_THREADS_ATTRIBUTE = "maxThreads";
    String MIN_SPARE_THREADS_ATTRIBUTE = "minSpareThreads";
    String MAX_IDLE_TIME_ATTRIBUTE = "maxIdleTime";
    String PRESTART_MIN_SPARE_THREADS_ATTRIBUTE = "prestartminSpareThreads";
    String TEMP_DIR_KEY = "java.io.tmpdir";
    int EMPTY_HTTP_CONTENT_LENGTH = -1;
    String MULTIPART_PATTERN = "multipart";
    String TRACE_ID_HEADER = "X-Trace-Id";
    String PROFILE_HEADER = "X-Profile";
    String ENVIRONMENT_PROPERTY = "environment";
    String HTTP_SERVER_THREAD = "http-server-bootstrap-thread";

    enum HttpResponseHandlingMode {
        UNCHECKED,
        CHECKED
    }

    interface HttpResourceServiceConstants {
        int DEFAULT_BUFFER_SIZE = 4096;
        String INDEX_HTML = "index.html";
        String HTTP_RESOURCE_BODY_REPLACEMENT = "<http-resource>";
        String ACCESS_CONTROL_ALLOW_ORIGIN_KEY = "Access-Control-Allow-Origin";
        String ACCESS_CONTROL_ALLOW_METHODS_KEY = "Access-Control-Allow-Methods";
        String ACCESS_CONTROL_ALLOW_HEADERS_KEY = "Access-Control-Allow-Headers";
        String ACCESS_CONTROL_MAX_AGE_HEADERS_KEY = "Access-Control-Max-Age";
        String ACCESS_CONTROL_ALLOW_CREDENTIALS_KEY = "Access-Control-Allow-Credentials";
        String ACCESS_CONTROL_ALLOW_METHODS_VALUE = "POST,GET,PUT,HEAD,OPTIONS";
        String ACCESS_CONTROL_ALLOW_HEADERS_VALUE = "content-type, accept, accept-encoding, authorization, dnt, origin, user-agent, location, access-control-allow-origin";
        String ACCESS_CONTROL_MAX_AGE_HEADERS_VALUE = "3600";
        String ACCESS_CONTROL_ALLOW_CREDENTIALS_VALUE = "true";

        interface ResourceExtensions {
            String DOT_WEBP = ".webp";
            String DOT_JPG = ".jpg";
            String DOT_ICO = ".ico";
            String DOT_JPEG = ".jpeg";
            String DOT_PNG = ".png";
            String DOT_SVG = ".svg";
            String DOT_CSS = ".css";
            String DOT_MAP = ".map";
            String DOT_JS = ".js";
            String DOT_HTML = ".html";
            String DOT_WSDL = ".wsdl";
            String DOT_XML = ".xml";
            String DOT_TXT = ".txt";
        }

        interface Methods {
            String GET_RESOURCE = "getResource";
        }

        interface HttpParameters {
            String RESOURCE = "resource";
        }

        enum HttpResourceType {
            STRING,
            BINARY
        }
    }

}
