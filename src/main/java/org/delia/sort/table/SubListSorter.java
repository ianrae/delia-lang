package org.delia.sort.table;

import java.util.List;

public interface SubListSorter<T> {
    List<T> sort(List<T> list);
}
