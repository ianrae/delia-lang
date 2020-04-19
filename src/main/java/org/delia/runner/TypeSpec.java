package org.delia.runner;

import java.util.List;

import org.delia.compiler.ast.TypeStatementExp;

public class TypeSpec {
	public String baseTypeName; //null or basetypename
	public List<String> fieldL;
	public TypeStatementExp typeExp;
}
