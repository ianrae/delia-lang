package org.delia.db.newhls.cud;


import static org.junit.Assert.assertEquals;

import org.delia.db.newhls.NewHLSTestBase;
import org.delia.db.sql.prepared.SqlStatement;
import org.delia.db.sql.prepared.SqlStatementGroup;
import org.junit.Test;

/**
 * TODO: when delete also delete DAT table, and non-optional child
 * @author Ian Rae
 *
 */
public class DeleteTests extends NewHLSTestBase {

	// --- filter tests ---
	@Test
	public void test1() {
		useCustomer11Src = true;
		String src = "delete Customer[55]";

		HLDDeleteStatement hlddelete = buildFromSrcDelete(src, 0); 
		SqlStatementGroup stmgrp = genDeleteSql(hlddelete, 2);
		dumpGrp(stmgrp);

		chkDeleteSql(stmgrp, 0, "UPDATE Address as t1 SET t1.cust = ? WHERE t1.cust = ?", null, "55");
		chkDeleteSql(stmgrp, 1, "DELETE FROM Customer as t0 WHERE t0.cid=?", "55");
	}
	@Test
	public void test1MandatoryChild() {
		useCustomer11MandatoryChildSrc = true;
		String src = "delete Customer[55]";

		HLDDeleteStatement hlddelete = buildFromSrcDelete(src, 0); 
		SqlStatementGroup stmgrp = genDeleteSql(hlddelete, 2);
		dumpGrp(stmgrp);

		chkDeleteSql(stmgrp, 0, "DELETE FROM Address as t1 WHERE t1.cust = ?", "55");
		chkDeleteSql(stmgrp, 1, "DELETE FROM Customer as t0 WHERE t0.cid=?", "55");
	}

	@Test
	public void test2() {
		useCustomer11Src = true;
		String src = "delete Address[100]";

		HLDDeleteStatement hlddelete = buildFromSrcDelete(src, 0); 
		SqlStatementGroup stmgrp = genDeleteSql(hlddelete, 1);
		dumpGrp(stmgrp);
		chkDeleteSql(stmgrp, 0, "DELETE FROM Address as t0 WHERE t0.id=?", "100");
	}
	@Test
	public void test2Mandatory() {
		useCustomer11MandatoryChildSrc = true;
		String src = "delete Address[100]";

		HLDDeleteStatement hlddelete = buildFromSrcDelete(src, 0); 
		SqlStatementGroup stmgrp = genDeleteSql(hlddelete, 2);
		dumpGrp(stmgrp);
		chkDeleteSql(stmgrp, 0, "DELETE FROM Customer as t1 WHERE t1.cid IN (SELECT t2.cust FROM Address as t2 WHERE t2.id=?)", "100");
		chkDeleteSql(stmgrp, 1, "DELETE FROM Address as t0 WHERE t0.id=?", "100");
	}
	@Test
	public void test3() {
		useCustomer11Src = true;
		String src = "delete Customer[x > 10]";

		HLDDeleteStatement hlddelete = buildFromSrcDelete(src, 0); 
		SqlStatementGroup stmgrp = genDeleteSql(hlddelete, 2);
		dumpGrp(stmgrp);
		chkDeleteSql(stmgrp, 0, "UPDATE Address as t1 SET t1.cust = ? WHERE t1.cust IN (SELECT t2.cid FROM Customer as t2 WHERE t2.x > ?)", null, "10");
		chkDeleteSql(stmgrp, 1, "DELETE FROM Customer as t0 WHERE t0.x > ?", "10");
	}


	//TODO: ****************** all remaining tests need to be done

