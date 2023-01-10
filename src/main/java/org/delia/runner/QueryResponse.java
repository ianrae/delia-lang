package org.delia.runner;


import org.delia.error.DeliaError;
import org.delia.type.DValue;

import java.util.List;

/**
 * The results of a database query.
 *
 * @author Ian Rae
 */
public class QueryResponse {
    public boolean ok;
    public DeliaError err;
    public List<DValue> dvalList;

    public boolean emptyResults() {
        return dvalList == null || dvalList.isEmpty();
    }

    public DValue getOne() {
        if (dvalList.size() > 1) {
            throw new RuntimeException("getOne found more than one!!");
        }
        return dvalList.get(0);
    }
}