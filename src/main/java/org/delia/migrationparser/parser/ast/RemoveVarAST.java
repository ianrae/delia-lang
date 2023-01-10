package org.delia.migrationparser.parser.ast;

import org.delia.migration.action.MigrationActionBase;
import org.delia.migrationparser.MigrationContext;
import org.delia.type.DTypeRegistry;

public class RemoveVarAST extends BaseAST {
    public RemoveVarAST(String varName) {
        this.target = varName;
    }

    @Override
    public void applyMigration(DTypeRegistry registry, MigrationContext ctx) {
        //TODO
    }

    @Override
    public MigrationActionBase generateAction() {
        return null;
    }
}
