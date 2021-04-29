package io.art.storage.configuration;

import io.art.core.module.ModuleConfiguration;
import io.art.core.module.ModuleConfigurator;
import io.art.core.source.ConfigurationSource;
import io.art.storage.registry.StorageSpacesRegistry;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import static io.art.core.checker.NullityChecker.apply;

@Getter
public class StorageModuleConfiguration  implements ModuleConfiguration {
    private StorageSpacesRegistry spacesRegistry = new StorageSpacesRegistry();

    @RequiredArgsConstructor
    public static class Configurator implements ModuleConfigurator<StorageModuleConfiguration, Configurator> {
        private final StorageModuleConfiguration configuration;

        @Override
        public Configurator from(ConfigurationSource source) {

            return this;
        }

        @Override
        public Configurator configure(StorageModuleConfiguration configuration) {
            apply(configuration.getSpacesRegistry(), registry -> this.configuration.spacesRegistry = registry);
            return this;
        }
    }
}
