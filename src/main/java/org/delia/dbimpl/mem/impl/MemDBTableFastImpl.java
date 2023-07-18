package org.delia.dbimpl.mem.impl;

import org.delia.type.DValue;

import java.util.ArrayList;
import java.util.List;

/**
 * Fastest but not at all thread-safe.
 * Useful during most test-driven development.
 *
 * DO NOT USE IN PRODUCTION.
 */
public class MemDBTableFastImpl implements MemDBTable  {
    private String name;
    private List<DValue> rowL = new ArrayList<>();

    public MemDBTableFastImpl(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<DValue> getList() {
        return rowL;
    }
}