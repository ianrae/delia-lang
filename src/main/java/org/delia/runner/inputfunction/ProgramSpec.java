package org.delia.runner.inputfunction;

import org.delia.compiler.ast.inputfunction.IdentPairExp;
import org.delia.tlang.runner.TLangProgram;
import org.delia.type.DValue;

public class ProgramSpec {
	public TLangProgram prog;
	public String inputField;
	public IdentPairExp outputField;
	public String viaPK; //normally null
	public DValue syntheticValue;
	public String syntheticFieldName;
}