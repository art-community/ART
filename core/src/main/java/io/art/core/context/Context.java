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

package io.art.core.context;

import io.art.core.collection.*;
import io.art.core.configuration.*;
import io.art.core.exception.*;
import io.art.core.module.Module;
import io.art.core.module.*;
import static io.art.core.caster.Caster.*;
import static io.art.core.checker.EmptinessChecker.*;
import static io.art.core.checker.NullityChecker.*;
import static io.art.core.constants.ContextConstants.UNLOAD_THREAD;
import static io.art.core.constants.ExceptionMessages.*;
import static io.art.core.constants.LoggingMessages.*;
import static io.art.core.constants.StringConstants.*;
import static io.art.core.extensions.ThreadExtensions.*;
import static io.art.core.factory.ListFactory.*;
import static io.art.core.factory.MapFactory.*;
import static io.art.core.factory.SetFactory.*;
import static java.lang.Runtime.*;
import static java.text.MessageFormat.*;
import static java.util.Collections.*;
import static java.util.Objects.*;
import java.util.*;
import java.util.function.*;

public class Context {
    private static Context INSTANCE;
    private final Map<String, Module<?, ?>> modules = map();
    private final ContextConfiguration configuration;
    private final Consumer<String> printer;
    private final Service service;

    private Context(ContextConfiguration configuration, Consumer<String> printer) {
        this.printer = printer;
        this.configuration = configuration;
        this.service = new Context.Service();
    }

    public static void prepareInitialization(ContextConfiguration configuration, Consumer<String> printer) {
        if (nonNull(INSTANCE)) {
            throw new InternalRuntimeException(CONTEXT_ALREADY_INITIALIZED);
        }
        INSTANCE = new Context(configuration, printer);
        getRuntime().addShutdownHook(newThread(UNLOAD_THREAD, INSTANCE::unload));
    }

    public static void processInitialization(ImmutableSet<Module<?, ?>> modules) {
        if (isNull(INSTANCE)) {
            throw new InternalRuntimeException(CONTEXT_NOT_INITIALIZED);
        }
        INSTANCE.load(modules);
    }

    public static Context context() {
        if (isNull(INSTANCE)) {
            throw new InternalRuntimeException(CONTEXT_NOT_INITIALIZED);
        }
        return INSTANCE;
    }

    public <C extends ModuleConfiguration> StatelessModuleProxy<C> getStatelessModule(String moduleId) {
        return new StatelessModuleProxy<>(cast(getModule(moduleId)));
    }

    public <C extends ModuleConfiguration, S extends ModuleState> StatefulModuleProxy<C, S> getStatefulModule(String moduleId) {
        return new StatefulModuleProxy<>(cast(getModule(moduleId)));
    }

    public Set<String> getModuleNames() {
        return modules.keySet();
    }

    public boolean hasModule(String moduleId) {
        if (isEmpty(modules)) {
            return false;
        }
        return modules.containsKey(moduleId);
    }

    public ContextConfiguration configuration() {
        return configuration;
    }


    private Module<?, ?> getModule(String moduleId) {
        Module<?, ?> module = modules.get(moduleId);
        if (nonNull(module)) {
            return module;
        }
        throw new InternalRuntimeException(format(MODULE_WAS_NOT_FOUND, moduleId));
    }

    private void load(ImmutableSet<Module<?, ?>> modules) {
        Set<String> messages = setOf(ART_BANNER);
        for (Module<?, ?> module : modules) {
            String moduleId = module.getId();
            messages.add(format(MODULE_LOADED_MESSAGE, moduleId));
            this.modules.put(moduleId, module);
        }
        messages.forEach(printer);
        for (Module<?, ?> module : this.modules.values()) {
            module.onLoad(service);
            ifNotEmpty(module.print(), printer);
        }
        apply(configuration.getOnLoad(), Runnable::run);
    }

    private void unload() {
        List<Module<?, ?>> modules = linkedListOf(this.modules.values());
        reverse(modules);
        for (Module<?, ?> module : modules) {
            printer.accept(format(MODULE_UNLOADED_MESSAGE, module.getId()));
            module.onUnload(service);
            this.modules.remove(module.getId());
        }
        apply(configuration.getOnUnload(), Runnable::run);
        INSTANCE = null;
    }

    public class Service {
        public void reload() {
            for (Map.Entry<String, Module<?, ?>> entry : modules.entrySet()) {
                Module<?, ?> module = entry.getValue();
                printer.accept(format(MODULE_RELOADING_START_MESSAGE, module.getId()));
                module.beforeReload(service);
            }
            apply(configuration.getBeforeReload(), Runnable::run);

            for (Module<?, ?> module : modules.values()) {
                configuration.getReload().accept(module);

                module.afterReload(service);
                printer.accept(format(MODULE_RELOADING_END_MESSAGE, module.getId()));
            }
            apply(configuration.getAfterReload(), Runnable::run);
        }
    }
}
