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

package ru.art.tarantool.initializer;

import com.mitchellbosecke.pebble.*;
import com.mitchellbosecke.pebble.loader.*;
import lombok.*;
import org.apache.logging.log4j.Logger;
import org.tarantool.*;
import org.zeroturnaround.exec.*;
import ru.art.tarantool.configuration.*;
import ru.art.tarantool.exception.*;
import ru.art.tarantool.module.*;
import static java.io.File.*;
import static java.lang.System.*;
import static java.lang.Thread.*;
import static java.nio.file.Files.*;
import static java.nio.file.Paths.*;
import static java.text.MessageFormat.*;
import static java.util.Objects.*;
import static java.util.Optional.*;
import static java.util.concurrent.ForkJoinPool.*;
import static java.util.concurrent.TimeUnit.*;
import static java.util.stream.Collectors.*;
import static lombok.AccessLevel.*;
import static org.apache.logging.log4j.io.IoBuilder.*;
import static ru.art.core.caster.Caster.*;
import static ru.art.core.checker.CheckerForEmptiness.*;
import static ru.art.core.constants.StringConstants.*;
import static ru.art.core.constants.SystemConstants.*;
import static ru.art.core.converter.WslPathConverter.*;
import static ru.art.core.determinant.SystemDeterminant.*;
import static ru.art.core.extension.FileExtensions.*;
import static ru.art.core.factory.CollectionsFactory.*;
import static ru.art.core.jar.JarExtensions.*;
import static ru.art.core.wrapper.ExceptionWrapper.*;
import static ru.art.logging.LoggingModule.*;
import static ru.art.tarantool.connector.TarantoolConnector.*;
import static ru.art.tarantool.constants.TarantoolModuleConstants.*;
import static ru.art.tarantool.constants.TarantoolModuleConstants.Directories.*;
import static ru.art.tarantool.constants.TarantoolModuleConstants.ExceptionMessages.*;
import static ru.art.tarantool.constants.TarantoolModuleConstants.LoggingMessages.UNABLE_TO_CONNECT_TO_TARANTOOL;
import static ru.art.tarantool.constants.TarantoolModuleConstants.LoggingMessages.*;
import static ru.art.tarantool.constants.TarantoolModuleConstants.Scripts.*;
import static ru.art.tarantool.constants.TarantoolModuleConstants.TarantoolInstanceMode.*;
import static ru.art.tarantool.constants.TarantoolModuleConstants.TemplateParameterKeys.*;
import static ru.art.tarantool.constants.TarantoolModuleConstants.Templates.USER;
import static ru.art.tarantool.constants.TarantoolModuleConstants.Templates.*;
import static ru.art.tarantool.module.TarantoolModule.*;
import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;

public class TarantoolInitializer {
    private final static OutputStream loggerOutputStream = forLogger(loggingModule().getLogger(TarantoolInitializer.class))
            .buildOutputStream();
    @Getter(lazy = true, value = PRIVATE)
    private static final Logger logger = loggingModule().getLogger(TarantoolInitializer.class);

    public static void initializeTarantools() {
        tarantoolModule().getTarantoolConfigurations()
                .entrySet()
                .stream()
                .filter(entry -> nonNull(entry) && nonNull(entry.getKey()) && nonNull(entry.getValue()))
                .map(Map.Entry::getKey)
                .map(TarantoolInitializer::initializeTarantool)
                .forEach(Runnable::run);
        ignoreException(loggerOutputStream::close, getLogger()::error);
    }

    private static Runnable initializeTarantool(String instanceId) {
        TarantoolConfiguration tarantoolConfiguration = getTarantoolConfiguration(instanceId, tarantoolModule().getTarantoolConfigurations());
        ForkJoinTask<?> task = commonPool().submit(() -> {
            try {
                initialTarantoolSynchronously(instanceId, tarantoolConfiguration);
            } catch (Throwable throwable) {
                loggingModule().getLogger(TarantoolInitializer.class).error(throwable.getMessage(), throwable);
                throw throwable;
            }
        });
        ignoreException(() -> sleep(DEFAULT_TARANTOOL_INSTANCE_STARTUP_BETWEEN_TIME));
        return () -> waitInitializationTask(instanceId, tarantoolConfiguration, task);
    }

    private static void waitInitializationTask(String instanceId, TarantoolConfiguration tarantoolConfiguration, ForkJoinTask<?> task) {
        try {
            switch (tarantoolConfiguration.getInstanceMode()) {
                case LOCAL:
                    task.get(tarantoolModule().getLocalConfiguration().getProcessStartupTimeoutMillis(), MILLISECONDS);
                    break;
                case REMOTE:
                    task.get(tarantoolModule().getConnectionTimeoutMillis(), MILLISECONDS);
                    break;
            }
        } catch (Throwable throwable) {
            TarantoolConnectionConfiguration connectionConfiguration = tarantoolConfiguration.getConnectionConfiguration();
            String address = connectionConfiguration.getHost() + COLON + connectionConfiguration.getPort();
            throw new TarantoolInitializationException(format(UNABLE_TO_CONNECT_TO_TARANTOOL, instanceId, address));
        }
    }

