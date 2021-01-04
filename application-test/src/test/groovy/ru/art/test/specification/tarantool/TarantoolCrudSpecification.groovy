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

package ru.art.test.specification.tarantool

import ru.art.entity.Entity
import ru.art.tarantool.configuration.lua.TarantoolIndexConfiguration
import spock.lang.Specification

import static java.util.Optional.empty
import static ru.art.config.extensions.activator.AgileConfigurationsActivator.useAgileConfigurations
import static ru.art.core.constants.StringConstants.EMPTY_STRING
import static ru.art.core.factory.CollectionsFactory.setOf
import static ru.art.entity.Entity.entityBuilder
import static ru.art.entity.PrimitivesFactory.stringPrimitive
import static ru.art.tarantool.configuration.TarantoolModuleConfiguration.fieldMapping
import static ru.art.tarantool.constants.TarantoolModuleConstants.TarantoolFieldType.STRING
import static ru.art.tarantool.dao.TarantoolDao.tarantool
import static ru.art.tarantool.model.TarantoolUpdateFieldOperation.*
import static ru.art.tarantool.module.TarantoolModule.tarantoolModuleState
import static ru.art.tarantool.service.TarantoolIndexService.createIndex
import static ru.art.tarantool.service.TarantoolIndexService.dropIndex
import static ru.art.tarantool.service.TarantoolSequenceService.dropSequence
import static ru.art.tarantool.service.TarantoolSpaceService.dropSpace

class TarantoolCrudSpecification extends Specification {
    def spaceName = "DataEntity"
    def masterId = "t-master-1"
    def replicaId = "t-master-2"
    def secondaryIndex = "SecondaryIndex"
    def secondaryIndexId = 1
    def sequence = "DataEntitySequence"
    def primaryIndex = "PrimaryIndex"
    def fieldName = "Data"
    def idField = "id"
    def dataValue = "Тест"
    def entity = entityBuilder().stringField(fieldName, dataValue).build()
    def dao
    def newValue = "Мяу"

    def "should run CRUD operations with tarantool with preparation steps"() {
        setup:
        useAgileConfigurations()
        dao = tarantool(masterId).clustered()
        def replicaDao = tarantool(replicaId)
        tarantoolModuleState().loadedCommonScripts.clear()
        tarantoolModuleState().loadedValueScripts.clear()
        createIndex(masterId, TarantoolIndexConfiguration.builder()
                .spaceName(spaceName)
                .indexName(secondaryIndex)
                .id(secondaryIndexId)
                .part(TarantoolIndexConfiguration.Part.builder()
                        .fieldNumber(fieldMapping(masterId, spaceName).map(fieldName))
                        .type(STRING)
                        .build())
                .build())

        when:
        entity = dao.put(spaceName, entity)

        then:
        replicaDao.selectByIndex(spaceName, secondaryIndex, setOf(entity.getString(fieldName)))
                .first()
                .getString(fieldName) == entity.getString(fieldName)

        when:
        def newEntity = dao.update(spaceName, setOf(entity.getInt(idField)) as Set<Integer>,
                assigment(fieldMapping(masterId, spaceName).map(fieldName), fieldName, stringPrimitive(newValue)))

        then:
        replicaDao.getByIndex(spaceName, secondaryIndex, setOf(newValue)) == newEntity

        when:
        dao.deleteByIndex(spaceName, secondaryIndex, setOf(newValue))

        then:
        replicaDao.getByIndex(spaceName, secondaryIndex, setOf(entity.getString(fieldName))) == empty()
        replicaDao.len(spaceName) == 0L

        cleanup:
        dropSpace(masterId, spaceName)
        dropIndex(masterId, spaceName, primaryIndex)
        dropIndex(masterId, spaceName, secondaryIndex)
        dropSequence(masterId, sequence)
    }

    def "should run CRUD operations with tarantool without preparation steps"() {
        setup:
        useAgileConfigurations()
        dao = tarantool(masterId)
        tarantoolModuleState().loadedCommonScripts.clear()
        tarantoolModuleState().loadedValueScripts.clear()

        when:
        entity = dao.put(spaceName, entity)

        then:
        entity.getString(fieldName) == dao.get(spaceName, entity.getInt(idField))
                .map { it.getString(fieldName) }
                .orElse(EMPTY_STRING)

        when:
        dao.delete(spaceName, entity.getInt(idField))

        then:
        empty() == dao.get(spaceName, entity.getInt(idField))
        0L == dao.len(spaceName)

        when:
        dao.upsert(spaceName, entity, assigment(fieldMapping(masterId, spaceName).map(fieldName), fieldName, stringPrimitive(newValue)))
        createIndex(masterId, TarantoolIndexConfiguration.builder()
                .spaceName(spaceName)
                .indexName(secondaryIndex)
                .part(TarantoolIndexConfiguration.Part.builder()
                        .fieldNumber(fieldMapping(masterId, spaceName).map(fieldName))
                        .type(STRING)
                        .build())
                .build())

        then:
        dao.getByIndex(spaceName, secondaryIndex, setOf(entity.getString(fieldName))).isPresent()

        when:
        dao.upsert(spaceName, entity, assigment(fieldMapping(masterId, spaceName).map(fieldName), fieldName, stringPrimitive("Гав")))

        then:
        dao.getByIndex(spaceName, secondaryIndex, setOf("Гав")).isPresent()

        when:
        dao.update(spaceName, setOf(entity.getInt(idField)) as Set<Integer>,
                assigment(fieldMapping(masterId, spaceName).map(fieldName), fieldName, stringPrimitive(newValue)))

        then:
        dao.getByIndex(spaceName, secondaryIndex, setOf(newValue)).isPresent()

        when:
        Entity insertedEntity = entityBuilder()
                .longField("Long", 1L)
                .build()
        dao.updateByIndex(spaceName, secondaryIndex, setOf(newValue) as Set<String>,
                insertion(fieldMapping(masterId, spaceName).map("Entity"), "Entity", insertedEntity),
                assigment(fieldMapping(masterId, spaceName).map(fieldName), fieldName, stringPrimitive("$newValue,$newValue")))

        then:
        dao.getByIndex(spaceName, secondaryIndex, setOf("$newValue,$newValue".toString())).isPresent()
        dao.getByIndex(spaceName, secondaryIndex, setOf("$newValue,$newValue".toString()))
                .map { it.getEntity("Entity") }
                .get() == insertedEntity

        when:
        dao.updateByIndex(spaceName, secondaryIndex, setOf("$newValue,$newValue".toString()) as Set<String>,
                deletion(fieldMapping(masterId, spaceName).map("Entity"), 1),
                assigment(fieldMapping(masterId, spaceName).map(fieldName), fieldName, stringPrimitive(newValue)))

        then:
        dao.getByIndex(spaceName, secondaryIndex, setOf(newValue)).isPresent()
        !dao.getByIndex(spaceName, secondaryIndex, setOf(newValue)).map { it.getEntity("Entity") }.isPresent()

        cleanup:
        dropSpace(masterId, spaceName)
        dropIndex(masterId, spaceName, primaryIndex)
        dropIndex(masterId, spaceName, secondaryIndex)
        dropSequence(masterId, sequence)
    }
}
