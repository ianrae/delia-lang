package org.delia.dbimpl.mem.impl;


import org.delia.error.ErrorTracker;
import org.delia.tok.Tok;
import org.delia.type.DStructType;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;

import java.util.List;

public interface RowSelector {
    void init(ErrorTracker et, Tok.WhereTok whereClause, DStructType dtype, DTypeRegistry registry);

    List<DValue> match(MemDBTable tbl);

    boolean wasError();

    void setTbl(MemDBTable tbl);

    MemDBTable getTbl();
}