package org.delia.migration;


import org.delia.exec.DeliaRunnerTestBase;
import org.delia.log.SimpleLog;
import org.delia.migrationparser.parser.MigrationParser;
import org.delia.migrationparser.parser.Token;
import org.delia.migrationparser.parser.ast.AST;
import org.delia.util.StrCreator;
import org.delia.util.StringTrail;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Migration file has 2 parts
 * ALTERATIONS:
 * .DROP C
 * .RENAME C TO D
 * .ALTER C DROP F
 * .ALTER C RENAME F TO G2
 * .REMOVE y  //vars
 * .ALTER C ALTER f ...
 * .ALTER C ALTER relation f ...
 * .ALTER C ADD  f ...  or relation f ...
 * .ALTER C ADD f sizeof(64)
 * ADDITIONS:
 * //new types here
 * <p>
 * Plan
 * -at startup
 * -find all migration files 1-...
 * -for each
 * -parse initial delia
 * -parse next into delia + ASTs
 * -apply ASTs
 * -add additions and render total Delia
 * -generate _delia.delia
 * -if not MEM then generate flyway migration files
 * -maybe only for newest delia-migration files
 */

public class MigrationParserTests extends DeliaRunnerTestBase {


    @Test
    public void test() {
        String src = buildMigration();
        log(src);
        MigrationParser parser = new MigrationParser(new SimpleLog());
        List<Token> tokens = parser.parseIntoTokens(src);

        for (Token tok : tokens) {
            String ss = tok.value == null ? "" : tok.value;
            log(String.format("%d %s", tok.tokType, ss));
        }
        assertEquals(53, tokens.size());

        List<AST> asts = parser.parseIntoAST(tokens);
        assertEquals(8, asts.size());
    }

    @Test
    public void test2() {
        chkParse("AB", "2");
        chkParse(" AB", "2");
        chkParse(" AB ", "2");
        chkParse("  ", "");
        chkParse("ALTERATIONS:  ", "2;22");
        chkParse("ALTERATIONS: \nAB ", "2;22;20;2");
        chkParse("RENAME C TO DO", "2;2;2;2");
        chkParse("ALTER sizeof(54)", "2;2;23;3;24");
    }

    @Test
    public void testDebug() {
//        chkParse("  ", "");
        chkParse("ALTER sizeof(54)", "2;2;23;3;24");
    }


    //---

    @Before
    public void init() {
    }

    private String buildMigration() {
        StrCreator sc = new StrCreator();
        sc.o("// comment");
        sc.nl();
        sc.o("ALTERATIONS:");
        sc.nl();
        sc.o("DROP C");
        sc.nl();
        sc.o("RENAME C TO D");
        sc.nl();
        sc.o("ALTER C DROP F");
        sc.nl();
        sc.o("ALTER C RENAME F TO G2");
        sc.nl();
        sc.o("REMOVE y");
        sc.nl();
        sc.o("ALTER C ALTER f int optional");
        sc.nl();
        sc.o("ALTER C ALTER relation cust Customer optional many");
        sc.nl();
        sc.o("ALTER C ADD f int optional");
        sc.nl();
        sc.o("ADDITIONS:");
        sc.nl();
        sc.o("Other stuff..");
        sc.nl();
        return sc.toString();
    }

    private void chkParse(String src, String expected) {
        MigrationParser parser = new MigrationParser(new SimpleLog());
        List<Token> tokens = parser.parseIntoTokens(src);

        StringTrail trail = new StringTrail();
        tokens.forEach(x -> trail.add(String.format("%d", x.tokType)));
        String trailStr = trail.getTrail();
        log(trailStr);
        assertEquals(expected, trailStr);
    }

}