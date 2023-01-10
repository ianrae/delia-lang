package org.delia.migration.action;

import org.delia.relation.RelationInfo;
import org.delia.type.DStructType;
import org.delia.type.TypePair;
import org.delia.util.DRuleHelper;
import org.delia.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

public class CreateTableAction extends MigrationActionBase {
    public List<String> fields; //fields in structtype to create column for
    public boolean isAssocTbl;

    public CreateTableAction(DStructType structType) {
        super(structType);

        fields = new ArrayList<>();
        for(TypePair pair: structType.getAllFields()) {
            boolean skip = false;
            if (pair.type.isStructShape()) {
                RelationInfo relinfo = DRuleHelper.findMatchingRuleInfo(structType, pair);
                if (relinfo != null && relinfo.isManyToMany() || relinfo.isParent) {
                    skip = true;
                }
            }

            if (!skip) {
                fields.add(pair.name);
            }
        }
    }

    @Override
    public String toString() {
        String fieldStr = fields == null ? "" : StringUtil.flatten(fields);
        String str = structType.getTypeName().toString();
        return String.format("+TBL(%s):%s", str, fieldStr);
    }
}
