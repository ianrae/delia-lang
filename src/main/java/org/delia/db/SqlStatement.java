package org.delia.db;

import java.util.ArrayList;
import java.util.List;

import org.delia.type.DValue;

public class SqlStatement {
	/**
	 * Statement or Simple object that this is the SQL of.
	 * Used to relate statements in an SqlStatementGroup back to the HLD object that
	 * they represent.
	 * 
	 * Note. owner can be null. Usually is null when doing low-level SQL operations.
	 */
	public Object owner; 
	public String sql;
	public List<DValue> paramL = new ArrayList<>();
	
	public SqlStatement() {
		this.owner = null;
	}
	public SqlStatement(Object owner) {
		this.owner = owner;
	}

}
