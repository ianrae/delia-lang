package org.delia.hld;

import org.delia.compiler.ast.AST;
import org.delia.compiler.ast.Exp;
import org.delia.type.DValue;

import java.util.List;

public class ConfigureHLDStatementBuilder implements HLDStatementBuilder {
    @Override
    public void build(AST.StatementAst statementParam, List<HLD.HLDStatement> hldStatements, HLDBuilderContext ctx) {
        AST.ConfigureStatementAst statement = (AST.ConfigureStatementAst) statementParam;

        HLD.ConfigureHLDStatement hld = new HLD.ConfigureHLDStatement(statement.getLoc());
        hld.configName = statement.configName;
        hld.dval = getAsDValue(statement.scalarElem);

        hldStatements.add(hld);
    }

    private DValue getAsDValue(Exp.ElementExp scalarElem) {
        Exp.ValueExp exp = (Exp.ValueExp) scalarElem;
        return exp.value;
    }

    @Override
    public void assignDATs(HLD.HLDStatement statement, List<HLD.HLDStatement> hldStatements, HLDBuilderContext ctx) {
    }

}
