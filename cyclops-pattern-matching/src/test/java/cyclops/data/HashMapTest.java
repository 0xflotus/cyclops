package cyclops.data;

import org.junit.Test;
import org.pcollections.HashTreePMap;
import org.pcollections.PMap;

import java.util.ArrayList;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class HashMapTest {

    @Test
    public void plusSize(){
        assertThat(TrieMap.empty().put("hello","world").size(),equalTo(1));
    }
    @Test
    public void add10000(){
        //17579
        long start = System.currentTimeMillis();
        TrieMap<Integer,Integer> v = TrieMap.empty();
        for(int i=0;i<100_000_00;i++){
            v =v.put(i,i);
        }
        System.out.println(System.currentTimeMillis()-start);
        System.out.println(v.size());
    }
    @Test
    public void read100_000_00(){
        //4557

        TrieMap<Integer,Integer> v = TrieMap.empty();
        for(int i=0;i<100_000_00;i++){
            v =v.put(i,i);
        }
        ArrayList<Integer> al = new ArrayList(v.size());
        long start = System.currentTimeMillis();
        for(int i=0;i<100_000_00;i++){
            al.add(v.getOrElse(i,null));
        }

        System.out.println(System.currentTimeMillis()-start);
        System.out.println(v.size());
    }
    @Test
    public void read100_000_00PC(){
        //1905

        PMap<Integer,Integer> v = HashTreePMap.empty();
        for(int i=0;i<100_000_00;i++){
            v =v.plus(i,i);
        }
        ArrayList<Integer> al = new ArrayList(v.size());
        long start = System.currentTimeMillis();
        for(int i=0;i<100_000_00;i++){
            al.add(v.get(i));
        }

        System.out.println(System.currentTimeMillis()-start);
        System.out.println(v.size());
    }

    @Test
    public void add10000PCol(){
        //27055
        long start = System.currentTimeMillis();
        PMap<Integer,Integer> v = HashTreePMap.empty();
        for(int i=0;i<100_000_00;i++){
            v =v.plus(i,i);
        }
        System.out.println(System.currentTimeMillis()-start);
        System.out.println(v.size());
    }

}