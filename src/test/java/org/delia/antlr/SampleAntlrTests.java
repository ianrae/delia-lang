package org.delia.antlr;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.NotNull;
import org.delia.compiler.antlr.listBaseVisitor;
import org.delia.compiler.antlr.listLexer;
import org.delia.compiler.antlr.listParser;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SampleAntlrTests {

    public class SumVisitor extends listBaseVisitor<Integer> {

        @Override
        public Integer visitList(@NotNull listParser.ListContext ctx) {
            return ctx.elems() == null ? 0 : this.visitElems(ctx.elems());
        }

        @Override
        public Integer visitElems(@NotNull listParser.ElemsContext ctx) {
            int sum = 0;
            for (listParser.ElemContext elemContext : ctx.elem()) {
                sum += this.visitElem(elemContext);
            }
            return sum;
        }

        @Override
        public Integer visitElem(@NotNull listParser.ElemContext ctx) {
            return Integer.valueOf(ctx.NUM().getText());
        }
    }


    @Test
    public void test() {
        assertEquals(1, 1);
        listLexer lexer = new listLexer(new ANTLRInputStream("[1, 2, 3]"));
        listParser parser = new listParser(new CommonTokenStream(lexer));
        Integer sum = new SumVisitor().visit(parser.list());
        System.out.println("sum=" + sum);
    }

    @Test
    public void test2() {
        assertEquals(1, 1);
        listLexer lexer = new listLexer(new ANTLRInputStream("2"));
        listParser parser = new listParser(new CommonTokenStream(lexer));
        Integer sum = new SumVisitor().visit(parser.list());
        System.out.println("sum=" + sum);
    }
}
