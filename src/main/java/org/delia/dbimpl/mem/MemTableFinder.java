package org.delia.dbimpl.mem;

import org.delia.type.DType;
import org.delia.dbimpl.mem.impl.MemDBTable;

public interface MemTableFinder {

//    MemDBTable findMemTable(String tableName);
    MemDBTable findMemTable(DType dtype);

}
