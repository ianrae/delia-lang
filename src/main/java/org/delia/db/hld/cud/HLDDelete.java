package org.delia.db.hld.cud;

import org.delia.db.hld.HLDQuery;
import org.delia.type.DStructType;
import org.delia.type.DValue;

public class HLDDelete extends HLDBase {
	public HLDQuery hld;
	public boolean useDeleteIn;
	public DValue deleteInDVal;
	public String mergeKey;
	public String mergeType;
	public String mergePKField;
	public String mergeOtherKey;
	
	public HLDDelete(HLDQuery hld) {
		super(new TypeOrTable(hld.fromType));
		this.hld = hld;
	}
	public HLDDelete(DStructType structType) {
		super(new TypeOrTable(structType));
	}
	public HLDDelete(String tblName, boolean isAssocTbl) {
		super(new TypeOrTable(tblName, isAssocTbl));
	}

	@Override
	public String toString() {
		return hld.toString();
	}
}