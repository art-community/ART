
package io.art.tests.specification.tarantool


import io.art.tarantool.space.TarantoolSpace
import io.art.value.immutable.Entity
import io.art.value.immutable.Value
import io.art.tarantool.instance.TarantoolInstance

import io.art.tarantool.model.operation.TarantoolUpdateFieldOperation
import spock.lang.Specification


import static io.art.launcher.Launcher.launch
import static io.art.model.configurator.ModuleModelConfigurator.*;


import static io.art.tarantool.configuration.space.TarantoolSpaceConfig.tarantoolSpaceConfig
import static io.art.tarantool.configuration.space.TarantoolSpaceFormat.tarantoolSpaceFormat
import static io.art.tarantool.configuration.space.TarantoolSpaceIndex.tarantoolSpaceIndex
import static io.art.tarantool.constants.TarantoolModuleConstants.TarantoolFieldType.NUMBER
import static io.art.tarantool.constants.TarantoolModuleConstants.TarantoolFieldType.UNSIGNED
import static io.art.tarantool.constants.TarantoolModuleConstants.TarantoolIndexType
import static io.art.tarantool.module.TarantoolModule.tarantoolInstance
import static io.art.value.factory.PrimitivesFactory.intPrimitive
import static io.art.value.factory.PrimitivesFactory.stringPrimitive

class TarantoolBenchmarks extends Specification {
    def benchmarkOpsCount = 100000
    def synchronizationTimeout = 50
    def benchSleepEvery = 500
    def benchTimeout = 10 //due to high freq of requests, every %benchSleepEvery% requests needed timeout to avoid client errors

    def setupSpec(){
        launch module().configure()
    }

    def "Storage CRUD(warmup)"(){
        setup:
        def spaceName = "s2_CRUD"
        def clusterId = "storage2"


        TarantoolInstance db = tarantoolInstance(clusterId)
        TarantoolSpace space = db.space(spaceName)



        Entity data = Entity.entityBuilder()
                .put("id", intPrimitive(3))
                .put("data", stringPrimitive("testData"))
                .put("anotherData", stringPrimitive("another data"))
                .build()
        Value request = intPrimitive(3)

        db.createSpace(spaceName, tarantoolSpaceConfig()
                .ifNotExists(true))
        db.formatSpace(spaceName, tarantoolSpaceFormat()
                .field("id", NUMBER, false))
        db.createIndex(spaceName, "primary", tarantoolSpaceIndex()
                .type(TarantoolIndexType.TREE)
                .part("id")
                .ifNotExists(true)
                .unique(true))


        when:
        def spaces = db.listSpaces()
        def indices = space.listIndices().get()
        then:
        spaces.get().contains(spaceName) && indices.contains("primary")

        when:
        space.insert(data)
        sleep(synchronizationTimeout)
        then:
        (Entity) space.get(request).get() == data


        when:
        space.autoIncrement(data)
        space.autoIncrement(data)
        space.autoIncrement(data)
        db.renameSpace(spaceName, spaceName = "s2_CRUD2")
        space = db.space(spaceName)
        data = Entity.entityBuilder()
                .put("id", intPrimitive(7))
                .put("data", stringPrimitive("testData"))
                .build()
        space.autoIncrement(data)
        sleep(synchronizationTimeout)
        then:
        ((space.len().get() == 5) && (space.schemaLen().get() == 2))


        when:
        request = intPrimitive(2)
        then:
        space.get(request).isEmpty() && space.select(request).execute().isEmpty()


        when:
        request = intPrimitive(7)
        Entity response = space.select(request).get().get(0) as Entity
        then:
        response == data


        when:
        space.truncate()
        sleep(synchronizationTimeout)
        then:
        (space.count().get() == 0) && (space.schemaCount().get() == 0)


        when:
        data = Entity.entityBuilder()
                .put("id", intPrimitive(7))
                .put("data", stringPrimitive("another data"))
                .build()
        space.put(data)
        sleep(synchronizationTimeout)
        then:
        space.get(request).get() == data


        when:
        space.delete(intPrimitive(7))
        sleep(synchronizationTimeout)
        then:
        space.get(request).isEmpty()


        when:
        space.put(data)
        space.update(intPrimitive(7),
                TarantoolUpdateFieldOperation.assigment(2, 'data', stringPrimitive("another")))
        sleep(synchronizationTimeout)
        then:
        (space.get(request).get() as Entity).get("data") == stringPrimitive("another")

        when:
        space.put(data)
        data = Entity.entityBuilder()
                .put("id", intPrimitive(7))
                .put("data", stringPrimitive("something"))
                .build()
        space.replace(data)
        sleep(synchronizationTimeout)
        then:
        space.get(intPrimitive(7)).get() == data

        when:
        space.upsert(data, TarantoolUpdateFieldOperation.addition(1, 1))
        space.upsert(data, TarantoolUpdateFieldOperation.addition(1, 1))
        sleep(synchronizationTimeout)
        then:
        space.get(intPrimitive(7)).isPresent() && space.get(intPrimitive(8)).isPresent()


        cleanup:
        db.dropSpace(spaceName)
    }

