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

package ru.art.config.extensions.tarantool;

public interface TarantoolConfigKeys {
    String TARANTOOL_SECTION_ID = "tarantool";
    String TARANTOOL_LOCAL_SECTION_ID = "tarantool.local";
    String TARANTOOL_INSTANCES_SECTION_ID = "tarantool.instances";
    String ENTITIES = "entities";
    String FIELDS = "fields";
    String CONNECTION_SECTION_ID = "connection";
    String INITIAL_SECTION_ID = "initial";
    String INSTANCE_MODE = "instanceMode";
    String PROBE_CONNECTION_TIMEOUT_MILLIS = "probeConnectionTimeoutMillis";
    String CONNECTION_TIMEOUT_MILLIS = "connectionTimeoutMillis";
    String INITIALIZATION_MODE = "initializationMode";
    String EXECUTABLE = "executable";
    String EXECUTABLE_FILE_PATH = "executableFilePath";
    String WORKING_DIRECTORY = "workingDirectory";
    String PROCESS_STARTUP_CHECK_INTERVAL_MILLIS = "processStartupCheckIntervalMillis";
    String PROCESS_STARTUP_TIMEOUT_MILLIS = "processStartupTimeoutMillis";
    String USERNAME = "username";
    String PASSWORD = "password";
    String OPERATION_TIMEOUT_MILLIS = "operationTimeoutMillis";
    String MAX_RETRY_COUNT = "maxRetryCount";
    String BACKGROUND = "background";
    String CUSTOM_PROC_TITLE = "customProcTitle";
    String MEMTX_DIR = "memtxDir";
    String VINYL_DIR = "vinylDir";
    String REPLICAS = "replicas";
    String WORK_DIR = "workDir";
    String PID_FILE = "pidFile";
    String READ_ONLY = "readOnly";
    String VINYL_TIMEOUT = "vinylTimeout";
    String WORKER_POOL_THREADS = "workerPoolThreads";
    String MEMTEX_MAX_TUPLE_SIZE = "memtexMaxTupleSize";
    String MEMTX_MEMORY = "memtxMemory";
    String SLAB_ALLOC_FACTOR = "slabAllocFactor";
    String SLAB_ALLOC_MAXIMAL = "slabAllocMaximal";
    String SLAB_ALLOC_ARENA = "slabAllocArena";
}

