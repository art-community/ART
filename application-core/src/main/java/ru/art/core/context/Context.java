/*
 * ART Java
 *
 * Copyright 2019 ART
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

package ru.art.core.context;

import ru.art.core.configuration.*;
import ru.art.core.configuration.ContextInitialConfiguration.*;
import ru.art.core.configurator.*;
import ru.art.core.constants.*;
import ru.art.core.exception.*;
import ru.art.core.module.Module;
import ru.art.core.module.*;
import ru.art.core.provider.*;
import static java.lang.Runtime.*;
import static java.lang.System.*;
import static java.text.MessageFormat.*;
import static java.util.Objects.*;
import static ru.art.core.caster.Caster.*;
import static ru.art.core.checker.CheckerForEmptiness.*;
import static ru.art.core.constants.ContextState.*;
import static ru.art.core.constants.ExceptionMessages.*;
import static ru.art.core.constants.LoggingMessages.*;
import static ru.art.core.constants.StringConstants.*;
import static ru.art.core.factory.CollectionsFactory.*;
import java.util.*;
import java.util.concurrent.locks.*;
import java.util.function.*;

public class Context {
    private static final ReentrantLock lock = new ReentrantLock();
    private static volatile Context instance;
    private volatile ContextInitialConfiguration initialConfiguration = new ContextInitialDefaultConfiguration();
    private volatile ContextState state = READY;
    private volatile Long lastActionTimestamp = currentTimeMillis();
    private Map<String, ModuleContainer<? extends ModuleConfiguration, ? extends ModuleState>> modules = concurrentHashMap();

    static {
        out.println(ART_BANNER);
    }

    private Context() {
        if (nonNull(instance)) {
            out.println(format(CONTEXT_CHANGED, initialConfiguration.getClass().getName()));
        }
        if (initialConfiguration.isUnloadModulesOnShutdown()) {
            getRuntime().addShutdownHook(new Thread(this::unloadModules));
        }
    }

    private Context(ContextInitialConfiguration initialConfiguration) {
        if (nonNull(instance)) {
            out.println(format(CONTEXT_CHANGED, initialConfiguration.getClass().getName()));
        }
        this.initialConfiguration = initialConfiguration;
        if (initialConfiguration.isUnloadModulesOnShutdown()) {
            getRuntime().addShutdownHook(new Thread(this::unloadModules));
        }
    }

    private Context(ContextInitialConfiguration contextInitialConfiguration, Map<String, ModuleContainer<? extends ModuleConfiguration, ? extends ModuleState>> modules) {
        if (nonNull(instance)) {
            out.println(format(CONTEXT_CHANGED, contextInitialConfiguration.getClass().getName()));
        }
        this.initialConfiguration = contextInitialConfiguration;
        this.modules = modules;
        if (initialConfiguration.isUnloadModulesOnShutdown()) {
            getRuntime().addShutdownHook(new Thread(this::unloadModules));
        }
    }

    public static Context initContext(ContextInitialConfiguration contextInitialConfiguration) {
        if (isNull(contextInitialConfiguration))
            throw new ContextInitializationException(CONTEXT_INITIAL_CONFIGURATION_IS_NULL);
        ReentrantLock lock = Context.lock;
        lock.lock();
        instance = new Context(contextInitialConfiguration);
        lock.unlock();
        return instance;
    }

    public static void recreateContext(ContextInitialConfiguration contextInitialConfiguration) {
        if (isNull(contextInitialConfiguration)) {
            throw new ContextInitializationException(CONTEXT_INITIAL_CONFIGURATION_IS_NULL);
        }
        ReentrantLock lock = Context.lock;
        lock.lock();
        long oldContextLastActionTimestamp = instance.lastActionTimestamp;
        final Map<String, ModuleContainer<? extends ModuleConfiguration, ? extends ModuleState>> modules = instance.modules;
        instance = new Context(contextInitialConfiguration, modules);
        instance.refreshAndReloadModules();
        out.println(format(CONTEXT_RELOADED_MESSAGE, currentTimeMillis() - oldContextLastActionTimestamp));
        instance.lastActionTimestamp = currentTimeMillis();
        lock.unlock();
    }

    public static Context context() {
        Context localInstance = instance;
        if (isNull(localInstance)) {
            ReentrantLock lock = Context.lock;
            lock.lock();
            localInstance = instance;
            if (isNull(localInstance)) {
                instance = new Context();
            }
            lock.unlock();
        }
        return instance;
    }

    public static ContextInitialConfiguration contextConfiguration() {
        return context().initialConfiguration;
    }

    public <C extends ModuleConfiguration, S extends ModuleState> C getModule(String moduleId, Module<C, S> toLoadIfNotExists) {
        if (isNull(moduleId)) throw new ContextInitializationException(MODULE_ID_IS_NULL);
        if (isNull(instance) || state != READY) {
            return toLoadIfNotExists.getDefaultConfiguration();
        }
        ReentrantLock lock = Context.lock;
        lock.lock();
        ModuleContainer<? extends ModuleConfiguration, ? extends ModuleState> moduleContainer = modules.get(moduleId);
        PreconfiguredModuleProvider preconfiguredModulesProvider;
        if (nonNull(moduleContainer)) {
            lock.unlock();
            return cast(moduleContainer.getConfiguration());
        }
        C configuration;
        if (nonNull(preconfiguredModulesProvider = contextConfiguration().getPreconfiguredModulesProvider())) {
            lock.unlock();
            return loadModule(toLoadIfNotExists, preconfiguredModulesProvider);
        }
        lock.unlock();
        loadModule(toLoadIfNotExists, (configuration = toLoadIfNotExists.getDefaultConfiguration()));
        return cast(configuration);
    }

    public <C extends ModuleConfiguration, S extends ModuleState> S getModuleState(String moduleId, Module<C, S> toLoadIfNotExists) {
        if (isNull(moduleId)) throw new ContextInitializationException(MODULE_ID_IS_NULL);
        ReentrantLock lock = Context.lock;
        lock.lock();
        if (isNull(instance) || state == LOADING_MODULES) {
            lock.unlock();
            return toLoadIfNotExists.getState();
        }
        ModuleContainer<? extends ModuleConfiguration, ? extends ModuleState> moduleContainer = modules.get(moduleId);
        PreconfiguredModuleProvider preconfiguredModulesProvider;
        if (nonNull(moduleContainer)) {
            lock.unlock();
            return cast(moduleContainer.getModule().getState());
        }
        if (nonNull(preconfiguredModulesProvider = contextConfiguration().getPreconfiguredModulesProvider())) {
            lock.unlock();
            loadModule(toLoadIfNotExists, preconfiguredModulesProvider);
            return cast(toLoadIfNotExists.getState());
        }
        lock.unlock();
        loadModule(toLoadIfNotExists, toLoadIfNotExists.getDefaultConfiguration());
        return cast(toLoadIfNotExists.getState());
    }

    public <C extends ModuleConfiguration, S extends ModuleState> C getModule(String moduleId, Supplier<Module<C, S>> toLoadIfNotExists) {
        if (isNull(moduleId)) throw new ContextInitializationException(MODULE_ID_IS_NULL);
        ReentrantLock lock = Context.lock;
        lock.lock();
        if (isNull(instance) || state != READY) {
            lock.unlock();
            return toLoadIfNotExists.get().getDefaultConfiguration();
        }
        ModuleContainer<? extends ModuleConfiguration, ? extends ModuleState> moduleContainer = modules.get(moduleId);
        PreconfiguredModuleProvider preconfiguredModulesProvider;
        if (nonNull(moduleContainer)) {
            lock.unlock();
            return cast(moduleContainer.getConfiguration());
        }
        C configuration;
        Module<C, S> module = toLoadIfNotExists.get();
        if (nonNull(preconfiguredModulesProvider = contextConfiguration().getPreconfiguredModulesProvider())) {
            lock.unlock();
            return loadModule(module, preconfiguredModulesProvider);
        }
        lock.unlock();
        loadModule(module, (configuration = module.getDefaultConfiguration()));
        return cast(configuration);
    }

    public <C extends ModuleConfiguration, S extends ModuleState> S getModuleState(String moduleId, Supplier<Module<C, S>> toLoadIfNotExists) {
        if (isNull(moduleId)) throw new ContextInitializationException(MODULE_ID_IS_NULL);
        ReentrantLock lock = Context.lock;
        lock.lock();
        if (isNull(instance) || state == LOADING_MODULES) {
            lock.unlock();
            return toLoadIfNotExists.get().getState();
        }
        ModuleContainer<? extends ModuleConfiguration, ? extends ModuleState> moduleContainer = modules.get(moduleId);
        PreconfiguredModuleProvider preconfiguredModulesProvider;
        if (nonNull(moduleContainer)) {
            lock.unlock();
            return cast(moduleContainer.getModule().getState());
        }
        Module<C, S> module = toLoadIfNotExists.get();
        if (nonNull(preconfiguredModulesProvider = contextConfiguration().getPreconfiguredModulesProvider())) {
            lock.unlock();
            loadModule(module, preconfiguredModulesProvider);
            return module.getState();
        }
        lock.unlock();
        loadModule(module, module.getDefaultConfiguration());
        return cast(module.getState());
    }

    public <C extends ModuleConfiguration, S extends ModuleState> Context loadModule(Module<C, S> module) {
        if (isNull(module)) throw new ContextInitializationException(MODULE_ID_IS_NULL);
        ReentrantLock lock = Context.lock;
        lock.lock();
        ContextState currentState = state;
        state = LOADING_MODULES;
        C configuration = module.getDefaultConfiguration();
        modules.put(module.getId(), new ModuleContainer<>(module, cast(configuration)));
        out.println(format(MODULE_LOADED_MESSAGE, module.getId(), currentTimeMillis() - lastActionTimestamp, configuration.getClass().getName()));
        state = currentState;
        lock.unlock();
        module.onLoad();
        lastActionTimestamp = currentTimeMillis();
        return this;
    }

    public <C extends ModuleConfiguration, S extends ModuleState> Context loadModule(Module<C, S> module, ModuleConfigurator<C, S> moduleConfigurator) {
        if (isNull(module)) throw new ContextInitializationException(MODULE_ID_IS_NULL);
        ReentrantLock lock = Context.lock;
        lock.lock();
        ContextState currentState = state;
        state = LOADING_MODULES;
        C configuration = cast(moduleConfigurator.configure(module));
        modules.put(module.getId(), new ModuleContainer<>(module, configuration));
        out.println(format(MODULE_LOADED_MESSAGE, module.getId(), currentTimeMillis() - lastActionTimestamp, configuration.getClass().getName()));
        state = currentState;
        lock.unlock();
        module.onLoad();
        lastActionTimestamp = currentTimeMillis();
        return this;
    }

    public <C extends ModuleConfiguration, S extends ModuleState> Context loadModule(Module<C, S> module, C customModuleConfiguration) {
        if (isNull(module)) throw new ContextInitializationException(MODULE_ID_IS_NULL);
        ReentrantLock lock = Context.lock;
        lock.lock();
        ContextState currentState = state;
        state = LOADING_MODULES;
        C configuration = cast(nonNull(customModuleConfiguration) ? customModuleConfiguration : module.getDefaultConfiguration());
        modules.put(module.getId(), new ModuleContainer<>(module, configuration));
        state = currentState;
        lock.unlock();
        module.onLoad();
        out.println(format(MODULE_LOADED_MESSAGE, module.getId(), currentTimeMillis() - lastActionTimestamp, configuration.getClass().getName()));
        lastActionTimestamp = currentTimeMillis();
        return this;
    }

    private <C extends ModuleConfiguration, S extends ModuleState> C loadModule(Module<C, S> module, PreconfiguredModuleProvider preconfiguredModulesProvider) {
        ReentrantLock lock = Context.lock;
        lock.lock();
        ContextState currentState = state;
        state = LOADING_MODULES;
        C configuration = cast(preconfiguredModulesProvider.getModuleConfiguration(module.getId()).orElse(module.getDefaultConfiguration()));
        state = currentState;
        lock.unlock();
        loadModule(module, configuration);
        return cast(configuration);
    }

    @SuppressWarnings("Duplicates")
    public <C extends ModuleConfiguration> Context overrideModule(String moduleId, C customModuleConfiguration) {
        if (isNull(moduleId)) throw new ContextInitializationException(MODULE_ID_IS_NULL);
        if (isNull(customModuleConfiguration)) {
            throw new ContextInitializationException(CUSTOM_MODULE_CONFIGURATION_IS_NULL);
        }
        ReentrantLock lock = Context.lock;
        lock.lock();
        ModuleContainer<? extends ModuleConfiguration, ? extends ModuleState> moduleContainer = modules.get(moduleId);
        if (isNull(moduleContainer)) {
            lock.unlock();
            return this;
        }
        ContextState currentState = state;
        state = LOADING_MODULES;
        modules.put(moduleId, moduleContainer.overrideConfiguration(cast(customModuleConfiguration)));
        out.println(format(MODULE_OVERRIDDEN_MESSAGE, moduleId, currentTimeMillis() - lastActionTimestamp));
        lastActionTimestamp = currentTimeMillis();
        state = currentState;
        lock.unlock();
        return this;
    }

    @SuppressWarnings("Duplicates")
    public Context reloadModule(String moduleId) {
        if (isNull(moduleId)) throw new ContextInitializationException(MODULE_ID_IS_NULL);
        ReentrantLock lock = Context.lock;
        lock.lock();
        ModuleContainer<? extends ModuleConfiguration, ? extends ModuleState> moduleContainer = modules.get(moduleId);
        if (isNull(moduleContainer)) {
            lock.unlock();
            return this;
        }
        ContextState currentState = state;
        state = RELOADING_MODULES;
        moduleContainer.reloadModule();
        out.println(format(MODULE_RELOADED_MESSAGE, moduleId, currentTimeMillis() - lastActionTimestamp));
        lastActionTimestamp = currentTimeMillis();
        state = currentState;
        lock.unlock();
        return this;
    }

    @SuppressWarnings("Duplicates")
    public Context refreshModule(String moduleId) {
        if (isNull(moduleId)) throw new ContextInitializationException(MODULE_ID_IS_NULL);
        ReentrantLock lock = Context.lock;
        lock.lock();
        ModuleContainer<? extends ModuleConfiguration, ? extends ModuleState> moduleContainer = modules.get(moduleId);
        if (isNull(moduleContainer)) {
            lock.unlock();
            return this;
        }
        ContextState currentState = state;
        state = REFRESHING_MODULES;
        moduleContainer.refreshConfiguration();
        out.println(format(MODULE_REFRESHED_MESSAGE, moduleId, currentTimeMillis() - lastActionTimestamp));
        lastActionTimestamp = currentTimeMillis();
        state = currentState;
        lock.unlock();
        return this;
    }

    public Context refreshAndReloadModule(String moduleId) {
        if (isNull(moduleId)) throw new ContextInitializationException(MODULE_ID_IS_NULL);
        ReentrantLock lock = Context.lock;
        lock.lock();
        ContextState currentState = state;
        state = REFRESHING_AND_RELOADING_MODULES;
        ModuleContainer<? extends ModuleConfiguration, ? extends ModuleState> moduleContainer = modules.get(moduleId);
        if (isNull(moduleContainer)) {
            lock.unlock();
            return this;
        }
        moduleContainer.reloadModule();
        moduleContainer.refreshConfiguration();
        out.println(format(MODULE_REFRESHED_AND_RELOADED_MESSAGE, moduleId, currentTimeMillis() - lastActionTimestamp));
        lastActionTimestamp = currentTimeMillis();
        state = currentState;
        lock.unlock();
        return this;
    }

    public Context refreshAndReloadModules() {
        modules.keySet().forEach(this::refreshAndReloadModule);
        return this;
    }

    public Context reloadModules() {
        modules.keySet().forEach(this::reloadModule);
        return this;
    }

    public Context refreshModules() {
        modules.keySet().forEach(this::refreshModule);
        return this;
    }

    public Set<String> getModuleNames() {
        return modules.keySet();
    }

    public boolean hasModule(String moduleId) {
        lastActionTimestamp = currentTimeMillis();
        if (isEmpty(modules)) {
            return false;
        }
        return modules.containsKey(moduleId);
    }

    public static boolean contextIsNotReady() {
        ReentrantLock lock = Context.lock;
        lock.lock();
        Context instance = Context.instance;
        lock.unlock();
        return isNull(instance) || instance.state != READY;
    }

    private void unloadModules() {
        modules.values().forEach(module -> module.getModule().onUnload());
        modules.clear();
    }
}
