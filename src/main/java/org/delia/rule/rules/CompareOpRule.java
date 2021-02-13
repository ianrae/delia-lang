package org.delia.rule.rules;


import java.time.ZonedDateTime;

import org.delia.core.DateFormatService;
import org.delia.dval.compare.DValueCompareService;
import org.delia.error.DeliaError;
import org.delia.error.ErrorTracker;
import org.delia.rule.DRuleBase;
import org.delia.rule.DRuleContext;
import org.delia.rule.DValueRuleOperand;
import org.delia.rule.FieldExistenceService;
import org.delia.rule.RuleGuard;
import org.delia.rule.RuleOperand;
import org.delia.rule.RuleRuleOperand;
import org.delia.rule.StructDValueRuleOperand;
import org.delia.type.DRelation;
import org.delia.type.DValue;
import org.delia.type.Shape;

public class CompareOpRule extends DRuleBase {
		private RuleOperand oper1;
		private String op;
		private RuleOperand oper2;
		private DateFormatService fmtSvc;
		
		public CompareOpRule(RuleGuard guard, RuleOperand oper1, String op, RuleOperand oper2, DateFormatService fmtSvc) {
			super("compare", guard);
			this.oper1 = oper1;
			this.op = op;
			this.oper2 = oper2;
			this.fmtSvc = fmtSvc;
		}
		
		@Override
		public void performCompilerPass4Checks(FieldExistenceService fieldExistSvc, ErrorTracker et) {
			fieldExistSvc.checkRuleOperand(getName(), oper1, et);
			fieldExistSvc.checkRuleOperand(getName(), oper2, et);
		}
		
		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		protected boolean onValidate(DValue dval, DRuleContext ctx) {
			Comparable obj1 = getAsObject(oper1, dval, ctx);
			Comparable obj2 = getAsObject(oper2, dval, ctx);
			if (ctx.hasErrors()) {
				return false;
			}
			
			//convert to date.
			//FUTURE: can this be done during compilation? to make it faster?
			if (obj1 instanceof ZonedDateTime && obj2 instanceof String) {
				obj2 = fmtSvc.parseDateTime((String) obj2); //will throw if can't convert
			} else if (obj1 instanceof String && obj2 instanceof ZonedDateTime) {
				obj1 = fmtSvc.parseDateTime((String) obj1); //will throw if can't convert
			}
			
			DValueCompareService compareSvc = ctx.getCompareSvc();
			int cmp = compareSvc.compareObjs(obj1, obj2);
			
			boolean b = false;
			switch(op) {
			case "<":
				b = cmp < 0;
				break;
			case "<=":
				b = cmp <= 0;
				break;
			case ">":
				b = cmp > 0;
				break;
			case ">=":
				b = cmp >= 0;
				break;
			case "==":
				b = cmp == 0;
				break;
			case "!=":
				b = cmp != 0;
				break;
				
			default:
				//err!
				break;
			}
			
			if (!b) {
				String s1 = String.format("%s", obj1);
				String s2 = String.format("%s", obj2);
				String msg = String.format("rulefail '%s' %s '%s'", s1, op, s2);
				ctx.addError(this, msg, oper1, oper2);
			}
			return b;
		}
		private Comparable<?> getAsObject(RuleOperand oper, DValue dval, DRuleContext ctx) {
			if (oper == null) { // < 10
				oper = new DValueRuleOperand();
			}
			
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
			case LONG:
				return oper.asLong(dval);
			case NUMBER:
				return oper.asNumber(dval);
			case DATE:
				return oper.asDate(dval);
			case RELATION:
				return getRelationKeyAsObject(oper, dval, ctx);
			default:
				return oper.asString(dval);
			}
		}
		
		private Comparable<?> getRelationKeyAsObject(RuleOperand oper, DValue dval, DRuleContext ctx) {
			DRelation drel = oper.asRelation(dval);
			DValue keyVal = drel.getForeignKey(); 
			
			Shape shape = keyVal.getType().getShape();
			if (shape == null) {
				return null;
			}
			switch(shape) {
			case INTEGER:
				return keyVal.asInt();
			case LONG:
				return keyVal.asLong();
			case NUMBER:
				return keyVal.asNumber();
			case DATE:
				return keyVal.asDate();
			default:
				return keyVal.asString();
			}
		}
		@Override
		public boolean dependsOn(String fieldName) {
			return oper1.dependsOn(fieldName) || oper2.dependsOn(fieldName);
		}
		@Override
		public String getSubject() {
			String s1 = oper1.getSubject();
			String s2 = oper2.getSubject();
			return String.format("%s %s %s", s1, op, s2);
		}
	}