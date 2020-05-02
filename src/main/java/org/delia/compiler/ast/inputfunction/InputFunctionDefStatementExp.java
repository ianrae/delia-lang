package org.delia.compiler.ast.inputfunction;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

import org.delia.compiler.ast.Exp;
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
	
	public List<InputFuncMappingExp> getMappings() {
		List<InputFuncMappingExp> list = new ArrayList<>();
		for(Exp exp: bodyExp.statementL) {
			if (exp instanceof InputFuncMappingExp) {
				list.add((InputFuncMappingExp)exp);
			}
		}
		return list;
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