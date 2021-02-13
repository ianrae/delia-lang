package org.delia.db.schema.modify;

import java.util.List;

public class SxMigrationPlan {
	public List<SchemaChangeOperation> opList;
	public SchemaDelta delta;
}
