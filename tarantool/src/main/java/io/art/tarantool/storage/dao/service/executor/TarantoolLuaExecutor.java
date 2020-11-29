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

package io.art.tarantool.storage.dao.service.executor;

import io.art.core.extensions.*;
import lombok.*;
import lombok.experimental.*;
import org.apache.logging.log4j.*;
import static io.art.logging.LoggingModule.*;
import static java.text.MessageFormat.*;
import static java.util.Objects.*;
import static lombok.AccessLevel.*;
import static io.art.tarantool.constants.TarantoolModuleConstants.ExceptionMessages.*;
import static io.art.tarantool.constants.TarantoolModuleConstants.LoggingMessages.*;
import static io.art.tarantool.module.TarantoolModule.*;
import java.io.*;

@UtilityClass
public final class TarantoolLuaExecutor {
    @Getter(lazy = true, value = PRIVATE)
    private static final Logger logger = logger(TarantoolLuaExecutor.class);

    public static void executeLuaScript(String instanceId, String scriptName) {
        String script;
        try (InputStream scriptStream = TarantoolLuaExecutor.class.getClassLoader().getResourceAsStream(scriptName)) {
            if (isNull(scriptStream)) {
                return;
            }
            script = InputStreamExtensions.toString(scriptStream);
            evaluateLuaScript(instanceId, script);
        } catch (IOException ioException) {
            getLogger().error(LUA_SCRIPT_READING_ERROR, ioException);
        }
    }

    public static void evaluateLuaScript(String instanceId, String script) {
        if (tarantoolModule().configuration().isEnableTracing()) {
            getLogger().trace(format(EVALUATING_LUA_SCRIPT, script));
        }
        getClient(instanceId).syncOps().eval(script);
    }
}