package ru.art.test.specification.core;

import lombok.*;
import ru.art.core.module.Module;
import static lombok.AccessLevel.*;
import static ru.art.core.context.Context.*;
import static ru.art.logging.LoggingModule.*;


@Getter
public class TestModule implements Module<TestModuleConfiguration, TestModuleState> {
    @Getter(lazy = true, value = PRIVATE)
    private final static TestModuleConfiguration testModule = context().getModule(TestModule.class.getName(), TestModule::new);
    @Getter(lazy = true, value = PRIVATE)
    private final static TestModuleState testModuleState = context().getModuleState(TestModule.class.getName(), TestModule::new);
    private final String id = TestModule.class.getName();
    private final TestModuleConfiguration defaultConfiguration = new TestModuleDefaultConfiguration();
    private final TestModuleState state = new TestModuleState();

    public static TestModuleConfiguration testModule() {
        if (contextIsNotReady()) {
            loggingModule().getLogger(TestModule.class).info("Context is not ready. Using default configuration");
            return new TestModuleDefaultConfiguration();
        }
        return getTestModule();
    }

    public static TestModuleState testModuleState() {
        return getTestModuleState();
    }
}