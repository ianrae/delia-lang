package org.delia.compiler.ast;

/**
 * TODO: What is this class? Can it be deleted?
 * @author Ian Rae
 *
 */
public class EndSourceStatementExp extends ExpBase {
	
	public EndSourceStatementExp() {
		super(99);
	}

	@Override
	public String strValue() {
		return "end-src";
	}

	@Override
	public String toString() {
		return strValue();
	}
}