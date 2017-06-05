package com.aol.cyclops.rx.adapter;

import com.aol.cyclops2.types.anyM.AnyMSeq;
import com.aol.cyclops2.types.stream.HeadAndTail;
import com.aol.cyclops2.types.traversable.Traversable;
import cyclops.async.adapters.QueueFactory;
import cyclops.collections.immutable.VectorX;
import cyclops.collections.mutable.ListX;
import cyclops.companion.rx.Observables;
import cyclops.control.Maybe;
import cyclops.control.lazy.Either;
import cyclops.function.Monoid;
import cyclops.function.Reducer;
import cyclops.monads.AnyM;
import cyclops.monads.Witness;
import cyclops.monads.Witness.reactiveSeq;
import cyclops.stream.ReactiveSeq;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Wither;
import org.jooq.lambda.tuple.Tuple;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;
import org.jooq.lambda.tuple.Tuple4;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import rx.Observable;


import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.*;
import java.util.stream.*;


@AllArgsConstructor
public class ObservableReactiveSeq<T> implements ReactiveSeq<T> {
    @Wither
    @Getter
    Observable<T> observable;

    public <R> ObservableReactiveSeq<R> observable(Observable<R> observable){
        return new ObservableReactiveSeq<>(observable);
    }
    public <R> ObservableReactiveSeq<R> observable(ReactiveSeq<R> observable){
        if(observable instanceof ObservableReactiveSeq){
            return  (ObservableReactiveSeq)observable;
        }
        return new ObservableReactiveSeq<>(Observable.from(observable));
    }

    @Override
    public <R> ReactiveSeq<R> coflatMap(Function<? super ReactiveSeq<T>, ? extends R> fn) {
        return observable(Observable.just(fn.apply(this)));
    }

    @Override
    public <T1> ReactiveSeq<T1> unit(T1 unit) {
        return observable(Observable.just(unit));
    }

    @Override
    public <U> U foldRight(U identity, BiFunction<? super T, ? super U, ? extends U> accumulator) {
        return observable.reduce(identity,(a,b)->accumulator.apply(b,a))
                         .toBlocking()
                         .first();
    }

    @Override
    public <U, R> ReactiveSeq<R> zipS(Stream<? extends U> other, BiFunction<? super T, ? super U, ? extends R> zipper) {
        if(other instanceof Publisher){
            return zipP((Publisher<U>)other,zipper);
        }
        return observable(observable.zipWith(ReactiveSeq.fromStream((Stream<U>)other),(a,b)->zipper.apply(a,b)));
    }

    @Override
    public <U, R> ReactiveSeq<R> zipLatest(Publisher<? extends U> other, BiFunction<? super T, ? super U, ? extends R> zipper) {
        Observable<R> obs = Observable.combineLatest(observable, Observables.observable(other), (a, b) -> zipper.apply((T)a, (U)b));
        return observable(obs);
    }

    @Override
    public <U, R> ReactiveSeq<R> zipP(Publisher<? extends U> other, BiFunction<? super T, ? super U, ? extends R> zipper) {
        return observable(observable.zipWith(Observables.observable(other),(a,b)->zipper.apply(a,b)));
    }

    @Override
    public <U> ReactiveSeq<Tuple2<T, U>> zipP(Publisher<? extends U> other) {
        return observable(observable.zipWith(Observables.observable(other),Tuple::tuple));
    }

    @Override
    public ReactiveSeq<T> cycle() {
        return observable(observable.repeat());
    }

    @Override
    public Tuple2<ReactiveSeq<T>, ReactiveSeq<T>> duplicate() {
        return Observables.reactiveStreamX(observable).duplicate().map((s1, s2)->Tuple.tuple(observable(s1),observable(s2)));
    }

    @Override
    public Tuple2<ReactiveSeq<T>, ReactiveSeq<T>> duplicate(Supplier<Deque<T>> bufferFactory) {
        return Observables.reactiveStreamX(observable).duplicate(bufferFactory).map((s1, s2)->Tuple.tuple(observable(s1),observable(s2)));
    }

