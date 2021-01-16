package org.delia.db.newhls;


import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import org.delia.api.Delia;
import org.delia.api.DeliaSession;
import org.delia.api.DeliaSessionImpl;
import org.delia.assoc.CreateNewDatIdVisitor;
import org.delia.bdd.BDDBase;
import org.delia.builder.ConnectionBuilder;
import org.delia.builder.ConnectionInfo;
import org.delia.builder.DeliaBuilder;
import org.delia.compiler.ast.DeleteStatementExp;
import org.delia.compiler.ast.Exp;
import org.delia.compiler.ast.LetStatementExp;
import org.delia.compiler.ast.QueryExp;
import org.delia.compiler.ast.UpdateStatementExp;
import org.delia.compiler.ast.UpsertStatementExp;
import org.delia.dao.DeliaGenericDao;
import org.delia.db.DBType;
import org.delia.db.newhls.cud.HLDDeleteStatement;
import org.delia.db.newhls.cud.HLDUpdateStatement;
import org.delia.db.newhls.cud.HLDUpsertStatement;
import org.delia.db.sql.prepared.SqlStatement;
import org.delia.db.sql.prepared.SqlStatementGroup;
import org.delia.runner.DoNothingVarEvaluator;
import org.delia.runner.ResultValue;
import org.delia.sprig.SprigService;
import org.delia.sprig.SprigServiceImpl;
import org.delia.type.DRelation;
import org.delia.type.DValue;
import org.delia.zdb.ZDBInterfaceFactory;
import org.delia.zdb.mem.MemZDBInterfaceFactory;
import org.junit.After;

/**
 * 
 * 
 * @author Ian Rae
 *
 */
public class NewHLSTestBase extends BDDBase {
	
	protected HLDInnerManager mgr;
	
	
	protected String addSrc(String src0, String src) {
		return src0 + "\n" + src;
	}

	protected void dumpGrp(SqlStatementGroup stmgrp) {
		log.log("grp: %s", stmgrp.statementL.size());
		for(SqlStatement stm: stmgrp.statementL) {
			StringJoiner joiner = new StringJoiner(",");
			for(DValue dval: stm.paramL) {
				if (dval == null) {
					joiner.add("null");
				} else {
					joiner.add(resolveParamAsString(dval));
				}
			}

			log.log("%s -- %s", stm.sql, joiner.toString());
		}
	}
	
	private String resolveParamAsString(DValue dval){
		if (dval.getType().isRelationShape()) {
			DRelation drel = (DRelation) dval.asRelation();
			DValue inner = drel.getForeignKey();
			return inner.asString();
		} else {
			return dval.asString();
		}
	}
	

	protected HLDQueryStatement buildFromSrc(String src, int expectedJoins) {
		QueryExp queryExp = compileQuery(src);
		log.log(src);
		
		mgr = createManager();
		HLDQueryStatement hld = mgr.fullBuildQuery(queryExp);
		log.log(hld.toString());
		assertEquals(expectedJoins, hld.hldquery.joinL.size());
		return hld;
	}

	protected HLDInnerManager createManager() {
		SprigService sprigSvc = new SprigServiceImpl(delia.getFactoryService(), this.session.getExecutionContext().registry);
		return new HLDInnerManager(this.session.getExecutionContext().registry, delia.getFactoryService(), this.session.getDatIdMap(), sprigSvc);
	}
	
	protected void chkRawSql(HLDQueryStatement hld, String expected) {
		String sql = mgr.generateRawSql(hld);
		log.log(sql);
		assertEquals(expected, sql);
	}
	protected void chkFullSql(HLDQueryStatement hld, String expected, String...args) {
		SqlStatement stm = mgr.generateSql(hld);
		chkStm(stm, expected, args);
	}
	protected void chkStm(SqlStatement stm, String expected, String... args) {
		log.log(stm.sql);
		assertEquals(expected, stm.sql);
		assertEquals(args.length, stm.paramL.size());
		for(int i = 0; i < args.length; i++) {
			String arg = args[i];
			DValue dval = stm.paramL.get(i);
			if (arg == null) {
				assertEquals(null, dval);
			} else {
				String s = resolveParamAsString(dval);
				assertEquals(arg, s);
			}
		}
	}


	
	
