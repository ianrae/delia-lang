package org.delia.lld.processor;

import org.delia.hld.HLD;
import org.delia.lld.LLD;

import java.util.List;

public class ConfigureLLDProcessor implements LLDProcessor {

    @Override
    public void build(HLD.HLDStatement hldStatementParam, List<LLD.LLStatement> lldStatements, LLDBuilderContext ctx) {
        HLD.ConfigureHLDStatement statement = (HLD.ConfigureHLDStatement) hldStatementParam;
        LLD.LLConfigure llAssign = new LLD.LLConfigure(hldStatementParam.getLoc());
        llAssign.configName = statement.configName;
        llAssign.dvalue = statement.dval;

        lldStatements.add(llAssign);
    }


}
