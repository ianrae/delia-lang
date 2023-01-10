package org.delia.typebuilder;

import org.delia.compiler.ast.AST;
import org.delia.error.DeliaError;

public class FutureDeclError extends DeliaError {
	public String baseTypeName;
	
	public FutureDeclError(String id, String msg, AST.Loc loc) {
		super(id, msg);
		setLoc(loc);
	}
}
