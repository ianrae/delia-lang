package org.delia.db.hld;


import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.delia.Delia;
import org.delia.DeliaSession;
import org.delia.bdd.BDDBase;
import org.delia.builder.ConnectionBuilder;
import org.delia.builder.ConnectionInfo;
import org.delia.builder.DeliaBuilder;
import org.delia.dao.DeliaGenericDao;
import org.delia.db.DBType;
import org.delia.db.SqlStatement;
import org.delia.type.DTypeRegistry;
import org.delia.zdb.CollectingObserverFactory;
import org.delia.zdb.DBConnectionObserverAdapter;
import org.delia.zdb.DBObserverAdapter;
import org.delia.zdb.DBObserverFactory;
import org.delia.zdb.DBExecutor;
import org.delia.zdb.DBInterfaceFactory;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 * @author Ian Rae
 *
 */
public class DBObserverTests extends BDDBase {
	
	public static class MyObsFactory implements DBObserverFactory {
		public DBObserverAdapter observer;

		@Override
		public DBExecutor createObserver(DBExecutor actual, DBConnectionObserverAdapter connAdapter, boolean ignoreSimpleSvcSql) {
			observer = new DBObserverAdapter(actual, ignoreSimpleSvcSql);
			return observer;
		}
		
		public void dump() {
			for(SqlStatement sql: observer.getStatementList()) {
				System.out.println(sql.sql);
			}
		}
	}
	
	@Test
	public void test() {
		String src = buildSrc(" insert Customer {id: 5, wid: 33, name:'bob'}");
		MyObsFactory factory = new MyObsFactory();
		delia.getOptions().dbObserverFactory = factory;
		delia.getFactoryService().setEnableMEMSqlGenerationFlag(true);
		session = delia.beginSession(src);
		
		assertEquals(1, factory.observer.getStatementList().size());
		factory.dump();
	}	

	@Test
	public void test2() {
		String src = buildSrc(" insert Customer {id: 5, wid: 33, name:'bob'}");
		CollectingObserverFactory factory = new CollectingObserverFactory();
		delia.getOptions().dbObserverFactory = factory;
		delia.getFactoryService().setEnableMEMSqlGenerationFlag(true);
		session = delia.beginSession(src);
		
		dump(factory.getObserver());
		assertEquals(1, factory.getObserver().getStatementList().size());
		
		src = " insert Customer {id: 6, wid: 33, name:'sie'}";
		delia.continueExecution(src, session);
		log.log("..2..");
		dump(factory.getObserver());
		assertEquals(2, factory.getObserver().getStatementList().size());
	}	

	@Test
	public void testBufferedReader() {
		String resourcePath = "test/northwind/northwind-small.txt";
		InputStream in = this.getClass().getClassLoader().getResourceAsStream(resourcePath);
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));

		session = delia.beginSession(reader);
		DTypeRegistry registry = session.getExecutionContext().registry;
		assertEquals(true, registry.existsType("Category"));
	}	
	
	
	@Test
	public void testConnAdapter() {
		String src = buildSrc(" insert Customer {id: 5, wid: 33, name:'bob'}");
		CollectingObserverFactory factory = new CollectingObserverFactory();
		delia.getOptions().dbObserverFactory = factory;
		delia.getOptions().observeHLDSQLOnly = false;
		delia.getFactoryService().setEnableMEMSqlGenerationFlag(true);
		session = delia.beginSession(src);
		
		dump(factory.getObserver());
		assertEquals(3, factory.getObserver().getStatementList().size());
		
		src = " insert Customer {id: 6, wid: 33, name:'sie'}";
		delia.continueExecution(src, session);
		log.log("..2..");
		dump(factory.getObserver());
		assertEquals(4, factory.getObserver().getStatementList().size());
	}	

	//-------------------------
	protected Delia delia;
	protected DeliaSession session;

	@Before
	public void init() {
		DeliaGenericDao dao = createDao();
		this.delia = dao.getDelia();
	}
	private DeliaGenericDao createDao() {
		ConnectionInfo info = ConnectionBuilder.dbType(DBType.MEM).build();
		Delia delia = DeliaBuilder.withConnection(info).build();
		return new DeliaGenericDao(delia);
	}

	private String buildSrc(String additionalSrc) {
		String src = String.format(" type Customer struct {id int primaryKey, wid int, name string } end");
		src += additionalSrc;
		return src;
	}
	@Override
	public DBInterfaceFactory createForTest() {
		// TODO Auto-generated method stub
		return null;
	}
	public void dump(DBObserverAdapter adapter) {
		for(SqlStatement sql: adapter.getStatementList()) {
			System.out.println(sql.sql);
		}
	}

}
