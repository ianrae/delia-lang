package org.delia.db.hls;

import org.delia.type.DStructType;
import org.delia.type.TypePair;

public class RenderedField {
	public String field;
	public TypePair pair;
	public boolean isAssocField;
	public DStructType structType;
	
	public int columnIndex; //used when reading resultset
}
