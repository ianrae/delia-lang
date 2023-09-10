package org.delia.dbimpl.mem;

import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.db.*;
import org.delia.dbimpl.mem.impl.*;
import org.delia.hld.dat.DatService;
import org.delia.lld.LLD;
import org.delia.log.DeliaLog;
import org.delia.runner.DeliaRunner;
import org.delia.runner.QueryResponse;
import org.delia.type.*;
import org.delia.util.DeliaExceptionHelper;

import java.util.List;

public class MemDBExecutor extends ServiceBase implements DBExecutor, DBExecutorEx, MemTableFinder {

    private final MemDBInterfaceFactory dbInterface;
    //private final Map<String, MemDBTable> tableMap;
    private final MemTableMap tableMap;
    private final FKResolver fkResolver;
    private DTypeRegistry registry;
    private DBStuff stuff;
    private DeliaRunner deliaRunner;

    public MemDBExecutor(FactoryService factorySvc, MemDBInterfaceFactory dbInterface, FKResolver fkResolver) {
        super(factorySvc);
        this.dbInterface = dbInterface;
        this.tableMap = dbInterface.createSingleMemDB();
        this.fkResolver = fkResolver;
    }

    @Override
    public DBConnection getDBConnection() {
        return null;//none for MEM
    }

    @Override
    public DeliaLog getLog() {
        return factorySvc.getLog();
    }

    @Override
    public void execCreateSchema(LLD.LLCreateSchema stmt) {
        //do nothing
    }

    @Override
    public void init1(DTypeRegistry registry, DatService datSvc, DeliaRunner deliaRunner) {
        this.registry = registry;
        this.deliaRunner = deliaRunner;
    }

    @Override
    public void execCreateTable(LLD.LLCreateTable stmt) {
        String tblName = stmt.getTableName();
        if (tableMap.containsTable(tblName)) {
            DeliaExceptionHelper.throwError("table-already-exists", "Table '%s' already exists in MEM", tblName);
        }
        tableMap.addTable(tblName);
    }

    @Override
    public void execCreateAssocTable(LLD.LLCreateAssocTable stmt) {
        //nothing to do. MEM doesn't need assoc tables
    }

    @Override
    public DValue execPreInsert(LLD.LLInsert stmt, DValue dval) {
        //TODO: do we need to check structType or is checking fields ok?
        if (!areFieldsToInsert(stmt)) {
            et.add("cant-insert-empty-type", String.format("type '%s' has no fields. Can't execute insert.", stmt.getTableName()));
            return null;
        }

        String tblName = stmt.getTableName();
        MemDBTable memTbl = tableMap.getTable(tblName);
        MemInsert memInsert = new MemInsert(factorySvc, registry, fkResolver);
        return memInsert.executePreInsert(memTbl, stmt.table.physicalType, stmt.fieldL, dval, findOrCreateStuff());
    }

    private boolean areFieldsToInsert(LLD.LLInsert stmt) {
        return stmt.areFieldsToInsert();
    }

    @Override
    public DValue execInsert(LLD.LLInsert stmt, DValue dval) {
        //TODO: do we need to check structType or is checking fields ok?
        if (!areFieldsToInsert(stmt)) {
            et.add("cant-insert-empty-type", String.format("type '%s' has no fields. Can't execute insert.", stmt.getTableName()));
            return null;
        }

        String tblName = stmt.getTableName();
        MemDBTable memTbl = tableMap.getTable(tblName);
        MemInsert memInsert = new MemInsert(factorySvc, registry, fkResolver);
        return memInsert.executeInsert(memTbl, stmt.table.physicalType, stmt.fieldL, dval, findOrCreateStuff());
    }

    @Override
    public void execDelete(LLD.LLDelete stmt) {
        String tblName = stmt.getTableName();
        MemDBTable memTbl = tableMap.getTable(tblName);
        MemDelete memDelete = new MemDelete(factorySvc, registry, fkResolver, this);
        QueryResponse qresp = memDelete.executeDelete(memTbl, stmt);
        //TODO what to do with errors?
    }

    @Override
    public void execUpdate(LLD.LLUpdate stmt) {
        doEffectiveIntSizeFixup(stmt.fieldL);
        String tblName = stmt.getTableName();
        MemDBTable memTbl = tableMap.getTable(tblName);
        MemUpdate memUpdate = new MemUpdate(factorySvc, registry, fkResolver, this);
        QueryResponse qresp = memUpdate.executeUpdate(memTbl, stmt);
        //TODO what to do with errors?
    }

    /**
     *  Shape.INTEGER can physically store a Long or an Integer in the dvalue.
     *  This is a problem for MEM orderByFunction which tries to use a TreeMap to sort, but fails when
     *   it sees a mixture of Long and Integer.
     *   See UpdateLongIssueTests.java
     **/
    private void doEffectiveIntSizeFixup(List<LLD.LLFieldValue> fieldL) {
        for(LLD.LLFieldValue field: fieldL) {
            DType dtype = field.field.physicalPair.type;
            if (Shape.INTEGER.equals(dtype.getShape())) {
                if (EffectiveShape.EFFECTIVE_INT.equals(dtype.getEffectiveShape())) {
                    if (field.dval.getObject() instanceof Long) {
                        Long longvalue = (Long) field.dval.getObject();
                        DValueImpl dvalimpl = (DValueImpl) field.dval;
                        dvalimpl.forceObject(longvalue.intValue());
                    }
                } else if (EffectiveShape.EFFECTIVE_LONG.equals(dtype.getEffectiveShape())) {
                    if (field.dval.getObject() instanceof Integer) {
                        Integer longvalue = (Integer) field.dval.getObject();
                        DValueImpl dvalimpl = (DValueImpl) field.dval;
                        dvalimpl.forceObject(longvalue.longValue());
                    }
                }
            }
        }
    }

    @Override
    public void execUpsert(LLD.LLUpsert stmt, DValue dval) {
        doEffectiveIntSizeFixup(stmt.fieldL);
        String tblName = stmt.getTableName();
        MemDBTable memTbl = tableMap.getTable(tblName);
        MemUpsert memUpsert = new MemUpsert(factorySvc, registry, fkResolver, this);
        QueryResponse qresp = memUpsert.executeUpsert(memTbl, stmt, dval, findOrCreateStuff());
        //TODO what to do with errors?
    }

    /**
     * Ugly. we need a serial provider per registry (really per runner i thinkg)
     */
    protected DBStuff findOrCreateStuff() {
        if (stuff == null) {
            stuff = new DBStuff();
            stuff.init(factorySvc, registry, dbInterface.getSerialMap());
        }
        return stuff;
    }


    @Override
    public QueryResponse execSelect(LLD.LLSelect stmt, SelectDBContext ctx) {
        //remove aliases
        AliasRemoverTokVisitor visitor = new AliasRemoverTokVisitor();
        stmt.whereTok.visit(visitor, null);

        String tblName = stmt.getTableName();
        MemDBTable memTbl = tableMap.getTable(tblName);
        MemSelect memSelect = new MemSelect(factorySvc, registry, fkResolver, this, deliaRunner, ctx);
        return memSelect.executeSelect(memTbl, stmt);
    }

    @Override
    public DBInterfaceFactory getDbInterface() {
        return dbInterface;
    }

    @Override
    public void close() throws Exception {
        //nothing to do
    }

//    @Override
//    public MemDBTable findMemTable(String tableName) {
//        return tableMap.get(tableName);
//    }

    @Override
    public MemDBTable findMemTable(DType dtype) {
        return tableMap.getTable((DStructType) dtype);
    }

}
