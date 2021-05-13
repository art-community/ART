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

package io.art.logging.constants;

import static io.art.core.constants.DateTimeConstants.*;
import static io.art.core.constants.StringConstants.*;
import static io.art.core.context.Context.*;
import static java.time.Duration.*;
import java.time.*;
import java.time.format.*;

public interface LoggingModuleConstants {
    interface ConfigurationKeys {
        String LOGGING_SECTION = "logging";
        String LOGGING_LOGGERS_SECTION = "logging.loggers";
        String LOGGING_DEFAULT_SECTION = "logging.default";
        String WRITER_SECTION = "writer";
        String WRITERS_SECTION = "writers";
        String LEVEL_KEY = "level";
        String ENABLED_KEY = "enabled";
        String TYPE_KEY = "key";
        String CATEGORIES_KEY = "categories";
        String DATE_TIME_FORMAT_KEY = "dateTimeFormat";
        String COLORED_KEY = "colored";
        String DIRECTORY_KEY = "directory";
        String PREFIX_KEY = "prefix";
        String SUFFIX_KEY = "suffix";
        String TIMESTAMP_FORMAT_KEY = "timestampFormat";
        String ROTATION_PERIOD_KEY = "rotationPeriod";
    }

    interface Defaults {
        String DEFAULT_LOG_FILE_NAME_PREFIX = context().configuration().getMainModuleId() + DASH;
        String DEFAULT_LOG_FILE_NAME_SUFFIX = ".log";
        DateTimeFormatter DEFAULT_LOG_DATE_TIME_FORMAT = DD_MM_YYYY_HH_MM_SS_24H_SSS_DASH_FORMAT;
        DateTimeFormatter DEFAULT_LOG_FILE_TIME_STAMP_FORMAT = DD_MM_YYYY_DASH_FORMAT;
        Duration DEFAULT_LOG_FILE_ROTATION_PERIOD = ofSeconds(10);
    }


    interface Errors {
        String UNABLE_TO_CREATE_LOG_DIRECTORY = "Unable to create log directory ''{0}''";
    }

    String LOGGING_FORMAT = "{0} {1} {2}: {3} - {4}";
}