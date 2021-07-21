package org.delia.db.hld.cud;

import static org.junit.Assert.assertEquals;

import org.delia.db.DBType;
import org.delia.db.SqlStatement;
import org.delia.db.SqlStatementGroup;
import org.delia.db.hld.NewHLSTestBase;
import org.delia.hld.HLDBuildService;
import org.delia.hld.HLDBuildServiceImpl;
import org.delia.hld.HLDFactory;
import org.delia.hld.HLDFactoryImpl;
import org.delia.hld.cud.HLDToSQLConverter;
import org.delia.hld.cud.HLDUpsertStatement;
import org.delia.sprig.SprigService;
import org.delia.sprig.SprigServiceImpl;
import org.junit.Test;

/**
 *
 * @author Ian Rae
 *
 */
public class UpsertPostgresTests extends NewHLSTestBase {

    // --- 1:1 ---
    @Test
    public void test1() {
        useCustomer11Src = true;
        String src = "upsert Customer[1] {x: 45}";

        HLDUpsertStatement hldupsert = buildFromSrcUpsert(src, 0);
//		chkUpsertSql(hldupsert, 1, "UPDATE Customer as t0 SET t0.x = ? WHERE t0.cid=?", "45", "1");
        chkUpsertSql(hldupsert, 1, "MERGE INTO Customer KEY(cid) VALUES(?, ?)", "1", "45");
    }
    @Test
    public void test2() {
        useCustomer11Src = true;
        String src = "upsert Address[100] { y: 45}";

        HLDUpsertStatement hldupsert = buildFromSrcUpsert(src, 0);
//		chkUpsertSql(hldupsert, 1, "UPDATE Address SET t0.y = ? WHERE t0.id=?", "45", "100");
        chkUpsertSql(hldupsert, 1, "MERGE INTO Address KEY(id) VALUES(?, ?)", "100", "45");
    }
    @Test
    public void test2a() {
        useCustomer11Src = true;
        String src0 = "insert Customer {cid: 55, x: 45}";
        String src = addSrc(src0, "upsert Address[1] {y: 45, cust:55}");

        HLDUpsertStatement hldupsert = buildFromSrcUpsert(src, 0);
        SqlStatementGroup stmgrp = genUpsertSql(hldupsert, 1);
        dumpGrp(stmgrp);
        chkUpsertSql(stmgrp, 0, "MERGE INTO Address KEY(id) VALUES(?, ?, ?)", "1", "45", "55");
    }

    // --- 1:N ---
    @Test
    public void test1N() {
        useCustomer1NSrc = true;
        String src = "upsert Customer[1] {x: 45}";

        HLDUpsertStatement hldupsert = buildFromSrcUpsert(src, 0);
//		chkUpsertSql(hldupsert, 1, "UPDATE Customer as t0 SET t0.x = ? WHERE t0.cid=?", "45", "1");
        chkUpsertSql(hldupsert, 1, "MERGE INTO Customer KEY(cid) VALUES(?, ?)", "1", "45");
    }
    @Test
    public void test1N2() {
        useCustomer1NSrc = true;
        String src = "upsert Address[100] {y: 45}";

        HLDUpsertStatement hldupsert = buildFromSrcUpsert(src, 0);
//		chkUpsertSql(hldupsert, 1, "UPDATE Address SET t0.y = ? WHERE t0.id=?", "45", "100");
        chkUpsertSql(hldupsert, 1, "MERGE INTO Address KEY(id) VALUES(?, ?)", "100", "45");
    }
    @Test
    public void test1N2a() {
        useCustomer1NSrc = true;
        String src0 = "insert Customer {cid: 55, x: 45}";
        String src = addSrc(src0, "upsert Address[100] {y: 45, cust:55}");

        HLDUpsertStatement hldupsert = buildFromSrcUpsert(src, 0);
        SqlStatementGroup stmgrp = genUpsertSql(hldupsert, 1);
        dumpGrp(stmgrp);

//		chkUpsertSql(stmgrp, 0, "UPDATE Address SET t0.y = ?, t0.cust = ? WHERE t0.id=?", "45", "55", "100");
        chkUpsertSql(stmgrp, 0, "MERGE INTO Address KEY(id) VALUES(?, ?, ?)", "100", "45", "55");
    }
    @Test
    public void test1NInsertParent() {
        //adapted from t0-insert-parent.txt: add workers
        useCustomer1NSrc = true;
        String src0 = "insert Customer {cid: 55, x: 45}";
        String src = addSrc(src0, "insert Address {id: 100, y: 45}");
        src = addSrc(src, "insert Address {id: '101', y:46 }");
        src = addSrc(src, "upsert Customer[56] {x:66, addr: ['100','101'] }");

        HLDUpsertStatement hldupsert = buildFromSrcUpsert(src, 0);
        SqlStatementGroup stmgrp = genUpsertSql(hldupsert, 3);
        dumpGrp(stmgrp);
        chkUpsertSql(stmgrp, 0, "MERGE INTO Customer KEY(cid) VALUES(?, ?)", "56", "66");
        chkUpsertSql(stmgrp, 1, "UPDATE Address SET cust = ? WHERE id=?", "56", "100");
        chkUpsertSql(stmgrp, 2, "UPDATE Address SET cust = ? WHERE id=?", "56", "101");
    }

