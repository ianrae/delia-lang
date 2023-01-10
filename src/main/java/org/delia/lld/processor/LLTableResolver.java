package org.delia.lld.processor;

import org.delia.lld.LLD;
import org.delia.type.DStructType;

public interface LLTableResolver {
    LLD.LLTable createOrGetLLTable(DStructType logicalType, LLD.LLTable llTable);

}
