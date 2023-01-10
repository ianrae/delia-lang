package org.delia.lld.processor;

import org.delia.hld.HLD;
import org.delia.lld.LLD;

import java.util.List;

public class LetAssignLLDProcessor implements LLDProcessor {

    @Override
    public void build(HLD.HLDStatement hldStatementParam, List<LLD.LLStatement> lldStatements, LLDBuilderContext ctx) {
        HLD.LetAssignHLDStatement statement = (HLD.LetAssignHLDStatement) hldStatementParam;
        LLD.LLAssign llAssign = new LLD.LLAssign(hldStatementParam.getLoc());
        llAssign.dtype = statement.dtype;
        llAssign.rhsExpr = statement.rhsExpr;
        llAssign.varName = statement.varName;
        llAssign.dvalue = statement.dvalue;

        lldStatements.add(llAssign);
    }


}
