package org.delia.hld.cond;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

/**
 * represents a fn call such as customer[createDate.year()]
 * @author ian
 *
 */
public class FilterFunc {
	public String fnName;
	public List<FilterVal> argL = new ArrayList<>();

	@Override
	public String toString() {
		StringJoiner joiner = new StringJoiner(",");
		for(FilterVal fval: argL) {
			joiner.add(fval.toString());
		}
		String s = String.format("%s(%s)", fnName, joiner.toString());
		return s;
	}
}