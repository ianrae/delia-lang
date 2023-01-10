package org.delia.lld.processor;

import org.delia.hld.HLD;
import org.delia.lld.LLD;

import java.util.Collection;
import java.util.List;

/**
 * We will have several varieties of association table updaters, from simple to fancy.
 */
public interface AssocLLDGenerator {

    Collection<? extends LLD.LLStatement> convertToUpdates(HLD.HLDUpdateUpsertBase hldStatement, List<LLD.LLInsert> assocInserts, LLDBuilderContext ctx);

    void genUpdateAssocAllRows(HLD.HLDUpdateUpsertBase hldStatement, LLD.LLInsert llInsert, List<LLD.LLStatement> statements, LLDBuilderContext ctx);

    void genUpdateAssocPK(HLD.HLDUpdateUpsertBase hldStatement, LLD.LLInsert llInsert, List<LLD.LLStatement> statements, boolean isFirst, LLDBuilderContext ctx);

    //TODO
    void genUpdateAssocFullQuery(HLD.HLDUpdateUpsertBase hldStatement, LLD.LLInsert llInsert, List<LLD.LLStatement> statements, LLDBuilderContext ctx);

}
