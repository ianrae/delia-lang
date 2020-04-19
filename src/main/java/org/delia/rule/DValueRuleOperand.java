package org.delia.rule;

import java.util.Date;
import java.util.List;

import org.delia.type.DRelation;
import org.delia.type.DValue;
import org.delia.type.Shape;

public class DValueRuleOperand implements RuleOperand {
	
	public DValueRuleOperand() {
	}
	@Override
	public String asString(DValue dval) {
		return dval.asString();
	}
	@Override
	public String getSubject() {
		return null;
	}
	@Override
	public Shape getShape(DValue dval) {
		return dval.getType().getShape();
	}
	@Override
	public Integer asInt(DValue dval) {
		return dval.asInt();
	}
	@Override
	public boolean dependsOn(String fieldName) {
		return false;
	}
	@Override
	public List<String> getFieldList() {
		return null;
	}
	@Override
	public Long asLong(DValue dval) {
		return dval.asLong();
	}
	@Override
	public Double asNumber(DValue dval) {
		return dval.asNumber();
	}
	@Override
	public Date asDate(DValue dval) {
		return dval.asDate();
	}
	@Override
	public DRelation asRelation(DValue dval) {
		return dval.asRelation();
	}
}