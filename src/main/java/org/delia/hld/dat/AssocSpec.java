package org.delia.hld.dat;

import org.delia.relation.RelationInfo;
import org.delia.type.DStructType;
import org.delia.util.DeliaExceptionHelper;

public class AssocSpec {
    //left and right are reserved words in some sql databases so we use leftv,rightv
    public static final String ASSOC_LEFT_FIELDNAME = "leftv";
    public static final String ASSOC_RIGHT_FIELDNAME = "rightv";

    public int datId;
    public DStructType leftType;
    public DStructType rightType;
    public String deliaLeftv; //name of field in delia type, eg 'addr'
    public String deliaRightv; //hmm. tends to be same value as deliaLeftv. why?
    public String otherSideFieldName;
    public String assocTblName;
    public String leftColumn; //name of column in assoc table
    public String rightColumn;
    public String relationName; //can be null

    public boolean isFlipped(RelationInfo relinfo) {
        return relinfo.nearType.equals(rightType);
    }

    public DStructType getTypeForField(String fieldName) {
        if (ASSOC_LEFT_FIELDNAME.equals(fieldName)) {
            return leftType;
        } else if (ASSOC_RIGHT_FIELDNAME.equals(fieldName)) {
            return rightType;
        } else {
            DeliaExceptionHelper.throwError("bad-assoc-field-name", "unknown assoc field '%s'", fieldName);
            return null;
        }
    }
//    public String getRelationFieldForAssocField(String fieldName) {
//        if (ASSOC_LEFT_FIELDNAME.equals(fieldName)) {
//            return deliaLeftv;
//        } else if (ASSOC_RIGHT_FIELDNAME.equals(fieldName)) {
//            return deliaRightv;
//        } else {
//            DeliaExceptionHelper.throwError("bad-assoc-field-name", "unknown assoc field '%s'", fieldName);
//            return null;
//        }
//    }
    /* Note on reverse methods. Given a query like this:
       SELECT a.id, b.rightv FROM customer as a LEFT JOIN customeraddressdat1 as b ON a.id=b.leftv WHERE a.id = 55
       The columnRun field (rff) has rightv. That associates with Address. But we're loading a Customer object
       so we want the other side of the relation (the Customer side).
     */

    public DStructType getReverseTypeForField(String fieldName) {
        if (ASSOC_LEFT_FIELDNAME.equals(fieldName)) {
            return rightType;
        } else if (ASSOC_RIGHT_FIELDNAME.equals(fieldName)) {
            return leftType;
        } else {
            DeliaExceptionHelper.throwError("bad-assoc-field-name", "unknown assoc field '%s'", fieldName);
            return null;
        }
    }
    public String getReverseRelationFieldForAssocField(String fieldName) {
        if (ASSOC_LEFT_FIELDNAME.equals(fieldName)) {
            return deliaRightv;
        } else if (ASSOC_RIGHT_FIELDNAME.equals(fieldName)) {
            return deliaLeftv;
        } else {
            DeliaExceptionHelper.throwError("bad-assoc-field-name", "unknown assoc field '%s'", fieldName);
            return null;
        }
    }

}
