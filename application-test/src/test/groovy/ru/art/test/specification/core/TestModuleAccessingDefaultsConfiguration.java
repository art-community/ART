package ru.art.test.specification.core;

import lombok.*;
import ru.art.test.specification.core.TestModuleConfiguration.*;
import static ru.art.test.specification.core.TestModule.*;

@Getter
public class TestModuleAccessingDefaultsConfiguration extends TestModuleFileConfiguration {
    private final String value;

    public TestModuleAccessingDefaultsConfiguration() {
        this.value = testModule().getValue() + testModuleState().collection.get(0);
        testModuleState().add(OVERRIDE);
    }
}