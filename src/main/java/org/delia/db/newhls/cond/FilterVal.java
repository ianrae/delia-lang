package org.delia.db.newhls.cond;

import org.delia.compiler.ast.BooleanExp;
import org.delia.compiler.ast.Exp;
import org.delia.compiler.ast.IntegerExp;
import org.delia.compiler.ast.LongExp;
import org.delia.compiler.ast.NumberExp;
import org.delia.compiler.ast.StringExp;
import org.delia.compiler.astx.XNAFSingleExp;
import org.delia.db.newhls.StructField;
import org.delia.db.newhls.ValType;
import org.delia.type.DValue;

/**
 * A value, symbol, or fn in a filter
 * @author ian
 *
 */
public class FilterVal {
	//name,int,boolean,string,fn
	public ValType valType;
	public Exp exp;
	public FilterFunc filterFn; //normally null
	public SymbolChain symchain; //normally null
	public CustomFilterValueRenderer customRenderer; //lower-level. renders single value
	
	//resolved later
	public StructField structField; //only set if SYMBOL or if SingleFilterCond
	public String alias;
	public DValue actualDateVal; //for date value

	public FilterVal(ValType valType, Exp exp) {
		this.valType = valType;
		this.exp = exp;
	}

	public boolean asBoolean() {
		BooleanExp bexp = (BooleanExp) exp;
		return bexp.val.booleanValue();
	}
	public int asInt() {
		IntegerExp exp1 = (IntegerExp) exp;
		return exp1.val.intValue();
	}
	public long asLong() {
		LongExp exp1 = (LongExp) exp;
		return exp1.val.longValue();
	}
	public double asNumber() {
		NumberExp exp1 = (NumberExp) exp;
		return exp1.val.doubleValue();
	}
	public String asString() {
		return exp.strValue(); //return any valtype as strings
	}
	public String asSymbol() {
		if (exp instanceof StringExp) return ((StringExp) exp).strValue();
		XNAFSingleExp nafexp = (XNAFSingleExp) exp;
		return nafexp.funcName;
	}
	public SymbolChain asSymbolChain() {
		return symchain;
	}
	public FilterFunc asFunc() {
		return filterFn; 
	}

	public boolean isBoolean() {
		return valType.equals(ValType.BOOLEAN);
	}
	public boolean isSymbol() {
		return valType.equals(ValType.SYMBOL);
	}
	public boolean isSymbolChain() {
		return valType.equals(ValType.SYMBOLCHAIN);
	}
	public boolean isFn() {
		return valType.equals(ValType.FUNCTION);
	}
	public boolean isScalar() {
		switch(valType) {
		case BOOLEAN:
		case INT:
		case LONG:
		case NUMBER:
		case STRING:
			return true;
		default:
			return false;
		}
	}

	@Override
	public String toString() {
		String fn = filterFn == null ? "" : ":" + filterFn.toString();
		String s = String.format("%s:%s%s", valType.name(), exp.strValue(), fn);
		return s;
	}

}