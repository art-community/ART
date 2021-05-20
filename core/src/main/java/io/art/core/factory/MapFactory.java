package io.art.core.factory;

import io.art.core.collection.*;
import lombok.experimental.*;
import static io.art.core.checker.EmptinessChecker.*;
import static io.art.core.collection.ImmutableMap.*;
import java.util.*;
import java.util.concurrent.*;

@UtilityClass
public class MapFactory {
    public static <K, V> Map<K, V> map() {
        return new LinkedHashMap<>();
    }

    public static <K, V> Map<K, V> map(int capacity) {
        return new LinkedHashMap<>(capacity);
    }

    public static <K, V> Map<K, V> mapOf(K key, V value) {
        LinkedHashMap<K, V> map = new LinkedHashMap<>();
        map.put(key, value);
        return map;
    }

    public static <K, V> Map<K, V> mapOf(Map<K, V> map) {
        if (isEmpty(map)) return new LinkedHashMap<>();
        return new LinkedHashMap<>(map);
    }

    public static <K, V> Map<K, V> mapOf(ImmutableMap<K, V> map) {
        return mapOf(map.toMutable());
    }

    public static <K, V> Map<K, V> mapOf(int size) {
        return new LinkedHashMap<>(size);
    }


    public static <K, V> ImmutableMap<K, V> immutableMapOf(Map<K, V> map) {
        return new ImmutableMapImplementation<>(map);
    }

    public static ImmutableMap<String, String> immutableMapOf(Properties properties) {
        return properties.entrySet().stream().collect(immutableMapCollector(entry -> entry.getKey().toString(), entry -> entry.getValue().toString()));
    }

    public static <K, V> ImmutableMap<K, V> immutableMapOf(ImmutableMap<K, V> map) {
        return new ImmutableMapImplementation<>(map);
    }


    public static <K, V> Map<K, V> concurrentMap() {
        return new ConcurrentHashMap<>();
    }

    public static <K, V> Map<K, V> concurrentMap(int initialCapacity) {
        return new ConcurrentHashMap<>(initialCapacity);
    }

    public static <K, V> Map<K, V> concurrentMapOf(K key, V value) {
        return new ConcurrentHashMap<>(mapOf(key, value));
    }

    public static <K, V> Map<K, V> concurrentMapOf(Map<K, V> map) {
        if (isEmpty(map)) return concurrentMap();
        return new ConcurrentHashMap<>(map);
    }

    public static <K, V> Map<K, V> concurrentMapOf(ImmutableMap<K, V> map) {
        return concurrentMapOf(map.toMutable());
    }


    public static <K, V> Map<K, V> weakMap() {
        return new WeakHashMap<>();
    }

    public static <K, V> Map<K, V> weakMapOf(K key, V value) {
        return new WeakHashMap<>(mapOf(key, value));
    }

    public static <K, V> Map<K, V> weakMapOf(Map<K, V> map) {
        if (isEmpty(map)) return weakMap();
        return new WeakHashMap<>(map);
    }

    public static <K, V> Map<K, V> weakMapOf(ImmutableMap<K, V> map) {
        return weakMapOf(map.toMutable());
    }
}
