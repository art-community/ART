package io.art.model.configurator;

import io.art.configurator.model.*;
import io.art.model.modeling.configurator.*;
import static io.art.core.constants.StringConstants.*;
import static io.art.core.factory.ArrayFactory.*;
import static io.art.core.factory.SetFactory.*;
import java.util.*;

public class ConfiguratorModelConfigurator {
    private final Set<CustomConfigurationModel> customConfigurations = set();

    public ConfiguratorModelConfigurator configuration(Class<?>... models) {
        streamOf(models).forEach(model -> customConfigurations.add(new CustomConfigurationModel(EMPTY_STRING, model)));
        return this;
    }

    public ConfiguratorModelConfigurator configuration(String section, Class<?> model) {
        customConfigurations.add(new CustomConfigurationModel(section, model));
        return this;
    }

    ConfiguratorModuleModel configure() {
        return new ConfiguratorModuleModel(immutableSetOf(customConfigurations));
    }
}