    @Override
    public Tuple3<ReactiveSeq<T>, ReactiveSeq<T>, ReactiveSeq<T>> triplicate() {
        return Observables.reactiveStreamX(observable).triplicate().map((s1, s2, s3)->Tuple.tuple(observable(s1),observable(s2),observable(s3)));
    }

    @Override
    public Tuple3<ReactiveSeq<T>, ReactiveSeq<T>, ReactiveSeq<T>> triplicate(Supplier<Deque<T>> bufferFactory) {
        return Observables.reactiveStreamX(observable).triplicate(bufferFactory).map((s1, s2, s3)->Tuple.tuple(observable(s1),observable(s2),observable(s3)));
    }

    @Override
    public Tuple4<ReactiveSeq<T>, ReactiveSeq<T>, ReactiveSeq<T>, ReactiveSeq<T>> quadruplicate() {
        return Observables.reactiveStreamX(observable).quadruplicate().map((s1, s2, s3, s4)->Tuple.tuple(observable(s1),observable(s2),observable(s3),observable(s4)));
    }

    @Override
    public Tuple4<ReactiveSeq<T>, ReactiveSeq<T>, ReactiveSeq<T>, ReactiveSeq<T>> quadruplicate(Supplier<Deque<T>> bufferFactory) {
        return Observables.reactiveStreamX(observable).quadruplicate(bufferFactory).map((s1, s2, s3, s4)->Tuple.tuple(observable(s1),observable(s2),observable(s3),observable(s4)));
    }

    @Override
    public Tuple2<Optional<T>, ReactiveSeq<T>> splitAtHead() {
        return Observables.reactiveStreamX(observable).splitAtHead().map((s1, s2)->Tuple.tuple(s1,observable(s2)));
    }

    @Override
    public Tuple2<ReactiveSeq<T>, ReactiveSeq<T>> splitAt(int where) {
        return Observables.reactiveStreamX(observable).splitAt(where).map((s1, s2)->Tuple.tuple(observable(s1),observable(s2)));
    }

    @Override
    public Tuple2<ReactiveSeq<T>, ReactiveSeq<T>> splitBy(Predicate<T> splitter) {
        return Observables.reactiveStreamX(observable).splitBy(splitter).map((s1, s2)->Tuple.tuple(observable(s1),observable(s2)));
    }

    @Override
    public Tuple2<ReactiveSeq<T>, ReactiveSeq<T>> partition(Predicate<? super T> splitter) {
        return Observables.reactiveStreamX(observable).partition(splitter).map((s1, s2)->Tuple.tuple(observable(s1),observable(s2)));
    }

    @Override
    public <U> ReactiveSeq<Tuple2<T, U>> zipS(Stream<? extends U> other) {
        if(other instanceof Publisher){
            return zipP((Publisher<U>)other);
        }
        return zipS(other,Tuple::tuple);
    }

    @Override
    public <S, U> ReactiveSeq<Tuple3<T, S, U>> zip3(Iterable<? extends S> second, Iterable<? extends U> third) {
        return zip(second,Tuple::tuple).zip(third,(a,b)->Tuple.tuple(a.v1,a.v2,b));
    }

    @Override
    public <T2, T3, T4> ReactiveSeq<Tuple4<T, T2, T3, T4>> zip4(Iterable<? extends T2> second, Iterable<? extends T3> third, Iterable<? extends T4> fourth) {
        return zip(second,Tuple::tuple).zip(third,(a,b)->Tuple.tuple(a.v1,a.v2,b))
                .zip(fourth,(a,b)->(Tuple4<T,T2,T3,T4>)Tuple.tuple(a.v1,a.v2,a.v3,b));
    }

    @Override
    public ReactiveSeq<VectorX<T>> sliding(int windowSize, int increment) {
        return observable(Observables.reactiveStreamX(observable).sliding(windowSize,increment));
    }

    @Override
    public ReactiveSeq<ListX<T>> grouped(int groupSize) {
        return observable(observable.buffer(groupSize).map(ListX::fromIterable));
    }

