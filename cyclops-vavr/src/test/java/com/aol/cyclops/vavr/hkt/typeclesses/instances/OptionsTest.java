package com.aol.cyclops.vavr.hkt.typeclesses.instances;
import static com.aol.cyclops.vavr.hkt.OptionKind.widen;
import static cyclops.function.Lambda.l1;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import cyclops.companion.vavr.Options;
import com.aol.cyclops.vavr.hkt.OptionKind;
import org.junit.Test;

import com.aol.cyclops2.hkt.Higher;
import cyclops.control.Maybe;
import cyclops.function.Fn1;
import cyclops.function.Lambda;
import cyclops.function.Monoid;

import javaslang.control.Option;

public class OptionsTest {

    @Test
    public void unit(){
        
        OptionKind<String> opt = Options.Instances.unit()
                                            .unit("hello")
                                            .convert(OptionKind::narrowK);
        
        assertThat(opt,equalTo(Option.of("hello")));
    }
    @Test
    public void functor(){
        
        OptionKind<Integer> opt = Options.Instances.unit()
                                     .unit("hello")
                                     .apply(h-> Options.Instances.functor().map((String v) ->v.length(), h))
                                     .convert(OptionKind::narrowK);
        
        assertThat(opt,equalTo(Option.of("hello".length())));
    }
    @Test
    public void apSimple(){
        Options.Instances.applicative()
            .ap(widen(Option.of(l1(this::multiplyByTwo))),widen(Option.of(1)));
    }
    private int multiplyByTwo(int x){
        return x*2;
    }
    @Test
    public void applicative(){
        
        OptionKind<Fn1<Integer,Integer>> optFn = Options.Instances.unit().unit(Lambda.l1((Integer i) ->i*2)).convert(OptionKind::narrowK);
        
        OptionKind<Integer> opt = Options.Instances.unit()
                                     .unit("hello")
                                     .apply(h-> Options.Instances.functor().map((String v) ->v.length(), h))
                                     .apply(h-> Options.Instances.applicative().ap(optFn, h))
                                     .convert(OptionKind::narrowK);
        
        assertThat(opt,equalTo(Option.of("hello".length()*2)));
    }
    @Test
    public void monadSimple(){
       OptionKind<Integer> opt  = Options.Instances.monad()
                                            .<Integer,Integer>flatMap(i->widen(Option.of(i*2)), widen(Option.of(3)))
                                            .convert(OptionKind::narrowK);
    }
    @Test
    public void monad(){
        
        OptionKind<Integer> opt = Options.Instances.unit()
                                     .unit("hello")
                                     .apply(h-> Options.Instances.monad().flatMap((String v) -> Options.Instances.unit().unit(v.length()), h))
                                     .convert(OptionKind::narrowK);
        
        assertThat(opt,equalTo(Option.of("hello".length())));
    }
    @Test
    public void monadZeroFilter(){
        
        OptionKind<String> opt = Options.Instances.unit()
                                     .unit("hello")
                                     .apply(h-> Options.Instances.monadZero().filter((String t)->t.startsWith("he"), h))
                                     .convert(OptionKind::narrowK);
        
        assertThat(opt,equalTo(Option.of("hello")));
    }
    @Test
    public void monadZeroFilterOut(){
        
        OptionKind<String> opt = Options.Instances.unit()
                                     .unit("hello")
                                     .apply(h-> Options.Instances.monadZero().filter((String t)->!t.startsWith("he"), h))
                                     .convert(OptionKind::narrowK);
        
        assertThat(opt,equalTo(Option.none()));
    }
    
    @Test
    public void monadPlus(){
        OptionKind<Integer> opt = Options.Instances.<Integer>monadPlus()
                                      .plus(OptionKind.widen(Option.none()), OptionKind.widen(Option.of(10)))
                                      .convert(OptionKind::narrowK);
        assertThat(opt,equalTo(Option.of(10)));
    }
    @Test
    public void monadPlusNonEmpty(){
        
        Monoid<OptionKind<Integer>> m = Monoid.of(OptionKind.widen(Option.none()), (a, b)->a.isDefined() ? b : a);
        OptionKind<Integer> opt = Options.Instances.<Integer>monadPlus(m)
                                      .plus(OptionKind.widen(Option.of(5)), OptionKind.widen(Option.of(10)))
                                      .convert(OptionKind::narrowK);
        assertThat(opt,equalTo(Option.of(10)));
    }
    @Test
    public void  foldLeft(){
        int sum  = Options.Instances.foldable()
                        .foldLeft(0, (a,b)->a+b, OptionKind.widen(Option.of(4)));
        
        assertThat(sum,equalTo(4));
    }
    @Test
    public void  foldRight(){
        int sum  = Options.Instances.foldable()
                        .foldRight(0, (a,b)->a+b, OptionKind.widen(Option.of(1)));
        
        assertThat(sum,equalTo(1));
    }
    @Test
    public void traverse(){
       Maybe<Higher<OptionKind.µ, Integer>> res = Options.Instances.traverse()
                                                                 .traverseA(Maybe.Instances.applicative(), (Integer a)->Maybe.just(a*2), OptionKind.of(1))
                                                                 .convert(Maybe::narrowK);
       
       
       assertThat(res,equalTo(Maybe.just(Option.of(2))));
    }
    
}
