package org.delia.seed.code;

import org.delia.codegen.DeliaEntity;

import java.util.HashMap;
import java.util.Map;

public class MyEntity implements DeliaEntity {
    public Map<String, Object> fieldMap = new HashMap<>(); //TODO need concurrent map?

    @Override
    public Map<String, Object> internalSetValueMap() {
        return fieldMap;
    }
}
