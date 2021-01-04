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

package ru.art.core.factory;


import lombok.*;
import lombok.experimental.*;
import ru.art.core.model.*;
import static java.util.Arrays.*;
import static java.util.Collections.*;
import static java.util.stream.Collectors.*;
import static ru.art.core.caster.Caster.*;
import static ru.art.core.checker.CheckerForEmptiness.*;
import java.util.ArrayList;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.*;

@UtilityClass
public class CollectionsFactory {
    public static <K, V> MapBuilder<K, V> mapOf(K k, V v) {
        return new MapBuilder<K, V>().add(k, v);
    }

    public static <K, V> MapBuilder<K, V> mapOf() {
        return new MapBuilder<>();
    }

    public static <K, V> MapBuilder<K, V> mapOf(Map<K, V> map) {
        if (isEmpty(map)) return mapOf();
        return new MapBuilder<>(map);
    }

    public static <K, V> MapBuilder<K, V> mapOf(int size) {
        return new MapBuilder<>(size);
    }

    public static <K, V> ConcurrentHashMap<K, V> concurrentHashMapOf(K k, V v) {
        return concurrentHashMap(new MapBuilder<K, V>().add(k, v));
    }

    public static <K, V> ConcurrentHashMap<K, V> concurrentHashMap() {
        return new ConcurrentHashMap<>();
    }

    public static <K, V> ConcurrentHashMap<K, V> concurrentHashMap(Map<K, V> map) {
        if (isEmpty(map)) return concurrentHashMap();
        return new ConcurrentHashMap<>(map);
    }


    public static <T> List<T> fixedArrayOf(Collection<T> elements) {
        return isEmpty(elements) ? emptyList() : new ArrayList<>(elements);
    }

    @SafeVarargs
    public static <T> List<T> fixedArrayOf(T... elements) {
        return isEmpty(elements) ? emptyList() : asList(elements);
    }

    public static <T> List<T> fixedArrayOf(Stream<T> stream) {
        return isEmpty(stream) ? emptyList() : asList(cast(stream.toArray()));
    }


    public static List<Long> arrayOf(long[] elements) {
        if (isEmpty(elements)) {
            return emptyList();
        }
        List<Long> array = arrayOf(elements.length);
        for (long element : elements) {
            array.add(element);
        }
        return array;
    }

    public static List<Integer> arrayOf(int[] elements) {
        if (isEmpty(elements)) {
            return emptyList();
        }
        List<Integer> array = arrayOf(elements.length);
        for (int element : elements) {
            array.add(element);
        }
        return array;
    }

    public static List<Byte> arrayOf(byte[] elements) {
        if (isEmpty(elements)) {
            return emptyList();
        }
        List<Byte> array = arrayOf(elements.length);
        for (byte element : elements) {
            array.add(element);
        }
        return array;
    }

    public static List<Double> arrayOf(double[] elements) {
        if (isEmpty(elements)) {
            return emptyList();
        }
        List<Double> array = arrayOf(elements.length);
        for (double element : elements) {
            array.add(element);
        }
        return array;
    }

    public static List<Float> arrayOf(float[] elements) {
        if (isEmpty(elements)) {
            return emptyList();
        }
        List<Float> array = arrayOf(elements.length);
        for (float element : elements) {
            array.add(element);
        }
        return array;
    }

    public static List<Boolean> arrayOf(boolean[] elements) {
        if (isEmpty(elements)) {
            return emptyList();
        }
        List<Boolean> array = arrayOf(elements.length);
        for (boolean element : elements) {
            array.add(element);
        }
        return array;
    }

    public static <T> List<T> arrayOf(int size) {
        return size <= 0 ? new ArrayList<>() : new ArrayList<>(size);
    }


    @SafeVarargs
    public static <T> List<T> dynamicArrayOf(T... elements) {
        return isEmpty(elements) ? new ArrayList<>() : new ArrayList<>(asList(elements));
    }

    public static <T> List<T> dynamicArrayOf(Stream<T> stream) {
        return isEmpty(stream) ? emptyList() : stream.collect(toList());
    }

    public static <T> List<T> dynamicArrayOf(Collection<T> elements) {
        return isEmpty(elements) ? new ArrayList<>() : new ArrayList<>(elements);
    }

    public static List<Long> dynamicArrayOf(long[] elements) {
        return arrayOf(elements);
    }

    public static List<Integer> dynamicArrayOf(int[] elements) {
        return arrayOf(elements);
    }

    public static List<Byte> dynamicArrayOf(byte[] elements) {
        return arrayOf(elements);
    }

