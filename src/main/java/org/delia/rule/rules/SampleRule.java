package org.delia.rule.rules;

import org.delia.rule.DRuleBase;
import org.delia.rule.DRuleContext;
import org.delia.rule.RuleGuard;
import org.delia.rule.RuleOperand;
import org.delia.rule.RuleRuleOperand;
import org.delia.type.DValue;
import org.delia.type.Shape;

public class SampleRule extends DRuleBase {
		private RuleOperand oper1;
		private String op;
		private RuleOperand oper2;
		
		public SampleRule(RuleGuard guard, RuleOperand oper1, String op, RuleOperand oper2) {
			super("sample", guard);
			this.oper1 = oper1;
			this.op = op;
			this.oper2 = oper2;
		}
		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		protected boolean onValidate(DValue dval, DRuleContext ctx) {
			Comparable obj1 = getAsObject(oper1, dval, ctx);
			Comparable obj2 = getAsObject(oper2, dval, ctx);
			
//			String s1 = oper1.asString(dval);
//			String s2 = oper2.asString(dval);
			
			boolean b = false;
			switch(op) {
			case "<":
				b = obj1.compareTo(obj2) < 0;
				break;
			case "<=":
				b = obj1.compareTo(obj2) <= 0;
				break;
			case ">":
				b = obj1.compareTo(obj2) > 0;
				break;
			case ">=":
				b = obj1.compareTo(obj2) >= 0;
				break;
			default:
				//err!
				break;
			}
			
			if (!b) {
				String s1 = String.format("%s", obj1);
				String s2 = String.format("%s", obj2);
				String msg = String.format("rulefail '%s' in '%s'", s1, s2);
				ctx.addError(this, msg, oper1, oper2);
			}
			return b;
		}
		private Comparable<?> getAsObject(RuleOperand oper, DValue dval, DRuleContext ctx) {
			if (oper instanceof RuleRuleOperand) {
				RuleRuleOperand rro = (RuleRuleOperand) oper;
				Comparable obj = (Comparable) rro.exec(dval, ctx);
				return obj;
			}
			
			
			Shape shape = oper.getShape(dval);
			if (shape == null) {
				return null;
			}
			switch(shape) {
			case INTEGER:
				return oper.asInt(dval);
			default:
				return oper.asString(dval);
			}
		}
		@Override
		public boolean dependsOn(String fieldName) {
			return oper1.dependsOn(fieldName) || oper2.dependsOn(fieldName);
		}
		
		@Override
		public String getSubject() {
			return "???";
		}
	}