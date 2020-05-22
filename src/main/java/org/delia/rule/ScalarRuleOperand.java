package org.delia.rule;

import java.util.Date;
import java.util.List;

import org.delia.type.DRelation;
import org.delia.type.DValue;
import org.delia.type.Shape;

public class ScalarRuleOperand implements RuleOperand {
	private Object val; //never null
	public ScalarRuleOperand(Object val) {
		this.val = val;
	}
	@Override
	public String asString(DValue dval) {
		return val.toString();
	}
	@Override
	public String getSubject() {
		return null;
	}
	@Override
	public Shape getShape(DValue dval) {
		if (val == null) {
			return null; //is this ok??
		} else if (val instanceof Integer) {
			return Shape.INTEGER;
		} else if (val instanceof Long) {
			return Shape.LONG;
		} else if (val instanceof Double) {
			return Shape.NUMBER;
		} else if (val instanceof Boolean) {
			return Shape.BOOLEAN;
		} else {
			return Shape.STRING; //date as string
		}
	}
	@Override
	public Integer asInt(DValue dval) {
		Integer n = (Integer) val;
		return n;
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
		Long n = (Long) val;
		return n;
	}
	@Override
	public Double asNumber(DValue dval) {
		Double n = (Double)val;
		return n;
	}
	@Override
	public Date asDate(DValue dval) {
		Date dt = (Date)val;
		return dt;
	}
	@Override
	public DRelation asRelation(DValue dval) {
		return null;
	}
}