    public static List<Double> dynamicArrayOf(double[] elements) {
        return arrayOf(elements);
    }

    public static List<Float> dynamicArrayOf(float[] elements) {
        return arrayOf(elements);
    }

    public static List<Boolean> dynamicArrayOf(boolean[] elements) {
        return arrayOf(elements);
    }

    @SafeVarargs
    public static <T> List<T> linkedListOf(T... elements) {
        return isEmpty(elements) ? new LinkedList<>() : new LinkedList<>(asList(elements));
    }

    public static <T> List<T> linkedListOf(Collection<T> elements) {
        return isEmpty(elements) ? new LinkedList<>() : new LinkedList<>(elements);
    }

    public static List<Long> linkedListOf(long[] elements) {
        if (isEmpty(elements)) {
            return linkedListOf();
        }
        List<Long> array = linkedListOf();
        for (long element : elements) {
            array.add(element);
        }
        return array;
    }

    public static List<Integer> linkedListOf(int[] elements) {
        if (isEmpty(elements)) {
            return linkedListOf();
        }
        List<Integer> array = linkedListOf();
        for (int element : elements) {
            array.add(element);
        }
        return array;
    }

    public static List<Byte> linkedListOf(byte[] elements) {
        if (isEmpty(elements)) {
            return linkedListOf();
        }
        List<Byte> array = linkedListOf();
        for (byte element : elements) {
            array.add(element);
        }
        return array;
    }

    public static List<Double> linkedListOf(double[] elements) {
        if (isEmpty(elements)) {
            return linkedListOf();
        }
        List<Double> array = linkedListOf();
        for (double element : elements) {
            array.add(element);
        }
        return array;
    }

    public static List<Float> linkedListOf(float[] elements) {
        if (isEmpty(elements)) {
            return linkedListOf();
        }
        List<Float> array = linkedListOf();
        for (float element : elements) {
            array.add(element);
        }
        return array;
    }

    public static List<Boolean> linkedListOf(boolean[] elements) {
        if (isEmpty(elements)) {
            return linkedListOf();
        }
        List<Boolean> array = linkedListOf();
        for (boolean element : elements) {
            array.add(element);
        }
        return array;
    }


    public static <T> Deque<T> dequeOf(Collection<T> elements) {
        return isEmpty(elements) ? new LinkedList<>() : new LinkedList<>(elements);
    }

    @SafeVarargs
    public static <T> Deque<T> dequeOf(T... elements) {
        return isEmpty(elements) ? new LinkedList<>() : new LinkedList<>(asList(elements));
    }

    public static Deque<Long> dequeOf(long[] elements) {
        if (isEmpty(elements)) {
            return dequeOf();
        }
        Deque<Long> deque = dequeOf();
        for (long element : elements) {
            deque.add(element);
        }
        return deque;
    }

    public static Deque<Integer> dequeOf(int[] elements) {
        if (isEmpty(elements)) {
            return dequeOf();
        }
        Deque<Integer> deque = dequeOf();
        for (int element : elements) {
            deque.add(element);
        }
        return deque;
    }

    public static Deque<Byte> dequeOf(byte[] elements) {
        if (isEmpty(elements)) {
            return dequeOf();
        }
        Deque<Byte> deque = dequeOf();
        for (byte element : elements) {
            deque.add(element);
        }
        return deque;
    }

    public static Deque<Double> dequeOf(double[] elements) {
        if (isEmpty(elements)) {
            return dequeOf();
        }
        Deque<Double> deque = dequeOf();
        for (double element : elements) {
            deque.add(element);
        }
        return deque;
    }

    public static Deque<Float> dequeOf(float[] elements) {
        if (isEmpty(elements)) {
            return dequeOf();
        }
        Deque<Float> deque = dequeOf();
        for (float element : elements) {
            deque.add(element);
        }
        return deque;
    }

    public static Deque<Boolean> dequeOf(boolean[] elements) {
        if (isEmpty(elements)) {
            return dequeOf();
        }
        Deque<Boolean> deque = dequeOf();
        for (boolean element : elements) {
            deque.add(element);
        }
        return deque;
    }


    @SafeVarargs
    public static <T> Queue<T> queueOf(T... elements) {
        return isEmpty(elements) ? new LinkedList<>() : new LinkedList<>(asList(elements));
    }

    public static Queue<Long> queueOf(long[] elements) {
        if (isEmpty(elements)) {
            return queueOf();
        }
        Queue<Long> queue = queueOf();
        for (long element : elements) {
            queue.add(element);
        }
        return queue;
    }

