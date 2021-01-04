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

import lombok.experimental.*;
import ru.art.tarantool.configuration.*;
import ru.art.tarantool.module.*;
import static ru.art.tarantool.constants.TarantoolModuleConstants.TarantoolIdCalculationMode.*;
import static ru.art.tarantool.module.TarantoolModule.*;

public class TarantoolDao {
    @Delegate
    private final TarantoolIndexDao indexDao;
    @Delegate(excludes = TarantoolCommonDao.class)
    private final TarantoolValueDao valueDao;

    private TarantoolDao(String instanceId) {
        indexDao = new TarantoolIndexDao(instanceId);
        valueDao = new TarantoolValueDao(instanceId);
    }

    public static TarantoolDao tarantool(String instanceId) {
        return new TarantoolDao(instanceId);
    }

    public static TarantoolDao tarantool(TarantoolConfiguration configuration) {
        return tarantool(TarantoolModule.class.getSimpleName(), configuration);
    }

    public static TarantoolDao tarantool(String instanceId, TarantoolConfiguration configuration) {
        tarantoolModule().getTarantoolConfigurations().putIfAbsent(instanceId, configuration);
        return new TarantoolDao(instanceId);
    }

    public TarantoolDao sequencedId() {
        indexDao.idCalculationMode = SEQUENCE;
        valueDao.idCalculationMode = SEQUENCE;
        return this;
    }

    public TarantoolDao manualId() {
        indexDao.idCalculationMode = MANUAL;
        valueDao.idCalculationMode = MANUAL;
        return this;
    }

    public TarantoolDao clustered() {
        indexDao.client = tarantoolModuleState().getClusterClient(indexDao.instanceId);
        valueDao.client = tarantoolModuleState().getClusterClient(valueDao.instanceId);
        indexDao.clusterIds.addAll(tarantoolModule().getTarantoolConfigurations().get(indexDao.instanceId).getReplicas());
        valueDao.clusterIds.addAll(tarantoolModule().getTarantoolConfigurations().get(valueDao.instanceId).getReplicas());
        return this;
    }
}
