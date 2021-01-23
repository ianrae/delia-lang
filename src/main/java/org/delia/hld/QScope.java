package org.delia.hld;

import org.delia.compiler.ast.QueryFuncExp;
import org.delia.type.DStructType;

public class QScope {
	public int index;
	public DStructType structType;
	public String fieldName;
	public QueryFuncExp qfnexp;
	public Object thing; //some hld thing
	
	public QScope(QueryFuncExp qfnexp) {
		this.qfnexp = qfnexp;
	}
	
	public void setDetails(DStructType dtype, String fieldName, Object thing) {
		this.structType = dtype;
		this.fieldName = fieldName;
		this.thing = thing;
	}

	@Override
	public String toString() {
		if (qfnexp != null) {
			return String.format("%d: %s", index, qfnexp.funcName);
		} else {
			return String.format("%d: %s.%s", index, structType.getName(), fieldName);
		}
	}
}
