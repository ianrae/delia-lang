package org.delia.dbimpl.mem.impl.func;

import org.apache.commons.collections.CollectionUtils;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.db.SelectDBContext;
import org.delia.dbimpl.mem.MemTableFinder;
import org.delia.dbimpl.mem.impl.FKResolver;
import org.delia.dbimpl.mem.impl.MemDBTable;
import org.delia.dbimpl.mem.impl.QueryTypeDetails;
import org.delia.dval.compare.DValueCompareService;
import org.delia.lld.LLD;
import org.delia.runner.DeliaRunner;
import org.delia.runner.QueryResponse;
import org.delia.sql.LLFieldHelper;
import org.delia.type.*;
import org.delia.util.DValueHelper;
import org.delia.util.DeliaExceptionHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/*
-need to do in correct order
-should not be too strict
 eg Customer[true].wid.orderBy('birthDate')
  -here orderby applies to customer and then we take wid
  -same as Customer[true].orderBy('birthDate').wid

so work left to right, using scope
 scope.structType = stmnt.tbl
 for each ...
   if is struct field //.addr
     endcurrent scope
     scope.structType = ..
   else if scalar field
     add to scope
   else //fn
     add to scope

 */
public class ScopedFunctionExecutor extends ServiceBase {

    private final MemTableFinder tableFinder;
    private FuncExecutor funcExecutor;
    private final DTypeRegistry registry;
    private final DeliaRunner deliaRunner;

    public ScopedFunctionExecutor(FactoryService factorySvc, DTypeRegistry registry, FKResolver fkResolver, MemTableFinder tableFinder, DeliaRunner deliaRunner) {
        super(factorySvc);
        this.funcExecutor = new FuncExecutor(factorySvc, registry, fkResolver, deliaRunner);
        this.registry = registry;
        this.tableFinder = tableFinder;
        this.deliaRunner = deliaRunner;
    }

//    public DStructType getFromType(List<QScope> scopes) {
//        DStructType structType = null;
//        for(QScope scope: scopes) {
//            if (scope.structField != null) {
//                TypePair pair = DValueHelper.findField(scope.structField.physicalTable.physicalType, scope.structField.fieldName);
//                if (pair != null && pair.type.isStructShape()) {
//                    structType = (DStructType) pair.type;
//                    continue;
//                }
//            }
//
//            structType = scope.structType;
//        }
//        return structType;
//    }

    public QueryResponse executeFunctionsAndFields(List<QScope> scopes, List<DValue> initialDvalList) {
        List<DValue> dvalList = initialDvalList;
        for (QScope scope : scopes) {
            if (scope.structField != null) {
                //get .addr for each in dvalList
                dvalList = extractStructFieldValues(scope, dvalList);
            }

            for (LLD.LLEx func : scope.funcL) {
                dvalList = execFunction(func, dvalList, scope);
            }

            if (scope.finalScalarField != null) {
                //extract .wid for dvallist
                dvalList = extractScalarFieldValues(scope, dvalList);
            }

            for (LLD.LLEx func : scope.scalarFuncL) {
                dvalList = execFunction(func, dvalList, scope);
            }
        }

        QueryResponse qresp = new QueryResponse();
        qresp.ok = true;
        qresp.dvalList = dvalList;
        return qresp;
    }

    private List<DValue> execFunction(LLD.LLEx funcEl, List<DValue> dvalList, QScope scope) {
        LLD.LLDFuncEx func = (LLD.LLDFuncEx) funcEl;
        return funcExecutor.execFunction(func, dvalList, scope);
    }

    private List<DValue> extractStructFieldValues(QScope scope, List<DValue> dvalList) {
        List<DValue> pkList = new ArrayList<>();
        String fieldName = scope.structField.fieldName;
        for (DValue dval : dvalList) {
            if (dval == null) {
                continue;
            }

            DValue inner = dval.asStruct().getField(fieldName);
            if (inner != null) {
                DRelation drel = inner.asRelation();
                pkList.addAll(drel.getMultipleKeys());
            }
        }

        //now get the entities for each pk
        dvalList = new ArrayList<>();
        DValueCompareService compareSvc = factorySvc.getDValueCompareService();
        DType dtype = DValueHelper.findFieldType(scope.structField.physicalTable.physicalType, fieldName);
        MemDBTable tbl = tableFinder.findMemTable(dtype);
        TypePair pkpair = DValueHelper.findPrimaryKeyFieldPair(dtype);
        for (DValue pkval : pkList) {
            for (DValue rowval : tbl.rowL) {
                DValue pkinner = rowval.asStruct().getField(pkpair.name);
                if (compareSvc.compare(pkval, pkinner) == 0) {
                    dvalList.add(rowval);
                }
            }
        }
        return dvalList;
    }

