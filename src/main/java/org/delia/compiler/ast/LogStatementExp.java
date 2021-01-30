package org.delia.compiler.ast;

public class LogStatementExp extends ExpBase {
	public Exp value;

	public LogStatementExp(int pos, Exp val) {
		super(pos);
		this.value = val;
	}
	@Override
	public String strValue() {
		return value.strValue();
	}
	@Override
	public String toString() {
		String s = "";
		if (value instanceof QueryExp) {
			s = value.toString();
		} else {
			s = value == null ? "" : formatValue(value);
		}
		
		String str = String.format("log %s", s);
		return str;
	}
}