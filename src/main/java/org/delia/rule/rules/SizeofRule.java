package org.delia.rule.rules;

import org.delia.rule.DRuleBase;
import org.delia.rule.DRuleContext;
import org.delia.rule.RuleGuard;
import org.delia.rule.RuleOperand;
import org.delia.type.DValue;
import org.delia.util.DeliaExceptionHelper;

public class SizeofRule extends DRuleBase {
	private RuleOperand oper1;
	private int sizeofAmount;

	public SizeofRule(RuleGuard guard, RuleOperand oper1, int sizeofAmount) {
		super("sizeof", guard);
		this.oper1 = oper1;
		this.sizeofAmount = sizeofAmount;
	}
	@Override
	protected boolean onValidate(DValue dval, DRuleContext ctx) {
		switch (dval.getType().getShape()) {
		case INTEGER:
			return validateInt(dval, ctx);
		case STRING:
			return validateString(dval, ctx);
			default:
				DeliaExceptionHelper.throwError("rule-wrong-field-type", "sizeof only supported on int and string types");
			break;
		}
		
		return true;
	}
	private boolean validateInt(DValue dval, DRuleContext ctx) {
		//postgres smallint, integer, and bigint. https://www.postgresql.org/docs/9.5/datatype-numeric.html#DATATYPE-INT
		int min;
		int max;
		
		switch(sizeofAmount) {
		case 8:
			min = -256;
			max = 255;
			return checkInt(dval, min, max, ctx);
		case 16:
			min = -32768;
			max = 32767;
			return checkInt(dval, min, max, ctx);
		case 32:
			min = -32768;
			max = 32767;
			return checkInt(dval, min, max, ctx);
		case 64:
			return checkLong(dval, ctx);
		default:
			DeliaExceptionHelper.throwError("sizeof-wrong-amount", "sizeof int only supports 8,16,32, and 64, not: %d", sizeofAmount);
			break;
		}
		
		return true;
	}
	private boolean checkLong(DValue dval, DRuleContext ctx) {
		//no way to check if bigger than long, because that's the longest delia supports
		return true;
	}
	private boolean checkInt(DValue dval, int min, int max, DRuleContext ctx) {
		int n = oper1.asInt(dval);

		if (n > max) {
			String msg = String.format("int value %d smaller than sizeof(%d) allows.", n, sizeofAmount);
			ctx.addError(this, msg, oper1);
			return false;
		} else if (n > max) {
			String msg = String.format("int value %d larger than sizeof(%d) allows.", n, sizeofAmount);
			ctx.addError(this, msg, oper1);
			return false;
		}
		
		return true;
	}
	private boolean validateString(DValue dval, DRuleContext ctx) {
		String s = oper1.asString(dval);

		int maxlen = sizeofAmount; 
		if (s.length() > maxlen) {
			String msg = String.format("string exceeds sizeof(%d) characters: string len=%d", maxlen, s.length());
			ctx.addError(this, msg, oper1);
			return false;
		}
		return true;
	}
	
	@Override
	public boolean dependsOn(String fieldName) {
		return oper1.dependsOn(fieldName);
	}
	@Override
	public String getSubject() {
		return oper1.getSubject();
	}
}