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

package ru.art.generator.mapper.constants;

import static java.io.File.*;

/**
 * Interface for common constants of mapper generator
 */
public interface Constants {

    String BUILD_METHOD = ".build()";
    String IS_NOT_EMPTY = "isNotEmpty";
    String MAPPER = "Mapper";
    String GET = "get";
    String IS = "is";
    String REQUEST = "Request";
    String RESPONSE = "Response";
    String START_GENERATING = "Start generating ''{0}''";
    String GENERATED_SUCCESSFULLY = "Mapper ''{0}'' has been generated \033[36msuccessfully\033[0m";
    String EMPTY_IF_NULL = "emptyIfNull";
    String NOT_GENERATED_FIELDS = "notGeneratedFields";
    String BUILDER = "Builder";

    interface SymbolsAndFormatting {
        String STRING_PATTERN = "$S";
        String PATTERN_FOR_GENERIC_INNER_TYPES = ".+<.+<.+";
    }

    interface PathAndPackageConstants {
        String SRC_MAIN_JAVA = "src" + separator + "main" + separator + "java";
        String DOT_MAPPER_DOT = ".mapper.";
        String DOT_MODEL_DOT = ".model.";
        String DOT_MAPPING_DOT = ".mapping.";
        String BUILD = "build";
        String BUILD_CLASSES_JAVA_MAIN = "build" + separator + "classes" + separator + "java" + separator + "main";
        String DOT_CLASS = ".class";
        String DOT_JAVA = ".java";
        String MAIN = "main";
        String DOT_JAR = ".jar";
    }

    interface SupportedJavaClasses {
        String CLASS_STRING = "java.lang.String";
        String CLASS_INTEGER = "java.lang.Integer";
        String CLASS_DOUBLE = "java.lang.Double";
        String CLASS_LONG = "java.lang.Long";
        String CLASS_BYTE = "java.lang.Byte";
        String CLASS_BOOLEAN = "java.lang.Boolean";
        String CLASS_FLOAT = "java.lang.Float";
        String CLASS_DATE = "java.util.Date";
        String CLASS_LIST = "java.util.List";
        String CLASS_SET = "java.util.Set";
        String CLASS_MAP = "java.util.Map";
        String CLASS_QUEUE = "java.util.Queue";
        String CLASS_INTEGER_UNBOX = "int";
        String CLASS_DOUBLE_UNBOX = "double";
        String CLASS_LONG_UNBOX = "long";
        String CLASS_BYTE_UNBOX = "byte";
        String CLASS_BOOLEAN_UNBOX = "boolean";
        String CLASS_FLOAT_UNBOX = "float";
        String CLASS_ENTITY = "ru.art.entity.Entity";
    }
}
