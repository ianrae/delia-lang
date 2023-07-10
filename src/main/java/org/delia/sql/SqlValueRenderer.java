package org.delia.sql;

import org.delia.compiler.ast.Exp;
import org.delia.core.DateFormatService;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.dval.DeferredValueService;
import org.delia.type.*;
import org.delia.util.DValueHelper;
import org.delia.util.DeliaExceptionHelper;
import org.delia.valuebuilder.ScalarValueBuilder;
import org.delia.varevaluator.VarEvaluator;

import java.time.ZonedDateTime;

public class SqlValueRenderer extends ServiceBase {
    private final DateFormatService dateFormatSvc;
    private final DeferredValueService deferredValueService;
    private final VarEvaluator varEvaluator;

    public SqlValueRenderer(FactoryService factorySvc, VarEvaluator varEvaluator) {
        super(factorySvc);
        this.dateFormatSvc = factorySvc.getDateFormatService();
        this.deferredValueService = new DeferredValueService(factorySvc);
        this.varEvaluator = varEvaluator;
    }

    public String opToSql(String op) {
        switch (op) {
            case "==":
                return "=";
            case "!=":
                return "<>";
            case "like":
                return "LIKE";
            default:
                return op;
        }
    }

    public String renderAsSql(Exp.ValueExp vexp) {
        DValue dval = vexp.value;
        return renderAsSql(dval, dval.getType(), null);
    }

    public String renderAsSql(DValue dval, DType dtype, DStructType parentType) {
        if (dval == null) {
            return "null";
        }
        switch (dtype.getShape()) {
            case BOOLEAN:
            case INTEGER:
//            case LONG:
            case NUMBER:
                return dval.asString();
            case STRING:
                return String.format("'%s'", dval.asString());
            case DATE:
                return String.format("'%s'", renderDate(dval));
            case STRUCT:
            {
                TypePair pkpair = DValueHelper.findPrimaryKeyFieldPair(parentType);
                return renderAsSql(dval, pkpair.type, null); //** recursion **
            }

//            case SYMBOL:
//                return renderTerm(val1.alias, val1.structField.fieldName);
//            case SYMBOLCHAIN:
//            {
//                SymbolChain chain = val1.asSymbolChain();
//                if (chain.el != null && chain.el.aliasNameAdditional != null) {
//                    return renderTerm(chain.el.aliasNameAdditional, chain.list.get(0)); //TODO: later support list > 1
//                } else {
//                    return renderTerm(val1.alias, chain.list.get(0)); //TODO: later support list > 1
//                }
//            }
//            case FUNCTION:
//            {
//                FilterFunc fn = val1.asFunc();
//                return genFnSQL(val1, fn);
//            }
            default:
                DeliaExceptionHelper.throwError("render-val-failed", "renderVal not impl1", "?");
        }
        return null;
    }

    private String renderDate(DValue dval) {
        //1999-01-08 04:05:06
        if (dval.getType().isShape(Shape.STRING)) {
            ZonedDateTime zdt = dateFormatSvc.parseDateTime(dval.asString());
            return dateFormatSvc.format(zdt);
        }
        return dateFormatSvc.format(dval.asDate());
    }
    private DValue renderDateParam(DValue dval, ScalarValueBuilder valueBuilder) {
        //this is a bit messy. but we were resolving deferred values in OuterRunner which won't
        //work if var is a Date because we return a different dval here.
        //So we need to resolve the var here
        //deferredValueService.resolveSingleDeferredVar(dval, valueBuilder, varEvaluator);
        //1999-01-08 04:05:06
        if (dval.getType().isShape(Shape.STRING)) {
            DValue nval = valueBuilder.buildDate(dval.asString());
//            ZonedDateTime zdt = dateFormatSvc.parseDateTime(dval.asString());
//            String s = dateFormatSvc.format(zdt);
//            return valueBuilder.buildString(s);
            return nval;
        }
        return dval;
    }

    public DValue noRenderSqlParam(DValue dval, DType dtype, ScalarValueBuilder valueBuilder) {
        return dval;
    }
    public DValue actualRenderSqlParam(DValue dval, DType dtype, ScalarValueBuilder valueBuilder) {
        if (dval == null) {
            return null;
        }
        if (dtype == null) {
            dtype = dval.getType();
        }

        switch (dtype.getShape()) {
            case BOOLEAN:
            case INTEGER:
//            case LONG:
            case NUMBER:
            case STRING:
            case STRUCT:
            case BLOB:
                return dval;
            case DATE:
                return renderDateParam(dval, valueBuilder);

//            case SYMBOL:
//                return renderTerm(val1.alias, val1.structField.fieldName);
//            case SYMBOLCHAIN:
//            {
//                SymbolChain chain = val1.asSymbolChain();
//                if (chain.el != null && chain.el.aliasNameAdditional != null) {
//                    return renderTerm(chain.el.aliasNameAdditional, chain.list.get(0)); //TODO: later support list > 1
//                } else {
//                    return renderTerm(val1.alias, chain.list.get(0)); //TODO: later support list > 1
//                }
//            }
//            case FUNCTION:
//            {
//                FilterFunc fn = val1.asFunc();
//                return genFnSQL(val1, fn);
//            }
            default:
                DeliaExceptionHelper.throwError("render-val-param-failed", "renderVal not impl1", "?");
        }
        return null;
    }
}
