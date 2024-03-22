package org.delia.lld.processor;

import org.delia.hld.dat.AssocSpec;
import org.delia.lld.LLD;
import org.delia.relation.RelationInfo;
import org.delia.type.*;
import org.delia.util.DRuleHelper;
import org.delia.util.DValueHelper;

public class LLDUtils {

    public static boolean isAssocField(LLD.LLField fld) {
        if (fld.physicalPair.type.isStructShape()) {
            RelationInfo relinfo = DRuleHelper.findMatchingRuleInfo(fld.physicalTable.physicalType, fld.physicalPair.name);
            return relinfo.isManyToMany();
        }
        return false;
    }

    //TODO: we lose dstruct instance comparison. will need compare by .getName()
    public static DStructType createDATType(DTypeRegistry registry, AssocSpec assoc) {
        TypePair pkvalLeft = DValueHelper.findPrimaryKeyFieldPair(assoc.leftType);
        TypePair pkvalRight = DValueHelper.findPrimaryKeyFieldPair(assoc.rightType);

        OrderedMap omap = new OrderedMap();
        omap.add(assoc.leftColumn, pkvalLeft.type, false, false, false, false, null);
        omap.add(assoc.rightColumn, pkvalRight.type, false, false, false, false, null);
        String schema = null; //TODO this should be same schema as leftv,right??
        DStructType dtype = new DStructTypeImpl(Shape.STRUCT, schema, assoc.assocTblName, null, omap, null);
        return dtype;
    }
}
