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

import org.tarantool.*;
import ru.art.entity.*;
import ru.art.tarantool.exception.*;
import ru.art.tarantool.model.*;
import static java.text.MessageFormat.*;
import static java.util.Arrays.*;
import static java.util.Collections.*;
import static java.util.Objects.*;
import static java.util.Optional.*;
import static java.util.stream.Collectors.*;
import static ru.art.core.caster.Caster.*;
import static ru.art.core.checker.CheckerForEmptiness.isEmpty;
import static ru.art.core.factory.CollectionsFactory.*;
import static ru.art.entity.Value.*;
import static ru.art.entity.Entity.*;
import static ru.art.entity.tuple.PlainTupleReader.*;
import static ru.art.entity.tuple.PlainTupleWriter.*;
import static ru.art.entity.tuple.schema.ValueSchema.*;
import static ru.art.tarantool.caller.TarantoolFunctionCaller.*;
import static ru.art.tarantool.constants.TarantoolModuleConstants.ExceptionMessages.*;
import static ru.art.tarantool.constants.TarantoolModuleConstants.Functions.*;
import static ru.art.tarantool.constants.TarantoolModuleConstants.*;
import static ru.art.tarantool.constants.TarantoolModuleConstants.TarantoolIdCalculationMode.*;
import static ru.art.tarantool.module.TarantoolModule.*;
import static ru.art.tarantool.service.TarantoolScriptService.*;
import java.util.*;

@SuppressWarnings("Duplicates")
public final class TarantoolValueDao extends TarantoolCommonDao {
    TarantoolValueDao(String instanceId) {
        super(instanceId);
    }

    public Entity put(String spaceName, Entity entity) {
        if (isNull(entity)) {
            throw new TarantoolDaoException(format(ENTITY_IS_NULL, spaceName));
        }
        if (!entity.getFieldNames().contains(ID_FIELD) && idCalculationMode == MANUAL) {
            throw new TarantoolDaoException(format(ENTITY_WITHOUT_ID_FILED, spaceName));
        }
        evaluateValueScript(clusterIds, spaceName);
        PlainTupleWriterResult valueTupleResult = writeTuple(merge(entityBuilder().longField(ID_FIELD, null).build(), entity));
        if (isNull(valueTupleResult)) {
            throw new TarantoolDaoException(format(ENTITY_IS_NULL, spaceName));
        }

        String valueFunctionName = PUT + spaceName + VALUE_POSTFIX;
        List<?> valueTuple = valueTupleResult.getTuple();
        List<?> schemaTuple = valueTupleResult.getSchema().toTuple();
        List<List<?>> result = cast(callTarantoolFunction(client, valueFunctionName, fixedArrayOf(valueTuple, schemaTuple)));
        if (isEmpty(result) || (isEmpty(result = cast(result.get(0)))) || result.size() == 1) {
            throw new TarantoolDaoException(format(RESULT_IS_INVALID, spaceName));
        }
        return asEntity(readTuple(result.get(0), fromTuple(result.get(1))));
    }

    public Entity put(String spaceName, Long id, Primitive primitive) {
        return put(spaceName, entityBuilder().longField(ID_FIELD, id).valueField(VALUE, primitive).build());
    }

    public Entity put(String spaceName, Long id, CollectionValue<?> collectionValue) {
        return put(spaceName, entityBuilder().longField(ID_FIELD, id).valueField(VALUE, collectionValue).build());
    }

    public Entity put(String spaceName, Long id, StringParametersMap stringParameters) {
        return put(spaceName, entityBuilder().longField(ID_FIELD, id).valueField(VALUE, stringParameters).build());
    }

    public Entity put(String spaceName, Long id, MapValue mapValue) {
        return put(spaceName, entityBuilder().longField(ID_FIELD, id).valueField(VALUE, mapValue).build());
    }


    public Entity put(String spaceName, Primitive primitive) {
        return put(spaceName, entityBuilder().valueField(VALUE, primitive).build());
    }

    public Entity put(String spaceName, CollectionValue<?> collectionValue) {
        return put(spaceName, entityBuilder().valueField(VALUE, collectionValue).build());
    }

    public Entity put(String spaceName, StringParametersMap stringParameters) {
        return put(spaceName, entityBuilder().valueField(VALUE, stringParameters).build());
    }

