package ru.art.test.specification.core;

import ru.art.core.module.*;

public interface TestModuleConfiguration extends ModuleConfiguration {
    String FROM_FILE = "from file";
    String DEFAULT = "default";
    String OVERRIDE = "override";

    String getValue();
}