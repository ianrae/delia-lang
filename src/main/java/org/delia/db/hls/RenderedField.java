package org.delia.db.hls;

import org.delia.db.hls.join.FieldGroup;
import org.delia.type.DStructType;
import org.delia.type.TypePair;

public class RenderedField {
	public String field;
	public TypePair pair;
	public boolean isAssocField;
	public DStructType structType;
	public FieldGroup fieldGroup;
	
	public int columnIndex; //used when reading resultset
	
	public RenderedField() {
	}
	public RenderedField(RenderedField src) {
		this.field = src.field;
		this.pair = new TypePair(src.pair.name, src.pair.type);
		this.isAssocField = src.isAssocField;
		this.structType = src.structType;
		this.columnIndex = src.columnIndex;
	}
}