    public Entity put(String spaceName, MapValue mapValue) {
        return put(spaceName, entityBuilder().valueField(VALUE, mapValue).build());
    }


    public Optional<Entity> get(String spaceName, Collection<?> keys) {
        evaluateValueScript(clusterIds, spaceName);

        List<?> result = cast(callTarantoolFunction(client, GET + spaceName + VALUE_POSTFIX, keys));
        if (isEmpty(result) || (isEmpty(result = cast(result.get(0)))) || result.size() == 1) {
            return empty();
        }
        List<List<?>> valueTuple = cast(result.get(0));
        List<List<?>> schemaTuple = cast(result.get(1));
        return ofNullable(asEntity(readTuple(valueTuple, fromTuple(schemaTuple))));
    }

    public Optional<Entity> get(String spaceName, long id) {
        return get(spaceName, setOf(id));
    }


    public Optional<Primitive> getPrimitive(String spaceName, long id) {
        return get(spaceName, id).map(value -> asPrimitive(asEntity(value).getValue(VALUE)));
    }

    public Optional<Primitive> getPrimitive(String spaceName) {
        return getPrimitive(spaceName, 1);
    }


    public Optional<CollectionValue<?>> getCollectionValue(String spaceName, long id) {
        return get(spaceName, id).map(value -> asCollection(asEntity(value).getValue(VALUE)));
    }

    public Optional<CollectionValue<?>> getCollectionValue(String spaceName) {
        return getCollectionValue(spaceName, 1);
    }


    public Optional<StringParametersMap> getStringParameters(String spaceName, long id) {
        return get(spaceName, id).map(value -> asStringParametersMap(asEntity(value).getValue(VALUE)));
    }

    public Optional<StringParametersMap> getStringParameters(String spaceName) {
        return getStringParameters(spaceName, 1);
    }


    public Optional<MapValue> getMapValue(String spaceName, long id) {
        return get(spaceName, id).map(value -> asMap(asEntity(value).getValue(VALUE)));
    }

    public Optional<MapValue> getMapValue(String spaceName) {
        return getMapValue(spaceName, 1);
    }


