package org.delia.db.sql.fragment;

public class OpFragment implements SqlFragment {
	public AliasedFragment left;
	public AliasedFragment right;
	public String op;
	public boolean leftNot;
	public boolean rightNot;
	
	public OpFragment(String op) {
		this.op = op;
	}
	
	@Override
	public String render() {
		String not1 = leftNot ? "NOT " : "";
		String not2 = rightNot ? "NOT " : "";
		String s = String.format(" %s%s %s %s%s", not1, left.render(), op, not2, right.render());
		return s;
	}
}