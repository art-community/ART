package ru.art.test.specification.core

import ru.art.core.configuration.ContextInitialConfiguration
import ru.art.core.configurator.ModuleConfigurator
import ru.art.core.module.ModuleConfiguration
import ru.art.core.provider.PreconfiguredModuleProvider
import spock.lang.Specification

import java.lang.reflect.Field
import java.util.concurrent.atomic.AtomicReference

import static java.lang.reflect.Modifier.FINAL
import static java.util.Optional.empty
import static java.util.Optional.of
import static ru.art.core.constants.ContextConstants.DEFAULT_MAIN_MODULE_ID
import static ru.art.core.context.Context.initContext
import static ru.art.test.specification.core.TestModule.testModule
import static ru.art.test.specification.core.TestModule.testModuleState
import static ru.art.test.specification.core.TestModuleConfiguration.*

class ModulesLoadingSpecification extends Specification {
    static applicationContextConfiguration = new ContextInitialConfiguration.ApplicationContextConfiguration(DEFAULT_MAIN_MODULE_ID, new PreconfiguredModuleProvider() {
        @Override
        Optional<ModuleConfiguration> getModuleConfiguration(String moduleId) {
            switch (moduleId) {
                case TestModule.class.name: return of(new TestModuleFileConfiguration())
                default: empty()
            }
        }
    })

    def "should load modules default configuration"() {
        when:
        def value = testModule().value

        then:
        value == DEFAULT
    }

    def "should load modules file configuration"() {
        setup:
        initContext(applicationContextConfiguration)

        when:
        def value = testModule().value

        then:
        value == FROM_FILE
    }

    def "should load modules override configuration"() {
        setup:
        initContext(applicationContextConfiguration).loadModule(new TestModule(), new TestModuleOverrideConfiguration())

        when:
        def value = testModule().value

        then:
        value == OVERRIDE
    }

    def "should save module between context changes"() {
        setup:
        testModuleState().add(DEFAULT)

        when:
        initContext(applicationContextConfiguration).loadModule(new TestModule(), new TestModuleOverrideConfiguration())
        testModuleState().add(FROM_FILE)

        then:
        testModuleState().collection == [DEFAULT, FROM_FILE]
    }

    def "should load module configuration with accessing state and default configuration during real configuration creating"() {
        setup:
        testModuleState().add(DEFAULT)

        when:
        initContext(applicationContextConfiguration).loadModule(new TestModule(), { new TestModuleAccessingDefaultsConfiguration() } as ModuleConfigurator)

        then:
        testModule().value == DEFAULT + DEFAULT
        testModuleState().collection == [DEFAULT, OVERRIDE]
    }

    def cleanup() {
        Field modifiersField = Field.class.getDeclaredFields0(false).find { field -> field.name == "modifiers" }
        modifiersField.setAccessible(true)

        Field testModule = TestModule.class.getDeclaredField("testModule");
        modifiersField.setInt(testModule, testModule.getModifiers() & ~FINAL);
        testModule.setAccessible(true)
        testModule.set(null, new AtomicReference())

        Field testModuleState = TestModule.class.getDeclaredField("testModuleState");
        modifiersField.setInt(testModuleState, testModuleState.getModifiers() & ~FINAL);
        testModuleState.setAccessible(true)
        testModuleState.set(null, new AtomicReference());
    }
}
