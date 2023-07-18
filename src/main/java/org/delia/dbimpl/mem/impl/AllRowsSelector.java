package org.delia.dbimpl.mem.impl;

import org.delia.error.ErrorTracker;
import org.delia.tok.Tok;
import org.delia.type.DStructType;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;

import java.util.ArrayList;
import java.util.List;

class AllRowsSelector extends RowSelectorBase {
    @Override
    public void init(ErrorTracker et, Tok.WhereTok whereClause, DStructType dtype, DTypeRegistry registry) {
        super.init(et, whereClause, dtype, registry);
    }

    @Override
    public List<DValue> match(MemDBTable tbl) {
        List<DValue> list = tbl.getList();
        List<DValue> copy = new ArrayList<>(list);
        return copy;
    }
}