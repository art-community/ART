
package io.art.tests.specification.storage

import io.art.model.configurator.TarantoolStorageModelConfigurator
import io.art.storage.registry.StorageSpacesRegistry
import io.art.tarantool.instance.TarantoolInstance
import io.art.tarantool.space.TarantoolSpace
import io.art.value.immutable.Entity
import io.art.value.immutable.Value
import spock.lang.Specification
import java.util.function.UnaryOperator

import static io.art.core.constants.EmptyFunctions.emptyFunction
import static io.art.launcher.Launcher.launch
import static io.art.model.configurator.ModuleModelConfigurator.module
import static io.art.tarantool.configuration.space.TarantoolSpaceConfig.tarantoolSpaceConfig
import static io.art.tarantool.configuration.space.TarantoolSpaceFormat.tarantoolSpaceFormat
import static io.art.tarantool.configuration.space.TarantoolSpaceIndex.tarantoolSpaceIndex
import static io.art.tarantool.constants.TarantoolModuleConstants.TarantoolFieldType.UNSIGNED
import static io.art.tarantool.constants.TarantoolModuleConstants.TarantoolIndexType
import static io.art.tarantool.module.TarantoolModule.tarantoolInstance
import static io.art.value.factory.PrimitivesFactory.intPrimitive
import static io.art.value.factory.PrimitivesFactory.stringPrimitive
import static io.art.storage.module.StorageModule.space

class TarantoolStorage extends Specification {

    def setupSpec(){
        UnaryOperator<TarantoolStorageModelConfigurator> storageConfigurator = { space ->
            space
                    .cluster("storage2")
                    .sharded(emptyFunction())
                    .searchBy("any", Value.class)
        }

        def moduleModel = module()
                .store({ storage -> storage.tarantool("s2_seq", Value.class, Value.class, storageConfigurator)})
                .configure()

        def supplier = moduleModel.storageModel.getStorages().get("Value").implement({ value -> value }, { value -> value }, { value -> value })
        def registry = new StorageSpacesRegistry()
        registry.register("Value", supplier)
        moduleModel.initialize({ custom -> custom.storage({ storage -> storage.registry(registry) }) })
        launch moduleModel
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


    def "Storage sequence"(){
        setup:
        def spaceName = "s2_seq"
        def clusterId = "storage2"
        TarantoolInstance db = tarantoolInstance(clusterId)
        TarantoolSpace space = space(Value.class)
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
