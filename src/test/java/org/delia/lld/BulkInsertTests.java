package org.delia.lld;

import org.delia.DeliaOptions;
import org.delia.ast.TestBase;
import org.delia.ast.code.HLDTestHelper;
import org.delia.compiler.ast.AST;
import org.delia.dbimpl.ExpTestHelper;
import org.delia.hld.DeliaExecutable;
import org.delia.hld.dat.SyntheticDatService;
import org.delia.lld.processor.LLDBuilder;
import org.delia.valuebuilder.ScalarValueBuilder;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 *
 */
public class BulkInsertTests extends TestBase {

    public static class SpanHolder {
        public List<LLD.LLInsert> statements = new ArrayList<>();

    }

    public static class BulkInsertBuilder {
        public List<LLD.LLStatement> process(List<LLD.LLStatement> statements) {
            List<Object> outList = buildListWithSpans(statements);

            List<LLD.LLStatement> resultL = processHolders(outList);
            return resultL;
        }

        //contiguous insert statements gathered into a spanholder object
        private List<Object> buildListWithSpans(List<LLD.LLStatement> statements) {
            boolean inRun = false;

            List<Object> outList = new ArrayList<>();
            for (LLD.LLStatement statement : statements) {
                if (statement instanceof LLD.LLInsert) {
                    LLD.LLInsert llInsert = (LLD.LLInsert) statement;
                    if (!inRun) {
                        inRun = true;
                        SpanHolder holder = new SpanHolder();
                        holder.statements.add(llInsert);
                        outList.add(holder);
                    } else {
                        SpanHolder holder = (SpanHolder) outList.get(outList.size() - 1);
                        holder.statements.add(llInsert);
                    }
                } else {
                    if (inRun) {
                        inRun = false;
                    }
                    outList.add(statement);
                }
            }
            return outList;
        }

        //replace each span with
        //(a) one or more LLBulkInserts
        //(b) its contents (LLInserts)
        //(c) mixture of (a) or (b)
        private List<LLD.LLStatement> processHolders(List<Object> list) {
            List<LLD.LLStatement> finalList = new ArrayList<>();
            for(Object obj: list) {
                if (obj instanceof SpanHolder) {
                    List<LLD.LLStatement> tmpL = processSpan((SpanHolder)obj);
                    finalList.addAll(tmpL);
                } else {
                    LLD.LLStatement stmt = (LLD.LLStatement) obj;
                    finalList.add(stmt);
                }
            }

            return finalList;
        }

        private List<LLD.LLStatement> processSpan(SpanHolder obj) {
            LLD.LLInsert candidate = null;

            for(LLD.LLInsert stmt: obj.statements) {
                if (candidate == null) {
                    candidate = stmt;
                } else {
                    if (isMatch(candidate, stmt)) {

                    }
                }
            }

        }


    }


    @Test
    public void test() {
        assertEquals(1, 1);
        DeliaExecutable executable = parseIntoHLD();

        SyntheticDatService datSvc = new SyntheticDatService();
        LLDBuilder builder = new LLDBuilder(factorySvc, datSvc, new DeliaOptions());
        builder.buildLLD(executable);
        dumpLL(executable.lldStatements);
    }

    //---

    @Before
    public void init() {
        super.init();
    }

    private DeliaExecutable parseIntoHLD() {
        AST.DeliaScript script = buildTwo();
        DeliaExecutable exec = HLDTestHelper.parseIntoHLD(script, factorySvc, new DeliaOptions());
        return exec;
    }

    private void dumpLL(List<LLD.LLStatement> statements) {
        log.log("--LL--");
        for (LLD.LLStatement hld : statements) {
            log.log(hld.toString());
        }
        log.log("--LL end.--");
    }

    protected AST.DeliaScript buildTwo() {
        ScalarValueBuilder valueBuilder = createValueBuilder();
        ExpTestHelper expHelper = new ExpTestHelper(factorySvc);
        AST.DeliaScript script = expHelper.buildScriptStart(false);

        AST.InsertStatementAst ins = new AST.InsertStatementAst();
        ins.typeName = "Person";
        ins.fields = Arrays.asList(expHelper.buildInsertFieldInt("id", 8),
                expHelper.buildInsertField("firstName", "sue"));
        script.add(ins);

        return script;
    }

}
