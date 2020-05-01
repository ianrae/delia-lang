package org.delia.runner.inputfunction;

import org.delia.compiler.ast.inputfunction.IdentPairExp;
import org.delia.tlang.runner.TLangProgram;
import org.delia.type.DValue;

public class ProgramSpec {
	public TLangProgram prog;
	public IdentPairExp outputField;
	public DValue syntheticValue;
}