package org.delia.migration;

import org.delia.relation.RelationInfo;
import org.delia.type.DStructType;
import org.delia.type.TypePair;

public class ManyToManyInfo {
    TypePair pair;
    RelationInfo relinfo;

    public ManyToManyInfo(TypePair pair, RelationInfo relinfo) {
        this.pair = pair;
        this.relinfo = relinfo;
    }

    public String makeKey() {
        return String.format("%s.%s", relinfo.nearType.getTypeName().toString(), relinfo.fieldName);
    }

    public String makeKey2() {
        if (relinfo.otherSide == null) return null;
        return String.format("%s.%s", relinfo.farType.getTypeName().toString(), relinfo.otherSide.fieldName);
    }

    public boolean isLeftTable(DStructType structType) {
        String name1 = relinfo.nearType.getTypeName().toString();
        String name2 = structType.getTypeName().toString();
        return name1.equals(name2);
    }
}
