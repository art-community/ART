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

package ru.art.http.client.constants;

public interface HttpClientExceptionMessages {
    String RESPONSE_CONTENT_TYPE_NOT_SUPPORTED = "Response content mapper ''{0}'' not supported";
    String REQUEST_CONTENT_TYPE_NOT_SUPPORTED = "Request content mapper ''{0}'' not supported";
    String REQUEST_INTERCEPTION_IS_NULL = "Request interception is null";
    String RESPONSE_INTERCEPTION_IS_NULL = "Response interception is null";
    String REQUEST_BODY_READING_EXCEPTION = "Request request reading exception";
    String HTTP_SSL_CONFIGURATION_FAILED = "Failed to configure SSL http client. Please check SSL settings";
    String HTTP_ASYNC_CONFIGURATION_FAILED = "Failed to configure async http client. Please check IOReactor settings";
    String HTTP_COMMUNICATION_TARGET_NOT_FOUND = "Http communication target for service ''{0}'' was not found";
}
