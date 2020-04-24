package org.delia.db.sql.fragment;

public class OffsetFragment implements SqlFragment {
	int amount;

	public OffsetFragment(Integer n) {
		this.amount = n;
	}

	@Override
	public String render() {
		return String.format(" OFFSET %d", amount);
	}
}