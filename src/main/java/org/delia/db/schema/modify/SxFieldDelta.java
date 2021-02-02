package org.delia.db.schema.modify;

import org.delia.db.SqlStatement;

public class SxFieldDelta {
	public String fieldName;
	public String fDelta; //null means no change. else is rename
	public String tDelta; //""
	public String flgsDelta; //""
	public Integer szDelta; //null means no change, else is new size
	public SxFieldInfo info; //when adding
	public String typeNamex;
	public boolean canCreateAssocTable; //true for 2nd one (when adding field)
	public SqlStatement assocUpdateStm; //only used for rename MM field

	public SxFieldDelta(String fieldName, String typeName) {
		this.fieldName = fieldName;
		this.typeNamex = typeName;
	}
	
	@Override
	public String toString() {
		return String.format("%s.%s fld:%s sz:%d", typeNamex, fieldName, flgsDelta, szDelta);
	}
	
}