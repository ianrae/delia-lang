package org.delia.db.schema.modify;

import java.util.ArrayList;
import java.util.List;

public class SchemaDefinition {
	public int ver;
	public List<SxTypeInfo> types = new ArrayList<>();
	public List<SxOtherInfo> others = new ArrayList<>();
}