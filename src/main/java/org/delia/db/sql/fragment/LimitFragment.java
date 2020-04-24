package org.delia.db.sql.fragment;

public class LimitFragment implements SqlFragment {
	int amount;

	public LimitFragment(Integer n) {
		this.amount = n;
	}

	@Override
	public String render() {
		return String.format(" LIMIT %d", amount);
	}
}