    public static Queue<Integer> queueOf(int[] elements) {
        if (isEmpty(elements)) {
            return queueOf();
        }
        Queue<Integer> queue = queueOf();
        for (int element : elements) {
            queue.add(element);
        }
        return queue;
    }

    public static Queue<Byte> queueOf(byte[] elements) {
        if (isEmpty(elements)) {
            return queueOf();
        }
        Queue<Byte> queue = queueOf();
        for (byte element : elements) {
            queue.add(element);
        }
        return queue;
    }

    public static Queue<Double> queueOf(double[] elements) {
        if (isEmpty(elements)) {
            return queueOf();
        }
        Queue<Double> queue = queueOf();
        for (double element : elements) {
            queue.add(element);
        }
        return queue;
    }

    public static Queue<Float> queueOf(float[] elements) {
        if (isEmpty(elements)) {
            return queueOf();
        }
        Queue<Float> queue = queueOf();
        for (float element : elements) {
            queue.add(element);
        }
        return queue;
    }

    public static Queue<Boolean> queueOf(boolean[] elements) {
        if (isEmpty(elements)) {
            return queueOf();
        }
        Queue<Boolean> deque = queueOf();
        for (boolean element : elements) {
            deque.add(element);
        }
        return deque;
    }


    @SafeVarargs
    public static <T> PriorityQueue<T> priorityQueueOf(Comparator<T> comparator, T... elements) {
        PriorityQueue<T> queue = new PriorityQueue<>(comparator);
        queue.addAll(asList(elements));
        return isEmpty(elements) ? new PriorityQueue<>(comparator) : queue;
    }

    public static <T> PriorityQueue<T> priorityQueueOf(Comparator<T> comparator, Collection<T> elements) {
        PriorityQueue<T> queue = new PriorityQueue<>(comparator);
        queue.addAll(elements);
        return isEmpty(elements) ? new PriorityQueue<>(comparator) : queue;
    }

    public static PriorityQueue<Long> priorityQueueOf(Comparator<Long> comparator, long[] elements) {
        PriorityQueue<Long> queue = new PriorityQueue<>(comparator);
        for (long element : elements) {
            queue.add(element);
        }
        return isEmpty(elements) ? new PriorityQueue<>(comparator) : queue;
    }

    public static PriorityQueue<Integer> priorityQueueOf(Comparator<Integer> comparator, int[] elements) {
        PriorityQueue<Integer> queue = new PriorityQueue<>(comparator);
        for (int element : elements) {
            queue.add(element);
        }
        return isEmpty(elements) ? new PriorityQueue<>(comparator) : queue;

    }

    public static PriorityQueue<Byte> priorityQueueOf(Comparator<Byte> comparator, byte[] elements) {
        PriorityQueue<Byte> queue = new PriorityQueue<>(comparator);
        for (byte element : elements) {
            queue.add(element);
        }
        return isEmpty(elements) ? new PriorityQueue<>(comparator) : queue;

    }

    public static PriorityQueue<Double> priorityQueueOf(Comparator<Double> comparator, double[] elements) {
        PriorityQueue<Double> queue = new PriorityQueue<>(comparator);
        for (double element : elements) {
            queue.add(element);
        }
        return isEmpty(elements) ? new PriorityQueue<>(comparator) : queue;

    }

    public static PriorityQueue<Float> priorityQueueOf(Comparator<Float> comparator, float[] elements) {
        PriorityQueue<Float> queue = new PriorityQueue<>(comparator);
        for (float element : elements) {
            queue.add(element);
        }
        return isEmpty(elements) ? new PriorityQueue<>(comparator) : queue;

    }

    public static PriorityQueue<Boolean> priorityQueueOf(Comparator<Boolean> comparator, boolean[] elements) {
        PriorityQueue<Boolean> queue = new PriorityQueue<>(comparator);
        for (boolean element : elements) {
            queue.add(element);
        }
        return isEmpty(elements) ? new PriorityQueue<>(comparator) : queue;

    }

    @SafeVarargs
    public static <T> Stack<T> stackOf(T... elements) {
        if (isEmpty(elements)) return new Stack<>();
        Stack<T> stack = new Stack<>();
        stack.addAll(asList(elements));
        return stack;
    }

    @SafeVarargs
    public static <T> Set<T> setOf(T... elements) {
        return isEmpty(elements) ? new LinkedHashSet<>() : new LinkedHashSet<>(asList(elements));
    }

    public static <T> Set<T> setOf(Collection<T> elements) {
        return isEmpty(elements) ? new LinkedHashSet<>() : new LinkedHashSet<>(elements);
    }

