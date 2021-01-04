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

package ru.art.tarantool.constants;

import lombok.*;
import ru.art.core.converter.WslPathConverter;
import static ru.art.core.converter.WslPathConverter.*;
import static ru.art.core.determinant.SystemDeterminant.isMac;
import static ru.art.core.determinant.SystemDeterminant.isWindows;
import static ru.art.core.network.selector.PortSelector.*;
import java.io.File;

public interface TarantoolModuleConstants {
    String TARANTOOL = "tarantool";
    String TARANTOOL_MODULE_ID = "TARANTOOL_MODULE";
    int DEFAULT_TARANTOOL_PROBE_CONNECTION_TIMEOUT = 3 * 1000;
    int DEFAULT_TARANTOOL_INSTANCE_STARTUP_BETWEEN_TIME = 1000;
    int DEFAULT_TARANTOOL_CONNECTION_TIMEOUT = 10 * 1000;
    int DEFAULT_TARANTOOL_OPERATION_TIMEOUT = 60 * 1000;
    int DEFAULT_TARANTOOL_PORT = findAvailableTcpPort();
    int DEFAULT_PROCESS_STARTUP_TIMEOUT = 60 * 60 * 1000;
    int DEFAULT_PROCESS_CHECK_INTERVAL = 1000;
    String DEFAULT_TARANTOOL_USERNAME = "guest";
    String LUA_REGEX = ".+\\.lua";
    String DEFAULT_TARANTOOL_EXECUTABLE = "tarantool";
    String DEFAULT_TARANTOOL_EXECUTABLE_FILE_PATH = new File("/usr/local/bin/tarantool").exists()
            ? "/usr/local/bin/tarantool"
            : new File("tarantool").exists()
            ? "tarantool"
            : new File("/usr/bin/tarantool").exists()
            ? "/usr/bin/tarantool"
            : new File("/bin/tarantool").exists()
            ? "/bin/tarantool" : null;
    String TWIG_TEMPLATE = ".twig";
    String IS_NULLABLE = "is_nullable";
    String COLLATION = "collation";
    String ID_FIELD = "id";
    String VALUE = "value";
    int DEFAULT_TARANTOOL_RETRIES = 3;

    interface Templates {
        String CONFIGURATION = "configuration.lua";
        String CREATE_INDEX = "create_index.lua";
        String ALTER_INDEX = "alter_index.lua";
        String CREATE_SPACE = "create_space.lua";
        String CREATE_SEQUENCE = "create_sequence.lua";
        String FORMAT_SPACE = "format_space.lua";
        String USER = "user.lua";
        String VALUE = "value.lua";
        String COMMON = "common.lua";
        String SIMPLE_VALUE = "simple_value.lua";
        String SPACE_MANAGEMENT = "manage_space.lua";
        String INDEX_MANAGEMENT = "manage_index.lua";
        String SEQUENCE_MANAGEMENT = "manage_sequence.lua";
    }

    interface Scripts {
        String INITIALIZATION = "initialization.lua";
    }

    interface Directories {
        String LUA = "lua";
        String BIN = "bin";
    }

    interface ExceptionMessages {
        String LUA_SCRIPT_READING_ERROR = "Lua script reading from resources exception";
        String CONFIGURATION_IS_NULL = "Tarantool ''{0}'' configuration is null. Please specify it.";
        String ENTITY_FIELDS_MAPPING_IS_NULL = "Tarantool ''{0}'' entity ''{1}'' fields mapping is null. Please specify it.";
        String UNABLE_TO_CONNECT_TO_TARANTOOL = "Unable to connect to tarantool ''{0}'' with address ''{1}''. Connection waiting time has passed";
        String UNABLE_TO_CONNECT_TO_TARANTOOL_RETRY = "Unable to connect to tarantool ''{0}'' with address ''{1}''. Connection waiting time has passed. Retrying...\n";
        String TARANTOOL_INITIALIZATION_SCRIP_NOT_EXISTS = "Tarantool ''{0}'' initialization script not exists inside classpath";
        String TARANTOOL_EXECUTABLE_NOT_EXISTS = "Tarantool ''{0}'' executable ''{1}'' not exists inside classpath";
        String TARANTOOL_PROCESS_FAILED = "Tarantool ''{0}'' process failed to start";
        String ENTITY_WITHOUT_ID_FILED = "Entity ''{0}'' does not has 'id' long field";
        String ENTITY_IS_NULL = "Entity ''{0}'' is null";
        String RESULT_IS_INVALID = "Result for entity ''{0}'' returned from Tarantool is invalid";
        String STARTUP_ERROR = "Unable to startup tarantool ''{0}''";
        String TARANTOOL_ON_MAC_EXCEPTION = "For Mac OS X you need to manually install tarantool executable and specify it in the configuration.\nUse command like 'brew install tarantool'";
    }

