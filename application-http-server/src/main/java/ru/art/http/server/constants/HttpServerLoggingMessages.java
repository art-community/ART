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

public interface HttpServerLoggingMessages {
    String TOMCAT_STARTED_MESSAGE = "Tomcat HTTP server stated in {0}[ms]";
    String TOMCAT_RESTARTED_MESSAGE = "Tomcat HTTP server restarted in {0}[ms]";
    String TOMCAT_STOPPED_MESSAGE = "Tomcat HTTP server stopped in {0}[ms]";
    String REGISTERING_HTTP_INTERCEPTOR = "Registering HTTP interceptor: ''{0}'' for url pattern ''{1}'';";
    String HTTP_SERVICE_REGISTERING_MESSAGE = "Registered HTTP service method for path: ''{0}'' with HTTP service id ''{1}'' and HTTP request types ''{2}''";
    String HTTP_REQUEST_HANDLING_EXCEPTION_MESSAGE = "Exception occurred during request handling:";
    String HTTP_SERVICES_CANCELED = "All http service canceled by interceptors";
    String HTTP_SERVICE_CANCELED = "HTTP service: ''{0}'' canceled by interceptor for path: ''{1}''";
    String HTTP_SERVICE_METHOD_CANCELED = "HTTP service: ''{0}'' method: ''{1}'' canceled by interceptor for path: ''{2}''";
    String HTTP_SERVLET_EVENT = "httpServletHandling";
}
