package org.delia.dbimpl.mem.impl;

public class MemDBFactoryImpl implements MemDBFactory {

    @Override
    public MemDBTable create(String tblName) {
//        return new MemDBTableFastImpl(tblName); //fast but not thread-safe.
        return new MemDBTableSynchronized(tblName); //thread-safe
    }
}
