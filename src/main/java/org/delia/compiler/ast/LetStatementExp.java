package org.delia.compiler.ast;

//FullAssignmentExp
public class LetStatementExp extends ExpBase {
	public static final String QUERY_RESPONSE_TYPE = "queryResponse";
	public static final String USER_FUNC_TYPE = "userFunc";
	public String varName;
	public String typeName;
	public boolean isTypeExplicit;
	public Exp value;

	public LetStatementExp(int pos, IdentExp varname, IdentExp typeName, Exp val) {
		super(pos);
		this.varName = varname.name();
		this.value = val;
		if (typeName == null) {
			autosetType();
			isTypeExplicit = false;
		} else {
			this.typeName = typeName.name();
			isTypeExplicit = true;
		}
	}
	public boolean isType(String typeName2) {
		return (typeName != null && typeName.equals(typeName2));
	}
	private void autosetType() {
		if (value instanceof LongExp) {
			typeName = "long";
		} else if (value instanceof IntegerExp) {
			typeName = "int";
		} else if (value instanceof NumberExp) {
			typeName = "number";
		} else if (value instanceof StringExp) {
			typeName = "string";
		} else if (value instanceof BooleanExp) {
			typeName = "boolean";
		} else if (value instanceof QueryExp) {
			typeName = QUERY_RESPONSE_TYPE; 
		} else if (value instanceof UserFnCallExp) {
			typeName = USER_FUNC_TYPE;
		}
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
		
		String s2 = (isTypeExplicit) ? String.format(" %s", typeName) : "";
		
		return "let " + varName + s2 + " = " + s;
	}
}