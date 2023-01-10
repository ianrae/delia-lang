package org.delia.migration.action;

import org.delia.type.BuiltInTypes;
import org.delia.type.DStructType;

public class AlterFieldAction extends AddFieldAction {
    //changeFlags, type, sizeof can be null
    public boolean isAssocTbl;

    public AlterFieldAction(DStructType structType) {
        super(structType);
    }

    @Override
    public String toString() {
        String typeStr = type == null ? "" : BuiltInTypes.convertDTypeNameToDeliaName(type.getName());
        String str = structType.getTypeName().toString();
        String relFlags = String.format("%s%s%s", boolToStr(isOne), boolToStr(isMany), boolToStr(isParent));
        return String.format("mFLD(%s.%s):%s:%s:%s:%d", str, fieldName, changeFlags, relFlags, typeStr, sizeOf);
    }
}
