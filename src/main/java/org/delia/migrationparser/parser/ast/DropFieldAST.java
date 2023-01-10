package org.delia.migrationparser.parser.ast;

import org.delia.migration.action.MigrationActionBase;
import org.delia.migration.action.RemoveFieldAction;
import org.delia.migrationparser.MigrationContext;
import org.delia.migrationparser.MigrationField;
import org.delia.relation.RelationInfo;
import org.delia.type.DStructType;
import org.delia.type.TypePair;
import org.delia.util.DRuleHelper;

import java.util.List;

public class DropFieldAST extends AlterFieldASTBase {
    private RemoveFieldAction action;

    public DropFieldAST(String typeName, String fieldName) {
        super(typeName, fieldName);
    }

    @Override
    protected String getActionName() {
        return "DropField";
    }

    @Override
    protected void onMigrateField(DStructType structType, TypePair pair, MigrationContext ctx) {
//        pairs.remove(pair);
        this.action = new RemoveFieldAction(structType);
        action.fieldName = pair.name;
        action.isPhysicalField = isPhysicalField(structType, pair);

        MigrationField mf = createMigrationField(structType);
        ctx.migrationFieldResult.applyDelete(mf, pair);
    }

    private boolean isPhysicalField(DStructType structType, TypePair pair) {
        if (pair.type.isStructShape()) {
            RelationInfo relinfo = DRuleHelper.findMatchingRuleInfo(structType, pair.name);
            if (relinfo.isParent || relinfo.isManyToMany()) {
            } else {
                return true;
            }
        } else {
            return true;
        }
        return false;
    }


    @Override
    public MigrationActionBase generateAction() {
        return action;
    }
}
