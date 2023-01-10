package org.delia.lld.processor;

import org.delia.type.DStructType;

public interface AliasAssigner {
    String findAlias(DStructType structType);

//        String findAlias(DStructType structType, String fieldName);

    void dumpAliases();
}