    @Override
    public ReactiveSeq<ListX<T>> groupedStatefullyUntil(BiPredicate<ListX<? super T>, ? super T> predicate) {
        return observable(Observables.reactiveStreamX(observable).groupedStatefullyUntil(predicate));
    }

    @Override
    public <C extends Collection<T>, R> ReactiveSeq<R> groupedStatefullyUntil(BiPredicate<C, ? super T> predicate, Supplier<C> factory, Function<? super C, ? extends R> finalizer) {
        return observable(Observables.reactiveStreamX(observable).groupedStatefullyUntil(predicate,factory,finalizer));
    }

    @Override
    public ReactiveSeq<ListX<T>> groupedStatefullyWhile(BiPredicate<ListX<? super T>, ? super T> predicate) {
        return observable(Observables.reactiveStreamX(observable).groupedStatefullyWhile(predicate));
    }

    @Override
    public <C extends Collection<T>, R> ReactiveSeq<R> groupedStatefullyWhile(BiPredicate<C, ? super T> predicate, Supplier<C> factory, Function<? super C, ? extends R> finalizer) {
        return observable(Observables.reactiveStreamX(observable).groupedStatefullyWhile(predicate,factory,finalizer));
    }

    @Override
    public ReactiveSeq<ListX<T>> groupedBySizeAndTime(int size, long time, TimeUnit t) {
        return observable(Observables.reactiveStreamX(observable).groupedBySizeAndTime(size, time, t));
    }

    @Override
    public <C extends Collection<? super T>> ReactiveSeq<C> groupedBySizeAndTime(int size, long time, TimeUnit unit, Supplier<C> factory) {
        return observable(Observables.reactiveStreamX(observable).groupedBySizeAndTime(size,time,unit,factory));
    }

    @Override
    public <C extends Collection<? super T>, R> ReactiveSeq<R> groupedBySizeAndTime(int size, long time, TimeUnit unit, Supplier<C> factory, Function<? super C, ? extends R> finalizer) {
        return observable(Observables.reactiveStreamX(observable).groupedBySizeAndTime(size,time,unit,factory,finalizer));
    }

    @Override
    public <C extends Collection<? super T>, R> ReactiveSeq<R> groupedByTime(long time, TimeUnit unit, Supplier<C> factory, Function<? super C, ? extends R> finalizer) {
        return groupedBySizeAndTime(Integer.MAX_VALUE,time,unit,factory,finalizer);
    }

    @Override
    public ReactiveSeq<ListX<T>> groupedByTime(long time, TimeUnit t) {
        return observable(Observables.reactiveStreamX(observable).groupedByTime(time, t));
    }

    @Override
    public <C extends Collection<? super T>> ReactiveSeq<C> groupedByTime(long time, TimeUnit unit, Supplier<C> factory) {
        return observable(Observables.reactiveStreamX(observable).groupedByTime(time, unit, factory));
    }

    @Override
    public <C extends Collection<? super T>> ReactiveSeq<C> grouped(int size, Supplier<C> supplier) {
        return observable(Observables.reactiveStreamX(observable).grouped(size,supplier));
    }

    @Override
    public ReactiveSeq<ListX<T>> groupedWhile(Predicate<? super T> predicate) {
        return observable(Observables.reactiveStreamX(observable).groupedWhile(predicate));
    }

    @Override
    public <C extends Collection<? super T>> ReactiveSeq<C> groupedWhile(Predicate<? super T> predicate, Supplier<C> factory) {
        return observable(Observables.reactiveStreamX(observable).groupedWhile(predicate,factory));
    }

    @Override
    public ReactiveSeq<T> distinct() {
        return observable(observable.distinct());
    }

    @Override
    public <U> ReactiveSeq<U> scanLeft(U seed, BiFunction<? super U, ? super T, ? extends U> function) {
        return observable(observable.scan(seed,(a,b)->function.apply(a,b)));
    }

    @Override
    public ReactiveSeq<T> sorted() {
        return observable(Observables.reactiveStreamX(observable).sorted());
    }

    @Override
    public ReactiveSeq<T> skip(long num) {
        return observable(observable.skip((int)num));
    }


