package org.delia.valuebuilder;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.delia.type.DStructType;
import org.delia.type.DType;
import org.delia.type.DValue;
import org.delia.type.DValueImpl;
import org.delia.type.Shape;
import org.delia.type.TypePair;


/**
 * This class is not thread-safe.
 * @author Ian Rae
 *
 */
public class StructValueBuilder extends DValueBuilder {
	private DStructType structType;
    public Map<String,DValue> map = new TreeMap<>();
    public List<TypePair>  allFields;

	public StructValueBuilder(DStructType type) {
		if (!type.isShape(Shape.STRUCT)) {
			addWrongTypeError("expecting struct");
			return;
		}
		this.type = type;
		this.structType = type;
		this.allFields = type.getAllFields();
	}

	public void buildFromString(String input) {
		//do nothing
	}
	public void addField(String fieldName, DValue dval) {
		addField(fieldName, dval, true);
	}
	public void addField(String fieldName, DValue dval, boolean logNullErr) {
		if (fieldName == null || fieldName.isEmpty()) {
			addNoDataError("null or empty fieldname");
			return;
		} else if (fieldName.contains(".")) {
			addNoDataError("struct field names cannot contain '.'");
		}
		
		boolean isOptional = structType.fieldIsOptional(fieldName);
		if (dval == null && !isOptional) {
			if (logNullErr) {
				addNoDataError("null field value");
			}
			return;
		}
		
		if (this.map.containsKey(fieldName)) {
			addDuplicateFieldError(String.format("already added field '%s'", fieldName), fieldName);
			return;
		}
		else if (! isValidFieldName(fieldName)) {
			addUnknownFieldError(String.format("fieldName not allowed: '%s'", fieldName));
			return;
		}
		
		TypePair pair = fieldExists(fieldName);
		if (pair == null) {
			addUnknownFieldError(String.format("unknown field '%s'", fieldName));
			return;
		}
		
		DType target = pair.type; 
		boolean isRelation = target.isStructShape();
		if (!isRelation && !isOptional && ! target.isAssignmentCompatible(dval.getType())) {
			this.addWrongTypeError(String.format("field %s", fieldName)); //!!
		}
		
		map.put(fieldName, dval);
	}


	private TypePair fieldExists(String targetFieldName) {
        for(TypePair pair : allFields) {
            if (pair.name.equals(targetFieldName)) {
                return pair;
            }
        }
        return null;
    }

    private boolean isValidFieldName(String fieldName) {
		
		for(int i = 0; i < fieldName.length(); i++) {
			char ch = fieldName.charAt(i);
			if (Character.isWhitespace(ch)) {
				return false;
			} else if (Character.isISOControl(ch)) {
				return false;
			}
		}
		return true;
	}

	@Override
	protected void onFinish() {
		if (wasSuccessful()) {
			for(TypePair pair : allFields) {
			    String fieldName = pair.name;
				if (! map.containsKey(fieldName) && ! isOptionalOrRelationOrSerial(pair)) {
					addMissingFieldError(String.format("value for field '%s' not added to struct", fieldName), fieldName);
				}
			}
			
			newDVal = new DValueImpl(type, map);
		}
	}

	/**
	 * We don't load relations eagerly, so ignore them
	 */
	private boolean isOptionalOrRelationOrSerial(TypePair pair) {
		if (structType.fieldIsOptional(pair.name)) {
			return true;
		} else if (structType.fieldIsSerial(pair.name)) {
			return true;
		} else if (pair.type.isStructShape()) {
			return true;
		}
		return false;
	}
	
	
}