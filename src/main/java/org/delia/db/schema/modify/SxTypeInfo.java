package org.delia.db.schema.modify;

import java.util.ArrayList;
import java.util.List;

public class SxTypeInfo {
	public String nm;
	public String ba;
	public List<SxFieldInfo> flds = new ArrayList<>();
	
	@Override
	public String toString() {
		return nm;
	}
	
}