    private List<DValue> extractScalarFieldValues(QScope scope, List<DValue> dvalList) {
        List<DValue> list = new ArrayList<>();
        String fieldName = scope.finalScalarField.fieldName;
        for (DValue dval : dvalList) {
            if (dval == null) {
                continue;
            }

            DValue inner = dval.asStruct().getField(fieldName);
            list.add(inner); //inner can be null
        }
        return list;
    }

    public List<QScope> buildScopeList(MemDBTable tbl, LLD.LLSelect stmt, QueryTypeDetails details, SelectDBContext ctx) {
        QScope currentScope = new QScope();
        currentScope.structType = details.targetType; //stmt.table.physicalType;
        if (CollectionUtils.isEmpty(stmt.finalFieldsL)) {
            List<LLD.LLEx> reorderedList = new ArrayList<>();
            int extra = addIfMissing(currentScope, reorderedList, "fks", ctx.enableRemoveFks, false);
            currentScope.funcL = reorderedList;
            return Collections.singletonList(currentScope);
        }

        List<QScope> scopes = new ArrayList<>();
        scopes.add(currentScope);
        for (LLD.LLEx el : stmt.finalFieldsL) {
            if (el instanceof LLD.LLFinalFieldEx) {
                LLD.LLFinalFieldEx field = (LLD.LLFinalFieldEx) el;
                TypePair pair = DValueHelper.findField(field.physicalTable.physicalType, field.fieldName);
                if (pair.type.isStructShape()) {
                    //don't compare types because of self join. Customer[55].manager.manager
                    currentScope = new QScope();
                    currentScope.structType = field.physicalTable.physicalType;
                    scopes.add(currentScope);
                    currentScope.structField = field;
                } else {
                    currentScope.finalScalarField = field;
                }
            } else if (el instanceof LLD.LLDFuncEx) {
                LLD.LLDFuncEx func = (LLD.LLDFuncEx) el;
                currentScope.funcL.add(func);
//            } else if (el instanceof LLD.LLFuncArg) {
//                LLD.LLFuncArg funcArg = (LLD.LLFuncArg) el;
//                scopes.add(currentScope); //so a new (non-initial) scope always begins with a field
//                //TODO: what is this???
            } else {
                DeliaExceptionHelper.throwNotImplementedError("unknown mem fn '%s'", el.getClass().getSimpleName());
            }
        }

        int index = 0;
        for (QScope scope : scopes) {
            reOrderFunctionsInScope(scope, index == scopes.size() - 1, ctx);
            index++;
        }

        return scopes;
    }

    private void reOrderFunctionsInScope(QScope scope, boolean isLast, SelectDBContext ctx) {
        List<LLD.LLEx> reorderedList = new ArrayList<>();
        List<LLD.LLEx> scalarList = new ArrayList<>();

        //NO //group A: distinct
        //findAndAddFunc(scope, reorderedList, "distinct");

        //group B: orderBy,offset,limit,first,last,ith
        findAndAddFunc(scope, reorderedList, "orderBy", "offset", "limit", "first", "last", "ith");

        //group C1: count,exist
        findAndAddFunc(scope, reorderedList, "count", "exists");
        //group C3: fetch
        int addedFetchCount = findAndAddFunc(scope, reorderedList, "fetch");

        //group C2: min,max,avg. must do _after_ scalar field
        //distinct must be before others
        findAndAddFunc(scope, scalarList, "distinct", "min", "max", "avg");

        //group D: fks
        //In MEM all dvalues already have fks in the mem tbls
        //So we add fksFunction which removes fks if needed
        int extra = addIfMissing(scope, reorderedList, "fks", isLast && ctx.enableRemoveFks, addedFetchCount > 0);

        int numFuncs = scope.funcL.size();
        if (numFuncs > 0 && reorderedList.size() + scalarList.size() - extra != numFuncs) {
            DeliaExceptionHelper.throwNotImplementedError("reOrderFunctionsInScope failed!");
        }
        scope.funcL = reorderedList;
        scope.scalarFuncL = scalarList;
    }

    private int addIfMissing(QScope scope, List<LLD.LLEx> reorderedList, String funcName, boolean isLast, boolean haveAddedFetch) {
        LLD.LLDFuncEx func = LLFieldHelper.findFunc(scope.funcL, funcName);
        int extra = 0;
        if (func == null && isLast && !haveAddedFetch) {
            func = new LLD.LLDFuncEx("remove-fks");
            extra = 1;
        }

        if (func != null) {
            reorderedList.add(func);
        }
        return extra;
    }

    private int findAndAddFunc(QScope scope, List<LLD.LLEx> reorderedList, String... funcNames) {
        int count = 0;
        for (String funcName : funcNames) {
            LLD.LLDFuncEx func = LLFieldHelper.findFunc(scope.funcL, funcName);
            if (func != null) {
                reorderedList.add(func);
                count++;
            }
        }
        return count;
    }

}
