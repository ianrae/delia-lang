package org.delia.db;

import org.delia.lld.LLD;
import org.delia.type.DValue;

/**
 * An exteneded interface used by some dbTypes
 */
public interface DBExecutorEx {
    DValue execPreInsert(LLD.LLInsert stmt, DValue dval);

}
