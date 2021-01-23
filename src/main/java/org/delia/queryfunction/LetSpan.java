package org.delia.queryfunction;

import java.util.ArrayList;
import java.util.List;

import org.delia.compiler.ast.QueryFuncExp;
import org.delia.runner.QueryResponse;
import org.delia.type.DType;

public class LetSpan {
	public DType dtype;
	public List<QueryFuncExp> qfeL = new ArrayList<>();
	public QueryResponse qresp;
	public boolean startsWithScopeChange;
	
	public LetSpan(DType dtype) {
		this.dtype = dtype;
	}
}