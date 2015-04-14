package com.aol.cyclops.javaslang;

import com.aol.simple.react.stream.lazy.LazyFutureStream;
import com.google.common.collect.FluentIterable;
import com.nurkiewicz.lazyseq.LazySeq;
import org.jooq.lambda.Seq;

import java.util.stream.Stream;

/**
 * Created by johnmcclean on 4/8/15.
 */
public class ToStream {

    public static <T> FluentIterable<T> toFluentIterable(javaslang.collection.Stream<T> s){
        return FluentIterable.from(s);
    }
    public static <T> Stream<T> toStream(javaslang.collection.Stream<T> s){
        return Seq.seq(s.iterator());
    }
    public static <T> Seq<T> toJooλ(javaslang.collection.Stream<T> s){
        return Seq.seq(s.iterator());
    }
    public static <T> LazyFutureStream<T> toFutureStream(javaslang.collection.Stream<T> s){
        return LazyFutureStream.futureStream(s.iterator());
    }
/**
    public static <T> javaslang.collection.Stream<T> toJavasLang(javaslang.collection.Stream<T> s) {
        return javaslang.collection.Stream.of(s.iterator());
    }**/
    public static <T> LazySeq<T> toLazySeq(javaslang.collection.Stream<T> s){
        return LazySeq.of(s.iterator());
    }

    public static <T> fj.data.Stream<T> toFunctionalJavaStream(javaslang.collection.Stream<T> s){
        return fj.data.Stream.iterableStream(s);
    }


}
