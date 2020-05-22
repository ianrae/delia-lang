package org.delia.type;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Represents all the known types in a given Delia program.
 * Includes built-in types and all custom scalar and struct types.
 * 
 * @author Ian Rae
 *
 */
public class DTypeRegistry {
	private Map<String,DType> map = new ConcurrentHashMap<>(); 
	private List<DType> orderedList = new ArrayList<>();
	private int nextBitIndex; //TODO !! atomic thing later for thread safety
	private DTypeHierarchy th;
	private DStructType schemaVersionType;
	private DStructType datType;
	
	public static final int NUM_BUILTIN_TYPES = 7;
	
	public synchronized void add(String typeName, DType dtype) {
        if (dtype == null || typeName == null || typeName.isEmpty()) {
            throw new IllegalArgumentException("name or type were null");
        }
		
	    dtype.setBitIndex(nextBitIndex++);
	    
	    if (map.containsKey(typeName)) {
	    	System.out.println("REDEF " + typeName); //TODO remove
	    }
	    
	    orderedList.add(dtype);
		map.put(typeName, dtype);
		
		th = null; //clear
	}
	
	/**
	 * Create lazily. must be thread-safe
	 * @return type heirarchy
	 */
	public synchronized DTypeHierarchy getHierarchy() {
	    if (th == null) {
	        th = new DTypeHierarchy();
	        th.build(map);
	    }
	    return th;
	}

	public Set<String> getAll() {
		return map.keySet();
	}

	public int size() {
		return map.size();
	}
	
	public boolean existsType(String name) {
	    return getType(name) != null;
	}

	public DType getType(String name) {
		return map.get(name);
	}
	public DType getType(BuiltInTypes builtInType) {
		return map.get(builtInType.name());
	}
	
    public List<DType> getChildTypes(DType type) {
        DTypeHierarchy th = this.getHierarchy();
        return th.findChildTypes(this.map, type);
    }
    public List<DType> getParentTypes(DType type) {
        DTypeHierarchy th = this.getHierarchy();
        return th.findParentTypes(this.map, type);
    }
	
    public List<DType> getOrderedList() {
        return orderedList;
    }

	public void setSchemaVersionType(DStructType dtype) {
		schemaVersionType = dtype;
	}
	public DStructType getSchemaVersionType() {
		return schemaVersionType;
	}
	public void setDATType(DStructType dtype) {
		datType = dtype;
	}
	public DStructType getDATType() {
		return datType;
	}
	public DStructType findTypeOrSchemaVersionType(String typeName) {
		if (schemaVersionType != null && schemaVersionType.getName().equals(typeName)) {
			return schemaVersionType;
		}
		if (datType != null && datType.getName().equals(typeName)) {
			return datType;
		}
		return (DStructType) getType(typeName);
	}
	
	public Set<String> getAllCustomTypes() {
		Set<String> list = new HashSet<>();
		for(String typeName: map.keySet()) {
			String s = BuiltInTypes.convertDTypeNameToDeliaName(typeName);
			if (s.equals(typeName)) {
				list.add(typeName);
			}
		}
		return list;
	}

	@Override
	public String toString() {
		//TODO: later limit to at most 50 type names
		StringJoiner joiner = new StringJoiner(",");
		for(DType type: orderedList) {
			joiner.add(type.getName());
		}
		return joiner.toString();
	}

//	public void performTypeReplacement(TypeReplaceSpec spec) {
//		for(String typeName: this.getAll()) {
//			DType currentType = getType(typeName);
//			currentType.performTypeReplacement(spec);
//		}
//	}
//    
}