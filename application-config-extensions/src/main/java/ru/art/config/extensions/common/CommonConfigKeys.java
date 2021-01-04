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

package ru.art.config.extensions.common;

public interface CommonConfigKeys {
    String HOST = "host";
    String PORT = "port";
    String PATH = "path";
    String URL = "url";
    String TARGETS = "targets";
    String ENABLE_TRACING = "enableTracing";
    String ENABLE_RAW_DATA_TRACING = "enableRawDataTracing";
    String ENABLE_VALUE_TRACING = "enableValueTracing";
    String ENABLE_METRICS = "enableMetrics";
    String THREAD_POOL_SIZE = "threadPoolSize";
    String TIMEOUT = "timeout";
    String SERVICES = "services";
    String INSTANCES = "instances";
    String KEEP_ALIVE_TIME_MILLIS = "keepAliveTimeMillis";
    String KEEP_ALIVE_TIME_OUT_MILLIS = "keepAliveTimeOutMillis";
    String KEEP_ALIVE_WITHOUT_CALLS = "keepAliveWithoutCalls";
    String IDLE_TIME_OUT_MILLIS = "idleTimeOutMillis";
    String WAIT_FOR_READY = "waitForReady";
}
