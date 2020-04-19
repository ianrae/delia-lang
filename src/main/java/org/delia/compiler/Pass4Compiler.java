package org.delia.compiler;

import java.util.List;

import org.delia.compiler.ast.Exp;
import org.delia.compiler.ast.TypeStatementExp;
import org.delia.core.FactoryService;
import org.delia.error.DeliaError;
import org.delia.runner.InternalCompileState;
import org.delia.type.DStructType;
import org.delia.type.DType;
import org.delia.type.DTypeRegistry;
import org.delia.type.TypePair;
import org.delia.util.DValueHelper;

//final checks using registry
public class Pass4Compiler extends CompilerPassBase {
	private DTypeRegistry registry;
	
	public Pass4Compiler(FactoryService factorySvc, ErrorLineFinder errorLineFinder, InternalCompileState execCtx, DTypeRegistry registry) {
		super(factorySvc, errorLineFinder, execCtx);
		this.registry = registry;
	}

	@Override
	public CompilerResults process(List<Exp> list) {
		CompilerResults results = new CompilerResults();
		results.list = list;
		
		for(Exp exp: list) {
			if (exp instanceof TypeStatementExp) {
				TypeStatementExp typeExp = (TypeStatementExp) exp;
				checkPrimaryKeys(typeExp, results);
			} 
		}
		
		return results;
	}

	private void checkPrimaryKeys(TypeStatementExp typeExp, CompilerResults results) {
		DType type = registry.getType(typeExp.typeName);
		if (! type.isStructShape()) {
			return;
		}
		DStructType structType = (DStructType) type;
		TypePair pair = DValueHelper.findPrimaryKeyFieldPair(structType);
		if (pair == null) {
			return;
		}
		
		if (! keyFieldIsAllowedType(pair)) {
			String msg = String.format("type '%s': primary key type %s is not allowed in field '%s'", typeExp.typeName, pair.type.getName(), pair.name);
			DeliaError err = createError("primary-key-type-not-allowed", msg, typeExp);
			results.errors.add(err);
		}
		if (! serialFieldIsAllowedType(pair, structType)) {
			String msg = String.format("type '%s': serial %s is not allowed in field '%s'", typeExp.typeName, pair.type.getName(), pair.name);
			DeliaError err = createError("primary-key-type-not-allowed", msg, typeExp);
			results.errors.add(err);
		}
	}
	
	private boolean keyFieldIsAllowedType(TypePair pair) {
		switch(pair.type.getShape()) {
		case INTEGER:
		case LONG:
		case BOOLEAN:
		case STRING:
		case DATE:
			return true;
		case NUMBER:
		case STRUCT:
		case RELATION:
		default:
			return false;
		}
	}

	private boolean serialFieldIsAllowedType(TypePair pair, DStructType structType) {
		if (! structType.fieldIsSerial(pair.name)) {
			return true;
		}
		
		switch(pair.type.getShape()) {
		case INTEGER:
		case LONG:
			return true;
		case STRING:
		case DATE:
		case BOOLEAN:
		case NUMBER:
		case STRUCT:
		case RELATION:
		default:
			return false;
		}
	}
}