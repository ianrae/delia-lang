package org.delia.compiler;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.InputMismatchException;
import org.delia.compiler.antlr.deliaLexer;
import org.delia.compiler.antlr.deliaParser;
import org.delia.compiler.ast.Exp;
import org.delia.compiler.impl.CompileScalarValueBuilder;
import org.delia.compiler.impl.CompilerResults;
import org.delia.compiler.impl.DeliaAntlrVisitor;
import org.delia.compiler.impl.ThrowingErrorListener;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.type.DTypeRegistry;
import org.delia.type.DTypeRegistryBuilder;
import org.delia.util.DeliaExceptionHelper;

public class DeliaCompiler extends ServiceBase {

    public DeliaCompiler(FactoryService factorySvc) {
        super(factorySvc);
    }

    public CompilerResults compile(String src) {
        if (src.trim().isEmpty()) {
            return new CompilerResults((Exp.ElementExp) null);
        }

        //step 1. create basic registry (built-in types)
        DTypeRegistryBuilder registryBuilder = new DTypeRegistryBuilder();
        registryBuilder.init();
        DTypeRegistry registry = registryBuilder.getRegistry();

        CharStream chstr = CharStreams.fromString(src);
        deliaLexer lexer = new deliaLexer(chstr);
        lexer.removeErrorListeners();
        lexer.addErrorListener(ThrowingErrorListener.INSTANCE);

        CommonTokenStream cts = new CommonTokenStream(lexer);
        deliaParser dp = new deliaParser(cts);
        dp.removeErrorListeners();
        dp.addErrorListener(ThrowingErrorListener.INSTANCE);

        DeliaAntlrVisitor visitor = new DeliaAntlrVisitor();
        visitor.builder = new CompileScalarValueBuilder(factorySvc, registry);

        CompilerResults zoo = null;
        try {
            deliaParser.DeliaStatementContext parseTree = dp.deliaStatement();
            visitor.builder = new CompileScalarValueBuilder(factorySvc, registry);
            zoo = visitor.visit(parseTree);
        } catch (InputMismatchException e) {
            String msg = e.getMessage();
            if (msg == null) {
                msg = "something";
            }
            DeliaExceptionHelper.throwError("parse-error", msg);
//        } catch(DeliaParseException e) {
//            String msg = e.getMessage();
//            if (msg == null) {
//                msg = "something";
//            }
//            DeliaExceptionHelper.throwError("xparse-error", msg);
        }
        return zoo;
    }

}
