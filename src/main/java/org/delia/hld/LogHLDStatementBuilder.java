package org.delia.hld;

import org.delia.compiler.ast.AST;
import org.delia.compiler.ast.Exp;
import org.delia.type.DValue;

import java.util.List;

public class LogHLDStatementBuilder implements HLDStatementBuilder {
    @Override
    public void build(AST.StatementAst statementParam, List<HLD.HLDStatement> hldStatements, HLDBuilderContext ctx) {
        AST.LogStatementAst statement = (AST.LogStatementAst) statementParam;

        HLD.LogHLDStatement hld = new HLD.LogHLDStatement(statementParam.getLoc());
        hld.varName = statement.varName;
        hld.dval = getAsDValue(statement.scalarElem);

        hldStatements.add(hld);
    }

    private DValue getAsDValue(Exp.ElementExp scalarElem) {
        if (scalarElem == null) return null;
        Exp.ValueExp exp = (Exp.ValueExp) scalarElem;
        return exp.value;
    }

    @Override
    public void assignDATs(HLD.HLDStatement statement, List<HLD.HLDStatement> hldStatements, HLDBuilderContext ctx) {
    }

}
