package org.delia.db.newhls.cond;

/**
 * A filter of the form: opfilter and/or opfilter
 * @author ian
 *
 */
public class OpAndOrFilter implements FilterCond {
	public boolean isAnd;
	public FilterCond cond1;
	public FilterCond cond2;

	@Override
	public String toString() {
		String fn = isAnd ? "AND" : "OR";
		String s = String.format("%s %s %s", cond1.toString(), fn, cond2.toString());
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