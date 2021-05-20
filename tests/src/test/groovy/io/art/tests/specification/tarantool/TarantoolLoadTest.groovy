
package io.art.tests.specification.tarantool

import io.art.tarantool.instance.TarantoolInstance
import io.art.tarantool.space.TarantoolSpace
import io.art.value.immutable.Entity
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
import static io.art.value.factory.PrimitivesFactory.*

class TarantoolLoadTest extends Specification {
    def benchmarkOpsCount = 1000000
    def benchSleepEvery = 500
    def benchTimeout = 10 //due to high freq of requests, every %benchSleepEvery% requests needed timeout to avoid client errors

    def setupSpec(){
        launch module().configure()
    }


    def "AutoIncrement on routers"(){
        setup:
        def spaceName = "r_load"
        def clusterId = "routers"

        TarantoolInstance db = tarantoolInstance(clusterId)
        TarantoolSpace space = db.space(spaceName)

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


        Entity data1 = Entity.entityBuilder()
                .put("id", intPrimitive(1))
                .put("bucket_id", intPrimitive(1))
                .put("data", stringPrimitive("testData"))
                .put("anotherData", stringPrimitive("another data"))
                .build()

        Entity data99 = Entity.entityBuilder()
                .put("id", intPrimitive(1))
                .put("bucket_id", intPrimitive(99))
                .put("data", stringPrimitive("testData"))
                .put("anotherData", stringPrimitive("another data"))
                .build()

        when:
        for (int i = 0; i<benchmarkOpsCount; i++){
            space.autoIncrement(data1)
            if (i%benchSleepEvery == 0) sleep(benchTimeout)
        }
        then:
        true

        cleanup:
        db.dropSpace(spaceName)
    }

    def "AutoIncrement on storage_1"(){
        setup:
        def spaceName = "s1_load"
        def clusterId = "storage1"

        TarantoolInstance db = tarantoolInstance(clusterId)
        TarantoolSpace space = db.space(spaceName)

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

        Entity data = Entity.entityBuilder()
                .put("id", intPrimitive(1))
                .put("data", stringPrimitive("testData"))
                .put("anotherData", stringPrimitive("another data"))
                .build()


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

    def "Routers transactional autoIncrement"(){
        def spaceName = "s1_load_tx"
        def clusterId = "storage1"
        int divider = 1000

        TarantoolInstance db = tarantoolInstance(clusterId)
        TarantoolSpace space = db.space(spaceName)

        db.createSpace(spaceName, tarantoolSpaceConfig()
                .ifNotExists(true))
        db.formatSpace(spaceName, tarantoolSpaceFormat()
                .field("id", UNSIGNED, false))
        db.createIndex(spaceName, "primary", tarantoolSpaceIndex()
                .type(TarantoolIndexType.TREE)
                .part("id")
                .ifNotExists(true)
                .unique(true))

        Entity data = Entity.entityBuilder()
                .put("id", intPrimitive(1))
                .put("bucket_id", intPrimitive(1))
                .put("data", stringPrimitive("testData"))
                .put("anotherData", stringPrimitive("another data"))
                .build()

        when:
        for(int j = 0; j<divider; j++) {
            db.beginTransaction()
            for (int i = 0; i < benchmarkOpsCount / divider; i++) {
                space.autoIncrement(data)
                if (i%benchSleepEvery == 0) sleep(benchTimeout)
            }
            db.commitTransaction()
        }
        then:
        true
    }
}
