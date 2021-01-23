package org.delia.db.hld.cond;

/**
 * One of the six operations. eg size > 10
 * @author ian
 *
 */
public class FilterOp {
	public String op; //==,!=,<,>,<=,>=

	public FilterOp(String op) {
		this.op = op;
	}

	@Override
	public String toString() {
		return op;
	}
}