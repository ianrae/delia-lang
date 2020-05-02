package org.delia.rule;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.delia.type.DRelation;
import org.delia.type.DValue;
import org.delia.type.Shape;

public class RuleRuleOperand implements RuleOperand {
	private String fnName;
	private DRule rule;
	private String fieldName;
	
	public RuleRuleOperand(String fnName, DRule rule, String fieldName) {
		this.fnName = fnName;
		this.rule = rule;
		this.fieldName = fieldName;
	}
	@Override
	public String asString(DValue dval) {
		return null; //11!!dval.asStruct().getField(fieldName).asString();
	}
	@Override
	public String getSubject() {
		return fnName;
	}
	@Override
	public Shape getShape(DValue dval) {
		return null; //dval.asStruct().getField(fieldName).getType().getShape();
	}
	@Override
	public Integer asInt(DValue dval) {
		return null; //dval.asStruct().getField(fieldName).asInt();
	}
	
	public Object exec(DValue dval, DRuleContext ctx) {
		if (fieldName != null) {
			dval = dval.asStruct().getField(fieldName);
		}
		return rule.exec(dval, ctx);
	}
	@Override
	public boolean dependsOn(String targetFieldName) {
		return fieldName.equals(targetFieldName);
	}
	
	@Override
	public List<String> getFieldList() {
		if (fieldName == null) {
			return null;
		}
		return Collections.singletonList(fieldName);
	}
	@Override
	public Long asLong(DValue dval) {
		return null;
	}
	@Override
	public Double asNumber(DValue dval) {
		return null;
	}
	@Override
	public Date asDate(DValue dval) {
		return null;
	}
	@Override
	public DRelation asRelation(DValue dval) {
		// TODO Auto-generated method stub
		return null;
	}
	public String getFieldName() {
		return fieldName;
	}
}