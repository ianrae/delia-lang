package org.delia.migration.action;

import org.delia.migrationparser.RelationDetails;
import org.delia.type.BuiltInTypes;
import org.delia.type.DStructType;
import org.delia.type.DType;
import org.delia.util.DTypeNameUtil;

public class AddFieldAction extends MigrationActionBase {
    public String fieldName;
    public String changeFlags; //eg +O+U
    public DType type;
    public int sizeOf;
    //relation flags
    public boolean isParent;
    public boolean isOne;
    public boolean isMany;

    public AddFieldAction(DStructType structType) {
        super(structType);
    }

    @Override
    public String toString() {
        String typeStr = type == null ? "" : BuiltInTypes.convertDTypeNameToDeliaName(type.getName());
        String str = structType.getTypeName().toString();
        String relFlags = String.format("%s%s%s", boolToStr(isOne), boolToStr(isMany), boolToStr(isParent));
        return String.format("+FLD(%s.%s):%s:%s:%s:%d", str, fieldName, changeFlags, relFlags, typeStr, sizeOf);
    }

    public void setRelationFlags(boolean isParent, boolean isOne, boolean isMany) {
        this.isParent = isParent;
        this.isOne = isOne;
        this.isMany = isMany;
    }

    public RelationDetails buildDetails() {
        return new RelationDetails(isParent, isOne, isMany);
    }

    protected Object boolToStr(boolean b) {
        return b ? "Y" : "N";
    }
}
