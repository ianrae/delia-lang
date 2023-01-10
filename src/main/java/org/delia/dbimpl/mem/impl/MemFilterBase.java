package org.delia.dbimpl.mem.impl;


import org.delia.core.DateFormatService;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.db.DBException;
import org.delia.dbimpl.mem.MemTableFinder;
import org.delia.error.DeliaError;
import org.delia.tok.Tok;
import org.delia.type.DStructType;
import org.delia.type.DTypeName;
import org.delia.type.DTypeRegistry;
import org.delia.util.DeliaExceptionHelper;

public class MemFilterBase extends ServiceBase {
    public static class SelectSpec {

        RowSelector selector;
        QueryTypeDetails details;

        public boolean isTable(String tblName) {
            return details.targetType.getName().equals(tblName);
        }

    }


    protected final DTypeRegistry registry;
    protected DateFormatService fmtSvc;
    protected FilterEvaluator evaluator;
    protected MemTableFinder tableFinder;

    public MemFilterBase(FactoryService factorySvc, DTypeRegistry registry, MemTableFinder tableFinder) {
        super(factorySvc);
        this.registry = registry;
        this.fmtSvc = factorySvc.getDateFormatService();
        this.evaluator = new FilterEvaluator(factorySvc);
        this.tableFinder = tableFinder;
    }

    protected RowSelector createSelector(MemDBTable tbl, DTypeName typeName, Tok.WhereTok whereClause) {
        SelectSpec spec = createSelectorEx(tbl, typeName, whereClause, null);
        return spec.selector;
    }

    protected SelectSpec createSelectorEx(MemDBTable tbl, DTypeName typeName, Tok.WhereTok whereClause, DStructType whereAllType) {
        if (tbl == null) {
            tbl = handleUnknownTable(typeName);
        }

        QueryTypeDetector queryTypeDetector = new QueryTypeDetector();
        RowSelector selector = null;
        QueryTypeDetails details = new QueryTypeDetails();
        QueryType queryType = queryTypeDetector.detectQueryType(whereClause, details);
        switch (queryType) {
            case ALL_ROWS:
                selector = new AllRowsSelector();
                break;
            case OP:
                selector = new OpRowSelector(fmtSvc, factorySvc, evaluator); //, implicitCtx);
                break;
            case PRIMARY_KEY:
            default:
                selector = new PrimaryKeyRowSelector(evaluator);
        }

        if (details.targetType == null || details.targetType.getTypeName().equals(typeName)) {
            DStructType dtype = whereAllType != null ? whereAllType : findType(typeName);
            if (!dtype.getName().equals(tbl.name)) {
                tbl = tableFinder.findMemTable(dtype);
            }
            initSelector(selector, whereClause, tbl, dtype); //normal query
            details.targetType = dtype;
        } else {
            tbl = tableFinder.findMemTable(details.targetType);
            initSelector(selector, whereClause, tbl, details.targetType); //query of other table
        }

        SelectSpec spec = new SelectSpec();
        spec.selector = selector;
        spec.details = details;
        return spec;
    }

    private void initSelector(RowSelector selector, Tok.WhereTok whereClause, MemDBTable tbl, DStructType dtype) {
        selector.setTbl(tbl);
        String typeName = dtype.getName();
        if (dtype == null) {
            DeliaExceptionHelper.throwError("struct-unknown-type-in-query", "unknown struct type '%s'", typeName);
        }

        selector.init(et, whereClause, dtype, registry);
        if (selector.wasError()) {
            DeliaError err = et.add("row-selector-error", String.format("row selector failed for type '%s'", typeName));
            throw new DBException(err);
        }
    }

    protected DStructType findType(DTypeName typeName) {
        DStructType structType = registry.findTypeOrSchemaVersionType(typeName);
        return structType;
    }

    public MemDBTable handleUnknownTable(DTypeName typeName) {
//        if (createTablesAsNeededFlag) {
//            this.rawCreateTable(typeName);
//            return tableMap.get(typeName);
//        } else {
        DeliaError err = et.add("unknown-table-type", String.format("can't find type '%s'", typeName));
        throw new DBException(err);
//        }
    }


}