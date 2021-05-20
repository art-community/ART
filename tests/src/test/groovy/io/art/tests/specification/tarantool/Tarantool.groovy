
package io.art.tests.specification.tarantool

import io.art.core.caster.Caster
import io.art.tarantool.space.TarantoolSpace
import io.art.value.immutable.Entity
import io.art.value.immutable.Value
import io.art.tarantool.instance.TarantoolInstance
import io.art.tarantool.model.operation.TarantoolUpdateFieldOperation

import spock.lang.Specification

import static io.art.launcher.Launcher.launch
import static io.art.model.configurator.ModuleModelConfigurator.*;
import static io.art.tarantool.configuration.space.TarantoolSpaceFormat.tarantoolSpaceFormat
import static io.art.tarantool.configuration.space.TarantoolSpaceIndex.tarantoolSpaceIndex
import static io.art.tarantool.configuration.space.TarantoolSpaceConfig.tarantoolSpaceConfig
import static io.art.tarantool.constants.TarantoolModuleConstants.TarantoolIndexType
import static io.art.tarantool.constants.TarantoolModuleConstants.TarantoolFieldType.*
import static io.art.tarantool.model.operation.TarantoolUpdateFieldOperation.assigment
import static io.art.value.factory.PrimitivesFactory.*
import static io.art.tarantool.module.TarantoolModule.*
import static io.art.tarantool.constants.TarantoolModuleConstants.TarantoolIndexIterator;

class Tarantool extends Specification {
    def synchronizationTimeout = 60

    def setupSpec(){
        launch module().configure()
    }

    static def createSpace(TarantoolInstance db, String spaceName){
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
    }

    static def createShardedSpace(TarantoolInstance db, String spaceName){
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
    }



    def "Storage CRUD"(){
        setup:
        def spaceName = "s2_CRUD"
        def clusterId = "storage2"
        TarantoolInstance db = tarantoolInstance(clusterId)
        TarantoolSpace space = db.space(spaceName)
        createSpace(db, spaceName)


        Entity data = Entity.entityBuilder()
                .put("id", intPrimitive(null))
                .put("data", stringPrimitive("testData"))
                .put("anotherData", stringPrimitive("another data"))
                .build()
        Value request = intPrimitive(1)


        when:
        def spaces = db.listSpaces()
        def indices = space.listIndices().get()
        then:
        spaces.get().contains(spaceName) && indices.contains("primary")

        when:
        space.insert(data)
        sleep(synchronizationTimeout)
        def response = space.get(request).synchronize()
        then:
        response.isPresent()


        when:

        space.insert(data)
        space.insert(data)
        space.insert(data)
        db.renameSpace(spaceName, spaceName = "s2_CRUD2")
        space = db.space(spaceName)
        data = Entity.entityBuilder()
                .put("id", intPrimitive(7))
                .put("data", stringPrimitive("testData"))
                .build()
        space.insert(data)
        sleep(synchronizationTimeout)
        then:
        space.len().get() == 5


        when:
        request = intPrimitive(5)
        then:
        space.get(request).isEmpty() && space.select(request).get().isEmpty()


        when:
        request = intPrimitive(7)
        response = space.select(request).get().get(0) as Entity
        then:
        response == data


        when:
        space.truncate()
        sleep(synchronizationTimeout)
        then:
        space.count().get() == 0


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
                assigment(2, 'data', stringPrimitive("temp")),
                assigment(2, 'data', stringPrimitive("another")))
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

        when:
        response = space.select(intPrimitive(7))
                .index("primary")
                .iterator(TarantoolIndexIterator.EQ)
                .execute().synchronize()
        then:
        ((Entity) response.get().get(0)).get("id") == intPrimitive(7) && response.get().size() == 1


