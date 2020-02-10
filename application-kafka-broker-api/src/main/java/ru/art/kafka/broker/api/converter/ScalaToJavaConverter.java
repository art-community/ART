package ru.art.kafka.broker.api.converter;

import scala.collection.JavaConverters;
import scala.collection.Seq;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Interface provides methods to convert Scala types and collections to Java types and collections.
 */
public interface ScalaToJavaConverter {
    /**
     * Method converts scala seq to java list.
     * @param scalaSequence - scala Seq;
     * @param <T> - type;
     * @return - java List.
     */
    static <T> List<T> seqToList(Seq<T> scalaSequence) {
        return scalaSequence == null || scalaSequence.isEmpty() ?
                new ArrayList<>() :
                JavaConverters.seqAsJavaList(scalaSequence);
    }

    /**
     *
     * @param javaSet
     * @param <T>
     * @return
     */
    static <T> scala.collection.immutable.Set<T> JavaSetToScalaSet(Set<T> javaSet) {
        return javaSet == null || javaSet.isEmpty() ? null : JavaConverters.asScalaSet(javaSet).toSet();

    }
}
