package org.delia.migrationparser.parser.ast;

import org.delia.migration.action.MigrationActionBase;
import org.delia.migration.action.RenameFieldAction;
import org.delia.migrationparser.MigrationContext;
import org.delia.migrationparser.MigrationField;
import org.delia.type.DStructType;
import org.delia.type.TypePair;

import java.util.List;

public class RenameFieldAST extends AlterFieldASTBase {
    public String newFieldName;
    private RenameFieldAction action;

    public RenameFieldAST(String typeName, String fieldName, String newName) {
        super(typeName, fieldName);
        this.newFieldName = newName;
    }

    @Override
    protected String getActionName() {
        return "RenameField";
    }

    @Override
    protected void onMigrateField(DStructType structType, TypePair pair, MigrationContext ctx) {
        String oldName = pair.name;
//        pair.name = newFieldName;
        this.action = new RenameFieldAction(structType);
        this.action.fieldName = oldName;
        this.action.newName = newFieldName;

//        String key = String.format("%s:%s", structType.getTypeName().toString(), newFieldName);
//        ctx.renamedFieldMap.put(key, oldName);

        MigrationField mf = createMigrationField(structType);
        TypePair newPair = new TypePair(newFieldName, pair.type);
        ctx.migrationFieldResult.applyRename(mf, newPair);
    }

    @Override
    public MigrationActionBase generateAction() {
        return action;
    }
}