    @Override
    public void forEach(Consumer<? super T> action) {
        Observables.reactiveStreamX(observable).forEach(action);
    }

    @Override
    public void forEachOrdered(Consumer<? super T> action) {
        Observables.reactiveStreamX(observable).forEachOrdered(action);
    }

    @Override
    public Object[] toArray() {
        return Observables.reactiveStreamX(observable).toArray();
    }

    @Override
    public <A> A[] toArray(IntFunction<A[]> generator) {
        return Observables.reactiveStreamX(observable).toArray(generator);
    }

    @Override
    public ReactiveSeq<T> skipWhile(Predicate<? super T> p) {
        return observable(observable.skipWhile(t->p.test(t)));
    }

    @Override
    public ReactiveSeq<T> limit(long num) {
        return observable(observable.take((int)num));
    }

    @Override
    public ReactiveSeq<T> limitWhile(Predicate<? super T> p) {
        return observable(Observables.reactiveStreamX(observable).takeWhile(p));
    }
    @Override
    public ReactiveSeq<T> limitWhileClosed(Predicate<? super T> p) {
        return observable(observable.takeWhile(t->p.test(t)));
    }

    @Override
    public ReactiveSeq<T> limitUntil(Predicate<? super T> p) {
       return observable(Observables.reactiveStreamX(observable).limitUntil(p));
    }

    @Override
    public ReactiveSeq<T> limitUntilClosed(Predicate<? super T> p) {
        return observable(observable.takeUntil(t->p.test(t)));
    }

    @Override
    public ReactiveSeq<T> parallel() {
        return this;
    }

    @Override
    public boolean allMatch(Predicate<? super T> c) {
        return Observables.reactiveStreamX(observable).allMatch(c);
    }

    @Override
    public boolean anyMatch(Predicate<? super T> c) {
        return Observables.reactiveStreamX(observable).anyMatch(c);
    }

    @Override
    public boolean xMatch(int num, Predicate<? super T> c) {
        return Observables.reactiveStreamX(observable).xMatch(num,c);
    }

    @Override
    public boolean noneMatch(Predicate<? super T> c) {
        return Observables.reactiveStreamX(observable).noneMatch(c);
    }

    @Override
    public String join() {
        return Observables.reactiveStreamX(observable).join();
    }

    @Override
    public String join(String sep) {
        return Observables.reactiveStreamX(observable).join(sep);
    }

    @Override
    public String join(String sep, String start, String end) {
        return Observables.reactiveStreamX(observable).join(sep,start,end);
    }

    @Override
    public HeadAndTail<T> headAndTail() {
        return Observables.reactiveStreamX(observable).headAndTail();
    }

    @Override
    public Optional<T> findFirst() {
        return Observables.reactiveStreamX(observable).findFirst();
    }

    @Override
    public Maybe<T> findOne() {
        return Observables.reactiveStreamX(observable).findOne();
    }

    @Override
    public Either<Throwable, T> findFirstOrError() {
        return Observables.reactiveStreamX(observable).findFirstOrError();
    }

    @Override
    public Optional<T> findAny() {
        return Observables.reactiveStreamX(observable).findAny();
    }

    @Override
    public <R> R mapReduce(Reducer<R> reducer) {
        return Observables.reactiveStreamX(observable).mapReduce(reducer);
    }

    @Override
    public <R> R mapReduce(Function<? super T, ? extends R> mapper, Monoid<R> reducer) {
        return Observables.reactiveStreamX(observable).mapReduce(mapper,reducer);
    }

    @Override
    public T reduce(Monoid<T> reducer) {
        return Observables.reactiveStreamX(observable).reduce(reducer);
    }

    @Override
    public Optional<T> reduce(BinaryOperator<T> accumulator) {
        return Observables.reactiveStreamX(observable).reduce(accumulator);
    }

    @Override
    public T reduce(T identity, BinaryOperator<T> accumulator) {
        return Observables.reactiveStreamX(observable).reduce(identity,accumulator);
    }

