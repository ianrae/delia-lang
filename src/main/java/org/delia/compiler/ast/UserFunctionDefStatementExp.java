package org.delia.compiler.ast;

import java.util.List;
import java.util.StringJoiner;

public class UserFunctionDefStatementExp extends ExpBase {
	public String funcName;
	public UserFunctionBodyExp bodyExp;
	public List<IdentExp> argsL;

	public UserFunctionDefStatementExp(int pos, UserFuncHeaderExp hdrExp,  UserFunctionBodyExp body) {
		super(pos);
		this.funcName = hdrExp.fnName;
		this.argsL = hdrExp.argsL;
		this.bodyExp = body;
	}
	
	@Override
	public String strValue() {
		String ss = String.format("function %s(", funcName);
		StringJoiner joiner = new StringJoiner(",");
		for(IdentExp exp: argsL) {
			joiner.add(exp.name());
		}
		ss += String.format("%s){", joiner.toString());
		
		ss += bodyExp.strValue();
		ss = String.format("%s}", ss);
		return ss;
	}

	@Override
	public String toString() {
		String ss = String.format("function %s(", funcName);
		StringJoiner joiner = new StringJoiner(",");
		for(IdentExp exp: argsL) {
			joiner.add(exp.name());
		}
		ss += String.format("%s){", joiner.toString());
		ss += bodyExp.toString();
		ss = String.format("%s}", ss);
		return ss;
	}
}