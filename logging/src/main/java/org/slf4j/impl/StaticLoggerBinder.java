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

package org.slf4j.impl;

import io.art.core.exception.*;
import lombok.*;
import org.slf4j.*;
import org.slf4j.spi.*;
import static io.art.core.constants.CompilerSuppressingWarnings.*;
import static io.art.logging.module.LoggingModule.*;

@SuppressWarnings(UNUSED)
public final class StaticLoggerBinder implements LoggerFactoryBinder {
    private static final ILoggerFactory FACTORY = name -> new Slf4jLogger(logger(name));
    private static final Class<? extends ILoggerFactory> FACTORY_CLASS = FACTORY.getClass();
    private static final StaticLoggerBinder BINDER = new StaticLoggerBinder();

    @Getter
    private final ILoggerFactory loggerFactory;

    private StaticLoggerBinder() {
        try {
            loggerFactory = FACTORY_CLASS.getConstructor().newInstance();
        } catch (Throwable throwable) {
            throw new InternalRuntimeException(throwable);
        }
    }

    public static StaticLoggerBinder getSingleton() {
        return BINDER;
    }

    @Override
    public String getLoggerFactoryClassStr() {
        return FACTORY_CLASS.getName();
    }
}
