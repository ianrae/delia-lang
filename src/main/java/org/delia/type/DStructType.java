package org.delia.type;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DStructType extends DType {
    private OrderedMap orderedMap;
	//!! add String naturalKeyField, for db query. eg. 'code'
    private List<TypePair> allFields; //lazy-created

	
	public DStructType(Shape shape, String name, DType baseType, OrderedMap orderedMap) {
		super(shape, name, baseType);
		this.orderedMap = orderedMap;
	}
	
	public boolean fieldIsOptional(String fieldname) {
	    boolean b = orderedMap.isOptional(fieldname);
	    if (b) {
	    	return true;
	    }
	    
	    //TODO: fix runaway risk
	    DStructType baseType = (DStructType) this.getBaseType();
	    while(true) {
	    	if (baseType == null) {
	    		return false;
	    	}
		    b = baseType.orderedMap.isOptional(fieldname);
		    if (b) {
		    	return true;
		    }
		    baseType = (DStructType) baseType.getBaseType();
	    }
	}
    public boolean fieldIsUnique(String fieldname) {
	    boolean b = orderedMap.isUnique(fieldname);
	    if (b) {
	    	return true;
	    }
	    
	    //TODO: fix runaway risk
	    DStructType baseType = (DStructType) this.getBaseType();
	    while(true) {
	    	if (baseType == null) {
	    		return false;
	    	}
		    b = baseType.orderedMap.isUnique(fieldname);
		    if (b) {
		    	return true;
		    }
		    baseType = (DStructType) baseType.getBaseType();
	    }
    }
    public boolean fieldIsPrimaryKey(String fieldname) {
	    boolean b = orderedMap.isPrimaryKey(fieldname);
	    if (b) {
	    	return true;
	    }
	    
	    //TODO: fix runaway risk
	    DStructType baseType = (DStructType) this.getBaseType();
	    while(true) {
	    	if (baseType == null) {
	    		return false;
	    	}
		    b = baseType.orderedMap.isPrimaryKey(fieldname);
		    if (b) {
		    	return true;
		    }
		    baseType = (DStructType) baseType.getBaseType();
	    }
    }
	public boolean fieldIsSerial(String fieldname) {
	    boolean b = orderedMap.isSerial(fieldname);
	    if (b) {
	    	return true;
	    }
	    
	    //TODO: fix runaway risk
	    DStructType baseType = (DStructType) this.getBaseType();
	    while(true) {
	    	if (baseType == null) {
	    		return false;
	    	}
		    b = baseType.orderedMap.isSerial(fieldname);
		    if (b) {
		    	return true;
		    }
		    baseType = (DStructType) baseType.getBaseType();
	    }
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

	//TODO: make this into an 'internal' api with DStructTypeInternal interface
	public void internalAdjustType(DType baseType, OrderedMap omap) {
		allFields = null; //reset
		internalAdjustType(baseType);
		this.orderedMap = omap;
	}

	@Override
	public void performTypeReplacement(TypeReplaceSpec spec) {
		super.performTypeReplacement(spec);

		orderedMap.performTypeReplacement(spec);
		
		for(TypePair pair: allFields) {
			if (spec.needsReplacement(pair.type)) {
				pair.type = spec.newType;
			}
		}
	}

}