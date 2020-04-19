package org.delia.compiler.ast;

public class FilterOpFullExp extends ExpBase {

	public boolean negFlag;
	public Exp opexp1;
	public Exp opexp2;
	public boolean isAnd;

	public FilterOpFullExp(int pos, boolean negFlag, Exp opexp1, boolean isAnd, Exp opexp2) {
		super(pos);
		this.negFlag = negFlag;
		this.opexp1 = opexp1;
		this.opexp2 = opexp2;
		this.isAnd = isAnd;
	}
	public FilterOpFullExp(int pos, FilterOpExp opexp1) {
		super(pos);
		this.negFlag = false;
		this.opexp1 = opexp1;
		this.opexp2 = null;
		this.isAnd = false;
	}
	public FilterOpFullExp(int pos, QueryInExp opexp1) {
		super(pos);
		this.negFlag = false;
		this.opexp1 = opexp1;
		this.opexp2 = null;
		this.isAnd = false;
	}
	//used by parser
	public FilterOpFullExp negate() {
		this.negFlag = !negFlag;
		return this;
	}
	
	public boolean isSingleFilterOpExp() {
		return opexp1 instanceof FilterOpExp && opexp2 == null;
	}
	public boolean isSingleQueryInExp() {
		return opexp1 instanceof QueryInExp && opexp2 == null;
	}
	
	@Override
	public String strValue() {
//		return opexp.strValue();
		return toString();
	}
	
	@Override
	public String toString() {
		String s1 = negFlag ? "!" : "";
		String s2 = opexp1.toString();
		if (opexp2 == null) {
			String s = String.format("%s%s", s1, s2);
			return s;
		} else {
			String s3 = isAnd ? "and" : "or";
			String s4 = opexp2.toString();
			String s = String.format("%s%s %s %s", s1, s2, s3, s4);
			return s;
		}
	}
}