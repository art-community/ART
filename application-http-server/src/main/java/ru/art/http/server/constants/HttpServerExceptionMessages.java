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

public interface HttpServerExceptionMessages {
    String INTERCEPTION_IS_NULL = "Interception is null";
    String REQUEST_ACCEPT_TYPE_NOT_SUPPORTED = "Request accept type ''{0}'' not supported";
    String REQUEST_CONTENT_TYPE_NOT_SUPPORTED = "Request content type ''{0}'' not supported";
    String CONSUMES_CONTENT_TYPE_NOT_SUPPORTED = "Consumes content type ''{0}'' not supported";
    String PRODUCES_CONTENT_TYPE_NOT_SUPPORTED = "Produces content type ''{0}'' not supported";
    String UNKNOWN_HTTP_REQUEST_DATA_SOURCE = "Unknown http request data source: ''{0}''";
    String TOMCAT_INITIALIZATION_FAILED = "Tomcat initialization failed";
    String TOMCAT_RESTARTING_FAILED = "Tomcat restarting failed";
    String TOMCAT_STOPPING_FAILED = "Tomcat stopping failed";
    String REQUEST_BODY_READING_EXCEPTION = "Request reading exception";
    String REQUEST_BODY_WRITING_EXCEPTION = "Request writing exception";
    String HTTP_METHOD_NOT_ALLOWED = "Http method ''{0}'' not allowed";
    String HTTP_METHOD_LISTENING_PATH_IS_EMPTY = "Http method listening path is empty";
    String HTTP_SERVICE_LISTENING_PATH_IS_EMPTY = "Http service listening path is empty";
    String HTTP_METHOD_IS_EMPTY = "Http method is empty";
    String HTTP_METHOD_IS_NULL = "Http method is null";
    String CANT_READ_MULTIPART_DATA_TERMINATE = "Can't read multipart form data for request. Terminating request handling";
    String CANT_READ_MULTIPART_DATA_SKIP = "Can't read multipart form data for request. Skipping parts";
    String EXCEPTION_OCCURRED_DURING_READING_PART = "Exception occurred during reading multi part";
    String RESOURCE_ERROR = "Error during getting resource";
    String HTTP_RESPONSE_MODE_IS_NULL = "Http response mode is null";
}
