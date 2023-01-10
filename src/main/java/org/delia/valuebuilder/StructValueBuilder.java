package org.delia.valuebuilder;

import org.delia.error.DetailedError;
import org.delia.type.*;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;


/**
 * This class is not thread-safe.
 * @author Ian Rae
 *
 */
public class StructValueBuilder extends DValueBuilder {
	private DStructType structType;
    public Map<String,DValue> map = new TreeMap<>();
    public List<TypePair>  allFields;
	private boolean ignoreMissingFields; //advanced use

	private String ignoreThisField; //used for upsert

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
				addNoDataError("null field value for mandatory field", fieldName);
			}
			return;
		}
		
		if (this.map.containsKey(fieldName)) {
			addDuplicateFieldError(String.format("Type '%s': already added field '%s'", structType.getName(), fieldName), fieldName);
			return;
		}
		else if (! isValidFieldName(fieldName)) {
			addUnknownFieldError(String.format("Type '%s': fieldName not allowed: '%s'", structType.getName(), fieldName));
			return;
		}
		
		TypePair pair = fieldExists(fieldName);
		if (pair == null) {
			addUnknownFieldError(String.format("Type '%s': unknown field '%s'", structType.getName(), fieldName));
			return;
		}
		
		DType target = pair.type; 
		boolean isRelation = target.isStructShape();
		if (!isRelation && !isOptional && ! target.isAssignmentCompatible(dval.getType())) {
			if (target.isShape(Shape.INTEGER) && BuiltInSizeofIntTypes.isSizeofType(target.getName())) {
				//not an error. ignore
			} else {
				this.addWrongTypeError(String.format("field %s", fieldName)); //!!
			}
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
			if (!ignoreMissingFields) {
				for(TypePair pair : allFields) {
					String fieldName = pair.name;
					if (! map.containsKey(fieldName) && ! isOptionalOrRelationOrSerial(pair) && !isIgnoringThisField(pair)) {
						addMissingFieldError(String.format("value for field '%s' not added to struct", fieldName), fieldName);
					}
				}
			}

			newDVal = new DValueImpl(type, map);
		}
		
		for(DetailedError err : getValidationErrors()){
			err.setTypeName(structType.getName());
		}
	}

	private boolean isIgnoringThisField(TypePair pair) {
		if (ignoreThisField != null && ignoreThisField.equals(pair.name)) {
			return true;
		}
		return false;
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

	public boolean isIgnoreMissingFields() {
		return ignoreMissingFields;
	}

	public void setIgnoreMissingFields(boolean ignoreMissingFields) {
		this.ignoreMissingFields = ignoreMissingFields;
	}
	public String getIgnoreThisField() {
		return ignoreThisField;
	}

	public void setIgnoreThisField(String ignoreThisField) {
		this.ignoreThisField = ignoreThisField;
	}


}