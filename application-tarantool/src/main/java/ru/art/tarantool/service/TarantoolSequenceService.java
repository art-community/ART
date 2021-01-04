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

package ru.art.tarantool.service;

import lombok.experimental.*;
import org.tarantool.*;
import ru.art.tarantool.configuration.lua.*;
import static ru.art.core.caster.Caster.*;
import static ru.art.core.checker.CheckerForEmptiness.*;
import static ru.art.core.factory.CollectionsFactory.*;
import static ru.art.tarantool.caller.TarantoolFunctionCaller.*;
import static ru.art.tarantool.configuration.lua.TarantoolSequenceConfiguration.*;
import static ru.art.tarantool.constants.TarantoolModuleConstants.Functions.*;
import static ru.art.tarantool.executor.TarantoolLuaExecutor.*;
import static ru.art.tarantool.module.TarantoolModule.*;

@UtilityClass
public final class TarantoolSequenceService {
    public static void createSequence(String instanceId, TarantoolSequenceConfiguration sequenceConfiguration) {
        evaluateLuaScript(instanceId, sequenceConfiguration.toCreateSequenceLua());
        evaluateLuaScript(instanceId, sequenceConfiguration.toManageSequenceLua());
    }

    public static void dropSequence(String instanceId, String sequenceName) {
        evaluateLuaScript(instanceId, tarantoolSequence(sequenceName).toManageSequenceLua());
        TarantoolClient client = tarantoolModuleState().getClient(instanceId);
        callTarantoolFunction(client, DROP + sequenceName);
    }

    public static Integer sequenceNext(String instanceId, String sequenceName) {
        evaluateLuaScript(instanceId, tarantoolSequence(sequenceName).toManageSequenceLua());
        TarantoolClient client = tarantoolModuleState().getClient(instanceId);
        Integer result = cast(callTarantoolFunction(client, NEXT + sequenceName).get(0));
        if (isEmpty(result)) return null;
        return result;
    }

    public static void resetSequence(String instanceId, String sequenceName) {
        evaluateLuaScript(instanceId, tarantoolSequence(sequenceName).toManageSequenceLua());
        TarantoolClient client = tarantoolModuleState().getClient(instanceId);
        callTarantoolFunction(client, RESET + sequenceName);
    }

    public static void setSequence(String instanceId, String sequenceName, int value) {
        evaluateLuaScript(instanceId, tarantoolSequence(sequenceName).toManageSequenceLua());
        TarantoolClient client = tarantoolModuleState().getClient(instanceId);
        callTarantoolFunction(client, SET + sequenceName, fixedArrayOf(value));
    }
}
