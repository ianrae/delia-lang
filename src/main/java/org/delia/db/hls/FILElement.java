package org.delia.db.hls;

import org.delia.compiler.ast.FilterOpFullExp;
import org.delia.compiler.ast.QueryExp;
import org.delia.compiler.ast.QueryInExp;

public class FILElement implements HLSElement {
	public QueryExp queryExp;

	public FILElement(QueryExp queryExp) {
		this.queryExp = queryExp;
	}
	
	public boolean isAll() {
		String s = queryExp.filter.cond.strValue();
		return s.equals("true");
	}
	public QueryInExp getAsInQuery() {
		if (queryExp.filter != null && queryExp.filter.cond instanceof FilterOpFullExp) {
			FilterOpFullExp fexp = (FilterOpFullExp) queryExp.filter.cond;
			if (fexp.opexp1 instanceof QueryInExp) {
				return (QueryInExp)fexp.opexp1;
			}
		}
		return null;
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