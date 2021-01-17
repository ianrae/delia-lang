package org.delia.db.newhls;

import org.delia.compiler.ast.QueryFuncExp;
import org.delia.type.DStructType;

public class QScope {
	public int index;
	public DStructType structType;
	public String fieldName;
	public QueryFuncExp qfnexp;
	
	public QScope(QueryFuncExp qfnexp) {
		this.qfnexp = qfnexp;
	}
	
	public void setDetails(DStructType dtype, String fieldName) {
		this.structType = dtype;
		this.fieldName = fieldName;
	}
}