    @Override
    public <U> U reduce(U identity, BiFunction<U, ? super T, U> accumulator, BinaryOperator<U> combiner) {
        return Observables.reactiveStreamX(observable).reduce(identity, accumulator, combiner);
    }

    @Override
    public ListX<T> reduce(Stream<? extends Monoid<T>> reducers) {
        return Observables.reactiveStreamX(observable).reduce(reducers);
    }

    @Override
    public ListX<T> reduce(Iterable<? extends Monoid<T>> reducers) {
        return Observables.reactiveStreamX(observable).reduce(reducers);
    }

    @Override
    public T foldRight(Monoid<T> reducer) {
        return Observables.reactiveStreamX(observable).foldRight(reducer);
    }

    @Override
    public T foldRight(T identity, BinaryOperator<T> accumulator) {
        return Observables.reactiveStreamX(observable).foldRight(identity,accumulator);
    }

    @Override
    public <T1> T1 foldRightMapToType(Reducer<T1> reducer) {
        return Observables.reactiveStreamX(observable).foldRightMapToType(reducer);
    }

    @Override
    public ReactiveSeq<T> stream() {
        return Observables.reactiveStreamX(observable);
    }

    @Override
    public <U> Traversable<U> unitIterator(Iterator<U> U) {
        return new ObservableReactiveSeq<>(Observable.from(()->U));
    }

    @Override
    public boolean startsWithIterable(Iterable<T> iterable) {
        return Observables.reactiveStreamX(observable).startsWithIterable(iterable);
    }

    @Override
    public boolean startsWith(Stream<T> stream) {
        return Observables.reactiveStreamX(observable).startsWith(stream);
    }

    @Override
    public AnyMSeq<reactiveSeq, T> anyM() {
        return AnyM.fromStream(this);
    }

    @Override
    public <R> ReactiveSeq<R> map(Function<? super T, ? extends R> fn) {
        return observable(observable.map(e->fn.apply(e)));
    }

    @Override
    public <R> ReactiveSeq<R> flatMap(Function<? super T, ? extends Stream<? extends R>> fn) {
        return observable(observable.flatMap(s->Observables.observable(ReactiveSeq.fromStream(fn.apply(s)))));
    }

    @Override
    public IntStream flatMapToInt(Function<? super T, ? extends IntStream> mapper) {
        return Observables.reactiveStreamX(observable).flatMapToInt(mapper);
    }

    @Override
    public LongStream flatMapToLong(Function<? super T, ? extends LongStream> mapper) {
        return Observables.reactiveStreamX(observable).flatMapToLong(mapper);
    }

    @Override
    public DoubleStream flatMapToDouble(Function<? super T, ? extends DoubleStream> mapper) {
        return Observables.reactiveStreamX(observable).flatMapToDouble(mapper);
    }

    @Override
    public <R> ReactiveSeq<R> flatMapAnyM(Function<? super T, AnyM<Witness.stream, ? extends R>> fn) {
        return observable(observable.flatMap(a->Observables.observable(fn.apply(a))));
    }

    @Override
    public <R> ReactiveSeq<R> flatMapI(Function<? super T, ? extends Iterable<? extends R>> fn) {
        return observable(observable.flatMapIterable(a->fn.apply(a)));
    }

    @Override
    public <R> ReactiveSeq<R> flatMapP(int maxConcurrency, QueueFactory<R> factory, Function<? super T, ? extends Publisher<? extends R>> mapper) {
        return observable(observable.flatMap(a->Observables.observable(mapper.apply(a))));
    }

    @Override
    public <R> ReactiveSeq<R> flatMapP(Function<? super T, ? extends Publisher<? extends R>> fn) {
        return observable(observable.flatMap(a->Observables.observable(fn.apply(a))));
    }

    @Override
    public <R> ReactiveSeq<R> flatMapP(int maxConcurrency, Function<? super T, ? extends Publisher<? extends R>> fn) {
        return observable(observable.flatMap(a->Observables.observable(fn.apply(a))));
    }

