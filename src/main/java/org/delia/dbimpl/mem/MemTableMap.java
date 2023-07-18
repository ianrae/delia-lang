package org.delia.dbimpl.mem;

import org.delia.dbimpl.mem.impl.MemDBFactory;
import org.delia.dbimpl.mem.impl.MemDBTable;
import org.delia.dbimpl.mem.impl.MemDBTableFastImpl;
import org.delia.type.DStructType;
import org.delia.type.DType;
import org.delia.type.DTypeName;
import org.delia.util.DTypeNameUtil;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MemTableMap {
    private final MemDBFactory factory;
    private Map<String, MemDBTable> tableMap = new ConcurrentHashMap<>(); //key is schema.typeName (lower-case)

    public MemTableMap(MemDBFactory factory) {
        this.factory = factory;
    }

    public boolean containsTable(String sqlTblName) {
        return tableMap.containsKey(sqlTblName);
    }

    public void addTable(String sqlTblName) {
        MemDBTable tbl = factory.create(sqlTblName);
        tableMap.put(sqlTblName, tbl);
    }

    public MemDBTable getTable(String sqlTblName) {
        return tableMap.get(sqlTblName);
    }

    public MemDBTable getTable(DStructType structType) {
        String sqlTblName = formatSqlTableName(structType);
        return tableMap.get(sqlTblName);
    }

    public static String formatSqlTableName(DStructType structType) {
        return DTypeNameUtil.formatSqlTableName(structType.getTypeName());
    }

}