    // --- M:N ---
    @Test
    public void testMN() {
        useCustomerManyToManySrc = true;
        String src = "upsert Customer[1] { x: 45}";

        HLDUpsertStatement hldupsert = buildFromSrcUpsert(src, 0);
        chkUpsertSql(hldupsert, 1, "MERGE INTO Customer KEY(cid) VALUES(?, ?)", "1", "45");
    }

    @Test
    public void testMN2() {
        useCustomerManyToManySrc = true;
        String src = "upsert Address[1] { y: 45}";

        HLDUpsertStatement hldupsert = buildFromSrcUpsert(src, 0);
        chkUpsertSql(hldupsert, 1, "MERGE INTO Address KEY(id) VALUES(?, ?)", "1", "45");
    }
    @Test
    public void testMNScenario1() {
        useCustomerManyToManySrc = true;
        String src0 = "insert Customer {cid: 55, x: 45}";
        String src = addSrc(src0, "upsert Address[100] {y: 45, cust:55}");
        //upsert by pk
        HLDUpsertStatement hldupsert = buildFromSrcUpsert(src, 0);
        SqlStatementGroup stmgrp = genUpsertSql(hldupsert, 3);
        dumpGrp(stmgrp);
        chkUpsertSql(stmgrp, 0, "INSERT INTO Address (id, y) VALUES(?, ?) ON CONFLICT(id) DO UPDATE SET y = ?", "100", "45", "45");
        chkUpsertSql(stmgrp, 1, "DELETE FROM CustomerAddressDat1 as t1 WHERE t1.rightv = ? AND t1.leftv <> ?", "100", "55");
        chkUpsertSql(stmgrp, 2, "INSERT INTO CustomerAddressDat1 VALUES(?, ?) ON CONFLICT(leftv,rightv) DO UPDATE SET rightv = ?", "55", "100", "100");
    }
    //scenario2 not support by upsert
    //scenario3 not support by upsert

    @Test
    public void testMNUpsertParent() {
        //adapted from t0-insert-parent.txt: add workers
        useCustomerManyToManySrc = true;
        String src0 = "insert Customer {cid: 55, x: 45}";
        String src = addSrc(src0, "insert Address {id: 100, y: 45}");
        src = addSrc(src, "insert Address {id: '101', y:46 }");
        src = addSrc(src, "upsert Customer[56] { x:66, addr: ['100','101'] }");

        HLDUpsertStatement hldupsert = buildFromSrcUpsert(src, 0);
        SqlStatementGroup stmgrp = genUpsertSql(hldupsert, 4);
        dumpGrp(stmgrp);
        chkUpsertSql(stmgrp, 0, "INSERT INTO Customer (cid, x) VALUES(?, ?) ON CONFLICT(cid) DO UPDATE SET x = ?", "56", "66", "66");
        chkUpsertSql(stmgrp, 1, "DELETE FROM CustomerAddressDat1 as t1 WHERE t1.leftv = ? AND t1.rightv <> ?", "56", "100");
        chkUpsertSql(stmgrp, 2, "INSERT INTO CustomerAddressDat1 VALUES(?, ?) ON CONFLICT(leftv,rightv) DO UPDATE SET leftv = ?", "56", "100", "56");
        chkUpsertSql(stmgrp, 3, "INSERT INTO CustomerAddressDat1 VALUES(?, ?) ON CONFLICT(leftv,rightv) DO UPDATE SET leftv = ?", "56", "101", "56");
        //TODO: the above is correct but not efficient. only need a single:
        //DELETE FROM CustomerAddressDat1 WHERE leftv = 56
    }



    //-------------------------

    protected void chkUpsertSql(HLDUpsertStatement hldupsert, int numStatements, String expected, String...args) {
        SqlStatementGroup stmgrp = genUpsertSql(hldupsert, numStatements);
        SqlStatement stm = stmgrp.statementL.get(0);
        chkStm(stm, expected, args);
    }

    protected SqlStatementGroup genUpsertSql(HLDUpsertStatement hldupsert, int numStatements) {
        SqlStatementGroup stmgrp = mgr.generateSql(hldupsert);
        assertEquals(numStatements, stmgrp.statementL.size());
        return stmgrp;
    }
    protected void chkUpsertSql(SqlStatementGroup stmgrp, int index, String expected, String...args) {
        SqlStatement stm = stmgrp.statementL.get(index);
        chkStm(stm, expected, args);
    }

    @Override
    protected HLDBuildService createManager() {
        SprigService sprigSvc = new SprigServiceImpl(delia.getFactoryService(), this.session.getExecutionContext().registry);
        HLDFactory hldFactory = new HLDFactoryImpl();
        //create Postgres-specific sql converter PostgresSqlGeneratorFactory
        HLDToSQLConverter converter = hldFactory.createConverter(delia.getFactoryService(), session.getExecutionContext().registry, DBType.POSTGRES);
        return new HLDBuildServiceImpl(session.getExecutionContext().registry, delia.getFactoryService(), session.getDatIdMap(), sprigSvc, DBType.MEM, converter);
    }


}
