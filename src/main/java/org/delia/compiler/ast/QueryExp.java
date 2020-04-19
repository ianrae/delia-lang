package org.delia.compiler.ast;

import java.util.ArrayList;
import java.util.List;

public class QueryExp extends ExpBase {
	public String typeName;
	public FilterExp filter;
	public List<QueryFuncExp> qfelist = new ArrayList<>();
	
	public QueryExp(int pos, IdentExp typeName, FilterExp filter, List<List<QueryFuncExp>> list1) {
		super(pos);
		this.typeName = typeName.name();
		this.filter = filter;
		
		if (list1 != null) {
			List<QueryFuncExp> list = new ArrayList<>();
			if (! list1.isEmpty()) {
				for(List<QueryFuncExp> sublist : list1) {
					for(QueryFuncExp inner: sublist) {
						list.add(inner);
					}
				}
			}
			qfelist = list;
		}
	}
	
	public String getTypeName() {
		return typeName;
	}
	
	@Override
	public String strValue() {
		return typeName;
	}
	
	@Override
	public String toString() {
		String s = (filter == null) ? "" : String.format("[%s]", filter.toString());
		
		if (qfelist != null && ! qfelist.isEmpty()) {
			for(QueryFuncExp qfe: qfelist) {
				String tmp = qfe.toString();
				s += tmp;
			}
		}
		
		return typeName + s;
	}
}