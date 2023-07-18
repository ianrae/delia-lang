package org.delia.dbimpl.mem.impl;

public class MemDBFactoryImpl implements MemDBFactory {

    @Override
    public MemDBTable create(String tblName) {
        return new MemDBTableFastImpl(tblName);
    }
}