    @Override
    public <R> ReactiveSeq<R> flatMapStream(Function<? super T, BaseStream<? extends R, ?>> fn) {
        
        return this.<R>observable((Observable)observable.flatMap(a->fn.andThen(s->{
            ReactiveSeq<R> res = s instanceof ReactiveSeq ? (ReactiveSeq) s : (ReactiveSeq) ReactiveSeq.fromSpliterator(s.spliterator());
           return Observables.observable(res);
                }
            
        ).apply(a)));
    }

    @Override
    public ReactiveSeq<T> filter(Predicate<? super T> fn) {
        return observable(observable.filter(t->fn.test(t)));
    }

    @Override
    public Iterator<T> iterator() {
        return Observables.reactiveStreamX(observable).iterator();
    }

    @Override
    public Spliterator<T> spliterator() {
        return Observables.reactiveStreamX(observable).spliterator();
    }

    @Override
    public boolean isParallel() {
        return false;
    }

    @Override
    public ReactiveSeq<T> sequential() {
        return this;
    }

    @Override
    public ReactiveSeq<T> unordered() {
        return this;
    }

    @Override
    public ReactiveSeq<T> reverse() {
        return observable(Observables.reactiveStreamX(observable).reverse());
    }

    @Override
    public ReactiveSeq<T> onClose(Runnable closeHandler) {
        return observable(observable.doOnCompleted(()->closeHandler.run()));
    }

    @Override
    public void close() {
        
    }

    @Override
    public ReactiveSeq<T> prependS(Stream<? extends T> stream) {
        return observable(Observables.reactiveStreamX(observable).prependS(stream));
    }

    @Override
    public ReactiveSeq<T> append(T... values) {
        return observable(Observables.reactiveStreamX(observable).append(values));
    }

    @Override
    public ReactiveSeq<T> append(T value) {
        return observable(Observables.reactiveStreamX(observable).append(value));
    }

    @Override
    public ReactiveSeq<T> prepend(T value) {
        return observable(Observables.reactiveStreamX(observable).prepend(value));
    }

    @Override
    public ReactiveSeq<T> prepend(T... values) {
        return observable(Observables.reactiveStreamX(observable).prepend(values));
    }

    @Override
    public boolean endsWithIterable(Iterable<T> iterable) {
        return Observables.reactiveStreamX(observable).endsWithIterable(iterable);
    }

    @Override
    public boolean endsWith(Stream<T> stream) {
        return Observables.reactiveStreamX(observable).endsWith(stream);
    }

    @Override
    public ReactiveSeq<T> skip(long time, TimeUnit unit) {
        return observable(observable.skip(time,unit));
    }

    @Override
    public ReactiveSeq<T> limit(long time, TimeUnit unit) {
        return observable(observable.take(time,unit));
    }

    @Override
    public ReactiveSeq<T> skipLast(int num) {
        return observable(observable.skipLast(num));
    }

    @Override
    public ReactiveSeq<T> limitLast(int num) {
        return observable(observable.takeLast(num));
    }

    @Override
    public T firstValue() {
        return observable.toBlocking().first();
    }

    @Override
    public ReactiveSeq<T> onEmptySwitch(Supplier<? extends Stream<T>> switchTo) {
        return observable(Observables.reactiveStreamX(observable).onEmptySwitch(switchTo));
    }

    @Override
    public ReactiveSeq<T> onEmptyGet(Supplier<? extends T> supplier) {
        return observable(Observables.reactiveStreamX(observable).onEmptyGet(supplier));
    }

    @Override
    public <X extends Throwable> ReactiveSeq<T> onEmptyThrow(Supplier<? extends X> supplier) {
        return observable(Observables.reactiveStreamX(observable).onEmptyThrow(supplier));
    }

    @Override
    public <U> ReactiveSeq<T> distinct(Function<? super T, ? extends U> keyExtractor) {
        return observable(observable.distinct(a->keyExtractor.apply(a)));
    }

    @Override
    public ReactiveSeq<T> xPer(int x, long time, TimeUnit t) {
        return observable(Observables.reactiveStreamX(observable).xPer(x,time,t));
    }

