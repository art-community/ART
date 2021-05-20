
package io.art.tests.specification.tarantool

import io.art.tarantool.constants.TarantoolModuleConstants
import io.art.tarantool.instance.TarantoolInstance
import io.art.tarantool.space.TarantoolSpace
import io.art.value.immutable.Entity
import io.art.value.immutable.Primitive
import spock.lang.Specification

import static io.art.launcher.Launcher.launch
import static io.art.model.configurator.ModuleModelConfigurator.module
import static io.art.tarantool.configuration.space.TarantoolSpaceConfig.tarantoolSpaceConfig
import static io.art.tarantool.configuration.space.TarantoolSpaceFormat.tarantoolSpaceFormat
import static io.art.tarantool.configuration.space.TarantoolSpaceIndex.tarantoolSpaceIndex
import static io.art.tarantool.constants.TarantoolModuleConstants.TarantoolFieldType.UNSIGNED
import static io.art.tarantool.constants.TarantoolModuleConstants.TarantoolIndexType
import static io.art.tarantool.model.operation.TarantoolFilterOperation.inRange
import static io.art.tarantool.model.operation.TarantoolSortOperation.ascending
import static io.art.tarantool.module.TarantoolModule.tarantoolInstance
import static io.art.value.factory.PrimitivesFactory.intPrimitive

class TarantoolSelect extends Specification {
    def recordsCount = 1000
    def synchronizationTimeout = 60
    def random = new Random()

    def setupSpec(){
        launch module().configure()
    }

    def newRecord(){
        return Entity.entityBuilder()
                .put("id", intPrimitive(null))
                .put("data", intPrimitive(random.nextInt(recordsCount)))
                .build()
    }

    def "Storage1 select"(){
        setup:
        def clusterId = "storage1"
        def spaceName = "s1_select"

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
                .unique(true)
                .sequence())

        when:
        for (int i = 0; i<recordsCount; i++){
            space.insert(newRecord())
        }
        def len = space.count().get()
        then:
        len == recordsCount


        when:
        def result = space.select(intPrimitive(recordsCount))
                .iterator(TarantoolModuleConstants.TarantoolIndexIterator.LE)
                .limit(10)
                .offset(5)
                .get()
        then:
        result.size() <= 5


        when:
        result = space.select(intPrimitive(recordsCount))
                .iterator(TarantoolModuleConstants.TarantoolIndexIterator.LE)
                .distinct(2)
                .get()
        then:
        result.size() <= recordsCount


        when:
        result = space.select(intPrimitive(recordsCount))
                .iterator(TarantoolModuleConstants.TarantoolIndexIterator.LE)
                .filter(inRange(1, 10, 11))
                .get()
        then:
        result.size() == 2


        when:
        result = space.select(intPrimitive(recordsCount))
                .iterator(TarantoolModuleConstants.TarantoolIndexIterator.LE)
                .sortBy(ascending(2))
                .get()
        then:
        boolean isPassed = true
        for (int i=0; i < recordsCount -1; i++){
            if (
            ((Primitive) ((Entity) result.get(i)).get('data')).getInt() > ((Primitive) ((Entity) result.get(i+1)).get("data")).getInt()
            ) isPassed = false
        }
        isPassed


        cleanup:
        db.dropSpace(spaceName)
    }

    def "routers select"(){
        setup:
        def clusterId = "routers"
        def spaceName = "r_select"

        TarantoolInstance db = tarantoolInstance(clusterId)
        TarantoolSpace space = db.space(spaceName)
        space.bucketIdGenerator({ ignore -> 99 })

        db.createSpace(spaceName, tarantoolSpaceConfig()
                .ifNotExists(true))
        db.formatSpace(spaceName, tarantoolSpaceFormat()
                .field("id", UNSIGNED, false)
                .field("bucket_id", UNSIGNED))
        db.createIndex(spaceName, "primary", tarantoolSpaceIndex()
                .type(TarantoolIndexType.TREE)
                .part("id")
                .ifNotExists(true)
                .unique(true)
                .sequence())
        db.createIndex(spaceName, 'bucket_id', tarantoolSpaceIndex()
                .part(2)
                .unique(false))
        db.createIndex(spaceName, 'data', tarantoolSpaceIndex()
                .part(3)
                .unique(false))

        when:
        for (int i = 0; i<recordsCount; i++){
            space.insert(newRecord())
        }
        def len = space.count().get()
        then:
        len == recordsCount


        when:
        def result = space.select(intPrimitive(recordsCount))
                .iterator(TarantoolModuleConstants.TarantoolIndexIterator.LE)
                .limit(10)
                .offset(5)
                .get()
        then:
        result.size() <= 5


        when:
        result = space.select(intPrimitive(recordsCount))
                .iterator(TarantoolModuleConstants.TarantoolIndexIterator.LE)
                .distinct(2)
                .get()
        then:
        result.size() <= recordsCount


        when:
        sleep(synchronizationTimeout)
        result = space.select(intPrimitive(recordsCount))
                .iterator(TarantoolModuleConstants.TarantoolIndexIterator.LE)
                .filter(inRange(1, 10, 11))
                .get()
        then:
        result.size() == 2


        when:
        result = space.select(intPrimitive(recordsCount))
                .iterator(TarantoolModuleConstants.TarantoolIndexIterator.LE)
                .sortBy(ascending(2))
                .get()
        then:
        boolean isPassed = true
        for (int i=0; i < recordsCount -1; i++){
            if (
            ((Primitive) ((Entity) result.get(i)).get('data')).getInt() > ((Primitive) ((Entity) result.get(i+1)).get("data")).getInt()
            ) isPassed = false
        }
        isPassed


        cleanup:
        db.dropSpace(spaceName)
    }

}
