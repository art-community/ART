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
     * TODO: see does it works with T for scala...it's different T..Tomorrow.
     * @param scalaSequence - scala Seq;
     * @param <T> - 
     * @return - java List.
     */
    static <T> List<T> seqToList(Seq<T> scalaSequence){
        return scalaSequence == null || scalaSequence.isEmpty() ?
                new ArrayList<>() :
                JavaConverters.seqAsJavaList(scalaSequence);
    }
}