        cleanup:
        db.dropSpace(spaceName)
    }

    def "Routers CRUD"() {
        setup:
        def clusterId = "routers"
        def spaceName = "r_crud"
        TarantoolInstance db = tarantoolInstance(clusterId)
        TarantoolSpace space = db.space(spaceName)
        createShardedSpace(db, spaceName)
        space.bucketIdGenerator({ ignore -> 99 })



        Entity data = Entity.entityBuilder()
                .put("id", intPrimitive(null))
                .put("data", stringPrimitive("testData"))
                .put("anotherData", stringPrimitive("another data"))
                .build()
        Value request = intPrimitive(1)




        when:
        def spaces = db.listSpaces()
        def indices = space.listIndices()
        then:
        spaces.get().contains(spaceName) && indices.get().contains("primary")


        when:
        space.insert(data)
        then:
        sleep(synchronizationTimeout)
        space.get(request).isPresent()


        when:
        space.insert(data)
        space.insert(data)
        space.insert(data)
        db.renameSpace(spaceName, spaceName = "r_crud2")
        data = Entity.entityBuilder()
                .put("id", intPrimitive(7))
                .put("data", stringPrimitive("testData"))
                .build()
        space = db.space(spaceName)
        space.bucketIdGenerator({ ignored -> 99 })
        space.insert(data)
        then:
        sleep(synchronizationTimeout)
        space.len().get() == 5


        when:
        request = intPrimitive(6)
        then:
        sleep(synchronizationTimeout)
        space.get(request).isEmpty() && space.select(request).get().isEmpty()


        when:
        request = intPrimitive(7)
        sleep(synchronizationTimeout)
        Entity response = Caster.cast(space.select(request).get().get(0))
        then:
        true
        response.get('data') == stringPrimitive("testData")


        when:
        space.truncate()
        sleep(synchronizationTimeout)
        then:
        space.count().get() == 0


        when:
        data = Entity.entityBuilder()
                .put("id", intPrimitive(7))
                .put("data", stringPrimitive("another data"))
                .build()
        space.put(data)
        then:
        sleep(synchronizationTimeout)
        space.get(request).get() == data


        when:
        Value key = intPrimitive(7)
        space.delete(key).synchronize()
        then:
        sleep(synchronizationTimeout)
        space.get(key).isEmpty()


        when:
        space.put(data)
        sleep(synchronizationTimeout)
        space.update(key, TarantoolUpdateFieldOperation.assigment(3, 'data', stringPrimitive("another")))
        then:
        sleep(synchronizationTimeout)
        (space.get(intPrimitive(7)).get() as Entity).get("data") == stringPrimitive("another")

        when:
        space.put(data)
        sleep(synchronizationTimeout)
        data = Entity.entityBuilder()
                .put("id", intPrimitive(7))
                .put("data", stringPrimitive("something"))
                .build()
        space.replace(data).synchronize()
        then:
        space.get(key).get() == data

        when:
        space.upsert(data, TarantoolUpdateFieldOperation.addition(1, 1))
        space.upsert(data, TarantoolUpdateFieldOperation.addition(1, 1))
        then:
        sleep(synchronizationTimeout)
        space.get(intPrimitive(7)).isPresent() && space.get(intPrimitive(8)).isPresent()

        cleanup:
        db.dropSpace(spaceName)
    }

    def "Routers select"(){
        setup:
        def clusterId = "routers"
        def spaceName = "r_select"
        TarantoolInstance db = tarantoolInstance(clusterId)
        TarantoolSpace space = db.space(spaceName)
        createShardedSpace(db, spaceName)
        db.createIndex(spaceName, "dataIndex", tarantoolSpaceIndex().unique(false).part(3))
        space.bucketIdGenerator({ ignore -> 99 })


        Entity data = Entity.entityBuilder()
                .put("id", intPrimitive(null))
                .put("data", stringPrimitive("testData"))
                .put("anotherData", stringPrimitive("another data"))
                .build()


        when:
        space.insert(data)
        space.insert(data)
        space.insert(data)
        space.insert(data)
        space.insert(data)
        sleep(synchronizationTimeout)
        def response = space.select(intPrimitive(5))
                .index("primary")
                .iterator(TarantoolIndexIterator.EQ)
                .execute().synchronize()
        then:
        ((Entity) response.get().get(0)).get("id") == intPrimitive(5) && response.get().size() == 1

        when:
        response = space.select(intPrimitive(3)).index("primary").iterator(TarantoolIndexIterator.GE).execute().synchronize()
        then:
        response.get().size() == 3

        when:
        response = space.select(stringPrimitive("testData")).index("dataIndex").execute().synchronize()
        then:
        response.get().size() == 5


        cleanup:
        db.dropSpace(spaceName)
    }

    def "Storage sequence"(){
        setup:
        def spaceName = "s2_seq"
        def clusterId = "storage2"
        TarantoolInstance db = tarantoolInstance(clusterId)
        TarantoolSpace space = db.space(spaceName)
        createSpace(db, spaceName)


        Entity data = Entity.entityBuilder()
                .put("id", intPrimitive(null))
                .put("data", stringPrimitive("testData"))
                .put("anotherData", stringPrimitive("another data"))
                .build()


        when:
        def response1 = space.insert(data).synchronize()
        space.insert(data)
        space.insert(data).synchronize()
        def response2 = space.get(intPrimitive(3)).synchronize()
        then:
        response1.isPresent() && ((Entity) response2.get()).get("id") == intPrimitive(3)

        cleanup:
        db.dropSpace(spaceName)
    }

}
