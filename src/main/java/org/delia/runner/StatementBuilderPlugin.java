package org.delia.runner;

import org.delia.compiler.ast.AST;

public interface StatementBuilderPlugin {

    AST.DeliaScript process(AST.DeliaScript script);

}
