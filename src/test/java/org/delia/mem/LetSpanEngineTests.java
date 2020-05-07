package org.delia.mem;


import static org.junit.Assert.*;

import org.delia.api.Delia;
import org.delia.bdd.NewBDDBase;
import org.delia.builder.ConnectionInfo;
import org.delia.builder.DeliaBuilder;
import org.delia.compiler.ast.QueryExp;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.builder.ConnectionBuilder;
import org.delia.dao.DeliaDao;
import org.delia.db.DBInterface;
import org.delia.db.DBType;
import org.delia.db.memdb.MemDBInterface;
import org.delia.log.Log;
import org.delia.log.StandardLogFactory;
import org.delia.runner.DeliaException;
import org.delia.runner.QueryResponse;
import org.delia.runner.ResultValue;
import org.delia.type.DValue;
import org.junit.Before;
import org.junit.Test;


public class LetSpanEngineTests extends NewBDDBase {

	public static class LetSpanEngine extends ServiceBase {

		public LetSpanEngine(FactoryService factorySvc) {
			super(factorySvc);
		}

		
		public QueryResponse process(QueryExp queryExp, QueryResponse qresp0) {
			return null;
		}
	}


	@Test
	public void testRaw() {
		String src = buildSrc();
		DeliaDao dao = createDao(); 
		boolean b = dao.initialize(src);
		assertEquals(true, b);

		Delia delia = dao.getDelia();
		ResultValue res = delia.continueExecution(src, dao.getMostRecentSession());
	}



	//---

	@Before
	public void init() {
	}

	private DeliaDao createDao() {
		ConnectionInfo info = ConnectionBuilder.dbType(DBType.MEM).build();
		Delia delia = DeliaBuilder.withConnection(info).build();
		return new DeliaDao(delia);
	}

	private String buildSrc() {
		String src = "type Flight struct {field1 int unique, field2 int } end";
		src += "\n insert Flight {field1: 1, field2: 10}";
		src += "\n insert Flight {field1: 2, field2: 20}";
		return src;
	}

	@Override
	public DBInterface createForTest() {
		return new MemDBInterface();
	}

}
