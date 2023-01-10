package org.delia.lld.processor;

import org.delia.hld.HLD;
import org.delia.lld.LLD;

import java.util.List;

public interface LLDProcessor {
    void build(HLD.HLDStatement hldStatement, List<LLD.LLStatement> lldStatements, LLDBuilderContext ctx);
}