    public List<Entity> select(String spaceName, Collection<?> keys) {
        evaluateValueScript(clusterIds, spaceName);

        List<List<?>> result = cast(callTarantoolFunction(client, SELECT + spaceName + VALUES_POSTFIX, keys));
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

    public List<Entity> select(String spaceName, long id) {
        return select(spaceName, setOf(id));
    }

    public List<Entity> selectAll(String spaceName) {
        return select(spaceName, emptySet());
    }


    public List<Primitive> selectPrimitives(String spaceName, Collection<?> keys) {
        return select(spaceName, keys).stream().map(entity -> asPrimitive(entity.getValue(VALUE))).collect(toList());
    }

    public List<Primitive> selectPrimitives(String spaceName, long id) {
        return selectPrimitives(spaceName, setOf(id));
    }

    public List<Primitive> selectAllPrimitives(String spaceName) {
        return selectPrimitives(spaceName, emptySet());
    }


    public List<CollectionValue<?>> selectCollections(String spaceName, Collection<?> keys) {
        return select(spaceName, keys).stream().map(entity -> asCollection(entity.getValue(VALUE))).collect(toList());
    }

    public List<CollectionValue<?>> selectCollections(String spaceName, long id) {
        return selectCollections(spaceName, setOf(id));
    }

    public List<CollectionValue<?>> selectAllCollections(String spaceName) {
        return selectCollections(spaceName, emptySet());
    }


    public List<StringParametersMap> selectStringParameters(String spaceName, Collection<?> keys) {
        return select(spaceName, keys).stream().map(entity -> asStringParametersMap(entity.getValue(VALUE))).collect(toList());
    }

    public List<StringParametersMap> selectStringParameters(String spaceName, long id) {
        return selectStringParameters(spaceName, setOf(id));
    }

    public List<StringParametersMap> selectAllStringParameters(String spaceName) {
        return selectStringParameters(spaceName, emptySet());
    }


    public List<MapValue> selectMaps(String spaceName, Collection<?> keys) {
        return select(spaceName, keys).stream().map(entity -> asMap(entity.getValue(VALUE))).collect(toList());
    }

    public List<MapValue> selectMaps(String spaceName, long id) {
        return selectMaps(spaceName, setOf(id));
    }

    public List<MapValue> selectAllMaps(String spaceName) {
        return selectMaps(spaceName, emptySet());
    }


    public Entity insert(String spaceName, Entity entity) {
        if (isNull(entity)) {
            throw new TarantoolDaoException(format(ENTITY_IS_NULL, spaceName));
        }
        if (!entity.getFieldNames().contains(ID_FIELD) && idCalculationMode == MANUAL) {
            throw new TarantoolDaoException(format(ENTITY_WITHOUT_ID_FILED, spaceName));
        }
        evaluateValueScript(clusterIds, spaceName);
        PlainTupleWriterResult valueTupleResult = writeTuple(merge(entityBuilder().longField(ID_FIELD, null).build(), entity));
        if (isNull(valueTupleResult)) {
            throw new TarantoolDaoException(format(ENTITY_IS_NULL, spaceName));
        }

        String valueFunctionName = INSERT + spaceName + VALUE_POSTFIX;
        List<?> valueTuple = valueTupleResult.getTuple();
        List<?> schemaTuple = valueTupleResult.getSchema().toTuple();
        List<List<?>> result = cast(callTarantoolFunction(client, valueFunctionName, fixedArrayOf(valueTuple, schemaTuple)));
        if (isEmpty(result) || (isEmpty(result = cast(result.get(0)))) || result.size() == 1) {
            throw new TarantoolDaoException(format(RESULT_IS_INVALID, spaceName));
        }
        return asEntity(readTuple(result.get(0), fromTuple(result.get(1))));
    }

    public Entity insert(String spaceName, Long id, Primitive primitive) {
        return insert(spaceName, entityBuilder().longField(ID_FIELD, id).valueField(VALUE, primitive).build());
    }

    public Entity insert(String spaceName, Long id, CollectionValue<?> collectionValue) {
        return insert(spaceName, entityBuilder().longField(ID_FIELD, id).valueField(VALUE, collectionValue).build());
    }

    public Entity insert(String spaceName, Long id, StringParametersMap stringParameters) {
        return insert(spaceName, entityBuilder().longField(ID_FIELD, id).valueField(VALUE, stringParameters).build());
    }

    public Entity insert(String spaceName, Long id, MapValue mapValue) {
        return insert(spaceName, entityBuilder().longField(ID_FIELD, id).valueField(VALUE, mapValue).build());
    }


    public Entity insert(String spaceName, Primitive primitive) {
        return put(spaceName, entityBuilder().valueField(VALUE, primitive).build());
    }

    public Entity insert(String spaceName, CollectionValue<?> collectionValue) {
        return put(spaceName, entityBuilder().valueField(VALUE, collectionValue).build());
    }

    public Entity insert(String spaceName, StringParametersMap stringParameters) {
        return put(spaceName, entityBuilder().valueField(VALUE, stringParameters).build());
    }

    public Entity insert(String spaceName, MapValue mapValue) {
        return put(spaceName, entityBuilder().valueField(VALUE, mapValue).build());
    }


    public Optional<Entity> delete(String spaceName, Collection<?> keys) {
        evaluateValueScript(clusterIds, spaceName);

        List<List<?>> result = cast(callTarantoolFunction(client, DELETE + spaceName + VALUES_POSTFIX, keys));
        if (isEmpty(result) || (isEmpty(result = cast(result.get(0)))) || result.size() == 1) {
            return empty();
        }
        List<List<?>> valueTuple = cast(result.get(0));
        List<List<?>> schemaTuple = cast(result.get(1));
        return ofNullable(asEntity(readTuple(valueTuple, fromTuple(schemaTuple))));
    }

    public Optional<Entity> delete(String spaceName, long id) {
        return delete(spaceName, setOf(id));
    }

    public List<Entity> deleteAll(String spaceName) {
        evaluateValueScript(clusterIds, spaceName);

        List<List<?>> result = cast(callTarantoolFunction(client, DELETE_ALL + spaceName + VALUES_POSTFIX));
        if (isEmpty(result) || (isEmpty(result = cast(result.get(0)))) || result.size() == 1) {
            return emptyList();
        }
        List<List<List<?>>> tuples = cast(result.get(0));
        List<List<List<?>>> schemes = cast(result.get(1));
        List<Entity> values = arrayOf(tuples.size());
        for (int i = 0; i < tuples.size(); i++) {
            values.add(asEntity(readTuple(tuples.get(i), fromTuple(schemes.get(i)))));
        }
        return values;
    }


    public Optional<Entity> update(String spaceName, Collection<?> keys, TarantoolUpdateFieldOperation... operations) {
        List<TarantoolUpdateFieldOperation> operationsWithSchema = stream(operations)
                .filter(operation -> !isEmpty(operation.getSchemaOperation()))
                .collect(toList());
        List<TarantoolUpdateFieldOperation> operationsWithoutSchema = stream(operations)
                .filter(operation -> isEmpty(operation.getSchemaOperation()))
                .collect(toList());
        Optional<Entity> entity = empty();
        if (!isEmpty(operationsWithSchema)) {
            entity = updateWithSchema(spaceName, keys, operationsWithSchema);
        }
        if (!isEmpty(operationsWithoutSchema)) {
            entity = updateWithoutSchema(spaceName, keys, operationsWithoutSchema);
        }
        return entity;
    }

    private Optional<Entity> updateWithSchema(String spaceName, Collection<?> keys, List<TarantoolUpdateFieldOperation> operations) {
        evaluateValueScript(clusterIds, spaceName);

        String functionName = UPDATE + spaceName + VALUE_POSTFIX + WITH_SCHEMA_POSTFIX;
        List<?> valueOperations = operations
                .stream()
                .map(TarantoolUpdateFieldOperation::getValueOperation)
                .collect(toList());
        List<?> schemaOperations = operations
                .stream()
                .map(TarantoolUpdateFieldOperation::getSchemaOperation)
                .collect(toList());
        List<List<?>> result = cast(callTarantoolFunction(client, functionName, fixedArrayOf(fixedArrayOf(keys), valueOperations, schemaOperations)));
        if (isEmpty(result) || (isEmpty(result = cast(result.get(0)))) || result.size() == 1) {
            return empty();
        }
        List<List<?>> valueTuple = cast(result.get(0));
        List<List<?>> schemaTuple = cast(result.get(1));
        return ofNullable(asEntity(readTuple(valueTuple, fromTuple(schemaTuple))));
    }

    private Optional<Entity> updateWithoutSchema(String spaceName, Collection<?> keys, List<TarantoolUpdateFieldOperation> operations) {
        evaluateValueScript(clusterIds, spaceName);

        String functionName = UPDATE + spaceName + VALUE_POSTFIX;
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

    public void upsert(String spaceName, Entity defaultEntity, TarantoolUpdateFieldOperation... operations) {
        if (isNull(defaultEntity)) {
            throw new TarantoolDaoException(format(ENTITY_IS_NULL, spaceName));
        }
        if (!defaultEntity.getFieldNames().contains(ID_FIELD) && idCalculationMode == MANUAL) {
            throw new TarantoolDaoException(format(ENTITY_WITHOUT_ID_FILED, spaceName));
        }
        evaluateValueScript(clusterIds, spaceName);
        PlainTupleWriterResult valueTupleResult = writeTuple(merge(entityBuilder().longField(ID_FIELD, null).build(), defaultEntity));
        if (isNull(valueTupleResult)) {
            throw new TarantoolDaoException(format(ENTITY_IS_NULL, spaceName));
        }

        String functionName = UPSERT + spaceName + VALUE_POSTFIX + WITH_SCHEMA_POSTFIX;
        List<?> schemaTuple = valueTupleResult.getSchema().toTuple();
        List<?> valueTuple = valueTupleResult.getTuple();
        List<?> schemaOperations = stream(operations)
                .filter(operation -> !isEmpty(operation.getSchemaOperation()))
                .map(TarantoolUpdateFieldOperation::getSchemaOperation)
                .collect(toList());
        List<?> valueOperations = stream(operations)
                .map(TarantoolUpdateFieldOperation::getValueOperation)
                .collect(toList());
        callTarantoolFunction(client, functionName, fixedArrayOf(valueTuple, schemaTuple, valueOperations, schemaOperations));
    }
}
