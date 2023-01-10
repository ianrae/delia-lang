package org.delia.migrationparser.parser.ast;

import org.delia.migration.action.MigrationActionBase;
import org.delia.migrationparser.MigrationContext;
import org.delia.type.DTypeRegistry;

public interface AST {
    void applyMigration(DTypeRegistry registry, MigrationContext ctx);

    MigrationActionBase generateAction();
}
