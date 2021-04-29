/*
 * ART
 *
 * Copyright 2019-2021 ART
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.art.launcher;

import io.art.communicator.module.*;
import io.art.configurator.module.*;
import io.art.core.annotation.*;
import io.art.core.configuration.*;
import io.art.core.context.*;
import io.art.core.module.*;
import io.art.core.property.*;
import io.art.http.module.*;
import io.art.json.module.*;
import io.art.logging.*;
import io.art.message.pack.module.*;
import io.art.model.customizer.*;
import io.art.model.modeling.communicator.*;
import io.art.model.modeling.module.*;
import io.art.model.modeling.server.*;
import io.art.protobuf.module.*;
import io.art.rocks.db.module.*;
import io.art.rsocket.module.*;
import io.art.scheduler.module.*;
import io.art.server.module.*;
import io.art.storage.module.*;
import io.art.tarantool.module.*;
import io.art.value.module.*;
import io.art.xml.module.*;
import io.art.yaml.module.*;
import lombok.experimental.*;
import org.apache.logging.log4j.*;
import static io.art.core.checker.EmptinessChecker.*;
import static io.art.core.checker.NullityChecker.*;
import static io.art.core.context.Context.*;
import static io.art.core.extensions.ThreadExtensions.*;
import static io.art.core.property.LazyProperty.*;
import static io.art.core.singleton.SingletonsRegistry.*;
import static io.art.http.module.HttpModule.*;
import static io.art.launcher.ModuleLauncherConstants.*;
import static io.art.logging.LoggingModule.*;
import static io.art.model.configurator.ModuleModelConfigurator.*;
import static io.art.rsocket.module.RsocketModule.*;
import static java.util.Objects.*;
import static java.util.Optional.*;
import java.util.concurrent.atomic.*;
import java.util.function.*;

@UtilityClass
@UsedByGenerator
public class ModuleLauncher {
    private static final AtomicBoolean LAUNCHED = new AtomicBoolean(false);

    public static void launch(ModuleModel model) {
        if (LAUNCHED.compareAndSet(false, true)) {
            ConfiguratorModule configurator = new ConfiguratorModule();
            ModuleCustomizer moduleCustomizer = model.getCustomizer();
            UnaryOperator<ConfiguratorCustomizer> customizeConfigurator = moduleCustomizer.configurator();
            UnaryOperator<ValueCustomizer> customizeValue = moduleCustomizer.value();
            UnaryOperator<LoggingCustomizer> customizeLogging = moduleCustomizer.logging();
            UnaryOperator<ServerCustomizer> customizeServer = moduleCustomizer.server();
            UnaryOperator<CommunicatorCustomizer> customizeCommunicator = moduleCustomizer.communicator();
            UnaryOperator<RsocketCustomizer> customizeRsocket = moduleCustomizer.rsocket();
            UnaryOperator<StorageCustomizer> customizeStorage = moduleCustomizer.storage();
            UnaryOperator<HttpCustomizer> customizeHttp = moduleCustomizer.http();
            ModuleInitializerRegistry modules = new ModuleInitializerRegistry();
            ModuleConfiguringState state = new ModuleConfiguringState(configurator.configure(), model);

            ConfiguratorCustomizer configuratorCustomizer = singleton(ConfiguratorCustomizer.class, () -> customizeConfigurator.apply(new ConfiguratorCustomizer()));
            ValueCustomizer valueCustomizer = singleton(ValueCustomizer.class, () -> customizeValue.apply(new ValueCustomizer()));
            LoggingCustomizer loggingCustomizer = singleton(LoggingCustomizer.class, () -> customizeLogging.apply(new LoggingCustomizer()));
            Function<ServerModule, ServerCustomizer> serverCustomizer = module ->
                    singleton(ServerCustomizer.class, () -> customizeServer.apply(new ServerCustomizer(module)));
            Function<CommunicatorModule, CommunicatorCustomizer> communicatorCustomizer = module ->
                    singleton(CommunicatorCustomizer.class, () -> customizeCommunicator.apply(new CommunicatorCustomizer(module)));
            Function<RsocketModule, RsocketCustomizer> rsocketCustomizer = module ->
                    singleton(RsocketCustomizer.class, () -> customizeRsocket.apply(new RsocketCustomizer(module)));
            Function<StorageModule, StorageCustomizer> storageCustomizer = module ->
                    singleton(StorageModule.class, () -> customizeStorage.apply(new StorageCustomizer()));
            Function<HttpModule, HttpCustomizer> httpCustomizer = module ->
                    singleton(HttpCustomizer.class, () -> customizeHttp.apply(new HttpCustomizer(module)));

            modules
                    .put(() -> configurator, module -> configurator(module, configuratorCustomizer))
                    .put(ValueModule::new, module -> value(module, state, valueCustomizer))
                    .put(LoggingModule::new, module -> logging(module, state, loggingCustomizer))
                    .put(SchedulerModule::new, module -> scheduler(module, state))
                    .put(JsonModule::new, module -> json(module, state))
                    .put(ProtobufModule::new, module -> protobuf(module, state))
                    .put(MessagePackModule::new, module -> messagePack(module, state))
                    .put(YamlModule::new, module -> yaml(module, state))
                    .put(XmlModule::new, module -> xml(module, state))
                    .put(ServerModule::new, module -> server(module, state, serverCustomizer.apply(module)))
                    .put(CommunicatorModule::new, module -> communicator(module, state, communicatorCustomizer.apply(module)))
                    .put(RsocketModule::new, module -> rsocket(module, state, rsocketCustomizer.apply(module)))
                    .put(HttpModule::new, module -> http(module, state, httpCustomizer.apply(module)))
                    .put(TarantoolModule::new, module -> tarantool(module, state))
                    .put(RocksDbModule::new, module -> rocksDb(module, state))
                    .put(StorageModule::new, module -> storage(module, state, storageCustomizer.apply(module)));

            LazyProperty<Logger> logger = lazy(() -> logger(Context.class));
            ContextConfiguration contextConfiguration = ContextConfiguration.builder()
                    .onUnload(model.getOnUnload())
                    .onLoad(model.getOnLoad())
                    .beforeReload(model.getBeforeReload())
                    .afterReload(model.getAfterReload())
                    .mainModuleId(model.getMainModuleId())
                    .build();
            initialize(contextConfiguration, modules.get(), message -> logger.get().info(message));
            LAUNCHED_MESSAGES.forEach(message -> logger.get().info(message));
            if (needBlock()) block();
        }
    }

    public static void launch() {
        launch(module().configure());
    }

    private static ConfiguratorModule configurator(ConfiguratorModule configuratorModule, ConfiguratorCustomizer configuratorCustomizer) {
        ofNullable(configuratorCustomizer)
                .ifPresent(customizer -> configuratorModule
                        .configure(configurator -> configurator
                                .configure(customizer.configure(configuratorModule.orderedSources()))));
        return configuratorModule;
    }

    private static ValueModule value(ValueModule value, ModuleConfiguringState state, ValueCustomizer customizer) {
        value.configure(configurator -> configurator.from(state.getConfigurator().orderedSources()));
        if (isNull(customizer)) {
            return value;
        }
        value.configure(configurator -> configurator.configure(customizer.getConfiguration()));
        return value;
    }

    private static SchedulerModule scheduler(SchedulerModule scheduler, ModuleConfiguringState state) {
        scheduler.configure(configurator -> configurator.from(state.getConfigurator().orderedSources()));
        return scheduler;
    }

    private static LoggingModule logging(LoggingModule logging, ModuleConfiguringState state, LoggingCustomizer customizer) {
        logging.configure(configurator -> configurator.from(state.getConfigurator().orderedSources()));
        if (isNull(customizer)) {
            return logging;
        }
        logging.configure(configurator -> configurator.configure(customizer.getConfiguration()));
        return logging;
    }

    private static JsonModule json(JsonModule json, ModuleConfiguringState state) {
        json.configure(configurator -> configurator.from(state.getConfigurator().orderedSources()));
        return json;
    }

    private static MessagePackModule messagePack(MessagePackModule messagePack, ModuleConfiguringState state) {
        messagePack.configure(configurator -> configurator.from(state.getConfigurator().orderedSources()));
        return messagePack;
    }

    private static ProtobufModule protobuf(ProtobufModule protobuf, ModuleConfiguringState state) {
        protobuf.configure(configurator -> configurator.from(state.getConfigurator().orderedSources()));
        return protobuf;
    }

    private static YamlModule yaml(YamlModule yaml, ModuleConfiguringState state) {
        yaml.configure(configurator -> configurator.from(state.getConfigurator().orderedSources()));
        return yaml;
    }

    private static XmlModule xml(XmlModule xml, ModuleConfiguringState state) {
        xml.configure(configurator -> configurator.from(state.getConfigurator().orderedSources()));
        return xml;
    }

    private static ServerModule server(ServerModule server, ModuleConfiguringState state, ServerCustomizer customizer) {
        server.configure(configurator -> configurator.from(state.getConfigurator().orderedSources()));
        if (isNull(customizer)) {
            return server;
        }
        server.configure(configurator -> configurator.configure(customizer.getConfiguration()));
        return server;
    }

    private static CommunicatorModule communicator(CommunicatorModule communicator, ModuleConfiguringState state, CommunicatorCustomizer customizer) {
        communicator.configure(configurator -> configurator.from(state.getConfigurator().orderedSources()));
        if (isNull(customizer)) {
            return communicator;
        }
        communicator.configure(configurator -> configurator.configure(customizer.getConfiguration()));
        return communicator;
    }

    private static RsocketModule rsocket(RsocketModule rsocket, ModuleConfiguringState state, RsocketCustomizer rsocketCustomizer) {
        ServerModuleModel serverModel = state.getModel().getServerModel();
        CommunicatorModuleModel communicatorModel = state.getModel().getCommunicatorModel();
        if (isNotEmpty(let(serverModel, ServerModuleModel::getRsocketServices))) {
            rsocketCustomizer.activateServer();
        }
        if (isNotEmpty(let(communicatorModel, CommunicatorModuleModel::getRsocketCommunicators))) {
            rsocketCustomizer.activateCommunicator();
        }
        rsocket.configure(configurator -> configurator
                .from(state.getConfigurator().orderedSources())
                .configure(rsocketCustomizer.getConfiguration()));
        return rsocket;
    }

    private static HttpModule http(HttpModule http, ModuleConfiguringState state, HttpCustomizer httpCustomizer) {
        ServerModuleModel serverModel = state.getModel().getServerModel();
        CommunicatorModuleModel communicatorModel = state.getModel().getCommunicatorModel();

        if (isNotEmpty(let(serverModel, ServerModuleModel::getHttpServices))) {
            httpCustomizer
                    .server(serverModel.getHttpServer())
                    .activateServer();
        }
//        if (isNotEmpty(let(communicatorModel, CommunicatorModuleModel::getCommunicators))) {
//            httpCustomizer.activateCommunicator();
//        }
        http.configure(configurator -> configurator
                .from(state.getConfigurator().orderedSources())
                .configure(httpCustomizer.getConfiguration()));
        return http;
    }

    private static TarantoolModule tarantool(TarantoolModule tarantool, ModuleConfiguringState state) {
        tarantool.configure(configurator -> configurator.from(state.getConfigurator().orderedSources()));
        return tarantool;
    }

    private static StorageModule storage(StorageModule storage, ModuleConfiguringState state, StorageCustomizer storageCustomizer) {
        storage.configure(configurator -> configurator.from(state.getConfigurator().orderedSources()));
        if (isNull(storageCustomizer)) {
            return storage;
        }
        storage.configure(configurator -> configurator.configure(storageCustomizer.getConfiguration()));
        return storage;
    }

    private static RocksDbModule rocksDb(RocksDbModule rocksDb, ModuleConfiguringState state) {
        rocksDb.configure(configurator -> configurator.from(state.getConfigurator().orderedSources()));
        return rocksDb;
    }

    private static boolean needBlock() {
        boolean rsocket = rsocketModule().configuration().isActivateServer();
        boolean http = httpModule().configuration().isActivateServer();
        return rsocket || http;
    }
}