    @Override
    public ReactiveSeq<T> onePer(long time, TimeUnit t) {
        return observable(Observables.reactiveStreamX(observable).onePer(time,t));
    }

    @Override
    public ReactiveSeq<T> debounce(long time, TimeUnit t) {
        return observable(Observables.reactiveStreamX(observable).debounce(time,t));
    }

    @Override
    public ReactiveSeq<T> fixedDelay(long l, TimeUnit unit) {
        return observable(Observables.reactiveStreamX(observable).fixedDelay(l,unit));
    }

    @Override
    public ReactiveSeq<T> jitter(long maxJitterPeriodInNanos) {
        return observable(Observables.reactiveStreamX(observable).jitter(maxJitterPeriodInNanos));
    }

    @Override
    public ReactiveSeq<T> recover(Function<? super Throwable, ? extends T> fn) {
        return observable(Observables.reactiveStreamX(observable).recover(fn));
    }

    @Override
    public <EX extends Throwable> ReactiveSeq<T> recover(Class<EX> exceptionClass, Function<? super EX, ? extends T> fn) {
        return observable(Observables.reactiveStreamX(observable).recover(exceptionClass,fn));
    }

    @Override
    public long count() {
        return Observables.reactiveStreamX(observable).count();
    }

    @Override
    public ReactiveSeq<T> appendS(Stream<? extends T> other) {
        return observable(Observables.reactiveStreamX(observable).appendS(other));
    }

    @Override
    public ReactiveSeq<T> append(Iterable<? extends T> other) {
        return  observable(Observables.reactiveStreamX(observable).append(other));
    }

    @Override
    public ReactiveSeq<T> prepend(Iterable<? extends T> other) {
        return observable(Observables.reactiveStreamX(observable).prepend(other));
    }

    @Override
    public ReactiveSeq<T> cycle(long times) {
        return observable(observable.repeat(times));
    }

    @Override
    public ReactiveSeq<T> skipWhileClosed(Predicate<? super T> predicate) {
        return observable(Observables.reactiveStreamX(observable).skipWhileClosed(predicate));
    }


    @Override
    public String format() {
        return Observables.reactiveStreamX(observable).format();
    }

    @Override
    public ReactiveSeq<T> changes() {
        return observable(Observables.reactiveStreamX(observable).changes());
    }



    @Override
    public <X extends Throwable> Subscription forEachSubscribe(Consumer<? super T> consumer) {
        return Observables.reactiveStreamX(observable).forEachSubscribe(consumer);
    }

    @Override
    public <X extends Throwable> Subscription forEachSubscribe(Consumer<? super T> consumer, Consumer<? super Throwable> consumerError) {
        return Observables.reactiveStreamX(observable).forEachSubscribe(consumer, consumerError);
    }

    @Override
    public <X extends Throwable> Subscription forEachSubscribe(Consumer<? super T> consumer, Consumer<? super Throwable> consumerError, Runnable onComplete) {
        return Observables.reactiveStreamX(observable).forEachSubscribe(consumer, consumerError,onComplete);
    }

    @Override
    public <R> R collect(Supplier<R> supplier, BiConsumer<R, ? super T> accumulator, BiConsumer<R, R> combiner) {

        return Observables.reactiveStreamX(observable).collect(supplier,accumulator,combiner);
    }

    @Override
    public <R, A> ReactiveSeq<R> collectStream(Collector<? super T, A, R> collector) {
        return observable(Observables.reactiveStreamX(observable).collectStream(collector));
    }

    @Override
    public <R, A> R collect(Collector<? super T, A, R> collector) {
        return Observables.reactiveStreamX(observable).collect((Collector<T,A,R>)collector);
    }
    

    @Override
    public T single() {
        return singleOptional().get();
    }

    @Override
    public T single(Predicate<? super T> predicate) {
        return filter(predicate).single();
    }

    @Override
    public Optional<T> singleOptional() {
        return Observables.reactiveStreamX(observable).singleOptional();
    }

    @Override
    public void subscribe(Subscriber<? super T> s) {
        Observables.publisher(observable).subscribe(s);
    }
}
