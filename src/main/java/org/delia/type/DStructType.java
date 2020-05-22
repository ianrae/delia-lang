package org.delia.type;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.delia.util.DeliaExceptionHelper;

public class DStructType extends DType implements DStructTypeInternal {
	
	private static final int MAX_INHERITANCE_DEPTH = 1000;
    private OrderedMap orderedMap;
    private List<TypePair> allFields; //lazy-created
    private PrimaryKey primaryKey; //can be null
	
	public DStructType(Shape shape, String name, DType baseType, OrderedMap orderedMap, PrimaryKey primaryKey) {
		super(shape, name, baseType);
		this.orderedMap = orderedMap;
		this.primaryKey = primaryKey;
	}
	@Override
	public void finishStructInitialization(DType baseType, OrderedMap orderedMap, PrimaryKey primaryKey) {
		this.baseType = baseType;
		this.orderedMap = orderedMap;
		this.primaryKey = primaryKey;
	}
	
	public boolean fieldIsOptional(String fieldName) {
	    boolean b = orderedMap.isOptional(fieldName);
	    if (b) {
	    	return true;
	    }
	    
	    DStructType baseType = (DStructType) this.getBaseType();
	    for(int k = 0; k < MAX_INHERITANCE_DEPTH; k++) { //don't try forever
	    	if (baseType == null) {
	    		return false;
	    	}
		    b = baseType.orderedMap.isOptional(fieldName);
		    if (b) {
		    	return true;
		    }
		    baseType = (DStructType) baseType.getBaseType();
	    }
	    DeliaExceptionHelper.throwError("struct-type-runaway", "Too many base types for field: %s", fieldName);
	    return false;
	}
    public boolean fieldIsUnique(String fieldName) {
	    boolean b = orderedMap.isUnique(fieldName);
	    if (b) {
	    	return true;
	    }
	    
	    DStructType baseType = (DStructType) this.getBaseType();
	    for(int k = 0; k < MAX_INHERITANCE_DEPTH; k++) { //don't try forever
	    	if (baseType == null) {
	    		return false;
	    	}
		    b = baseType.orderedMap.isUnique(fieldName);
		    if (b) {
		    	return true;
		    }
		    baseType = (DStructType) baseType.getBaseType();
	    }
	    DeliaExceptionHelper.throwError("struct-type-runaway", "Too many base types for field: %s", fieldName);
	    return false;
    }
    public boolean fieldIsPrimaryKey(String fieldName) {
	    boolean b = orderedMap.isPrimaryKey(fieldName);
	    if (b) {
	    	return true;
	    }
	    
	    DStructType baseType = (DStructType) this.getBaseType();
	    for(int k = 0; k < MAX_INHERITANCE_DEPTH; k++) { //don't try forever
	    	if (baseType == null) {
	    		return false;
	    	}
		    b = baseType.orderedMap.isPrimaryKey(fieldName);
		    if (b) {
		    	return true;
		    }
		    baseType = (DStructType) baseType.getBaseType();
	    }
	    DeliaExceptionHelper.throwError("struct-type-runaway", "Too many base types for field: %s", fieldName);
	    return false;
    }
	public boolean fieldIsSerial(String fieldName) {
	    boolean b = orderedMap.isSerial(fieldName);
	    if (b) {
	    	return true;
	    }
	    
	    DStructType baseType = (DStructType) this.getBaseType();
	    for(int k = 0; k < MAX_INHERITANCE_DEPTH; k++) { //don't try forever
	    	if (baseType == null) {
	    		return false;
	    	}
		    b = baseType.orderedMap.isSerial(fieldName);
		    if (b) {
		    	return true;
		    }
		    baseType = (DStructType) baseType.getBaseType();
	    }
	    DeliaExceptionHelper.throwError("struct-type-runaway", "Too many base types for field: %s", fieldName);
	    return false;
	}

	public Map<String, DType> getDeclaredFields() {
		return orderedMap.map;
	}
	public List<String> orderedList() {
	    return orderedMap.orderedList;
	}
	
	//not thread-safe!!
    public List<TypePair> getAllFields() {
        if (allFields == null) {
            allFields = doAllFieldsForType(this);
        }
        return allFields;
    }

    private List<TypePair> doAllFieldsForType(DStructType dtype) {
        DStructType baseType = (DStructType) dtype.getBaseType();
        if (baseType == null) {
            List<TypePair> list = new ArrayList<>();
            for(String fieldName: dtype.orderedList()) {
                DType field = dtype.getDeclaredFields().get(fieldName);
                list.add(new TypePair(fieldName, field));
            }
            return list;
        } else {
            List<TypePair> list = doAllFieldsForType(baseType);  //**recursion**
            for(String fieldName: dtype.orderedList()) {
                DType field = dtype.getDeclaredFields().get(fieldName);
                list.add(new TypePair(fieldName, field));
            }
            return list;
        }
    }

    //helps see typename in debugger
	@Override
	public String toString() {
		return super.toString();
	}

	@Override
	public void performTypeReplacement(TypeReplaceSpec spec) {
		super.performTypeReplacement(spec);

		orderedMap.performTypeReplacement(spec);
		
		for(TypePair pair: allFields) {
			if (spec.needsReplacement(this, pair.type)) {
				pair.type = spec.newType;
			}
		}
	}

	public PrimaryKey getPrimaryKey() {
		return primaryKey;
	}

}