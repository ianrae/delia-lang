package org.delia;

import org.delia.sort.table.ListRearranger;
import org.delia.sort.table.SubListSorter;
import org.delia.util.render.ObjectRendererImpl;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import static org.junit.Assert.assertEquals;

public class SortTests {

    public static class MySubListSorter implements SubListSorter<String> {
        public List<String> sort(List<String> list) {
            list.sort(String.CASE_INSENSITIVE_ORDER);
            return list;
        }
    }


    @Test
    public void test1() {
        List<String> list = Arrays.asList("a", "b", "B", "A", "c");

        Predicate<String> upperCasePred =  x -> Character.isUpperCase(x.charAt(0));
        ListRearranger mySorter = new ListRearranger();
        list = mySorter.sortSubList(list, upperCasePred, new MySubListSorter());
        ObjectRendererImpl or = new ObjectRendererImpl();
        log(or.render(list));
        assertEquals("b", list.get(1));
        assertEquals("A", list.get(2));
        assertEquals("B", list.get(3));
    }

    @Test
    public void test2() {
        List<String> list = Arrays.asList("a", "b", "c");

        Predicate<String> upperCasePred =  x -> Character.isUpperCase(x.charAt(0));

        ListRearranger mySorter = new ListRearranger();
        list = mySorter.sortSubList(list, upperCasePred, new MySubListSorter());
        ObjectRendererImpl or = new ObjectRendererImpl();
        log(or.render(list));
        assertEquals("a", list.get(0));
        assertEquals("b", list.get(1));
        assertEquals("c", list.get(2));
    }

    //--

    private void log(String s) {
        System.out.println(s);
    }

}