	// --- 1:N ---
	@Test
	public void test1N() {
		useCustomer1NSrc = true;
		String src = "delete Customer[1]";

		HLDDeleteStatement hlddelete = buildFromSrcDelete(src, 0); 
		SqlStatementGroup stmgrp = genDeleteSql(hlddelete, 2);
		dumpGrp(stmgrp);
		//delete from Address inner join Customer as t0.id=t1.cust where (select count(*) from Address where t0.cust=1) > 0
		chkDeleteSql(stmgrp, 0, "UPDATE Address as t1 SET t1.cust = ? WHERE t1.cust = ?", null, "1");
		chkDeleteSql(stmgrp, 1, "DELETE FROM Customer as t0 WHERE t0.cid=?", "1");
	}
	@Test
	public void test1NMandatory() {
		useCustomer1NMandatoryChildSrc = true;
		String src = "delete Customer[1]";

		HLDDeleteStatement hlddelete = buildFromSrcDelete(src, 0); 
		SqlStatementGroup stmgrp = genDeleteSql(hlddelete, 2);
		dumpGrp(stmgrp);
		chkDeleteSql(stmgrp, 0, "DELETE FROM Address as t1 WHERE t1.cust IN (SELECT t2.cid FROM Customer as t2 INNER JOIN Address as t3 ON t2.cid=t3.cust WHERE t2.cid=? GROUP BY t2.cid HAVING COUNT(t2.cid)=1)", "1");
		chkDeleteSql(stmgrp, 1, "DELETE FROM Customer as t0 WHERE t0.cid=?", "1");
	}
	@Test
	public void test1NChild() {
		useCustomer1NSrc = true;
		String src = "delete Address[100]";

		HLDDeleteStatement hlddelete = buildFromSrcDelete(src, 0); 
		SqlStatementGroup stmgrp = genDeleteSql(hlddelete, 1);
		dumpGrp(stmgrp);
		chkDeleteSql(stmgrp, 0, "DELETE FROM Address as t0 WHERE t0.id=?", "100");
	}
	@Test
	public void test1NMandatoryChild() {
		useCustomer1NMandatoryChildSrc = true;
		String src = "delete Address[100]";

		HLDDeleteStatement hlddelete = buildFromSrcDelete(src, 0); 
		SqlStatementGroup stmgrp = genDeleteSql(hlddelete, 2);
		dumpGrp(stmgrp);
		//		chkDeleteSql(stmgrp, 0, "DELETE FROM Customer as t0 WHERE t0.cid IN (SELECT t1.cust FROM Address as t1 INNER JOIN Customer as t2 ON t1.cust=t2.cid WHERE t1.id=? GROUP BY t1.cust HAVING COUNT(t1.cid)=1)", null, "100");
		chkDeleteSql(stmgrp, 0, "DELETE FROM Customer as t1 WHERE t1.cid IN (SELECT t2.cid FROM Customer as t2 INNER JOIN Address as t3 ON t2.cid=t3.cust WHERE t2.id=? GROUP BY t2.cid HAVING COUNT(t2.cid)=1)", "100");
		chkDeleteSql(stmgrp, 1, "DELETE FROM Address as t0 WHERE t0.id=?", "100");
	}