    public static Set<Long> setOf(long[] elements) {
        if (isEmpty(elements)) {
            return setOf();
        }
        Set<Long> set = setOf();
        for (long element : elements) {
            set.add(element);
        }
        return set;
    }

    public static Set<Integer> setOf(int[] elements) {
        if (isEmpty(elements)) {
            return setOf();
        }
        Set<Integer> set = setOf();
        for (int element : elements) {
            set.add(element);
        }
        return set;
    }

    public static Set<Byte> setOf(byte[] elements) {
        if (isEmpty(elements)) {
            return setOf();
        }
        Set<Byte> set = setOf();
        for (byte element : elements) {
            set.add(element);
        }
        return set;
    }

    public static Set<Double> setOf(double[] elements) {
        if (isEmpty(elements)) {
            return setOf();
        }
        Set<Double> set = setOf();
        for (double element : elements) {
            set.add(element);
        }
        return set;
    }

    public static Set<Float> setOf(float[] elements) {
        if (isEmpty(elements)) {
            return setOf();
        }
        Set<Float> set = setOf();
        for (float element : elements) {
            set.add(element);
        }
        return set;
    }

    public static Set<Boolean> setOf(boolean[] elements) {
        if (isEmpty(elements)) {
            return setOf();
        }
        Set<Boolean> set = setOf();
        for (boolean element : elements) {
            set.add(element);
        }
        return set;
    }

    @SafeVarargs
    public static <T> Set<T> treeOf(Comparator<T> comparator, T... elements) {
        TreeSet<T> treeSet = new TreeSet<>(comparator);
        if (isEmpty(elements)) return treeSet;
        treeSet.addAll(asList(elements));
        return treeSet;
    }

    @SafeVarargs
    public static <T extends Comparable<T>> Set<T> treeOf(T... elements) {
        TreeSet<T> treeSet = new TreeSet<>();
        if (isEmpty(elements)) return treeSet;
        treeSet.addAll(asList(elements));
        return treeSet;
    }

    public static <T> Set<T> treeOf(Collection<T> elements, Comparator<T> comparator) {
        TreeSet<T> treeSet = new TreeSet<>(comparator);
        if (isEmpty(elements)) return treeSet;
        treeSet.addAll(elements);
        return treeSet;
    }

    public static Set<Long> treeOf(Comparator<Long> comparator, long[] elements) {
        TreeSet<Long> treeSet = new TreeSet<>();
        if (isEmpty(elements)) return treeSet;
        for (long element : elements) {
            treeSet.add(element);
        }
        return treeSet;
    }

    public static Set<Integer> treeOf(Comparator<Integer> comparator, int[] elements) {
        TreeSet<Integer> treeSet = new TreeSet<>();
        if (isEmpty(elements)) return treeSet;
        for (int element : elements) {
            treeSet.add(element);
        }
        return treeSet;
    }

    public static Set<Byte> treeOf(Comparator<Byte> comparator, byte[] elements) {
        TreeSet<Byte> treeSet = new TreeSet<>();
        if (isEmpty(elements)) return treeSet;
        for (byte element : elements) {
            treeSet.add(element);
        }
        return treeSet;
    }

    public static Set<Double> treeOf(Comparator<Double> comparator, double[] elements) {
        TreeSet<Double> treeSet = new TreeSet<>();
        if (isEmpty(elements)) return treeSet;
        for (double element : elements) {
            treeSet.add(element);
        }
        return treeSet;
    }

    public static Set<Float> treeOf(Comparator<Float> comparator, float[] elements) {
        TreeSet<Float> treeSet = new TreeSet<>();
        if (isEmpty(elements)) return treeSet;
        for (float element : elements) {
            treeSet.add(element);
        }
        return treeSet;
    }

    public static Set<Boolean> treeOf(Comparator<Boolean> comparator, boolean[] elements) {
        TreeSet<Boolean> treeSet = new TreeSet<>();
        if (isEmpty(elements)) return treeSet;
        for (boolean element : elements) {
            treeSet.add(element);
        }
        return treeSet;
    }


    public static <K, V> Pair<K, V> pairOf(K k, V v) {
        return new Pair<>(k, v);
    }

    @NoArgsConstructor
    public static class MapBuilder<K, V> extends LinkedHashMap<K, V> implements Map<K, V> {
        MapBuilder(Map<K, V> map) {
            super(map);
        }

        MapBuilder(int size) {
            super(size);
        }

        public MapBuilder<K, V> add(K key, V value) {
            put(key, value);
            return this;
        }

        public Map<K, V> toMap(K key, V value) {
            put(key, value);
            return this;
        }

        public Map<K, V> toMap() {
            return this;
        }
    }
}
