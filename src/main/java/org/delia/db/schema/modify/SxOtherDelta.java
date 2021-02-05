package org.delia.db.schema.modify;

import java.util.List;

public class SxOtherDelta {
	public String typeName;
	public String name; //name of constraint or index
	public List<String> newArgs;
	public SxOtherInfo info;
	
	public SxOtherDelta(String name) {
		this.name = name;
	}
}