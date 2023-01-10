package org.delia.runner;

import org.delia.compiler.ast.AST;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.compiler.impl.CompilerResults;
import org.delia.compiler.DeliaCompiler;

public class DeliaStatementBuilder extends ServiceBase {
    private final SimpleDValueBuilder dvalBuilder;
    private StatementBuilderPlugin plugin;

    public DeliaStatementBuilder(FactoryService factorySvc) {
        super(factorySvc);
        this.dvalBuilder = new SimpleDValueBuilder(factorySvc);
    }

    public CompilerResults compile(String src) {
        DeliaCompiler compiler = new DeliaCompiler(factorySvc);
        return compiler.compile(src);
    }

    public AST.DeliaScript buildScript(CompilerResults compileRes) {
        AST.DeliaScript script = buildScriptStart();
        script.statements.addAll(compileRes.getStatements());
        return script;
    }

    private AST.DeliaScript buildScriptStart() {
        AST.DeliaScript script = new AST.DeliaScript();
        if (plugin == null) {
            return script;
        }

        return plugin.process(script);
    }

    public StatementBuilderPlugin getPlugin() {
        return plugin;
    }

    public void setPlugin(StatementBuilderPlugin plugin) {
        this.plugin = plugin;
    }

}
