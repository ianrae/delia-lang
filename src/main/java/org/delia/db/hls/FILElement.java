package org.delia.db.hls;

import org.delia.compiler.ast.QueryExp;

public class FILElement implements HLSElement {
	public QueryExp queryExp;

	public FILElement(QueryExp queryExp) {
		this.queryExp = queryExp;
	}
	
	public boolean isAll() {
		return queryExp.filter.cond.equals("true");
	}

	@Override
	public String toString() {
		String s = queryExp.toString();
		int pos = s.indexOf('[');
		s = pos < 0 ? s : s.substring(pos);
		pos = s.indexOf(']');
		s = pos < 0 ? s : s.substring(0, pos+1);
		
		return "" + s;
	}
	
}