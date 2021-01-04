/*
 *    Copyright 2019 ART
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package ru.art.test.specification.configuration

import org.apache.logging.log4j.Level
import org.junit.Ignore
import ru.art.config.module.ConfigModule
import ru.art.core.configurator.ModuleConfigurator
import ru.art.http.constants.MimeToContentTypeMapper
import ru.art.http.server.module.HttpServerModule
import ru.art.logging.LoggingModule
import spock.lang.Specification
import spock.lang.Unroll

import java.util.concurrent.locks.ReentrantLock

import static org.apache.logging.log4j.Level.TRACE
import static ru.art.config.constants.ConfigType.*
import static ru.art.config.module.ConfigModule.configModule
import static ru.art.core.context.Context.context
import static ru.art.http.constants.MimeToContentTypeMapper.imageGif
import static ru.art.http.constants.MimeToContentTypeMapper.imageJpeg
import static ru.art.http.server.HttpServerModuleConfiguration.HttpServerModuleDefaultConfiguration
import static ru.art.http.server.constants.HttpServerModuleConstants.HTTP_SERVER_MODULE_ID
import static ru.art.http.server.module.HttpServerModule.httpServerModule
import static ru.art.logging.LoggingModule.loggingModule
import static ru.art.logging.LoggingModuleConfiguration.LoggingModuleDefaultConfiguration
import static ru.art.logging.LoggingModuleConstants.LOGGING_MODULE_ID
import static ru.art.test.specification.configuration.Configurations.lazyHttpServerAgileConfiguration
import static ru.art.test.specification.configuration.Configurations.lazyLoggingAgileConfiguration
import static ru.art.test.specification.configuration.ModuleConfigGenerator.restoreConfig
import static ru.art.test.specification.configuration.ModuleConfigGenerator.writeModuleConfig

@Ignore
class ConfigurationSpecification extends Specification {
    static lock = new ReentrantLock()
    def expectedLoggingConfiguration = new LoggingModuleDefaultConfiguration() {
        Level level = TRACE
    }
    def expectedHttpServerConfiguration = new HttpServerModuleDefaultConfiguration() {
        int port = 12
        String path = "myPath"
        int maxThreadsCount = 123
        int minSpareThreadsCount = 456
        boolean enableRawDataTracing = true
        boolean enableValueTracing = true
        boolean enableMetrics = false
        MimeToContentTypeMapper consumesMimeTypeMapper = imageGif()
        MimeToContentTypeMapper producesMimeTypeMapper = imageJpeg()
        String host = "myHost"
        boolean web = false
    }


    @Unroll
    "Should read configuration from #type and compare it with expected"() {
        setup:
        lock.lock()
        writeModuleConfig(type)
        def configuration = new ConfigModuleConfiguration(configType: type)
        context().with {
            loadModule new ConfigModule(), { configuration } as ModuleConfigurator
            (configModule() as ConfigModuleConfiguration).configType = type
            loadModule new LoggingModule(), { lazyLoggingAgileConfiguration() } as ModuleConfigurator
            loadModule new HttpServerModule(), { lazyHttpServerAgileConfiguration() } as ModuleConfigurator
            refreshModule LOGGING_MODULE_ID
            refreshModule HTTP_SERVER_MODULE_ID
        }

        expect:
        loggingModule().with {
            level == expectedLoggingConfiguration.level
        }
        httpServerModule().with {
            port == expectedHttpServerConfiguration.port
            path == expectedHttpServerConfiguration.path
            maxThreadsCount == expectedHttpServerConfiguration.maxThreadsCount
            minSpareThreadsCount == expectedHttpServerConfiguration.minSpareThreadsCount
            enableValueTracing == expectedHttpServerConfiguration.enableValueTracing
            enableMetrics == expectedHttpServerConfiguration.enableMetrics
            consumesMimeTypeMapper == expectedHttpServerConfiguration.consumesMimeTypeMapper
            producesMimeTypeMapper == expectedHttpServerConfiguration.producesMimeTypeMapper
            host == expectedHttpServerConfiguration.host
            web == expectedHttpServerConfiguration.web
        }

        cleanup:
        restoreConfig()
        lock.unlock()

        where:
        type       || _
        YAML       || _
        PROPERTIES || _
        JSON       || _
    }
}
