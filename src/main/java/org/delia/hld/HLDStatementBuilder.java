package org.delia.hld;

import org.delia.compiler.ast.AST;

import java.util.List;

public interface HLDStatementBuilder {
    void build(AST.StatementAst statement, List<HLD.HLDStatement> hldStatements, HLDBuilderContext ctx);

    void assignDATs(HLD.HLDStatement statement, List<HLD.HLDStatement> hldStatements, HLDBuilderContext ctx);
}
