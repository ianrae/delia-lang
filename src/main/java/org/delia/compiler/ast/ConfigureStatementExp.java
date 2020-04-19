package org.delia.compiler.ast;

public class ConfigureStatementExp extends ExpBase {
	public String varName;
	public Exp value;
	private IdentExp prefix;

	public ConfigureStatementExp(int pos, IdentExp prefix, IdentExp varname, Exp val) {
		super(pos);
		this.prefix = prefix; //may be null
		this.varName = varname.name();
		this.value = val;
	}
	@Override
	public String strValue() {
		return varName;
	}
	@Override
	public String toString() {
		String s = "";
		if (value instanceof QueryExp) {
			s = value.toString();
		} else {
			s = value == null ? "" : formatValue(value);
		}
		
		String str = prefix == null ? "" : prefix.name() + ".";
		str = String.format("configure %s%s = %s", str, varName, s);
		return str;
	}
	public String getPrefix() {
		return prefix.name();
	}
}