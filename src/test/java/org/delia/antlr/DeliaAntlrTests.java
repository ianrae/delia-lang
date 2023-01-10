package org.delia.antlr;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.commons.lang3.StringUtils;
import org.delia.ast.TestBase;
import org.delia.compiler.antlr.deliaBaseVisitor;
import org.delia.compiler.antlr.deliaLexer;
import org.delia.compiler.antlr.deliaParser;
import org.delia.compiler.ast.Exp;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.type.DTypeRegistry;
import org.delia.type.DTypeRegistryBuilder;
import org.delia.valuebuilder.ScalarValueBuilder;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class DeliaAntlrTests extends TestBase {

    public static class FilterCompileResults {
        public String a;
        public Exp.ElementExp elem;
        public Exp.OperandExp operandExp;

        public FilterCompileResults(Exp.ElementExp elem) {
            this.elem = elem;
        }

        public FilterCompileResults(Exp.OperandExp operandExp) {
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
    }

    public static class OldElemVisitor extends deliaBaseVisitor<FilterCompileResults> {
        public ScalarValueBuilder builder;

        @Override
        public FilterCompileResults visitFilter(deliaParser.FilterContext ctx) {
            FilterCompileResults zoo = this.visitCexpr(ctx.cexpr());
            return zoo;
        }

        @Override
        public FilterCompileResults visitCexpr(deliaParser.CexprContext ctx) {
            FilterCompileResults zz = visitChildren(ctx);
            if (zz != null && zz.elem != null) {
                System.out.println("zz " + zz.elem.getClass().getSimpleName());
            }


            if (zz.elem instanceof Exp.ListExp) {
                Exp.OperatorExp exp = new Exp.OperatorExp();
                exp.negFlag = false;

                for(int i = 0; i < ctx.getChildCount(); i++) {
                    ParseTree cc = ctx.getChild(i);
                    System.out.println(String.format("  %s", cc.getText()));
                }

                if (ctx.getChildCount() > 0 && StringUtils.equals(ctx.getChild(0).getText(), "!")) {
                    exp.negFlag = true;
                }

                Exp.ListExp lexp = (Exp.ListExp) zz.elem;
                exp.op1 = new Exp.DottedExp(lexp.listL.get(0));
                exp.op2 = new Exp.DottedExp(lexp.listL.get(1));
                String op = ctx.getChild(1).getText();
                exp.op = op;
                return new FilterCompileResults(exp);
            } else if (zz.elem instanceof Exp.OperatorExp) {
                Exp.OperatorExp exp = (Exp.OperatorExp) zz.elem;
                exp.negFlag = false;
                if (ctx.getChildCount() > 0 && StringUtils.equals(ctx.getChild(0).getText(), "!")) {
                    exp.negFlag = true;
                }
                return zz;
            } else {
                return zz;
            }
        }

        @Override
        public FilterCompileResults visitElem(deliaParser.ElemContext ctx) {
            FilterCompileResults zoo = visitChildren(ctx);
            return zoo;
        }

        @Override
        public FilterCompileResults visitFn(deliaParser.FnContext ctx) {
            String s = StringUtils.substringBefore(ctx.getText(), "(");
            Exp.FunctionExp exp = new Exp.FunctionExp(s);
            FilterCompileResults zz = visitFnargs(ctx.fnargs());
            if (zz.elem == null) {

            } else if (zz.elem instanceof Exp.ListExp) {
                Exp.ListExp lexp = (Exp.ListExp) zz.elem;
                exp.argsL.addAll(lexp.listL);
            } else {
                exp.argsL.add(zz.elem);
            }
            return new FilterCompileResults(exp);
        }

        @Override
        public FilterCompileResults visitFnargs(deliaParser.FnargsContext ctx) {
            if (ctx == null) {
                return new FilterCompileResults((Exp.ElementExp)null);
            }
            FilterCompileResults zoo = visitChildren(ctx);
            return zoo;
        }

        @Override
        public FilterCompileResults visitName(deliaParser.NameContext ctx) {
            Exp.FieldExp vexp = new Exp.FieldExp(ctx.getText(), null);
            return new FilterCompileResults(vexp);
        }

        @Override
        public FilterCompileResults visitNum(deliaParser.NumContext ctx) {
            Exp.ValueExp vexp = new Exp.ValueExp();
            vexp.value = builder.buildInt(ctx.getText());
            return new FilterCompileResults(vexp);
        }
        @Override
        public FilterCompileResults visitNegNum(deliaParser.NegNumContext ctx) {
            Exp.ValueExp vexp = new Exp.ValueExp();
            vexp.value = builder.buildInt(ctx.getText());
            return new FilterCompileResults(vexp);
        }
        @Override
        public FilterCompileResults visitReal(deliaParser.RealContext ctx) {
            Exp.ValueExp vexp = new Exp.ValueExp();
            vexp.value = builder.buildNumber(ctx.getText());
            return new FilterCompileResults(vexp);
        }
        @Override
        public FilterCompileResults visitNegReal(deliaParser.NegRealContext ctx) {
            Exp.ValueExp vexp = new Exp.ValueExp();
            vexp.value = builder.buildNumber(ctx.getText());
            return new FilterCompileResults(vexp);
        }

        @Override
        public FilterCompileResults visitBool(deliaParser.BoolContext ctx) {
            Exp.ValueExp vexp = new Exp.ValueExp();
            vexp.value = builder.buildBoolean(ctx.getText());
            return new FilterCompileResults(vexp);
        }
        @Override
        public FilterCompileResults visitStr(deliaParser.StrContext ctx) {
            Exp.ValueExp vexp = new Exp.ValueExp();
            int pos = ctx.getText().indexOf('"');
            int posEnd = ctx.getText().indexOf( '"', pos + 1);
            String str = ctx.getText().substring(pos+1, posEnd);
            vexp.value = builder.buildString(str);
            return new FilterCompileResults(vexp);
        }
        @Override
        public FilterCompileResults visitStr2(deliaParser.Str2Context ctx) {
            Exp.ValueExp vexp = new Exp.ValueExp();
            int pos = ctx.getText().indexOf('\'');
            int posEnd = ctx.getText().indexOf( '\'', pos + 1);
            String str = ctx.getText().substring(pos+1, posEnd);
            vexp.value = builder.buildString(str);
            return new FilterCompileResults(vexp);
        }

        @Override
        protected FilterCompileResults aggregateResult(FilterCompileResults aggregate, FilterCompileResults nextResult) {
            if (aggregate != null && nextResult != null) {
                if (aggregate.elem instanceof Exp.ListExp) {
                    Exp.ListExp lexp = (Exp.ListExp) aggregate.elem;
                    lexp.listL.add(nextResult.elem);
                } else {
                    Exp.ListExp lexp = new Exp.ListExp();
                    lexp.listL.add(aggregate.elem);
                    lexp.listL.add(nextResult.elem);
                    aggregate.elem = lexp;
                }
            }

            if (aggregate != null) {
                return aggregate;
            } else {
                return nextResult;
            }
        }
    }

    //only compiles filter part of statement
    public static class DeliaFilterCompiler extends ServiceBase {

        public DeliaFilterCompiler(FactoryService factorySvc) {
            super(factorySvc);
        }

        public FilterCompileResults compile(String src) {
            //step 1. create basic registry (built-in types)
            DTypeRegistryBuilder registryBuilder = new DTypeRegistryBuilder();
            registryBuilder.init();
            DTypeRegistry registry = registryBuilder.getRegistry();

            CharStream chstr = CharStreams.fromString(src);
            deliaLexer lexer = new deliaLexer(chstr);
            CommonTokenStream cts = new CommonTokenStream(lexer);
            deliaParser dp = new deliaParser(cts);

            deliaParser.FilterContext parseTree = dp.filter();

            OldElemVisitor visitor = new OldElemVisitor();
            visitor.builder = factorySvc.createScalarValueBuilder(registry);

            FilterCompileResults zoo = visitor.visit(parseTree);
            return zoo;
        }

    }


    @Test
    public void test() throws IOException {
//        Zoo zoo = compileAndRun("[ true ]");
//        assertEquals("true", zoo.elem.strValue());

        chkParse("[false] ", "false");
        chkParse("[true] ", "true");
        chkParse("[ 15] ", "15");
        chkParse("[ -15] ", "-15");
        chkParse("[-0] ", "0");
        chkParse("[-0] ", "0");
        chkParse("[ x < 15] ", "x < 15");
        chkParse("[ x] ", "x");
        chkParse("[ x.y] ", "x.y");
        chkParse("[ f()] ", "f()");
        chkParse("[ a.f()] ", "a.f()");
        chkParse("[ f(1)] ", "f(1)");
        chkParse("[ f(1,z)] ", "f(1, z)");
        //chkParse("[ !(x < 7)] ", "!(x < 7)");
        chkParse("[55.6] ", "55.6");
        chkParse("[-55.6] ", "-55.6");
        chkParse("[-55.6] ", "-55.6");
        chkParse("[0.0] ", "0.0");
        chkParse("[-0.0] ", "-0.0");
        chkParse("['abc'] ", "abc");
        chkParse("[\"a and\"] ", "a and");
        chkParse("[''] ", "");
        chkParse("[\"\"] ", "");
    }

    @Test
    public void testDebug() throws IOException {
        chkParse("[ a] ", "a");
    }

    private void chkParse(String src, String s1) {
        FilterCompileResults zoo = compileAndRun(src);
        if (zoo.operandExp != null) {
            assertEquals(s1, zoo.operandExp.strValue());
        } else {
            assertEquals(s1, zoo.elem.strValue());
        }
    }

    private FilterCompileResults compileAndRun(String src) {
        DeliaFilterCompiler compiler = new DeliaFilterCompiler(factorySvc);
        return compiler.compile(src);
    }

    //---

    @Before
    public void init() {
        super.init();
    }


}