	protected QueryExp compileQuery(String src) {
		LetStatementExp letStatement = compileQueryToLetStatement(src);
		
		QueryExp queryExp = (QueryExp) letStatement.value;
		return queryExp;
	}
	
	protected LetStatementExp compileQueryToLetStatement(String src) {
		DeliaSessionImpl sessimpl = doCompileStatement(src);
		LetStatementExp letStatement = findLet(sessimpl);
		return letStatement;
	}
	protected DeliaSessionImpl doCompileStatement(String src) {
		String initialSrc;
		if  (useCustomerManyToManySrc) {
			initialSrc = buildCustomerNNSrc();
		} else if (useCustomer11Src) {
			initialSrc = buildCustomer11Src();
		} else if (useCustomer11OtherWaySrc) {
			initialSrc = buildCustomer11OtherWasySrc();
		} else if (useCustomer1NSrc) {
			initialSrc = buildCustomer1NSrc();
		} else if (useCustomer1NOtherWaySrc) {
			initialSrc = buildCustomer1NOtherWaySrc();
		} else {
			initialSrc = buildSrc();
		}
		log.log("initial: " + initialSrc);
		
		DeliaGenericDao dao = createDao(); 
		boolean b = dao.initialize(initialSrc);
		assertEquals(true, b);

		Delia delia = dao.getDelia();
		this.session = dao.getMostRecentSession();
		ResultValue res = delia.continueExecution(src, session);
		
		DeliaSessionImpl sessimpl = (DeliaSessionImpl) session;
		return sessimpl;
	}

	protected LetStatementExp findLet(DeliaSession session) {
		DeliaSessionImpl sessimpl = (DeliaSessionImpl) session;
		for(Exp exp: sessimpl.mostRecentContinueExpL) {
			if (exp instanceof LetStatementExp) {
				return (LetStatementExp) exp;
			}
		}
		return null;
	}

	//---
	protected Delia delia;
	protected DeliaSession session;
	protected boolean useCustomerManyToManySrc = false;
	protected boolean useCustomer11Src = false;
	protected boolean useCustomer11OtherWaySrc = false;
	protected boolean useCustomer1NSrc = false;
	protected boolean useCustomer1NOtherWaySrc = false;
	protected boolean insertSomeRecords = false;
	protected boolean flipAssocTbl = false; //mosts tests assume CustomerAddressAssoc
	
	//---

	protected DeliaGenericDao createDao() {
		ConnectionInfo info = ConnectionBuilder.dbType(DBType.MEM).build();
		this.delia = DeliaBuilder.withConnection(info).build();
		MemZDBInterfaceFactory memDBinterface = (MemZDBInterfaceFactory) delia.getDBInterface();
		memDBinterface.createSingleMemDB();
		CreateNewDatIdVisitor.hackFlag = true;
		
//		if (flipAssocTbl) {
//			createTable(memDBinterface, "AddressCustomerDat1");
//		} else {
//			createTable(memDBinterface, "CustomerAddressDat1");
//		}
		
		return new DeliaGenericDao(delia);
	}
	
//	protected void createTable(MemZDBInterfaceFactory db, String tableName) {
//		TestCreatorHelper.createTable(db, tableName);
//	}

