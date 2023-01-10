package org.delia.migrationparser.parser.ast;

import org.delia.migration.action.MigrationActionBase;
import org.delia.migration.action.RenameTableAction;
import org.delia.migrationparser.MigrationContext;
import org.delia.type.DStructType;
import org.delia.type.DTypeRegistry;

public class RenameTypeAST extends BaseAST {
    public String newName;
    private RenameTableAction action;

    public RenameTypeAST(String typeName, String newName) {
        this.target = typeName;
        this.newName = newName;
    }

    @Override
    public void applyMigration(DTypeRegistry registry, MigrationContext ctx) {
        //TODO support schema
        //TODO check if not found
        DStructType structType = ASTHelper.findType(registry, target);
        if (structType != null) {
            ctx.renamedTypeMap.put(structType.getName(), newName);
            this.action = new RenameTableAction(structType);
            this.action.newName = newName;
        }
    }

    @Override
    public MigrationActionBase generateAction() {
        return action;
    }
}
