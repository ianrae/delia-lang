package org.delia.db.hld.cond;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

/**
 * A filter of the form: value1 in (val1, val2,...)
 * or: !(value1 op value2)
 * @author ian
 *
 */
public class InFilterCond implements FilterCond {
	//[not] val in (...)
	public boolean isNot;
	public FilterVal val1;
	public FilterOp op = new FilterOp("in");
	public List<FilterVal> list = new ArrayList<>();

	@Override
	public String toString() {
		String fn = isNot ? "!" : "";
		StringJoiner joiner = new StringJoiner(",");
		for(FilterVal fv: list) {
			joiner.add(fv.toString());
		}
		
		
		String s = String.format("%s%s %s (%s)", fn, val1.toString(), op.toString(), joiner.toString());
		return s;
	}
}
