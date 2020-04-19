package org.delia.compiler;

import java.util.ArrayList;
import java.util.List;

import org.delia.compiler.ast.Exp;
import org.delia.error.DeliaError;

public class CompilerResults {
	public List<Exp> list;
	public List<DeliaError> errors = new ArrayList<>();
	
	public boolean success() {
		return errors.isEmpty();
	}
}