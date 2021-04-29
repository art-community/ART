package io.art.model.modeling.configurator;

import io.art.configurator.model.*;
import io.art.core.collection.*;
import lombok.*;
import static io.art.core.collection.ImmutableSet.*;

@Getter
@AllArgsConstructor
public class ConfiguratorModuleModel {
    private final ImmutableSet<CustomConfigurationModel> customConfigurations;

    public ImmutableSet<CustomConfigurationModel> byClass(Class<?> modelClass) {
        return customConfigurations.stream()
                .filter(model -> model.getConfigurationClass().equals(modelClass))
                .collect(immutableSetCollector());
    }
}
