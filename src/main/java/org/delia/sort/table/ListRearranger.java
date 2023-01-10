package org.delia.sort.table;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Given a list of strings, it removes all the upper-case strings, sorts the upper-case strings,
 * and then re-inserts them at the index of the original first upper-case string.
 * <p>
 * The final order will be the same if all upper-case strings were contiguous.
 */
public class ListRearranger<T> {

   public List<T> sortSubList(List<T> list, Predicate<T> pred, SubListSorter<T> sorter) {
        Optional<T> ss = list.stream().filter(s -> pred.test(s)).findFirst();
        if (!ss.isPresent()) {
            return list;
        }
        int firstIndex = Optional.ofNullable(ss).map(z -> list.indexOf(z.get())).orElse(-1);

        List<T> sublist = list.stream().filter(s -> pred.test(s)).collect(Collectors.toList());
        sublist = sorter.sort(sublist);

        List<T> finalList = new ArrayList<>();
        for (T s : list) {
            if (!sublist.contains(s)) {
                finalList.add(s);
            }
        }

        finalList.addAll(firstIndex, sublist);
        return finalList;
    }
}
