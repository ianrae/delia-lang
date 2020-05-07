package org.delia.zqueryresponse;

import java.util.ArrayList;
import java.util.List;

import org.delia.compiler.ast.QueryFuncExp;
import org.delia.runner.QueryResponse;
import org.delia.type.DStructType;
import org.delia.type.DType;

public class LetSpan {
	public DStructType structType;
	public List<QueryFuncExp> qfeL = new ArrayList<>();
	public QueryResponse qresp;
	
	public LetSpan(DType dtype) {
		this.structType = (DStructType) dtype;
	}
}