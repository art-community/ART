package ru.art.kafka.broker.api.converter;

import scala.collection.JavaConverters;
import scala.collection.Seq;
import scala.collection.immutable.HashSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Interface provides methods to convert Scala types and collections to Java types and collections.
 */
public interface ScalaToJavaConverter {
    /**
     * Method converts scala Seq into java List. If input seq is empty or null returns empty ArrayList.
     * @param scalaSequence - scala Seq needed to convert;
     * @param <T> - generic type;
     * @return - java List.
     */
    static <T> List<T> seqToList(Seq<T> scalaSequence) {
        return scalaSequence == null || scalaSequence.isEmpty() ?
                new ArrayList<>() :
                JavaConverters.seqAsJavaList(scalaSequence);
    }

    /**
     * Method converts java Set into scala Immutable Set. If input set is empty or null returns empty HashSet.
     * @param javaSet - java Set needed to convert;
     * @param <T> - generic type;
     * @return - scala Immutable Set;
     */
    static <T> scala.collection.immutable.Set<T> JavaSetToScalaImmutableSet(Set<T> javaSet) {
        return javaSet == null || javaSet.isEmpty() ? new HashSet<T>() : JavaConverters.asScalaSet(javaSet).toSet();
    }
}
