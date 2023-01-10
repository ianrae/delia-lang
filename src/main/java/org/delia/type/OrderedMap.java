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
    public Map<String,DType> map = new ConcurrentHashMap<>(); //fieldName,dtype
    public List<String> orderedList = new ArrayList<>();  //ordered by when added
    private Map<String,Boolean> optionalMap = new ConcurrentHashMap<>(); //fieldname,isOptional
    private Map<String,Boolean> uniqueMap = new ConcurrentHashMap<>(); //fieldname,isUnique
    private Map<String,Boolean> primaryKeyMap = new ConcurrentHashMap<>(); //fieldname,isPrimaryKey
    private Map<String,Boolean> serialMap = new ConcurrentHashMap<>(); //fieldname,isSerial
    
    public void add(String fieldName, DType type, boolean optional, boolean unique, boolean primaryKey, boolean serial) {
    	//ConcurrentHashMap doesn't allow null key
    	if (fieldName == null) {
    		throw new IllegalArgumentException("OrderedMap doesn't allow null key");
    	}
    	//ConcurrentHashMap doesn't allow null type.
    	//This can occur when we have circular dependency between relations
    	//(such as a Customer and Address)
    	//we end up re-executing the types. so simply silently return here.
    	if (type == null) {
    		return;
    	}
        map.put(fieldName, type);
        optionalMap.put(fieldName, optional);
        uniqueMap.put(fieldName, unique);
        primaryKeyMap.put(fieldName, primaryKey);
        serialMap.put(fieldName, serial);
        orderedList.add(fieldName);
    }
    public boolean containsKey(String fieldName) {
        return map.containsKey(fieldName);
    }
    public boolean isOptional(String fieldName) {
        Boolean bb = optionalMap.get(fieldName);
        return (bb == null) ? false : bb;
    }
    public boolean isUnique(String fieldName) {
        Boolean bb = uniqueMap.get(fieldName);
        return (bb == null) ? false : bb;
    }
    public boolean isPrimaryKey(String fieldName) {
        Boolean bb = primaryKeyMap.get(fieldName);
        return (bb == null) ? false : bb;
    }
    public boolean isSerial(String fieldName) {
        Boolean bb = serialMap.get(fieldName);
        return (bb == null) ? false : bb;
    }

}
