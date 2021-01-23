package org.delia.db.hld.cond;

/**
 * A filter of the form: value1 op value2
 * or: !(value1 op value2)
 * @author ian
 *
 */
public class OpFilterCond implements FilterCond {
	//[not] val op val
	public boolean isNot;
	public FilterVal val1;
	public FilterOp op;
	public FilterVal val2;
	public CustomFilterValueRenderer customRenderer; //higher-level. renders entire a op b

	@Override
	public String toString() {
		String fn = isNot ? "!" : "";
		String s = String.format("%s%s %s %s", fn, val1.toString(), op.toString(), val2.toString());
		return s;
	}
}
//	public static class LikeFilterCond implements FilterCond {
//		//[not] val like val
//		public boolean isNot;
//		public FilterVal val1;
//		public List<FilterVal> inList;
//	}
//	public static class AndOrFilterCond implements FilterCond {
//		//[not] cond and/or cond
//		public boolean isNot;
//		public FilterCond val1;
//		public boolean isAnd; //if false then is OR
//		public FilterCond val2;
//	}