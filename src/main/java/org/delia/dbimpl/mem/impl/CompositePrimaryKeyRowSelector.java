package org.delia.dbimpl.mem.impl;

import org.delia.tok.Tok;
import org.delia.type.DValue;
import org.delia.type.PrimaryKey;
import org.delia.type.TypePair;

import java.util.ArrayList;
import java.util.List;

public class CompositePrimaryKeyRowSelector extends PrimaryKeyRowSelector {
    private PrimaryKey primaryKey;
    private List<String> compositeKeyFields = new ArrayList<>();

    public CompositePrimaryKeyRowSelector(FilterEvaluator evaluator) {
        super(evaluator);
    }

    @Override
    protected void doInnerInit() {
        Tok.PKWhereTok pkWhereTok = (Tok.PKWhereTok) whereClause.where;
        primaryKey = pkWhereTok.primaryKey;

        //Note we set keyField here just for error tracking.  compositeKeyFields is where we have the multiple keys
        for (TypePair pair : primaryKey.getKeys()) {
            this.keyField = findKeyField(pair);
            compositeKeyFields.add(this.keyField);
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
    }

    @Override
    protected List<DValue> traverseList(List<DValue> list, List<DValue> resultL) {
        Tok.PKWhereTok pkWhereTok = (Tok.PKWhereTok) whereClause.where;
        for (DValue dval : list) {
            if (isMatch(dval, pkWhereTok)) {
                resultL.add(dval); //only one row
                break;
            }
        }
        return resultL;
    }

    private boolean isMatch(DValue dval, Tok.PKWhereTok pkWhereTok) {
        //do all composite fields, in order
        int keyIndex = 0;
        int matchCount = 0;
        for (String fieldName : compositeKeyFields) {
            DValue key = dval.asStruct().getField(fieldName);
            if (key == null) {
                wasError = true;
                //err!!
                return false;
            }

            String target = pkWhereTok.listValue.listL.get(keyIndex++).strValue();
            if (evaluator.isEqualTo(key, target)) {
                matchCount++;
            } else {
                break; //not a match - no need to check further
            }
        }
        return matchCount == compositeKeyFields.size();
    }
}