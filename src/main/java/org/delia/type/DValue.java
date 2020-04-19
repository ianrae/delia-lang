package org.delia.type;

import java.util.Date;
import java.util.Map;


/**
 * Represents a single Delia value.
 * May be a scalar value or a struct value.
 * 
 * @author Ian Rae
 *
 */
public interface DValue {
    
	DType getType();
	Object getObject();
	ValidationState getValidationState();
	boolean isValid();
	int asInt();
    double asNumber();
	long asLong();
	String asString();
	boolean asBoolean();
	Date asDate();
	Map<String,DValue> asMap();
	DStructHelper asStruct();
	DRelation asRelation();
    Object getPersistenceId();
}