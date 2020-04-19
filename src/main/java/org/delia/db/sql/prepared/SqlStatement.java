package org.delia.db.sql.prepared;

import java.util.ArrayList;
import java.util.List;

import org.delia.type.DValue;

public class SqlStatement {
	
	public String sql;
	public List<DValue> paramL = new ArrayList<>();

}