    private static void initialTarantoolSynchronously(String instanceId, TarantoolConfiguration tarantoolConfiguration) {
        TarantoolInstanceMode instanceMode = tarantoolConfiguration.getInstanceMode();
        TarantoolConnectionConfiguration connectionConfiguration = tarantoolConfiguration.getConnectionConfiguration();
        String address = connectionConfiguration.getHost() + COLON + connectionConfiguration.getPort();
        try {
            TarantoolClient tarantoolClient = tryConnectToTarantool(instanceId);
            if (tarantoolClient.isAlive()) {
                return;
            }
        } catch (Throwable throwable) {
            if (instanceMode == LOCAL) {
                getLogger().warn(format(UNABLE_TO_CONNECT_TO_TARANTOOL_ON_STARTUP, instanceId, address));
                startTarantool(instanceId);
                return;
            }
            throw throwable;
        }
        if (instanceMode == LOCAL) {
            getLogger().warn(format(UNABLE_TO_CONNECT_TO_TARANTOOL_ON_STARTUP, instanceId, address));
            startTarantool(instanceId);
        }
    }

    @SuppressWarnings("Duplicates")
    private static void startTarantool(String instanceId) {
        try {
            Map<String, TarantoolConfiguration> configurations = tarantoolModule().getTarantoolConfigurations();
            TarantoolConfiguration configuration = getTarantoolConfiguration(instanceId, configurations);

            TarantoolConnectionConfiguration connectionConfiguration = configuration.getConnectionConfiguration();
            TarantoolLocalConfiguration localConfiguration = tarantoolModule().getLocalConfiguration();
            String address = connectionConfiguration.getHost() + COLON + connectionConfiguration.getPort();

            String workingDirectory = localConfiguration.getWorkingDirectory() + separator + instanceId;
            String luaDirectory = workingDirectory + separator + LUA;
            createDirectories(get(luaDirectory));

            Set<String> replicas = setOf(configuration.getReplicas());
            if (isNotEmpty(replicas)) {
                replicas.add(instanceId);
            }
            replicas = replicas.stream()
                    .map(replica -> getTarantoolConfiguration(replica, configurations))
                    .map(TarantoolConfiguration::getConnectionConfiguration)
                    .map(replicaConfiguration ->
                            replicaConfiguration.getUsername() + COLON + replicaConfiguration.getPassword() + AT_SIGN +
                                    replicaConfiguration.getHost() + COLON + replicaConfiguration.getPort())
                    .collect(toSet());
            String luaConfiguration = configuration.getInitialConfiguration().toLua(connectionConfiguration.getPort(), replicas);
            Path luaConfigurationPath = get(getLuaScriptPath(instanceId, localConfiguration, CONFIGURATION));
            writeFile(luaConfigurationPath, luaConfiguration);
            getLogger().info(format(WRITING_TARANTOOL_CONFIGURATION, instanceId,
                    address,
                    luaConfigurationPath.toAbsolutePath()));
            StringWriter templateWriter = new StringWriter();
            Map<String, Object> templateContext = cast(mapOf()
                    .add(USERNAME, connectionConfiguration.getUsername())
                    .add(PASSWORD, connectionConfiguration.getPassword()));
            new PebbleEngine.Builder()
                    .loader(new ClasspathLoader())
                    .autoEscaping(false)
                    .cacheActive(false)
                    .build()
                    .getTemplate(USER + TWIG_TEMPLATE)
                    .evaluate(templateWriter, templateContext);
            String luaUserConfiguration = templateWriter.toString();
            Path userConfigurationPath = get(getLuaScriptPath(instanceId, localConfiguration, USER));
            writeFile(userConfigurationPath, luaUserConfiguration);
            getLogger().info(format(WRITING_TARANTOOL_USER_CONFIGURATION,
                    instanceId,
                    address,
                    userConfigurationPath.toAbsolutePath()));

            if (isEmpty(localConfiguration.getExecutableFilePath()) && isMac()) {
                throw new TarantoolInitializationException(TARANTOOL_ON_MAC_EXCEPTION);
            }

            if (insideJar(TarantoolModule.class)) {
                startTarantoolFromJar(instanceId, localConfiguration, address);
                return;
            }
            startTarantoolOutOfJar(instanceId, localConfiguration, address);

        } catch (Throwable throwable) {
            getLogger().error(format(STARTUP_ERROR, instanceId), throwable);
            throw new TarantoolInitializationException(throwable);
        }
    }

