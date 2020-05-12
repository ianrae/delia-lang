package org.delia.compiler.ast.inputfunction;

import org.delia.compiler.ast.ExpBase;
import org.delia.compiler.ast.IdentExp;

public class OutputTargetExp extends ExpBase {
	public IdentPairExp outputExp; //a.points
	public IdentExp targetExp; //[filmId], normally null

	public OutputTargetExp(int pos, IdentExp targetId, String s1, String s2) {
		super(pos);
		this.outputExp = new IdentPairExp(pos, s1, s2);
		this.targetExp = targetId;
	}
	
	@Override
	public String strValue() {
		if (targetExp == null) {
			return targetExp.strValue();
		}
		return String.format("%s[%s]", outputExp.strValue(), targetExp.strValue());
	}

	@Override
	public String toString() {
		return strValue();
	}
}