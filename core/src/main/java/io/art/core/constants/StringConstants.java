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

package io.art.core.constants;

import static java.util.regex.Pattern.*;
import java.util.regex.*;

public interface StringConstants {
    String ART_BANNER = "Welcome to ART!";
    String TRUE_NUMERIC = "1";
    String FALSE_NUMERIC = "0";
    String SHARP = "#";
    String SLASH = "/";
    String BACKWARD_SLASH = "\\";
    String SLASH_REGEX = "\\/";
    String BACKWARD_SLASH_REGEX = "\\\\";
    String DOUBLE_QUOTES = "\"";
    String WILDCARD = "*";
    String EMPTY_STRING = "";
    String SEMICOLON = ";";
    String COLON = ":";
    String DOT = ".";
    String EQUAL = "=";
    String NULL_STRING = "null";
    String SINGLE_QUOTE = "'";
    String COMMA = ",";
    String BRACKETS = "()";
    String BRACES = "{}";
    String SQUARE_BRACES = "[]";
    String UNDERSCORE = "_";
    String SPACE = " ";
    String PLUS = "+";
    String OPENING_BRACKET = "(";
    String CLOSING_BRACKET = ")";
    String OPENING_BRACES = "{";
    String CLOSING_BRACES = "}";
    String OPENING_SQUARE_BRACES = "[";
    String CLOSING_SQUARE_BRACES = "]";
    String AMPERSAND = "&";
    String SCHEME_DELIMITER = "://";
    String DASH = "-";
    String NEW_LINE = "\n";
    String LINE_DELIMITER = "\r\n";
    String ESCAPED_DOT = "\\.";
    String IP_ADDRESS_REGEX = "^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";
    Pattern IP_4_REGEX_PATTERN = compile(IP_ADDRESS_REGEX);
    String TABULATION = "\t";
    String DOUBLE_TABULATION = "\t\t";
    String DOLLAR = "$";
    String AT_SIGN = "@";
    String WINDOWS_DISK_PATH_SLASH = ":/";
    String WINDOWS_DISK_PATH_BACKWARD_SLASH = ":\\";
    String WINDOWS_DISK_PATH_BACKWARD_SLASH_REGEX = ":\\\\";
    String WSL_DISK_PREFIX = "/mnt/";
    String PIPE = "|";
    String ANSI_RESET = "\u001B[0m";
    String EXCLAMATION_MARK = "!";
    String LESS = "<";
    String MORE = ">";
    String JAVA_SOURCE_FILE_EXTENSION = ".java";
    String JAVA_CLASS_FILE_EXTENSION = ".class";
    String FORMAT_REGEX = "\\{}";
    Pattern FORMAT_REGEX_PATTERN = compile(FORMAT_REGEX);
    String NEXT_ARROW = "->";
}
