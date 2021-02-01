package org.delia.db.schema.modify;

import java.util.ArrayList;
import java.util.List;

public class SxTypeDelta {
	public String typeName; 
	public String nmDelta; //null means no change. else is rename
	public String baDelta; //""
	public List<SxFieldDelta> fldsI = new ArrayList<>();
	public List<SxFieldDelta> fldsU = new ArrayList<>();
	public List<SxFieldDelta> fldsD = new ArrayList<>();
	public SxTypeInfo info; //original
	
	public SxTypeDelta(String typeName) {
		this.typeName = typeName;
	}
}