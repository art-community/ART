
package io.art.tests.specification.tarantool

import io.art.tarantool.instance.TarantoolInstance
import io.art.tarantool.space.TarantoolSpace
import io.art.value.immutable.Entity
import io.art.value.immutable.Value
import spock.lang.Specification

import static io.art.launcher.Launcher.launch
import static io.art.model.configurator.ModuleModelConfigurator.module
import static io.art.tarantool.configuration.space.TarantoolSpaceConfig.tarantoolSpaceConfig
import static io.art.tarantool.configuration.space.TarantoolSpaceFormat.tarantoolSpaceFormat
import static io.art.tarantool.configuration.space.TarantoolSpaceIndex.tarantoolSpaceIndex
import static io.art.tarantool.constants.TarantoolModuleConstants.TarantoolFieldType.STRING
import static io.art.tarantool.constants.TarantoolModuleConstants.TarantoolFieldType.UNSIGNED
import static io.art.tarantool.constants.TarantoolModuleConstants.TarantoolIndexType
import static io.art.tarantool.model.operation.TarantoolUpdateFieldOperation.assigment
import static io.art.tarantool.module.TarantoolModule.tarantoolInstance
import static io.art.value.factory.PrimitivesFactory.intPrimitive
import static io.art.value.factory.PrimitivesFactory.stringPrimitive

class TarantoolTransaction extends Specification {
    def synchronizationTimeout = 60

    def setupSpec(){
        launch module().configure()
    }

    def "Storage1 transactions"() {
        setup:
        def clusterId = "storage1"
        def spaceName = 's1_tx'

        TarantoolInstance db = tarantoolInstance(clusterId)
        TarantoolSpace space = db.space(spaceName)

        Value data = Entity.entityBuilder()
                .put("id", intPrimitive(null))
                .put("data", stringPrimitive("testData"))
                .put("anotherData", stringPrimitive("another data"))
                .build()

        db.createSpace(spaceName, tarantoolSpaceConfig()
                .ifNotExists(true))
        db.formatSpace(spaceName, tarantoolSpaceFormat()
                .field("id", UNSIGNED, false)
                .field("data", STRING, false))
        db.createIndex(spaceName, "primary", tarantoolSpaceIndex()
                .type(TarantoolIndexType.TREE)
                .part("id")
                .ifNotExists(true)
                .unique(true)
                .sequence())
        db.createIndex(spaceName, 'data', tarantoolSpaceIndex()
                .part('data')
                .ifNotExists(true)
                .unique(false))


        when:
        space.beginTransaction()
        def result1 = space.insert(data)
        space.insert(data)
        space.insert(data)
        def result2 = space.insert(data)
        space.commitTransaction()

        then:
        ((Entity) result1.get()).get('id') == intPrimitive(1) &&
                ((Entity) result2.get()).get('id') == intPrimitive(4)

        when:
        db.beginTransaction()
        result1 = space.insert(data)
        result2 = space.select(result1.useResultField('data'))
                .index('data')
                .execute()
        db.commitTransaction()
        result1.synchronize()
        then:
        result2.get().size() == 5


        cleanup:
        db.dropSpace(spaceName)

    }

    def "Routers transaction1"() {
        setup:
        def clusterId = "routers"
        def spaceName = 'r_tx_ez'

        def db = tarantoolInstance(clusterId)
        def space = db.space(spaceName)
        space.bucketIdGenerator({ignore -> (long) 99})

        Entity data = Entity.entityBuilder()
                .put("id", intPrimitive(null))
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
                .unique(true)
                .sequence())
        db.createIndex(spaceName, 'bucket_id', tarantoolSpaceIndex()
                .part(2)
                .unique(false))


        when:
        space.beginTransaction(space.bucketOf(data))
        def result1 = space.insert(data)
        space.insert(data)
        space.insert(data)
        def result2 = space.insert(data)
        space.commitTransaction()

        then:
        ((Entity) result1.get()).get('id') == intPrimitive(1) &&
                ((Entity) result2.get()).get('id') == intPrimitive(4)

        when:
        db.beginTransaction(space.bucketOf(data))
        result1 = space.insert(data)
        result2 = space.get(result1.useResultField('id'))
        db.commitTransaction()
        result1.synchronize()
        then:
        ((Entity) result2.get()).get('id') == ((Entity) result1.get()).get('id')


        cleanup:
        db.dropSpace(spaceName)

    }

    def "Routers transaction2"() {
        setup:
        def clusterId = "routers"
        def spaceName = "r_tx"


        def db = tarantoolInstance(clusterId)
        def space = db.space(spaceName)
        space.bucketIdGenerator({ignore -> (long) 99})



        Entity data = Entity.entityBuilder()
                .put("id", intPrimitive(3))
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
                .unique(true)
                .sequence())
        db.createIndex(spaceName, 'bucket_id', tarantoolSpaceIndex()
                .part(2)
                .unique(false))


        when:
        db.beginTransaction(space.bucketOf(data))
        def response1 = space.insert(data)
        def response2 = space.get(intPrimitive(3))
        db.commitTransaction()
        response1.get()
        then:
        response1.get() == response2.get()


        when:
        data = Entity.entityBuilder()
                .put("id", intPrimitive(null))
                .put("data", stringPrimitive("testData"))
                .put("anotherData", stringPrimitive("another data"))
                .build()

        db.beginTransaction(space.bucketOf(data))
        space.insert(data)
        space.insert(data)
        space.insert(data)
        db.commitTransaction()
        db.renameSpace(spaceName, spaceName = "r_tx2")

        space = db.space(spaceName)
        space.bucketIdGenerator({ignore -> (long) 99})

        space.insert(data)
        then:
        def len = space.len()
        sleep(synchronizationTimeout)
        len.get() == 5


        when:
        data = Entity.entityBuilder()
                .put("id", intPrimitive(7))
                .put("data", stringPrimitive("another data"))
                .build()

        db.beginTransaction(space.bucketOf(data))
        space.put(data)
        response1 = space.get(intPrimitive(7))
        db.commitTransaction()
        response1.synchronize()

        then:
        response1.get() == data


        when:
        Value key = intPrimitive(7)

        db.beginTransaction(space.bucketOf(data))
        response1 = space.delete(key)
        response2 = space.get(key)
        db.commitTransaction()
        response1.synchronize()
        then:
        response2.isEmpty()


        when:
        db.beginTransaction(space.bucketOf(data))
        space.put(data)
        space.update(key,
                assigment(2, 'data', stringPrimitive("temp")),
                assigment(2, 'data', stringPrimitive("another")))
        response1 = space.get(key)
        db.commitTransaction()
        response1.synchronize()

        then:
        ((Entity)response1.get()).get("data") == stringPrimitive("another")

        when:
        db.beginTransaction(space.bucketOf(data))
        space.put(data)
        data = Entity.entityBuilder()
                .put("id", intPrimitive(7))
                .put("data", stringPrimitive("something"))
                .build()
        space.replace(data)
        response1 = space.get(key)
        db.commitTransaction()
        response1.synchronize()
        then:
        response1.get() == data

        cleanup:
        db.dropSpace(spaceName)
    }

}
