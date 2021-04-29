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

package io.art.core.source;

import io.art.core.annotation.*;
import io.art.core.collection.*;
import io.art.core.extensions.*;
import io.art.core.parser.*;
import static io.art.core.checker.EmptinessChecker.*;
import static io.art.core.checker.NullityChecker.*;
import static io.art.core.constants.StringConstants.*;
import static io.art.core.extensions.CollectionExtensions.*;
import static io.art.core.extensions.StringExtensions.*;
import static java.util.Objects.*;
import java.time.*;
import java.util.*;
import java.util.function.*;

@UsedByGenerator
public interface ConfigurationSource {
    String getSection();

    default String getParent() {
        String section = emptyIfNull(getSection());
        return section.substring(section.lastIndexOf(DOT) + 1);
    }

    ModuleConfigurationSourceType getType();

    ImmutableSet<String> getKeys();

    NestedConfiguration getNested(String path);

    default boolean has(String path) {
        return nonNull(getNested(path));
    }

    default void refresh() {

    }

    default Boolean getBool(String path) {
        return let(getNested(path), NestedConfiguration::asBool);
    }

    default String getString(String path) {
        return let(getNested(path), NestedConfiguration::asString);
    }

    default Integer getInt(String path) {
        return letIfNotEmpty(getString(path), Integer::parseInt);
    }

    default Long getLong(String path) {
        return letIfNotEmpty(getString(path), Long::parseLong);
    }

    default Double getDouble(String path) {
        return letIfNotEmpty(getString(path), Double::parseDouble);
    }

    default Float getFloat(String path) {
        return letIfNotEmpty(getString(path), Float::parseFloat);
    }

    default Short getShort(String path) {
        return letIfNotEmpty(getString(path), Short::parseShort);
    }

    default Character getChar(String path) {
        String string = getString(path);
        return letIfNotEmpty(string, notEmpty -> notEmpty.charAt(0));
    }

    default Byte getByte(String path) {
        return letIfNotEmpty(getString(path), Byte::parseByte);
    }

    default Duration getDuration(String path) {
        return letIfNotEmpty(getString(path), DurationParser::parseDuration);
    }

    default UUID getUuid(String path) {
        return letIfNotEmpty(getString(path), UUID::fromString);
    }

    default LocalDateTime getLocalDateTime(String path) {
        return letIfNotEmpty(getString(path), LocalDateTime::parse);
    }

    default ZonedDateTime getZonedDateTime(String path) {
        return letIfNotEmpty(getString(path), ZonedDateTime::parse);
    }

    default Date getDate(String path) {
        return letIfNotEmpty(getZonedDateTime(path), DateTimeExtensions::toSimpleDate);
    }


    default ImmutableArray<Boolean> getBoolArray(String path) {
        return orEmptyImmutableArray(getNested(path), NestedConfiguration::asBoolArray);
    }

    default ImmutableArray<String> getStringArray(String path) {
        return orEmptyImmutableArray(getNested(path), NestedConfiguration::asStringArray);
    }

    default ImmutableArray<Integer> getIntArray(String path) {
        return orEmptyImmutableArray(getNested(path), NestedConfiguration::asIntArray);
    }

    default ImmutableArray<Long> getLongArray(String path) {
        return orEmptyImmutableArray(getNested(path), NestedConfiguration::asLongArray);
    }

    default ImmutableArray<Float> getFloatArray(String path) {
        return orEmptyImmutableArray(getNested(path), NestedConfiguration::asFloatArray);
    }

    default ImmutableArray<Double> getDoubleArray(String path) {
        return orEmptyImmutableArray(getNested(path), NestedConfiguration::asDoubleArray);
    }

    default ImmutableArray<Short> getShortArray(String path) {
        return orEmptyImmutableArray(getNested(path), NestedConfiguration::asShortArray);
    }

    default ImmutableArray<Character> getCharArray(String path) {
        return orEmptyImmutableArray(getNested(path), NestedConfiguration::asCharArray);
    }

    default ImmutableArray<Byte> getByteArray(String path) {
        return orEmptyImmutableArray(getNested(path), NestedConfiguration::asByteArray);
    }

    default ImmutableArray<Duration> getDurationArray(String path) {
        return orEmptyImmutableArray(getNested(path), NestedConfiguration::asDurationArray);
    }

    default ImmutableArray<UUID> getUuidArray(String path) {
        return orEmptyImmutableArray(getNested(path), NestedConfiguration::asUuidArray);
    }

    default ImmutableArray<LocalDateTime> getLocalDateTimeArray(String path) {
        return orEmptyImmutableArray(getNested(path), NestedConfiguration::asLocalDateTimeArray);
    }

    default ImmutableArray<ZonedDateTime> getZonedDateTimeArray(String path) {
        return orEmptyImmutableArray(getNested(path), NestedConfiguration::asZonedDateTimeArray);
    }

    default ImmutableArray<Date> getDateArray(String path) {
        return orEmptyImmutableArray(getNested(path), NestedConfiguration::asDateArray);
    }


    default <T> T getNested(String path, Function<NestedConfiguration, T> mapper) {
        return let(getNested(path), mapper);
    }

    default <T> ImmutableArray<T> getNestedArray(String path, Function<NestedConfiguration, T> mapper) {
        return orEmptyImmutableArray(getNested(path), configuration -> configuration.asArray(mapper));
    }

    default <T> ImmutableMap<String, T> getNestedMap(String path, Function<NestedConfiguration, T> mapper) {
        return orEmptyImmutableMap(getNested(path), configuration -> configuration.asMap(mapper));
    }

    String dump();

    interface ModuleConfigurationSourceType {
        int getOrder();
    }
}
