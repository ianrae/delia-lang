package org.delia.runner.bulkinsert;

import org.delia.lld.LLD;

import java.util.ArrayList;
import java.util.List;

//holds a contiguous set of LLInserts that may be candidates for bulk insert
public class SpanHolder {
    public List<LLD.LLInsert> statements = new ArrayList<>();
}
