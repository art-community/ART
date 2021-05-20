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

import lombok.*;
import org.slf4j.*;

@AllArgsConstructor
public class Slf4jLogger implements Logger {
    private final io.art.logging.logger.Logger logger;

    @Override
    public String getName() {
        return logger.getName();
    }

    @Override
    public boolean isTraceEnabled() {
        return logger.isTraceEnabled();
    }

    @Override
    public void trace(String message) {
        logger.trace(message);
    }

    @Override
    public void trace(String format, Object argument) {
        logger.trace(format, argument);
    }

    @Override
    public void trace(String format, Object firstArgument, Object secondArgument) {
        logger.trace(format, firstArgument, secondArgument);
    }

    @Override
    public void trace(String format, Object... arguments) {
        logger.trace(format, arguments);
    }

    @Override
    public void trace(String message, Throwable throwable) {
        logger.trace(message, throwable);
    }

    @Override
    public boolean isTraceEnabled(Marker marker) {
        return logger.isTraceEnabled();
    }

    @Override
    public void trace(Marker marker, String message) {
        logger.trace(message);
    }

    @Override
    public void trace(Marker marker, String format, Object argument) {
        logger.trace(format, argument);
    }

    @Override
    public void trace(Marker marker, String format, Object firstArgument, Object secondArgument) {
        logger.trace(format, firstArgument, secondArgument);
    }

    @Override
    public void trace(Marker marker, String format, Object... argArray) {
        logger.trace(format, argArray);
    }

    @Override
    public void trace(Marker marker, String message, Throwable throwable) {
        logger.trace(message, marker, throwable);
    }

    @Override
    public boolean isDebugEnabled() {
        return logger.isDebugEnabled();
    }

    @Override
    public void debug(String message) {
        logger.debug(message);
    }

    @Override
    public void debug(String format, Object argument) {
        logger.debug(format, argument);
    }

    @Override
    public void debug(String format, Object firstArgument, Object secondArgument) {
        logger.debug(format, firstArgument, secondArgument);
    }

    @Override
    public void debug(String format, Object... arguments) {
        logger.debug(format, arguments);
    }

    @Override
    public void debug(String message, Throwable throwable) {
        logger.debug(message, throwable);
    }

    @Override
    public boolean isDebugEnabled(Marker marker) {
        return logger.isDebugEnabled();
    }

    @Override
    public void debug(Marker marker, String message) {
        logger.debug(message);
    }

    @Override
    public void debug(Marker marker, String format, Object argument) {
        logger.debug(format, argument);
    }

    @Override
    public void debug(Marker marker, String format, Object firstArgument, Object secondArgument) {
        logger.debug(format, firstArgument, secondArgument);
    }

    @Override
    public void debug(Marker marker, String format, Object... arguments) {
        logger.debug(format, format, arguments);
    }

    @Override
    public void debug(Marker marker, String message, Throwable throwable) {
        logger.debug(message, throwable);
    }

    @Override
    public boolean isInfoEnabled() {
        return logger.isInfoEnabled();
    }

    @Override
    public void info(String message) {
        logger.info(message);
    }

    @Override
    public void info(String format, Object argument) {
        logger.info(format, argument);
    }

    @Override
    public void info(String format, Object firstArgument, Object secondArgument) {
        logger.info(format, firstArgument, secondArgument);
    }

    @Override
    public void info(String format, Object... arguments) {
        logger.info(format, arguments);
    }

    @Override
    public void info(String message, Throwable throwable) {
        logger.info(message, throwable);
    }

    @Override
    public boolean isInfoEnabled(Marker marker) {
        return logger.isInfoEnabled();
    }

    @Override
    public void info(Marker marker, String message) {
        logger.info(message);
    }

    @Override
    public void info(Marker marker, String format, Object argument) {
        logger.info(format, argument);
    }

    @Override
    public void info(Marker marker, String format, Object firstArgument, Object secondArgument) {
        logger.info(format, firstArgument, secondArgument);
    }

    @Override
    public void info(Marker marker, String format, Object... arguments) {
        logger.info(format, arguments);
    }

    @Override
    public void info(Marker marker, String message, Throwable throwable) {
        logger.info(message, throwable);
    }

    @Override
    public boolean isWarnEnabled() {
        return logger.isWarnEnabled();
    }

    @Override
    public void warn(String message) {
        logger.warn(message);
    }

    @Override
    public void warn(String format, Object argument) {
        logger.warn(format, argument);
    }

    @Override
    public void warn(String format, Object... arguments) {
        logger.warn(format, arguments);
    }

    @Override
    public void warn(String format, Object firstArgument, Object secondArgument) {
        logger.warn(format, firstArgument, secondArgument);
    }

    @Override
    public void warn(String message, Throwable throwable) {
        logger.warn(message, throwable);
    }

    @Override
    public boolean isWarnEnabled(Marker marker) {
        return logger.isWarnEnabled();
    }

    @Override
    public void warn(Marker marker, String message) {
        logger.warn(message);
    }

    @Override
    public void warn(Marker marker, String format, Object argument) {
        logger.warn(format, argument);
    }

    @Override
    public void warn(Marker marker, String format, Object firstArgument, Object secondArgument) {
        logger.warn(format, firstArgument, secondArgument);
    }

    @Override
    public void warn(Marker marker, String format, Object... arguments) {
        logger.warn(format, arguments);
    }

    @Override
    public void warn(Marker marker, String message, Throwable throwable) {
        logger.warn(message, message, throwable);
    }

    @Override
    public boolean isErrorEnabled() {
        return logger.isErrorEnabled();
    }

    @Override
    public void error(String message) {
        logger.error(message);
    }

    @Override
    public void error(String format, Object argument) {
        logger.error(format, argument);
    }

    @Override
    public void error(String format, Object firstArgument, Object secondArgument) {
        logger.error(format, firstArgument, secondArgument);
    }

    @Override
    public void error(String format, Object... arguments) {
        logger.error(format, arguments);
    }

    @Override
    public void error(String message, Throwable throwable) {
        logger.error(message, throwable);
    }

    @Override
    public boolean isErrorEnabled(Marker marker) {
        return logger.isErrorEnabled();
    }

    @Override
    public void error(Marker marker, String message) {
        logger.error(message);
    }

    @Override
    public void error(Marker marker, String format, Object argument) {
        logger.error(format, argument);
    }

    @Override
    public void error(Marker marker, String format, Object firstArgument, Object secondArgument) {
        logger.error(format, firstArgument, secondArgument);
    }

    @Override
    public void error(Marker marker, String format, Object... arguments) {
        logger.error(format, format, arguments);
    }

    @Override
    public void error(Marker marker, String message, Throwable throwable) {
        logger.error(message, throwable);
    }
}