    def "Routers art.get benchmark"(){
        setup:
        def clusterId = "routers"
        def spaceName = "r1_art_get_bench"

        TarantoolInstance db = tarantoolInstance(clusterId)
        TarantoolSpace space = db.space(spaceName)



        Entity data = Entity.entityBuilder()
                .put("id", intPrimitive(3))
                .put("bucket_id", intPrimitive(99))
                .put("data", stringPrimitive("testData"))
                .put("anotherData", stringPrimitive("another data"))
                .build()
        Value request = intPrimitive(3)

        db.createSpace(spaceName, tarantoolSpaceConfig()
                .ifNotExists(true))
        db.formatSpace(spaceName, tarantoolSpaceFormat()
                .field("id", UNSIGNED, false)
                .field("bucket_id", UNSIGNED))
        db.createIndex(spaceName, "primary", tarantoolSpaceIndex()
                .type(TarantoolIndexType.TREE)
                .part("id")
                .ifNotExists(true)
                .unique(true))
        db.createIndex(spaceName, 'bucket_id', tarantoolSpaceIndex()
                .part(2)
                .unique(false))

        when:
        space.insert(data)

        for (int i = 0; i<benchmarkOpsCount; i++){
            space.get(request)
            if (i%benchSleepEvery == 0) sleep(benchTimeout)
        }
        then:
        true

        cleanup:
        db.dropSpace(spaceName)
    }

    def "Routers art.auto_increment benchmark"(){
        setup:
        def clusterId = "routers"
        def spaceName = "r2_art_inc_bench"

        TarantoolInstance db = tarantoolInstance(clusterId)
        TarantoolSpace space = db.space(spaceName)



        Entity data = Entity.entityBuilder()
                .put("id", intPrimitive(3))
                .put("bucket_id", intPrimitive(99))
                .put("data", stringPrimitive("testData"))
                .put("anotherData", stringPrimitive("another data"))
                .build()

        db.createSpace(spaceName, tarantoolSpaceConfig()
                .ifNotExists(true))
        db.formatSpace(spaceName, tarantoolSpaceFormat()
                .field("id", UNSIGNED, false)
                .field("bucket_id", UNSIGNED))
        db.createIndex(spaceName, "primary", tarantoolSpaceIndex()
                .type(TarantoolIndexType.TREE)
                .part("id")
                .ifNotExists(true)
                .unique(true))
        db.createIndex(spaceName, 'bucket_id', tarantoolSpaceIndex()
                .part(2)
                .unique(false))

        when:
        for (int i = 0; i<benchmarkOpsCount; i++){
            space.autoIncrement(data)
            if (i%benchSleepEvery == 0) sleep(benchTimeout)
        }
        then:
        true

        cleanup:
        db.dropSpace(spaceName)
    }

    def "Storage1 art.get benchmark"(){
        setup:
        def spaceName = "s1_art_get_bench"
        def clusterId = "storage1"

        TarantoolInstance db = tarantoolInstance(clusterId)
        TarantoolSpace space = db.space(spaceName)



        Entity data = Entity.entityBuilder()
                .put("id", intPrimitive(3))
                .put("data", stringPrimitive("testData"))
                .put("anotherData", stringPrimitive("another data"))
                .build()
        Value request = intPrimitive(3)

        db.createSpace(spaceName, tarantoolSpaceConfig()
                .ifNotExists(true))
        db.formatSpace(spaceName, tarantoolSpaceFormat()
                .field("id", NUMBER, false))
        db.createIndex(spaceName, "primary", tarantoolSpaceIndex()
                .type(TarantoolIndexType.TREE)
                .id(0)
                .part("id")
                .ifNotExists(true)
                .unique(true))

        when:
        space.insert(data)

        for (int i = 0; i<benchmarkOpsCount; i++){
            space.get(request)
            if (i%benchSleepEvery == 0) sleep(benchTimeout)
        }
        then:
        true

        cleanup:
        db.dropSpace(spaceName)
    }

    def "Storage2 art.auto_increment benchmark"(){
        setup:
        def spaceName = "s2_art_inc_bench"
        def clusterId = "storage2"

        TarantoolInstance db = tarantoolInstance(clusterId)
        TarantoolSpace space = db.space(spaceName)



        Entity data = Entity.entityBuilder()
                .put("id", intPrimitive(3))
                .put("data", stringPrimitive("testData"))
                .put("anotherData", stringPrimitive("another data"))
                .build()

        db.createSpace(spaceName, tarantoolSpaceConfig()
                .ifNotExists(true))
        db.formatSpace(spaceName, tarantoolSpaceFormat()
                .field("id", NUMBER, false))
        db.createIndex(spaceName, "primary", tarantoolSpaceIndex()
                .type(TarantoolIndexType.TREE)
                .id(0)
                .part("id")
                .ifNotExists(true)
                .unique(true))

        when:
        space.insert(data)

        for (int i = 0; i<benchmarkOpsCount; i++){
            space.autoIncrement(data)
            if (i%benchSleepEvery == 0) sleep(benchTimeout)
        }
        then:
        true

        cleanup:
        db.dropSpace(spaceName)
    }

}
