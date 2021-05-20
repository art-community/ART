package io.art.storage.module;

import io.art.core.module.*;
import io.art.storage.configuration.*;
import io.art.storage.state.*;
import io.art.storage.space.*;
import lombok.*;
import static io.art.core.caster.Caster.*;
import static io.art.core.context.Context.*;
import static io.art.storage.configuration.StorageModuleConfiguration.*;
import static lombok.AccessLevel.*;

@Getter
public class StorageModule implements StatefulModule<StorageModuleConfiguration, Configurator, StorageModuleState> {
    @Getter(lazy = true, value = PRIVATE)
    private static final StatefulModuleProxy<StorageModuleConfiguration, StorageModuleState> storageModule = context().getStatefulModule(StorageModule.class.getSimpleName());

    private final String id = StorageModule.class.getSimpleName();
    private final StorageModuleConfiguration configuration = new StorageModuleConfiguration();
    private final Configurator configurator = new Configurator(configuration);
    private final StorageModuleState state = new StorageModuleState();

    public static StatefulModuleProxy<StorageModuleConfiguration, StorageModuleState> storageModule() {
        return getStorageModule();
    }

    public static <T extends Space<?, ?>> T space(Class<?> spaceModelClass) {
        return space(spaceModelClass.getSimpleName());
    }

    public static <T extends Space<?, ?>> T space(String spaceID) {
        return cast(storageModule().configuration().getSpacesRegistry().get(spaceID));
    }
}
