package org.delia.bddnew.core;

import org.delia.db.DBType;
import org.delia.db.sql.ConnectionDefinition;

public interface ConnectionProvider {
    ConnectionDefinition getConnectionDef();

    //    DBSchemaBuilder getSchemaBuilder(DeliaSession sess, Log log);
    DBType getDBType();
}
