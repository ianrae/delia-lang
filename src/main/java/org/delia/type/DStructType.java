package org.delia.type;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface DStructType extends DType {

    boolean fieldIsOptional(String fieldName);

    boolean fieldIsUnique(String fieldName);

    boolean fieldIsPrimaryKey(String fieldName);

    boolean fieldIsSerial(String fieldName);

    Optional<String> fieldHasDefaultValue(String fieldName); //if field has default value, it's string representation is returned here

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