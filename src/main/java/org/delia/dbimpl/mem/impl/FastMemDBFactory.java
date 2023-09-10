package org.delia.dbimpl.mem.impl;

import org.delia.log.DeliaLog;

public class FastMemDBFactory implements MemDBFactory {
    private DeliaLog log;

    public FastMemDBFactory(DeliaLog log) {
        this.log = log;
    }

    @Override
    public MemDBTable create(String tblName) {
        log.log("***** Warning. Using non-thread-safe MEM MemDBTableFastImpl! It's fast but do not use in production. *****");
        return new MemDBTableFastImpl(tblName);
    }
}
