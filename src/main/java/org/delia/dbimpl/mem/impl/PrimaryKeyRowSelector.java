package org.delia.dbimpl.mem.impl;

import java.util.ArrayList;
import java.util.List;

import org.delia.error.ErrorTracker;
import org.delia.tok.Tok;
import org.delia.type.DStructType;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.type.TypePair;
import org.delia.util.DValueHelper;

public class PrimaryKeyRowSelector extends RowSelectorBase {
    protected FilterEvaluator evaluator;

    public PrimaryKeyRowSelector(FilterEvaluator evaluator) {
        super();
        this.evaluator = evaluator;
    }

    @Override
    public void init(ErrorTracker et, Tok.WhereTok whereClause, DStructType dtype, DTypeRegistry registry) {
        super.init(et, whereClause, dtype, registry);
        evaluator.init(whereClause);

        doInnerInit();
    }

    protected void doInnerInit() {
        TypePair pair = DValueHelper.findPrimaryKeyFieldPair(dtype);
        this.keyField = findKeyField(pair);
        if (this.keyField == null) {
            //err!!
            et.add("struct-missing-primary-key-field", "struct needs a unique or primaryKey field");
            wasError = true;
        }

        if (! keyFieldIsAllowedType(pair)) {
            //err!!
            String msg = String.format("type '%s' not allowed as primary key", pair.type.getName());
            et.add("struct-primary-key-field-wrong-type", msg);
            wasError = true;
        }
    }

    protected boolean keyFieldIsAllowedType(TypePair pair) {
        switch(pair.type.getShape()) {
            case INTEGER:
//            case LONG:
            case BOOLEAN:
            case STRING:
            case DATE:
                return true;
            case NUMBER:
            case STRUCT:
            case RELATION:
            default:
                return false;
        }
    }

    @Override
    public List<DValue> match(MemDBTable tbl) {
        List<DValue> list = tbl.getList();
        if (keyField == null) {
            wasError = true;
            //err!!
            return null;
        } else {
            List<DValue> resultL = new ArrayList<>();
            if (tbl.needsSynchronizationOnTraverse()) {
                synchronized (list) {
                    return traverseList(list, resultL);
                }
            } else {
                return traverseList(list, resultL);
            }
        }
    }

    protected List<DValue> traverseList(List<DValue> list, List<DValue> resultL) {
        for(DValue dval: list) {
            DValue key = dval.asStruct().getField(keyField);
            if (key == null) {
                wasError = true;
                //err!!
                return resultL;
            }

            Object target = whereClause.strValue();
            if (evaluator.isEqualTo(key, target)) {
                resultL.add(dval); //only one row
                break;
            }
        }
        return resultL;
    }

    protected String findKeyField(TypePair pair) {
        if (pair == null) {
            return null;
        } else {
            return pair.name;
        }
    }
}