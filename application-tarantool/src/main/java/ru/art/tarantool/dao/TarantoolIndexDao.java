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

import ru.art.entity.*;
import ru.art.tarantool.model.*;
import static java.util.Arrays.*;
import static java.util.Collections.*;
import static java.util.Optional.*;
import static java.util.stream.Collectors.*;
import static ru.art.core.caster.Caster.*;
import static ru.art.core.checker.CheckerForEmptiness.isEmpty;
import static ru.art.core.factory.CollectionsFactory.*;
import static ru.art.entity.Value.*;
import static ru.art.entity.tuple.PlainTupleReader.*;
import static ru.art.entity.tuple.schema.ValueSchema.*;
import static ru.art.tarantool.caller.TarantoolFunctionCaller.*;
import static ru.art.tarantool.constants.TarantoolModuleConstants.Functions.*;
import static ru.art.tarantool.service.TarantoolScriptService.*;
import java.util.*;

@SuppressWarnings("Duplicates")
public final class TarantoolIndexDao extends TarantoolCommonDao {
    TarantoolIndexDao(String instanceId) {
        super(instanceId);
    }

    public Optional<Entity> getByIndex(String spaceName, String indexName, Collection<?> keys) {
        evaluateValueIndexScript(clusterIds, spaceName, indexName);

        List<?> result = callTarantoolFunction(client, GET + spaceName + VALUE_POSTFIX + BY + indexName, keys);
        if (isEmpty(result) || (isEmpty(result = cast(result.get(0)))) || result.size() == 1) {
            return empty();
        }
        List<List<?>> valueTuple = cast(result.get(0));
        List<List<?>> schemaTuple = cast(result.get(1));
        return ofNullable(asEntity(readTuple(valueTuple, fromTuple(schemaTuple))));
    }

    public Optional<Entity> getByIndex(String spaceName, String indexName) {
        return getByIndex(spaceName, indexName, emptySet());
    }


    public List<Entity> selectByIndex(String spaceName, String indexName, Collection<?> keys) {
        evaluateValueIndexScript(clusterIds, spaceName, indexName);

        List<List<?>> result = cast(callTarantoolFunction(client, SELECT + spaceName + VALUES_POSTFIX + BY + indexName, keys));
        if (isEmpty(result) || (isEmpty(result = cast(result.get(0)))) || result.size() == 1) {
            return emptyList();
        }
        List<List<List<?>>> valueTuples = cast(result.get(0));
        List<List<List<?>>> schemeTuples = cast(result.get(1));
        List<Entity> values = arrayOf(valueTuples.size());
        for (int i = 0; i < valueTuples.size(); i++) {
            values.add(asEntity(readTuple(valueTuples.get(i), fromTuple(schemeTuples.get(i)))));
        }
        return values;
    }

    public List<Entity> selectAllByIndex(String spaceName, String indexName) {
        return selectByIndex(spaceName, indexName, emptySet());
    }


    public Optional<Entity> deleteByIndex(String spaceName, String indexName, Collection<?> keys) {
        evaluateValueIndexScript(clusterIds, spaceName, indexName);

        List<List<?>> result = cast(callTarantoolFunction(client, DELETE + spaceName + VALUES_POSTFIX + BY + indexName, keys));
        if (isEmpty(result) || (isEmpty(result = cast(result.get(0)))) || result.size() == 1) {
            return empty();
        }
        List<List<?>> valueTuple = cast(result.get(0));
        List<List<?>> schemaTuple = cast(result.get(1));
        return ofNullable(asEntity(readTuple(valueTuple, fromTuple(schemaTuple))));
    }


    public long countByIndex(String spaceName, String indexName, Collection<?> keys) {
        evaluateValueIndexScript(clusterIds, spaceName, indexName);

        List<?> result = callTarantoolFunction(client, COUNT + spaceName + VALUES_POSTFIX + BY + indexName, keys);
        if (isEmpty(result)) {
            return 0L;
        }
        return ((Number) result.get(0)).longValue();
    }

    public long countByIndex(String spaceName, String indexName) {
        return countByIndex(spaceName, indexName, emptySet());
    }

    public long lenByIndex(String spaceName, String indexName) {
        evaluateValueIndexScript(clusterIds, spaceName, indexName);

        List<?> result = callTarantoolFunction(client, LEN + spaceName + VALUES_POSTFIX + BY + indexName);
        if (isEmpty(result)) {
            return 0L;
        }
        return ((Number) result.get(0)).longValue();
    }

    public Optional<Entity> updateByIndex(String spaceName, String indexName, Collection<?> keys, TarantoolUpdateFieldOperation... operations) {
        List<TarantoolUpdateFieldOperation> operationsWithSchema = stream(operations)
                .filter(operation -> !isEmpty(operation.getSchemaOperation()))
                .collect(toList());
        List<TarantoolUpdateFieldOperation> operationsWithoutSchema = stream(operations)
                .filter(operation -> isEmpty(operation.getSchemaOperation()))
                .collect(toList());
        Optional<Entity> entity = empty();
        if (!isEmpty(operationsWithSchema)) {
            entity = updateWithSchema(spaceName, indexName, keys, operationsWithSchema);
        }
        if (!isEmpty(operationsWithoutSchema)) {
            entity = updateWithoutSchema(spaceName, indexName, keys, operationsWithoutSchema);
        }
        return entity;
    }

    private Optional<Entity> updateWithSchema(String spaceName, String indexName, Collection<?> keys, List<TarantoolUpdateFieldOperation> operations) {
        evaluateValueIndexScript(clusterIds, spaceName, indexName);

        String functionName = UPDATE + spaceName + VALUE_POSTFIX + WITH_SCHEMA_POSTFIX + BY + indexName;
        List<?> valueOperations = operations
                .stream()
                .map(TarantoolUpdateFieldOperation::getValueOperation)
                .collect(toList());
        List<?> schemaOperations = operations
                .stream()
                .map(TarantoolUpdateFieldOperation::getSchemaOperation)
                .collect(toList());
        List<List<?>> arguments = fixedArrayOf(fixedArrayOf(keys), valueOperations, schemaOperations);
        List<List<?>> result = cast(callTarantoolFunction(client, functionName, arguments));
        if (isEmpty(result) || (isEmpty(result = cast(result.get(0)))) || result.size() == 1) {
            return empty();
        }
        List<List<?>> valueTuple = cast(result.get(0));
        List<List<?>> schemaTuple = cast(result.get(1));
        return ofNullable(asEntity(readTuple(valueTuple, fromTuple(schemaTuple))));
    }

    private Optional<Entity> updateWithoutSchema(String spaceName, String indexName, Collection<?> keys, List<TarantoolUpdateFieldOperation> operations) {
        evaluateValueIndexScript(clusterIds, spaceName, indexName);

        String functionName = UPDATE + spaceName + VALUE_POSTFIX + BY + indexName;
        List<List<?>> result = cast(callTarantoolFunction(client, functionName, fixedArrayOf(fixedArrayOf(keys), operations
                .stream()
                .map(TarantoolUpdateFieldOperation::getValueOperation)
                .collect(toList()))));
        if (isEmpty(result) || (isEmpty(result = cast(result.get(0)))) || result.size() == 1) {
            return empty();
        }
        List<List<?>> valueTuple = cast(result.get(0));
        List<List<?>> schemaTuple = cast(result.get(1));
        return ofNullable(asEntity(readTuple(valueTuple, fromTuple(schemaTuple))));
    }

}
