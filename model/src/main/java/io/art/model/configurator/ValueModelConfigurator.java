package io.art.model.configurator;

import io.art.model.modeling.value.*;
import static io.art.core.factory.ArrayFactory.*;
import static io.art.core.factory.SetFactory.*;
import java.lang.reflect.*;
import java.util.*;

public class ValueModelConfigurator {
    private final Set<Type> mappedTypes = set();

    public ValueModelConfigurator mapping(Type... types) {
        streamOf(types).forEach(mappedTypes::add);
        return this;
    }

    ValueModuleModel configure() {
        return new ValueModuleModel(immutableSetOf(mappedTypes));
    }

}
