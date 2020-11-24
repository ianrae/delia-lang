package org.delia.rule;

import java.time.ZonedDateTime;
import java.util.List;

import org.delia.type.DRelation;
import org.delia.type.DValue;
import org.delia.type.Shape;

public interface RuleOperand {
	Shape getShape(DValue dval);
	String asString(DValue dval);
	Integer asInt(DValue dval);
	Long asLong(DValue dval);
	Double asNumber(DValue dval);
	ZonedDateTime asDate(DValue dval);
	DRelation asRelation(DValue dval);
	
	String getSubject();
	boolean dependsOn(String fieldName);
	List<String> getFieldList();
}