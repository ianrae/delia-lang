package org.delia.dbimpl.mem.impl;

import org.delia.tok.Tok;
import org.delia.type.DValue;
import org.delia.type.PrimaryKey;
import org.delia.type.TypePair;
import org.delia.util.DValueHelper;

import java.util.List;

public class CompositePrimaryKeyRowSelector extends PrimaryKeyRowSelector {

    public CompositePrimaryKeyRowSelector(FilterEvaluator evaluator) {
        super(evaluator);
    }

    @Override
    protected void doInnerInit() {
        Tok.PKWhereTok pkWhereTok = (Tok.PKWhereTok) whereClause.where;
        PrimaryKey primaryKey = pkWhereTok.primaryKey;

        TypePair pair = DValueHelper.findPrimaryKeyFieldPair(dtype);
        this.keyField = findKeyField(pair);
        if (this.keyField == null) {
            //err!!
            et.add("struct-missing-primary-key-field", "struct needs a unique or primaryKey field");
            wasError = true;
        }

        if (!keyFieldIsAllowedType(pair)) {
            //err!!
            String msg = String.format("type '%s' not allowed as primary key", pair.type.getName());
            et.add("struct-primary-key-field-wrong-type", msg);
            wasError = true;
        }
    }

    @Override
    protected List<DValue> traverseList(List<DValue> list, List<DValue> resultL) {
        for (DValue dval : list) {
            DValue key = dval.asStruct().getField(keyField);
            if (key == null) {
                wasError = true;
                //err!!
                return resultL;
            }

            if (evaluator.isEqualTo(key)) {
                resultL.add(dval); //only one row
                break;
            }
        }
        return resultL;
    }
}