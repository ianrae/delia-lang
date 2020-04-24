package org.delia.db.sql.fragment;

public class OpFragment implements SqlFragment {
	public AliasedFragment left;
	public AliasedFragment right;
	public String op;
	
	public OpFragment(String op) {
		this.op = op;
	}
	
	@Override
	public String render() {
		String s = String.format(" %s %s %s", left.render(), op, right.render());
		return s;
	}
}