	// --- M:N ---
	@Test
	public void testNN() {
		useCustomerManyToManySrc = true;
		String src = "delete Customer[1]";

		HLDDeleteStatement hlddelete = buildFromSrcDelete(src, 0); 
		SqlStatementGroup stmgrp = genDeleteSql(hlddelete, 2);
		dumpGrp(stmgrp);
		chkDeleteSql(stmgrp, 0, "DELETE FROM CustomerAddressDat1 as t1 WHERE t1.leftv = ?", "1");
		chkDeleteSql(stmgrp, 1, "DELETE FROM Customer as t0 WHERE t0.cid=?", "1");
	}
	@Test
	public void testNNMandatory() {
		useCustomerNNMandatoryChildSrc = true;
		String src = "delete Customer[1]";

		HLDDeleteStatement hlddelete = buildFromSrcDelete(src, 0); 
		SqlStatementGroup stmgrp = genDeleteSql(hlddelete, 2);
		dumpGrp(stmgrp);
		chkDeleteSql(stmgrp, 0, "DELETE FROM Address as t1 WHERE t1.cust IN (SELECT t2.cid FROM Customer as t2 INNER JOIN Address as t3 ON t2.cid=t3.cust WHERE t2.cid=? GROUP BY t2.cid HAVING COUNT(t2.cid)=1)", "1");
		chkDeleteSql(stmgrp, 1, "DELETE FROM Customer as t0 WHERE t0.cid=?", "1");
	}
	@Test
	public void testNNChild() {
		useCustomerManyToManySrc = true;
		String src = "delete Address[100]";

		HLDDeleteStatement hlddelete = buildFromSrcDelete(src, 0); 
		SqlStatementGroup stmgrp = genDeleteSql(hlddelete, 1);
		dumpGrp(stmgrp);
		chkDeleteSql(stmgrp, 0, "DELETE FROM Address as t0 WHERE t0.id=?", "100");
	}
	@Test
	public void testNNMandatoryChild() {
		useCustomerNNMandatoryChildSrc = true;
		String src = "delete Address[100]";

		HLDDeleteStatement hlddelete = buildFromSrcDelete(src, 0); 
		SqlStatementGroup stmgrp = genDeleteSql(hlddelete, 2);
		dumpGrp(stmgrp);
		//		chkDeleteSql(stmgrp, 0, "DELETE FROM Customer as t0 WHERE t0.cid IN (SELECT t1.cust FROM Address as t1 INNER JOIN Customer as t2 ON t1.cust=t2.cid WHERE t1.id=? GROUP BY t1.cust HAVING COUNT(t1.cid)=1)", null, "100");
		chkDeleteSql(stmgrp, 0, "DELETE FROM Customer as t1 WHERE t1.cid IN (SELECT t2.cid FROM Customer as t2 INNER JOIN Address as t3 ON t2.cid=t3.cust WHERE t2.id=? GROUP BY t2.cid HAVING COUNT(t2.cid)=1)", "100");
		chkDeleteSql(stmgrp, 1, "DELETE FROM Address as t0 WHERE t0.id=?", "100");
	}


	//-------------------------
	private boolean useCustomer11MandatoryChildSrc;
	private boolean useCustomer1NMandatoryChildSrc;
	private boolean useCustomerNNMandatoryChildSrc;

	@Override
	protected String buildSrc() {
		if (useCustomer11MandatoryChildSrc) {
			return buildCustomer11MandatoryChildSrc();
		} else if (useCustomer1NMandatoryChildSrc) {
			return buildCustomer1NMandatoryChildSrc();
		} else if (useCustomerNNMandatoryChildSrc) {
			return buildCustomerNNMandatoryChildSrc();
		} else {
			return super.buildSrc();
		}
	}


	private String buildCustomerNNMandatoryChildSrc() {
		String src = " type Customer struct {cid int unique, x int, relation addr Address many optional  } end";
		src += "\n type Address struct {id int unique, y int, relation cust Customer  many } end";

		if (insertSomeRecords) {
			src += "\n insert Customer {cid:55, x:10}";
			src += "\n insert Customer {cid:56, x:11}";
			src += "\n insert Address {id:100, y:20, cust:55}";
			src += "\n insert Address {id:101, y:20, cust:55}";
		}
		return src;
	}
	private String buildCustomer11MandatoryChildSrc() {
		String src = " type Customer struct {cid int unique, x int, relation addr Address one optional parent  } end";
		src += "\n type Address struct {id int unique, y int, relation cust Customer  one  } end";
		return src;
	}
	private String buildCustomer1NMandatoryChildSrc() {
		String src = " type Customer struct {cid int unique, x int, relation addr Address many optional  } end";
		src += "\n type Address struct {id int unique, y int, relation cust Customer one } end";
		return src;
	}


	protected SqlStatementGroup genDeleteSql(HLDDeleteStatement hldupdate, int numStatements) {
		SqlStatementGroup stmgrp = mgr.generateSql(hldupdate);
		assertEquals(numStatements, stmgrp.statementL.size());
		return stmgrp;
	}
	protected void chkDeleteSql(HLDDeleteStatement hlddel, String expected, String...args) {
		SqlStatementGroup stmgrp = mgr.generateSql(hlddel);
		SqlStatement stm = stmgrp.statementL.get(0);
		chkStm(stm, expected, args);
	}
	protected void chkDeleteSql(SqlStatementGroup stmgrp, int index, String expected, String...args) {
		SqlStatement stm = stmgrp.statementL.get(index);
		chkStm(stm, expected, args);
	}
}