    interface LoggingMessages {
        String TARANTOOL_CLIENT_CLOSED = "Tarantool client for instance ''{0}'' closed";
        String TARANTOOL_SUCCESSFULLY_CONNECTED = "Tarantool ''{0}'' with address ''{1}'' successfully connected";
        String TARANTOOL_PROCESS_SUCCESSFULLY_STARTED = "Tarantool ''{0}'' with address ''{1}'' process successfully started";
        String WRITING_TARANTOOL_CONFIGURATION = "Writing Tarantool ''{0}'' address ''{1}'' configuration to file ''{2}''";
        String EVALUATING_LUA_SCRIPT = "Evaluating lua script: ''{0}''";
        String EXTRACT_TARANTOOL_LUA_SCRIPTS = "Extract Tarantool ''{0}'' with address''{1}'' lua scripts to ''{2}''";
        String EXTRACT_TARANTOOL_BINARY = "Extract Tarantool ''{0}'' with address ''{1}'' binary executable to ''{2}''";
        String USING_TARANTOOL_BINARY = "Using Tarantool ''{0}'' with address ''{1}'' binary executable: ''{2}''";
        String WRITING_TARANTOOL_USER_CONFIGURATION = "Writing Tarantool ''{0}'' with address = ''{1}'' user configuration to file ''{2}''";
        String UNABLE_TO_CONNECT_TO_TARANTOOL_ON_STARTUP = "Unable to connect to tarantool ''{0}'' with address ''{1}'' on startup. Therefore, we will try to run the tarantool";
        String UNABLE_TO_CONNECT_TO_TARANTOOL = "Unable to connect to tarantool ''{0}'' with address ''{1}'' on startup";
        String CALLING_FUNCTION = "Calling tarantool function ''{0}'' with arguments: {1}";
        String CALLED_FUNCTION = "Called tarantool function ''{0}'' with result: {1}";
        String FAILED_FUNCTION = "Failed to call tarantool function ''{0}''";
        String FAILED_SET_EXECUTABLE = "Failed apply setExecutable(true) for ''{0}''. Possibly *.jar runner user hasn't permissions to set file as executable";
        String TRYING_TO_CONNECT = "Trying to connect to tarantool ''{0}'' with address ''{1}'' during the ''{2,number,#}[ms]''";
        String WAITING_FOR_INITIALIZATION = "Waiting for tarantool ''{0}'' with address ''{1}'' to be initialized during ''{2,number,#}[ms]''. Checking every ''{3,number,#}[ms]''";
        String WAITING_FOR_CONNECT = "Waiting for tarantool ''{0}'' with address ''{1}'' to be connected during ''{2,number,#}[ms]''";
    }

    interface TemplateParameterKeys {
        String LISTEN = "listen";
        String BACKGROUND = "background";
        String CUSTOM_PROC_TITLE = "customProcTitle";
        String MEMTX_DIR = "memtxDir";
        String VINYL_DIR = "vinylDir";
        String WORK_DIR = "workDir";
        String USERNAME = "username";
        String PID_FILE = "pidFile";
        String READ_ONLY = "readOnly";
        String VINYL_TIMEOUT = "vinylTimeout";
        String WORKER_POOL_THREADS = "workerPoolThreads";
        String REPLICAS = "replicas";
        String STRING_OPTIONS = "stringOptions";
        String NUMBER_OPTIONS = "numberOptions";
        String PASSWORD = "password";
        String INDEX_NAME = "indexName";
        String SPACE_NAME = "spaceName";
        String SEQUENCE_NAME = "sequenceName";
        String PARTS = "parts";
        String TYPE = "type";
        String UNIQUE = "unique";
        String DIMENSION = "dimension";
        String DISTANCE = "distance";
        String BLOOM_FPR = "bloomFpr";
        String PAGE_SIZE = "pageSize";
        String RANGE_SIZE = "rangeSize";
        String RUN_COUNT_PER_LEVEL = "runCountPerLevel";
        String RUN_SIZE_RATIO = "runSizeRatio";
        String SEQUENCE = "sequence";
        String TEMPORARY = "temporary";
        String ENGINE = "engine";
        String FIELD_COUNT = "fieldCount";
        String FORMAT = "format";
        String IS_LOCAL = "isLocal";
        String USER = "user";
        String START = "start";
        String MIN = "min";
        String MAX = "max";
        String CYCLE = "cycle";
        String CACHE = "cache";
        String STEP = "step";
        String MEMTX_MAX_TUPLE_SIZE = "memtxMaxTupleSize";
        String MEMTX_MEMORY = "memtxMemory";
        String SLAB_ALLOC_FACTOR = "slabAllocFactor";
        String SLAB_ALLOC_MAXIMAL = "slabAllocMaximal";
        String SLAB_ALLOC_ARENA = "slabAllocArena";
    }

    interface Functions {
        String PUT = "put";
        String GET = "get";
        String BY = "By";
        String SELECT = "select";
        String DELETE = "delete";
        String DELETE_ALL = "deleteAll";
        String TRUNCATE = "truncate";
        String COUNT = "count";
        String LEN = "len";
        String DROP = "drop";
        String RENAME = "rename";
        String NEXT = "next";
        String SET = "set";
        String RESET = "reset";
        String INSERT = "insert";
        String UPDATE = "update";
        String UPSERT = "upsert";
        String VALUES_POSTFIX = "Values";
        String VALUE_POSTFIX = "Value";
        String WITH_SCHEMA_POSTFIX = "WithSchema";
    }

    enum TarantoolInstanceMode {
        LOCAL,
        REMOTE
    }

    enum TarantoolInitializationMode {
        BOOTSTRAP,
        LAZY
    }

    enum TarantoolFieldType {
        UNSIGNED,
        STRING,
        INTEGER,
        NUMBER,
        BOOLEAN,
        ARRAY,
        SCALAR
    }

    enum TarantoolIndexType {
        HASH,
        TREE,
        BITSET,
        RTREE
    }

    enum TarantoolIdCalculationMode {
        MANUAL,
        SEQUENCE
    }

    @Getter
    @AllArgsConstructor
    enum TarantoolOperator {
        ADDITION("+"),
        SUBTRACTION("-"),
        AND("&"),
        OR("|"),
        XOR("^"),
        STRING_SPLICE(":"),
        INSERTION("!"),
        DELETION("#"),
        ASSIGMENT("=");

        private final String operator;
    }
}
