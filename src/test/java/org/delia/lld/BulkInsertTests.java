package org.delia.lld;

import org.delia.DeliaOptions;
import org.delia.ast.TestBase;
import org.delia.ast.code.HLDTestHelper;
import org.delia.compiler.ast.AST;
import org.delia.dbimpl.ExpTestHelper;
import org.delia.hld.DeliaExecutable;
import org.delia.hld.dat.SyntheticDatService;
import org.delia.lld.processor.LLDBuilder;
import org.delia.runner.bulkinsert.BulkInsertBuilder;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 *
 */
public class BulkInsertTests extends TestBase {

    @Test
    public void test0() {
        options.bulkInsertEnabled = false;
        DeliaExecutable executable = parseIntoHLD(2);
        List<LLD.LLStatement> list2 = runBulkBuilder(executable, 3);
        dumpLL(list2);
        chkInsert(list2, 1);
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
    private DeliaOptions options = new DeliaOptions();

    @Before
    public void init() {
        super.init();
        options.bulkInsertEnabled = true;
    }

    private DeliaExecutable parseIntoHLD(int n) {
        return parseIntoHLD(n, "Person");
    }
    private DeliaExecutable parseIntoHLD(int n, String tableName) {
        AST.DeliaScript script = buildInserts(n, tableName);
        DeliaExecutable exec = HLDTestHelper.parseIntoHLD(script, factorySvc, options);
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
        LLDBuilder builder = new LLDBuilder(factorySvc, datSvc, options);
        builder.buildLLD(executable);
        dumpLL(executable.lldStatements);

        log.log("and..");
        BulkInsertBuilder bulkInsertBuilder = new BulkInsertBuilder(options);
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
