package org.delia.db.schema;

import java.util.ArrayList;
import java.util.List;

public class MigrationPlan {
	public List<SchemaType> diffL;
	public List<SchemaChangeAction> changeActionL = new ArrayList<>();
	public boolean runResultFlag;

}
