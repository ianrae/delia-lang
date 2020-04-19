package org.delia.type;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Note. unique and primaryKey are orthagonal. Even though primaryKey implies uniquess you
 * need to check for both.
 * 
 * This class is thread-safe.
 * @author Ian Rae
 *
 */
public class OrderedMap {
    public Map<String,DType> map = new ConcurrentHashMap<>();
    public List<String> orderedList = new ArrayList<>();  //ordered by when added
    private Map<String,Boolean> optionalMap = new ConcurrentHashMap<>();
    private Map<String,Boolean> uniqueMap = new ConcurrentHashMap<>();
    private Map<String,Boolean> primaryKeyMap = new ConcurrentHashMap<>();
    private Map<String,Boolean> serialMap = new ConcurrentHashMap<>();
    
    public void add(String name, DType type, boolean optional, boolean unique, boolean primaryKey, boolean serial) {
    	//ConcurrentHashMap doesn't allow null key
    	if (name == null) {
    		throw new IllegalArgumentException("OrderedMap doesn't allow null key");
    	}
    	//ConcurrentHashMap doesn't allow null type.
    	//This can occur when we have circular dependency between relations
    	//(such as a Customer and Address)
    	//we end up re-executing the types. so simply silently return here.
    	if (type == null) {
    		return;
    	}
        map.put(name, type);
        optionalMap.put(name, optional);
        uniqueMap.put(name, unique);
        primaryKeyMap.put(name, primaryKey);
        serialMap.put(name, serial);
        orderedList.add(name);
    }
    public boolean containsKey(String name) {
        return map.containsKey(name);
    }
    public boolean isOptional(String name) {
        Boolean bb = optionalMap.get(name);
        return (bb == null) ? false : bb;
    }
    public boolean isUnique(String name) {
        Boolean bb = uniqueMap.get(name);
        return (bb == null) ? false : bb;
    }
    public boolean isPrimaryKey(String name) {
        Boolean bb = primaryKeyMap.get(name);
        return (bb == null) ? false : bb;
    }
    public boolean isSerial(String name) {
        Boolean bb = serialMap.get(name);
        return (bb == null) ? false : bb;
    }
}
