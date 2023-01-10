package org.delia.migration;

import org.delia.DeliaSession;
import org.delia.migration.action.MigrationActionBase;
import org.delia.type.DTypeRegistry;

import java.util.ArrayList;
import java.util.List;

public class SchemaMigration {
    public DeliaSession sess; //mainly for registry. is just a temp MEM session just to get the registry and DatIdMap
    public List<MigrationActionBase> actions = new ArrayList<>(); //usually alterations then additions (CreateTableAction) at end

    DTypeRegistry getRegistry() {
        return sess.getRegistry();
    }

    public void addAction(MigrationActionBase action) {
        actions.add(action);
    }
}