	protected String buildSrc() {
		String src = "type Flight struct {field1 int primaryKey, field2 int } end";
		src += "\n insert Flight {field1: 1, field2: 10}";
		src += "\n insert Flight {field1: 2, field2: 20}";
		return src;
	}
	protected String buildCustomerNNSrc() {
		String src = " type Customer struct {cid int unique, x int, relation addr Address many optional  } end";
		src += "\n type Address struct {id int unique, y int, relation cust Customer  many optional } end";
		
		if (insertSomeRecords) {
			src += "\n insert Customer {cid:55, x:10}";
			src += "\n insert Customer {cid:56, x:11}";
			src += "\n insert Address {id:100, y:20, cust:55}";
			src += "\n insert Address {id:101, y:20, cust:55}";
		}
		return src;
	}
	protected String buildCustomer11Src() {
		String src = " type Customer struct {cid int unique, x int, relation addr Address one optional parent  } end";
		src += "\n type Address struct {id int unique, y int, relation cust Customer  one optional } end";
		return src;
	}
	protected String buildCustomer11OtherWasySrc() {
		String src = " type Customer struct {cid int unique, x int, relation addr Address one optional   } end";
		src += "\n type Address struct {id int unique, y int, relation cust Customer  one optional parent} end";
		return src;
	}
	protected String buildCustomer1NSrc() {
		String src = " type Customer struct {cid int unique, x int, relation addr Address many optional  } end";
		src += "\n type Address struct {id int unique, y int, relation cust Customer  one optional } end";
		return src;
	}
	protected String buildCustomer1NOtherWaySrc() {
		String src = " type Customer struct {cid int unique, x int, relation addr Address one optional  } end";
		src += "\n type Address struct {id int unique, y int, relation cust Customer many optional } end";
		return src;
	}
	
	protected HLDDeleteStatement buildFromSrcDelete(String src, int expectedJoins) {
		DeleteStatementExp deleteExp = compileToDeleteStatement(src);
		QueryExp queryExp = deleteExp.queryExp;
		log.log(src);
		
		mgr = createManager(); 
		HLDDeleteStatement hlddel = mgr.fullBuildDelete(queryExp);
		log.log(hlddel.toString());
		assertEquals(expectedJoins, hlddel.hlddelete.hld.joinL.size());
		return hlddel;
	}

	protected DeleteStatementExp compileToDeleteStatement(String src) {
		DeliaSessionImpl sessimpl = doCompileStatement(src);
		for(Exp exp: sessimpl.mostRecentContinueExpL) {
			if (exp instanceof DeleteStatementExp) {
				return (DeleteStatementExp) exp;
			}
		}
		return null;
	}
	protected HLDUpdateStatement buildFromSrcUpdate(String src, int statementIndex) {
		UpdateStatementExp updateExp = compileToUpdateStatement(src, statementIndex);
		log.log(src);
		
		mgr = createManager(); 
		HLDUpdateStatement hldupdate = mgr.fullBuildUpdate(updateExp, new DoNothingVarEvaluator());
		log.log(hldupdate.toString());
		return hldupdate;
	}
	protected UpdateStatementExp compileToUpdateStatement(String src, int statementIndex) {
		DeliaSessionImpl sessimpl = doCompileStatement(src);
		List<Exp> list = sessimpl.mostRecentContinueExpL.stream().filter(exp -> exp instanceof UpdateStatementExp).collect(Collectors.toList());
		Exp exp = list.get(statementIndex);
		return (UpdateStatementExp) exp;
	}
	
	protected HLDUpsertStatement buildFromSrcUpsert(String src, int statementIndex) {
		UpsertStatementExp upsertExp = compileToUpsertStatement(src, statementIndex);
		log.log(src);
		
		mgr = createManager(); 
		HLDUpsertStatement hld = mgr.fullBuildUpsert(upsertExp, new DoNothingVarEvaluator());
		log.log(hld.toString());
		return hld;
	}
	protected UpsertStatementExp compileToUpsertStatement(String src, int statementIndex) {
		DeliaSessionImpl sessimpl = doCompileStatement(src);
		List<Exp> list = sessimpl.mostRecentContinueExpL.stream().filter(exp -> exp instanceof UpsertStatementExp).collect(Collectors.toList());
		Exp exp = list.get(statementIndex);
		return (UpsertStatementExp) exp;
	}
	

	@Override
	public ZDBInterfaceFactory createForTest() {
		return null;
//		MemZDBInterfaceFactory db = new MemZDBInterfaceFactory(createFactorySvc());
//		db.createSingleMemDB();
//		return db;
	}
	

	@After
	public void shutdown() {
//		System.out.println("sdfffffffffffffffffffffffffffffff");
//		TableExistenceServiceImpl.hackYesFlag = false;
		CreateNewDatIdVisitor.hackFlag = false;
	}
}
