package org.delia.compiler.impl;

import org.delia.compiler.ast.AST;
import org.delia.compiler.ast.Exp;

import java.util.ArrayList;
import java.util.List;

public class CompilerResults {
    public Exp.ElementExp elem;
    public Exp.OperandExp operandExp;
    private List<AST.StatementAst> statements = new ArrayList<>();

    public CompilerResults(Exp.ElementExp elem) {
        this.elem = elem;
    }

    public CompilerResults(Exp.OperandExp operandExp) {
        this.operandExp = operandExp;
    }

    public Exp.WhereClause getAsWhereClause() {
        if (operandExp != null) {
            return new Exp.WhereClause(operandExp);
        } else {
            Exp.DottedExp dexp = new Exp.DottedExp(elem);
            return new Exp.WhereClause(dexp);
        }
    }

    public AST.LetStatementAst getLetStatementAst() {
        int n = statements.size();
        return statements.isEmpty() ? null : (AST.LetStatementAst) statements.get(n-1);
    }
    public void addLetStatementAst(AST.LetStatementAst stmt) {
        statements.add(stmt);
    }
    public void addStatementAst(AST.StatementAst stmt) {
        statements.add(stmt);
    }
    public List<AST.StatementAst> getStatements() {
        return statements;
    }
    public AST.StatementAst getStatementAst() {
        int n = statements.size();
        return statements.isEmpty() ? null : statements.get(n-1);
    }

}
