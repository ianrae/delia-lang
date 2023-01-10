package org.delia.type;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.delia.util.DeliaExceptionHelper;


/**
 * Represents all the known types in a given Delia program.
 * Includes built-in types and all custom scalar and struct types.
 * 
 * @author Ian Rae
 *
 */
public class DTypeRegistry {
	private Map<DTypeName,DType> map = new ConcurrentHashMap<>();
	private List<DType> orderedList = new ArrayList<>();
	private static AtomicInteger nextBitIndex = new AtomicInteger(0); //thread-safe
	private DTypeHierarchy th;
	private DStructType schemaVersionType;
	private DStructType datType;
	
	public static final int NUM_BUILTIN_TYPES = 7; //8;

	public static DTypeName createDTypeName(String typeName) {
		return new DTypeName(null, typeName);
	}
	
	public synchronized void registerType(DTypeName typeName, DType dtype) {
        if (dtype == null || typeName == null || typeName.getTypeName().isEmpty()) {
            throw new IllegalArgumentException("name or type were null");
        }
		
	    dtype.setBitIndex(nextBitIndex.incrementAndGet());
	    
	    if (map.containsKey(typeName)) {
	    	DeliaExceptionHelper.throwError("redefine-type-not-allowed", "Type '%s' is already registered. Did you have it twice in your Delia source code?", typeName);
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

	public Set<DTypeName> getAll() {
		return map.keySet();
	}

	public int size() {
		return map.size();
	}
	
	public boolean existsType(DTypeName name) {
	    return getType(name) != null;
	}

//	public DType getType(String name) {
//		return map.get(createDTypeName(name));
//	}
	public DType getType(DTypeName typeName) {
		return map.get(typeName);
	}
	public DStructType getStructType(DTypeName typeName) {
		DType dtype = map.get(typeName);
		if (dtype != null && !dtype.isStructShape()) {
			DeliaExceptionHelper.throwError("not-struct-type", "Type '%s' is not a struct", typeName);
		}
		return (DStructType) dtype;
	}
	public DType getType(BuiltInTypes builtInType) {
		return map.get(createDTypeName(builtInType.name()));
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
	public DStructType findTypeOrSchemaVersionType(DTypeName typeName) {
		String schema = typeName.getSchema();
		//TODO for now schemaVersionType and DatType are in null schema
		if (schema == null && schemaVersionType != null && schemaVersionType.getName().equals(typeName)) {
			return schemaVersionType;
		}
		if (schema == null && datType != null && datType.getName().equals(typeName)) {
			return datType;
		}
		
		DType dtype = getType(typeName);
		if (dtype != null && dtype.isStructShape()) {
			return (DStructType) dtype;
		} else {
			return null; //TODO: is this a problem??
		}
	}
	
	public Set<String> getAllCustomTypes() {
		Set<String> list = new HashSet<>();
		for(DTypeName typeName: map.keySet()) {
			String s = BuiltInTypes.convertDTypeNameToDeliaName(typeName.getTypeName());
			if (s.equals(typeName)) {
				list.add(typeName.getTypeName());
			}
		}
		return list;
	}

	@Override
	public String toString() {
		//FUTURE: later limit to at most 50 type names
		StringJoiner joiner = new StringJoiner(",");
		for(DType type: orderedList) {
			joiner.add(type.getName());
		}
		return joiner.toString();
	}

}