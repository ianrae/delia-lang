package org.delia.db.schema.modify;

public class SxFieldDelta {
	public String fieldName;
	public String fDelta; //null means no change. else is rename
	public String tDelta; //""
	public String flgsDelta; //""
	public Integer szDelta; //null means no change, else is new size
	//		public int datId;  never changes
	public SxFieldInfo info; //when adding
	public String typeNamex;
	public boolean canCreateAssocTable; //true for 2nd one

	public SxFieldDelta(String fieldName, String typeName) {
		this.fieldName = fieldName;
		this.typeNamex = typeName;
	}
	
	@Override
	public String toString() {
		return String.format("%s.%s fld:%s sz:%d", typeNamex, fieldName, flgsDelta, szDelta);
	}
	
}