package org.delia.migrationparser.parser.ast;

import org.delia.migration.action.DeleteTableAction;
import org.delia.migration.action.MigrationActionBase;
import org.delia.migrationparser.MigrationContext;
import org.delia.type.DStructType;
import org.delia.type.DTypeRegistry;

public class DropTypeAST extends BaseAST {
    private DeleteTableAction action;

    public DropTypeAST(String typeName) {
        this.target = typeName;
    }

    @Override
    public void applyMigration(DTypeRegistry registry, MigrationContext ctx) {
        //TODO support schema
        //TODO check if not found
        DStructType structType = ASTHelper.findType(registry, target);
        if (structType != null) {
            ctx.doomedL.add(structType);
            this.action = new DeleteTableAction(structType);
        }
    }

    @Override
    public MigrationActionBase generateAction() {
        return action;
    }
}
