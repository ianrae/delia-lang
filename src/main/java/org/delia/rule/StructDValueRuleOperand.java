package org.delia.rule;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.delia.type.DRelation;
import org.delia.type.DValue;
import org.delia.type.Shape;

public class StructDValueRuleOperand implements RuleOperand {
	private String fieldName;
	
	public StructDValueRuleOperand(String fieldName) {
		this.fieldName = fieldName;
	}
	@Override
	public String asString(DValue dval) {
		return dval.asStruct().getField(fieldName).asString();
	}
	@Override
	public String getSubject() {
		return fieldName;
	}
	@Override
	public Shape getShape(DValue dval) {
		return dval.asStruct().getField(fieldName).getType().getShape();
	}
	@Override
	public Integer asInt(DValue dval) {
		return dval.asStruct().getField(fieldName).asInt();
	}
	@Override
	public boolean dependsOn(String targetFieldName) {
		return fieldName.equals(targetFieldName);
	}
	@Override
	public List<String> getFieldList() {
		return Collections.singletonList(fieldName);
	}
	@Override
	public Long asLong(DValue dval) {
		return dval.asStruct().getField(fieldName).asLong();
	}
	@Override
	public Double asNumber(DValue dval) {
		return dval.asStruct().getField(fieldName).asNumber();
	}
	@Override
	public Date asDate(DValue dval) {
		return dval.asStruct().getField(fieldName).asLegacyDate();
	}
	@Override
	public DRelation asRelation(DValue dval) {
		DValue inner = dval.asStruct().getField(fieldName);
		return inner == null ? null : inner.asRelation();
	}
}