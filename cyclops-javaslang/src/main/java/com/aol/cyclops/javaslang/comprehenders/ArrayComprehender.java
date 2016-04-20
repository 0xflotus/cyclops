package com.aol.cyclops.javaslang.comprehenders;

import java.util.Collection;
import java.util.Iterator;
import java.util.function.Function;
import java.util.stream.BaseStream;

import com.aol.cyclops.types.extensability.Comprehender;

import javaslang.collection.Array;

public class ArrayComprehender implements Comprehender<Array> {

	@Override
	public Object map(Array t, Function fn) {
		return t.map(s -> fn.apply(s));
	}
	@Override
	public Object executeflatMap(Array t, Function fn){
		return flatMap(t,input -> unwrapOtherMonadTypes(this,fn.apply(input)));
	}
	@Override
	public Object flatMap(Array t, Function fn) {
		return t.flatMap(s->fn.apply(s));
	}

	@Override
	public Array of(Object o) {
		return Array.of(o);
	}

	@Override
	public Array empty() {
		return Array.empty();
	}

	@Override
	public Class getTargetClass() {
		return Array.class;
	}
	static Array unwrapOtherMonadTypes(Comprehender<Array> comp,Object apply){
		if (comp.instanceOfT(apply))
			return (Array) apply;
		if(apply instanceof java.util.stream.Stream)
			return Array.of( ((java.util.stream.Stream)apply));
		if(apply instanceof Iterable)
			return Array.of( ((Iterable)apply));
		
		if(apply instanceof Collection){
			return Array.ofAll((Collection)apply);
		}
		final Object finalApply = apply;
		if(apply instanceof BaseStream){
			return Array.ofAll( () -> ((BaseStream)finalApply).iterator());
					
		}
		return Comprehender.unwrapOtherMonadTypes(comp,apply);
		
	}
	@Override
	public Object resolveForCrossTypeFlatMap(Comprehender comp, Array apply) {
		return comp.fromIterator(apply.iterator());
	}
	@Override
	public Array fromIterator(Iterator o) {
		return  Array.ofAll(()->o);
	}
}
