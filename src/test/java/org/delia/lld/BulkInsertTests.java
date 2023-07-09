package org.delia.lld;

import org.delia.DeliaOptions;
import org.delia.ast.TestBase;
import org.delia.ast.code.HLDTestHelper;
import org.delia.compiler.ast.AST;
import org.delia.dbimpl.ExpTestHelper;
import org.delia.hld.DeliaExecutable;
import org.delia.hld.dat.SyntheticDatService;
import org.delia.lld.processor.LLDBuilder;
import org.delia.util.StrCreator;
import org.delia.util.StringUtil;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

/**
 *
 */
public class BulkInsertTests extends TestBase {

    //holds a contiguous set of LLInserts that may be candidates for bulk insert
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
            for (Object obj : list) {
                if (obj instanceof SpanHolder) {
                    List<LLD.LLStatement> tmpL = processSpan((SpanHolder) obj);
                    finalList.addAll(tmpL);
                } else {
                    LLD.LLStatement stmt = (LLD.LLStatement) obj;
                    finalList.add(stmt);
                }
            }

            return finalList;
        }

        private List<LLD.LLStatement> processSpan(SpanHolder obj) {
            List<LLD.LLStatement> resultL = new ArrayList<>();
            LLD.LLInsert candidate = null;
            LLD.LLBulkInsert bulkInsert = null;

            for (LLD.LLInsert stmt : obj.statements) {
                if (candidate == null) {
                    candidate = stmt;
                    bulkInsert = new LLD.LLBulkInsert(stmt.loc);
                    bulkInsert.first = stmt;
                    bulkInsert.insertStatements.add(stmt);
                } else {
                    if (isMatch(candidate, stmt)) {
                        bulkInsert.insertStatements.add(stmt);
                    } else {
                        addBulkIfNeeded(bulkInsert, resultL);

                        candidate = stmt;
                        bulkInsert = new LLD.LLBulkInsert(stmt.loc);
                        bulkInsert.first = stmt;
                        bulkInsert.insertStatements.add(stmt);
                    }
                }
            }

            addBulkIfNeeded(bulkInsert, resultL);
            return resultL;
        }

        private boolean isMatch(LLD.LLInsert candidate, LLD.LLInsert stmt) {
            if (candidate.subQueryInfo != null || stmt.subQueryInfo != null) {
                return false;
            }
            String fingerprint = buildFingerprint(candidate);
            String fingerprint2 = buildFingerprint(stmt);
            return fingerprint.equals(fingerprint2);
        }

        //TODO: do we need to include fields in fingerprint?
        private String buildFingerprint(LLD.LLInsert candidate) {
            StrCreator sc = new StrCreator();
            sc.o("%s;", candidate.getTableName());
            List<String> nameL = candidate.fieldL.stream().map(x -> x.field.getFieldName()).collect(Collectors.toList());
            sc.addStr(StringUtil.flatten(nameL));
            return sc.toString();
        }

        private void addBulkIfNeeded(LLD.LLBulkInsert bulkInsert, List<LLD.LLStatement> resultL) {
            if (bulkInsert != null) {
                if (bulkInsert.insertStatements.size() == 1) { //just one? not a bulk insert
                    resultL.add(bulkInsert.insertStatements.get(0));
                } else {
                    resultL.add(bulkInsert);
                }
            }
        }


    }

    @Test
    public void test1() {
        DeliaExecutable executable = parseIntoHLD(1);
        List<LLD.LLStatement> list2 = runBulkBuilder(executable, 2);
        dumpLL(list2);
        chkInsert(list2, 1);
    }
    @Test
    public void test2() {
        DeliaExecutable executable = parseIntoHLD(2);
        List<LLD.LLStatement> list2 = runBulkBuilder(executable, 2);
        dumpLL(list2);
        chkBulk(list2, 1, 2);
    }
    @Test
    public void test3() {
        DeliaExecutable executable = parseIntoHLD(3);
        List<LLD.LLStatement> list2 = runBulkBuilder(executable, 2);
        dumpLL(list2);
        chkBulk(list2, 1, 3);
    }

    @Test
    public void test3a() {
        DeliaExecutable executable = parseIntoHLD(3, "Other");
        List<LLD.LLStatement> list2 = runBulkBuilder(executable, 4);
        dumpLL(list2);
        chkBulk(list2, 3, 2);
    }

    //---

    @Before
    public void init() {
        super.init();
    }

    private DeliaExecutable parseIntoHLD(int n) {
        return parseIntoHLD(n, "Person");
    }
    private DeliaExecutable parseIntoHLD(int n, String tableName) {
        AST.DeliaScript script = buildInserts(n, tableName);
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

    protected AST.DeliaScript buildInserts(int n, String tableName) {
        ExpTestHelper expHelper = new ExpTestHelper(factorySvc);
        AST.DeliaScript script = expHelper.buildScriptStart(false, !tableName.equals("Person"));

        if (n >= 2) {
            AST.InsertStatementAst ins = new AST.InsertStatementAst();
            ins.typeName = tableName;
            ins.fields = Arrays.asList(expHelper.buildInsertFieldInt("id", 8),
                    expHelper.buildInsertField("firstName", "sue"));
            script.add(ins);
        }
        if (n >= 3) {
            AST.InsertStatementAst ins = new AST.InsertStatementAst();
            ins.typeName = tableName;
            ins.fields = Arrays.asList(expHelper.buildInsertFieldInt("id", 8),
                    expHelper.buildInsertField("firstName", "sue"));
            script.add(ins);
        }

        return script;
    }

    private List<LLD.LLStatement> runBulkBuilder(DeliaExecutable executable, int expectedSize) {
        SyntheticDatService datSvc = new SyntheticDatService();
        LLDBuilder builder = new LLDBuilder(factorySvc, datSvc, new DeliaOptions());
        builder.buildLLD(executable);
        dumpLL(executable.lldStatements);

        log.log("and..");
        BulkInsertBuilder bulkInsertBuilder = new BulkInsertBuilder();
        List<LLD.LLStatement> list2 = bulkInsertBuilder.process(executable.lldStatements);
        assertEquals(expectedSize, list2.size());
        return list2;
    }

    private void chkBulk(List<LLD.LLStatement> list, int i, int size) {
        LLD.LLBulkInsert bulkInsert = (LLD.LLBulkInsert) list.get(i);
        assertEquals(size, bulkInsert.insertStatements.size());
    }
    private void chkInsert(List<LLD.LLStatement> list, int i) {
        LLD.LLInsert insert = (LLD.LLInsert) list.get(i);
    }
}
