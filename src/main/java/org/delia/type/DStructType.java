package org.delia.type;

import java.util.List;
import java.util.Map;

public interface DStructType extends DType {

    boolean fieldIsOptional(String fieldName);

    boolean fieldIsUnique(String fieldName);

    boolean fieldIsPrimaryKey(String fieldName);

    boolean fieldIsSerial(String fieldName);

    Map<String, DType> getDeclaredFields();

    List<String> orderedList();

    //not thread-safe!!
    List<TypePair> getAllFields();

    TypePair findField(String fieldName);

    boolean hasField(String fieldName);

    PrimaryKey getPrimaryKey();

    String getSchema();

    //normally only done once when creating structtype
    void setSchema(String schema);

}