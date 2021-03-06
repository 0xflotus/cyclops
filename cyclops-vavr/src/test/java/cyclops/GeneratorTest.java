package cyclops;

import com.oath.cyclops.ReactiveConvertableSequence;
import cyclops.reactive.Generator;
import cyclops.reactive.ReactiveSeq;
import cyclops.reactive.collections.immutable.VectorX;
import io.vavr.collection.Vector;
import org.junit.Test;


import static com.oath.cyclops.types.foldable.Evaluation.LAZY;
import static cyclops.reactive.Generator.suspend;
import static cyclops.reactive.Generator.times;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Created by johnmcclean on 02/06/2017.
 */
public class GeneratorTest {

    int i=0;
    int k=0;
    /**
    @Test //TODO uncomment
    public void generate() {
        i = 100;
        k = 9999;

        Vector<Integer> vec = suspend((Integer i) -> i != 4, s -> {


                      Generator<Integer> gen1 = suspend(times(2), s2 -> s2.yield(i++));
                      Generator<Integer> gen2 = suspend(times(2), s2 -> s2.yield(k--));

                      return s.yieldAll(gen1.stream(), gen2.stream());
          }
               ).to(ReactiveConvertableSequence::converter)
                .vectorX(LAZY)
                .take(5)
                .to(VavrConverters::Vector);

        System.out.println(vec);
        //Vector(100, 101, 9999, 9998, 102)
    }**/

  @Test
  public void generateFromIterable() {
    i = 100;
    k = 9999;

    Vector<Integer> vec = VectorX.fromIterable(suspend((Integer i) -> i != 4, s -> {


        Generator<Integer> gen1 = suspend(times(2), s2 -> s2.yield(i++));
        Generator<Integer> gen2 = suspend(times(2), s2 -> s2.yield(k--));

        return s.yieldAll(gen1.stream(), gen2.stream());
      }
    ))
      .take(5)
      .to(VavrConverters::Vector);

    System.out.println(vec);
    //Vector(100, 101, 9999, 9998, 102)
  }


}
