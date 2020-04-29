package org.delia.compiler.ast.inputfunction;

import java.util.List;
import java.util.StringJoiner;

import org.delia.compiler.ast.ExpBase;

public class InputFunctionDefStatementExp extends ExpBase {
	public String funcName;
	public InputFunctionBodyExp bodyExp;
	public List<IdentPairExp> argsL;

	public InputFunctionDefStatementExp(int pos, InputFuncHeaderExp hdrExp,  InputFunctionBodyExp body) {
		super(pos);
		this.funcName = hdrExp.fnName;
		this.argsL = hdrExp.argsL;
		this.bodyExp = body;
	}
	
	@Override
	public String strValue() {
		String ss = String.format("function %s(", funcName);
		StringJoiner joiner = new StringJoiner(",");
		for(IdentPairExp exp: argsL) {
			joiner.add(exp.strValue());
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
		for(IdentPairExp exp: argsL) {
			joiner.add(exp.strValue());
		}
		ss += String.format("%s){", joiner.toString());
		ss += bodyExp.toString();
		ss = String.format("%s}", ss);
		return ss;
	}
}