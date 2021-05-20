package io.art.core.source;

import io.art.core.annotation.*;
import io.art.core.collection.*;
import io.art.core.extensions.*;
import io.art.core.parser.*;
import static io.art.core.checker.EmptinessChecker.*;
import static io.art.core.checker.NullityChecker.*;
import static io.art.core.collection.ImmutableMap.*;
import static java.util.function.Function.*;
import java.time.*;
import java.util.*;
import java.util.function.*;

@UsedByGenerator
public interface NestedConfiguration extends ConfigurationSource {
    Boolean asBool();

    String asString();

    default Integer asInt() {
        return letIfNotEmpty(asString(), Integer::parseInt);
    }

    default Long asLong() {
        return letIfNotEmpty(asString(), Long::parseLong);
    }

    default Double asDouble() {
        return letIfNotEmpty(asString(), Double::parseDouble);
    }

    default Float asFloat() {
        return letIfNotEmpty(asString(), Float::parseFloat);
    }

    default Short asShort() {
        return letIfNotEmpty(asString(), Short::parseShort);
    }

    default Character asChar() {
        String string = asString();
        return letIfNotEmpty(string, notEmpty -> notEmpty.charAt(0));
    }

    default Byte asByte() {
        return letIfNotEmpty(asString(), Byte::parseByte);
    }

    default Duration asDuration() {
        return letIfNotEmpty(asString(), DurationParser::parseDuration);
    }

    default UUID asUuid() {
        return letIfNotEmpty(asString(), UUID::fromString);
    }

    default LocalDateTime asLocalDateTime() {
        return letIfNotEmpty(asString(), LocalDateTime::parse);
    }

    default ZonedDateTime asZonedDateTime() {
        return letIfNotEmpty(asString(), ZonedDateTime::parse);
    }

    default Date asDate() {
        return letIfNotEmpty(asZonedDateTime(), DateTimeExtensions::toSimpleDate);
    }


    ImmutableArray<NestedConfiguration> asArray();

    <T> ImmutableArray<T> asArray(Function<NestedConfiguration, T> mapper);

    default ImmutableArray<Boolean> asBoolArray() {
        return asArray(NestedConfiguration::asBool);
    }

    default ImmutableArray<String> asStringArray() {
        return asArray(NestedConfiguration::asString);
    }

    default ImmutableArray<Integer> asIntArray() {
        return asArray(NestedConfiguration::asInt);
    }

    default ImmutableArray<Long> asLongArray() {
        return asArray(NestedConfiguration::asLong);
    }

    default ImmutableArray<Float> asFloatArray() {
        return asArray(NestedConfiguration::asFloat);
    }

    default ImmutableArray<Double> asDoubleArray() {
        return asArray(NestedConfiguration::asDouble);
    }

    default ImmutableArray<Short> asShortArray() {
        return asArray(NestedConfiguration::asShort);
    }

    default ImmutableArray<Character> asCharArray() {
        return asArray(NestedConfiguration::asChar);
    }

    default ImmutableArray<Byte> asByteArray() {
        return asArray(NestedConfiguration::asByte);
    }

    default ImmutableArray<Duration> asDurationArray() {
        return asArray(NestedConfiguration::asDuration);
    }

    default ImmutableArray<UUID> asUuidArray() {
        return asArray(NestedConfiguration::asUuid);
    }

    default ImmutableArray<LocalDateTime> asLocalDateTimeArray() {
        return asArray(NestedConfiguration::asLocalDateTime);
    }

    default ImmutableArray<ZonedDateTime> asZonedDateTimeArray() {
        return asArray(NestedConfiguration::asZonedDateTime);
    }

    default ImmutableArray<Date> asDateArray() {
        return asArray(NestedConfiguration::asDate);
    }


    default ImmutableMap<String, NestedConfiguration> asMap() {
        return getKeys()
                .stream()
                .collect(immutableMapCollector(identity(), this::getNested));
    }

    default <T> ImmutableMap<String, T> asMap(Function<NestedConfiguration, T> mapper) {
        return getKeys()
                .stream()
                .collect(immutableMapCollector(identity(), key -> let(getNested(key), mapper)));
    }
}