    private static void startTarantoolFromJar(String instanceId, TarantoolLocalConfiguration localConfiguration, String address) throws IOException {
        String workingDirectory = localConfiguration.getWorkingDirectory() + separator + instanceId;
        String executableDirectory = workingDirectory + separator + BIN;
        String luaDirectory = workingDirectory + separator + LUA;
        String executableFilePath = localConfiguration.getExecutableFilePath();
        extractCurrentJarEntry(TarantoolModule.class, LUA_REGEX, getLuaScriptPath(instanceId, localConfiguration, EMPTY_STRING));
        if (isEmpty(executableFilePath) || !isMac()) {
            createDirectories(get(executableDirectory));
            extractCurrentJarEntry(TarantoolModule.class, localConfiguration.getExecutable(), executableDirectory);
        }
        getLogger().info(format(EXTRACT_TARANTOOL_LUA_SCRIPTS, instanceId, address, luaDirectory));
        String executableLogMessage = isEmpty(executableFilePath) || !isMac() ? EXTRACT_TARANTOOL_BINARY : USING_TARANTOOL_BINARY;
        getLogger().info(format(executableLogMessage, instanceId, address, executableDirectory));
        String executableFile = isEmpty(executableFilePath) || !isMac() ? executableDirectory + separator + localConfiguration.getExecutable() : executableFilePath;
        if (!new File(executableFile).setExecutable(true)) {
            getLogger().warn(format(FAILED_SET_EXECUTABLE, executableFile));
        }
        List<String> executableCommand = isWindows()
                ? dynamicArrayOf(WSL, convertToWslPath(executableFile))
                : dynamicArrayOf(executableFile);
        executableCommand.add(convertToWslPath(getLuaScriptPath(instanceId, localConfiguration, INITIALIZATION)));
        StartedProcess process = new ProcessExecutor()
                .command(executableCommand)
                .directory(new File(workingDirectory))
                .redirectOutputAlsoTo(loggerOutputStream)
                .redirectErrorAlsoTo(loggerOutputStream)
                .start();
        waitForTarantoolProcess(instanceId, localConfiguration, address, process);
    }

    private static void startTarantoolOutOfJar(String instanceId, TarantoolLocalConfiguration localConfiguration, String address) throws IOException {
        String workingDirectory = localConfiguration.getWorkingDirectory() + separator + instanceId;
        URL executableUrl = ofNullable(localConfiguration.getExecutableFilePath())
                .filter(file -> isMac())
                .map(File::new)
                .filter(File::exists)
                .map(File::toURI)
                .map(uri -> wrapException(uri::toURL, TarantoolExecutionException::new))
                .orElse(TarantoolInitializer.class.getClassLoader().getResource(localConfiguration.getExecutable()));
        if (isNull(executableUrl)) {
            throw new TarantoolInitializationException(format(TARANTOOL_EXECUTABLE_NOT_EXISTS, address, localConfiguration.getExecutable()));
        }
        if (!new File(executableUrl.getPath()).setExecutable(true)) {
            getLogger().warn(format(FAILED_SET_EXECUTABLE, executableUrl.getPath()));
        }
        List<String> executableCommand;
        executableCommand = isWindows()
                ? dynamicArrayOf(WSL, convertToWslPath(executableUrl.getPath()))
                : dynamicArrayOf(executableUrl.getPath());
        URL initializationScriptUrl = TarantoolInitializer.class.getClassLoader().getResource(INITIALIZATION);
        if (isNull(initializationScriptUrl)) {
            throw new TarantoolInitializationException(format(TARANTOOL_INITIALIZATION_SCRIP_NOT_EXISTS, address));
        }
        executableCommand.add(isWindows() ? convertToWslPath(initializationScriptUrl.getPath()) : initializationScriptUrl.getPath());
        StartedProcess process = new ProcessExecutor()
                .command(executableCommand)
                .directory(new File(workingDirectory))
                .redirectOutputAlsoTo(loggerOutputStream)
                .redirectErrorAlsoTo(loggerOutputStream)
                .start();
        waitForTarantoolProcess(instanceId, localConfiguration, address, process);
    }

    private static String getLuaScriptPath(String instanceId, TarantoolLocalConfiguration localConfiguration, String scriptName) {
        String workingDirectory = localConfiguration.getWorkingDirectory() + separator + instanceId;
        String luaDirectory = workingDirectory + separator + LUA;
        return luaDirectory + separator + scriptName;
    }

    private static void waitForTarantoolProcess(String instanceId, TarantoolLocalConfiguration localConfiguration, String address, StartedProcess process) {
        long current = currentTimeMillis();
        int checkIntervalMillis = localConfiguration.getProcessStartupCheckIntervalMillis();
        int startupTimeoutMillis = localConfiguration.getProcessStartupTimeoutMillis();
        while (!process.getProcess().isAlive()) {
            getLogger().info(format(WAITING_FOR_INITIALIZATION, instanceId, address, startupTimeoutMillis, checkIntervalMillis));
            ignoreException(() -> sleep(checkIntervalMillis));
            if (currentTimeMillis() - current > startupTimeoutMillis) {
                throw new TarantoolInitializationException(format(TARANTOOL_PROCESS_FAILED, instanceId));
            }
        }
        ignoreException(() -> sleep(checkIntervalMillis));
        getLogger().info(format(TARANTOOL_PROCESS_SUCCESSFULLY_STARTED, instanceId, address));
    }
}
