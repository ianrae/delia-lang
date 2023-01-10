package org.delia.runner;

import org.delia.dbimpl.mem.MemTableMap;
import org.delia.lld.LLD;
import org.delia.sort.table.SubListSorter;
import org.delia.type.DTypeName;
import org.delia.util.DTypeNameUtil;

import java.util.ArrayList;
import java.util.List;

public class MyCreateTableSorter implements SubListSorter<LLD.LLStatement> {
    private final List<DTypeName> orderL;

    public MyCreateTableSorter(List<DTypeName> orderL) {
        this.orderL = orderL;
    }

    public List<LLD.LLStatement> sort(List<LLD.LLStatement> list) {
        List<LLD.LLStatement> newList = new ArrayList<>();

        for(DTypeName typeName: orderL) {
            LLD.LLCreateTable stmt = findInList(list, typeName);
            if (stmt != null) {
                newList.add(stmt);
            }
        }

        //add assoc tables last. they never refer to each other so they don't need internal sorting
        for(LLD.LLStatement stmt: list) {
            if (stmt instanceof LLD.LLCreateAssocTable) {
                newList.add(stmt);
            }
        }

        return newList;
    }

    private LLD.LLCreateTable findInList(List<LLD.LLStatement> list, DTypeName typeName) {
        for(LLD.LLStatement stmt: list) {
            if (stmt instanceof  LLD.LLCreateTable) {
                LLD.LLCreateTable createTable = (LLD.LLCreateTable) stmt;
                String tbl1 = createTable.getTableName();
                String tbl2 = DTypeNameUtil.formatSqlTableName(typeName);
                if (tbl1.equals(tbl2)) {
                    return createTable;
                }
            }
        }
        return null;
    }
}
