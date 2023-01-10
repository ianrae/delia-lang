package org.delia.dbimpl.mem.impl;

import org.delia.error.ErrorTracker;
import org.delia.tok.Tok;
import org.delia.type.DStructType;
import org.delia.type.DTypeRegistry;

public abstract class RowSelectorBase implements RowSelector {
    //    protected FilterExp filter;
    protected DStructType dtype;
    protected String keyField;
    protected ErrorTracker et;
    protected boolean wasError;
    protected MemDBTable tbl;
    protected Tok.WhereTok whereClause;
    protected DTypeRegistry registry;

    @Override
    public void init(ErrorTracker et, Tok.WhereTok whereClause, DStructType dtype, DTypeRegistry registry) {
        this.et = et;
//        this.filter = spec.queryExp.filter;
        this.whereClause = whereClause;
        this.dtype = dtype;
        this.registry = registry;
    }

    @Override
    public boolean wasError() {
        return wasError;
    }

    @Override
    public void setTbl(MemDBTable tbl) {
        this.tbl = tbl;
    }

    @Override
    public MemDBTable getTbl() {
        return tbl;
    }

}