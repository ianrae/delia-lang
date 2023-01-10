package org.delia.type;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.delia.util.DeliaExceptionHelper;

public class DStructTypeImpl extends DTypeImpl implements DStructType, DStructTypeInternal {

    private static final int MAX_INHERITANCE_DEPTH = 1000;
    private OrderedMap orderedMap;
    private List<TypePair> allFields; //lazy-created
    private PrimaryKey primaryKey; //can be null

    public DStructTypeImpl(Shape shape, String schema, String name, DType baseType, OrderedMap orderedMap, PrimaryKey primaryKey) {
        super(shape, schema, name, baseType);
        this.orderedMap = orderedMap;
        this.primaryKey = primaryKey;
    }
    @Override
    public void finishStructInitialization(DType baseType, OrderedMap orderedMap, PrimaryKey primaryKey) {
        this.baseType = baseType;
        this.orderedMap = orderedMap;
        this.primaryKey = primaryKey;
    }

    @Override
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

            DStructTypeImpl baseimpl = (DStructTypeImpl) baseType;
            b = baseimpl.orderedMap.isOptional(fieldName);
            if (b) {
                return true;
            }
            baseType = (DStructType) baseType.getBaseType();
        }
        DeliaExceptionHelper.throwError("struct-type-runaway", "Too many base types for field: %s", fieldName);
        return false;
    }
    @Override
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
            DStructTypeImpl baseimpl = (DStructTypeImpl) baseType;
            b = baseimpl.orderedMap.isUnique(fieldName);
            if (b) {
                return true;
            }
            baseType = (DStructType) baseType.getBaseType();
        }
        DeliaExceptionHelper.throwError("struct-type-runaway", "Too many base types for field: %s", fieldName);
        return false;
    }
    @Override
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
            DStructTypeImpl baseimpl = (DStructTypeImpl) baseType;
            b = baseimpl.orderedMap.isPrimaryKey(fieldName);
            if (b) {
                return true;
            }
            baseType = (DStructType) baseType.getBaseType();
        }
        DeliaExceptionHelper.throwError("struct-type-runaway", "Too many base types for field: %s", fieldName);
        return false;
    }
    @Override
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
            DStructTypeImpl baseimpl = (DStructTypeImpl) baseType;
            b = baseimpl.orderedMap.isSerial(fieldName);
            if (b) {
                return true;
            }
            baseType = (DStructType) baseType.getBaseType();
        }
        DeliaExceptionHelper.throwError("struct-type-runaway", "Too many base types for field: %s", fieldName);
        return false;
    }

    @Override
    public Map<String, DType> getDeclaredFields() {
        return orderedMap.map;
    }
    @Override
    public List<String> orderedList() {
        return orderedMap.orderedList;
    }

    //not thread-safe!!
    @Override
    public List<TypePair> getAllFields() {
        if (allFields == null) {
            allFields = doAllFieldsForType(this);
        }
        return allFields;
    }
    @Override
    public TypePair findField(String fieldName) {
        for(TypePair pair: getAllFields()) {
            if (pair.name.equals(fieldName)) {
                return pair;
            }
        }
        return null;
    }
    @Override
    public boolean hasField(String fieldName) {
        TypePair pair = findField(fieldName);
        return pair != null;
    }

    private List<TypePair> doAllFieldsForType(DStructTypeImpl dtype) {
        DStructTypeImpl baseType = (DStructTypeImpl) dtype.getBaseType();
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
    public PrimaryKey getPrimaryKey() {
        return primaryKey;
    }

    @Override
    public String getSchema() {
        return schema;
    }

    //normally only done once when creating structtype
    @Override
    public void setSchema(String schema) {
        this.schema = schema;
    }

    public void setEffectiveShapeType(String fieldName, DType intType) {
        orderedMap.map.put(fieldName, intType);
        allFields = null; //force rebuild
    }
}