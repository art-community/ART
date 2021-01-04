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

package ru.art.tarantool.dao;

import org.tarantool.TarantoolClient;
import static java.util.Collections.emptySet;
import static ru.art.core.checker.CheckerForEmptiness.isEmpty;
import static ru.art.core.factory.CollectionsFactory.setOf;
import static ru.art.tarantool.caller.TarantoolFunctionCaller.callTarantoolFunction;
import static ru.art.tarantool.constants.TarantoolModuleConstants.Functions.*;
import static ru.art.tarantool.constants.TarantoolModuleConstants.TarantoolIdCalculationMode;
import static ru.art.tarantool.constants.TarantoolModuleConstants.TarantoolIdCalculationMode.SEQUENCE;
import static ru.art.tarantool.module.TarantoolModule.tarantoolModuleState;
import static ru.art.tarantool.service.TarantoolScriptService.evaluateCommonScript;
import java.util.List;
import java.util.Set;

class TarantoolCommonDao {
    final Set<String> clusterIds = setOf();
    final String instanceId;
    TarantoolClient client;
    TarantoolIdCalculationMode idCalculationMode = SEQUENCE;

    TarantoolCommonDao(String instanceId) {
        this.clusterIds.add(instanceId);
        this.client = tarantoolModuleState().getClient(instanceId);
        this.instanceId = instanceId;
    }

    public long count(String spaceName, Set<?> keys) {
        evaluateCommonScript(clusterIds, spaceName);
        List<?> result = callTarantoolFunction(client, COUNT + spaceName, keys);
        if (isEmpty(result)) {
            return 0L;
        }
        return ((Number) result.get(0)).longValue();
    }

    public long count(String spaceName, long id) {
        return count(spaceName, setOf(id));
    }

    public long count(String spaceName) {
        return count(spaceName, emptySet());
    }


    public long len(String spaceName) {
        evaluateCommonScript(clusterIds, spaceName);

        List<?> result = callTarantoolFunction(client, LEN + spaceName);
        if (isEmpty(result)) {
            return 0L;
        }
        return ((Number) result.get(0)).longValue();
    }


    public void truncate(String spaceName) {
        evaluateCommonScript(clusterIds, spaceName);

        callTarantoolFunction(client, TRUNCATE + spaceName);
    }
}
