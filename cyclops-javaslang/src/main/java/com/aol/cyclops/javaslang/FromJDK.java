package com.aol.cyclops.javaslang;

import java.util.Iterator;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.aol.cyclops.react.async.subscription.Continueable;
import com.aol.cyclops.util.ExceptionSoftener;

import javaslang.Function1;
import javaslang.Function2;
import javaslang.collection.Stream;
import javaslang.control.Option;

public class FromJDK<T,R> {
	
	public static <T,R>  Function1<T,R> f1(Function<T,R> fn){
		return (t) -> fn.apply(t);
	}
	public static <T,X,R>  Function2<T,X,R> f2(BiFunction<T,X,R> fn){
		return (t,x) -> fn.apply(t,x);
	}
	public static<T> Option<T> option(java.util.Optional<T> o){
		return Option.of(o.orElse(null));
	}
	public static<T> Stream<T> stream(java.util.stream.Stream<T> stream){
		return Stream.ofAll(()->stream.iterator());
	}
	
	
	
}
