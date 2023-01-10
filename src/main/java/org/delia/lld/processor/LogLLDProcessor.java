package org.delia.lld.processor;

import org.delia.hld.HLD;
import org.delia.lld.LLD;

import java.util.List;

public class LogLLDProcessor implements LLDProcessor {

    @Override
    public void build(HLD.HLDStatement hldStatementParam, List<LLD.LLStatement> lldStatements, LLDBuilderContext ctx) {
        HLD.LogHLDStatement statement = (HLD.LogHLDStatement) hldStatementParam;
        LLD.LLLog llLog = new LLD.LLLog(hldStatementParam.getLoc());
        llLog.varName = statement.varName;
        llLog.dvalue = statement.dval;

        lldStatements.add(llLog);
    }


}
