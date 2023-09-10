package org.delia.dbimpl.mem.impl;

import org.delia.type.DValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Thread-safe version of MemDBTable.
 *
 * https://howtodoinjava.com/java/collections/arraylist/synchronize-arraylist/
 * -it is recommended that we should manually synchronize the returned list when traversing it via Iterator, Spliterator or Stream else it may result in non-deterministic behavior.
 * -No explicit synchronization is needed to add, or remove elements from this synchronized arraylist.
 *
 */
public class MemDBTableSynchronized implements MemDBTable  {
    private String name;
    private List<DValue> rowL = new ArrayList<>();
    private List<DValue> safeList = Collections.synchronizedList(rowL);

    public MemDBTableSynchronized(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<DValue> getList() {
        return safeList;
    }

    @Override
    public boolean needsSynchronizationOnTraverse() {
        return true;
    }
}
