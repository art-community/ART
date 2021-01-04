package ru.art.test.specification.configuration

import ru.art.config.Config
import ru.art.config.ConfigProvider
import ru.art.core.constants.StringConstants
import ru.art.core.extension.FileExtensions
import spock.lang.Specification


import static ru.art.config.extensions.activator.AgileConfigurationsActivator.useAgileConfigurations
import static ru.art.core.constants.SystemProperties.CONFIG_FILE_PATH_PROPERTY
import static ru.art.core.extension.FileExtensions.*

class ConfigurationReadingSpecification extends Specification {
    def "should read YAML config"() {
        setup:
        def configFile = "config.yml"
        System.setProperty(CONFIG_FILE_PATH_PROPERTY, configFile)
        def config = """
        config:
            string: "test"
            int: 123
            bool: false
            intList: [1,2,3]
            map:
                a: 
                    fieldString: test
                    fieldInt: 123
                c: 
                    fieldString: test
            properties:
                a: b
                c: d
            list:
                - config1:
                    inner: a
                - config2:
                    inner: b
            inner:
                test: 123      
            path.with.dot: "test"               
            embedded:
                path:
                    with.dot: "test"               
                list:
                    - with.dot: "test"               
                    - with.dot: "test"               
        """
        FileExtensions.writeFile(configFile, config)

        when:
        useAgileConfigurations()
        def providedConfig = ConfigProvider.config("config")
        def stringValue = providedConfig.getString("string")
        def intValue = providedConfig.getInt("int")
        def boolValue = providedConfig.getBool("bool")
        def intListValue = providedConfig.getIntList("intList")
        def mapValue = providedConfig.getKeys("map").collectEntries { key -> [(key): providedConfig.getConfig("map.$key")] }
        def propertiesValue = providedConfig.getProperties("properties")
        def listConfigValue = providedConfig.getConfigList("list")
        def innerValue = providedConfig.getConfig("inner").getString("test")
        def pathWithDotValue = providedConfig.getString("path.with.dot")
        def embeddedPathWithDotValue = providedConfig.getString("embedded.path.with.dot")
        def embeddedListValue = providedConfig.getConfigList("embedded.list")
        def embeddedPropertiesValue = providedConfig.getProperties("embedded.path")

        then:
        stringValue == "test"
        intValue == 123
        !boolValue
        intListValue == [1, 2, 3]
        (mapValue["a"] as Config).getString("fieldString") == "test"
        (mapValue["a"] as Config).getInt("fieldInt") == 123
        (mapValue["c"] as Config).getString("fieldString") == "test"
        propertiesValue["a"] == "b"
        propertiesValue["c"] == "d"
        innerValue == "123"
        pathWithDotValue == "test"
        embeddedPathWithDotValue == "test"
        listConfigValue.size() == 2
        listConfigValue[0].getConfig("config1").getString("inner") == "a"
        listConfigValue[1].getString("config2.inner") == "b"
        embeddedListValue.size() == 2;
        embeddedListValue[0].getString("with.dot") == "test"
        embeddedListValue[1].getString("with.dot") == "test"
        embeddedPropertiesValue["with.dot"] == "test"

        cleanup:
        deleteFileRecursive(configFile)
        System.setProperty(CONFIG_FILE_PATH_PROPERTY, StringConstants.EMPTY_STRING)
    }
}