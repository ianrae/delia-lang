package org.delia.migrationparser;

import org.delia.type.OrderedMap;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class OrderedMapEx {
    public OrderedMap omap = new OrderedMap();
    public Map<String,Boolean> parentMap = new ConcurrentHashMap<>(); //fieldname, flag
    public Map<String,Boolean> oneMap = new ConcurrentHashMap<>();
    public Map<String,Boolean> manyMap = new ConcurrentHashMap<>();

    public boolean isRelationField(String fieldName) {
        if (parentMap.containsKey(fieldName)) {
            return true;
        }
        if (oneMap.containsKey(fieldName)) {
            return true;
        }
        if (manyMap.containsKey(fieldName)) {
            return true;
        }
        return false;
    }

}
