package org.delia.db.hls;

import org.delia.compiler.ast.QueryFuncExp;

public class GElement implements HLSElement {
	public QueryFuncExp qfe;

	public GElement(QueryFuncExp qfe) {
		this.qfe = qfe;
	}

	@Override
	public String toString() {
		String s = String.format("%s", qfe.funcName);
		return s;
	}
	
}