package org.delia.db.schema.modify;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class SchemaDefinition {
	public int ver;
	public List<SxTypeInfo> types = new ArrayList<>();
	public List<SxOtherInfo> others = new ArrayList<>();
	
	@Override
	public String toString() {
		StringJoiner joiner = new StringJoiner(",");
		for(SxTypeInfo info: types) {
			joiner.add(info.nm);
		}
		return joiner.toString();
	}
}