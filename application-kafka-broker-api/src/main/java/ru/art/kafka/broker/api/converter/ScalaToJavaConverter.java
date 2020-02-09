package ru.art.kafka.broker.api.converter;

import scala.collection.JavaConverters;
import scala.collection.Seq;

import java.util.ArrayList;
import java.util.List;

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
}
