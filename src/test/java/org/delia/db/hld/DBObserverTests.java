package org.delia.db.hld;


import static org.junit.Assert.*;

import org.delia.api.Delia;
import org.delia.api.DeliaSession;
import org.delia.bdd.BDDBase;
import org.delia.builder.ConnectionBuilder;
import org.delia.builder.ConnectionInfo;
import org.delia.builder.DeliaBuilder;
import org.delia.dao.DeliaGenericDao;
import org.delia.db.DBType;
import org.delia.db.SqlStatement;
import org.delia.zdb.DBObserverAdapter;
import org.delia.zdb.DBObserverFactory;
import org.delia.zdb.ZDBExecutor;
import org.delia.zdb.ZDBInterfaceFactory;
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
		public ZDBExecutor createObserver(ZDBExecutor actual) {
			observer = new DBObserverAdapter(actual);
			return observer;
		}
		
		public void dump() {
			for(SqlStatement sql: observer.statements) {
				System.out.println(sql.sql);
			}
		}
	}
	
	@Test
	public void test() {
		String src = buildSrc(" insert Customer {id: 5, wid: 33, name:'bob'}");
		MyObsFactory factory = new MyObsFactory();
		delia.getOptions().dbObserverFactory = factory;
		session = delia.beginSession(src);
		
		assertEquals(1, factory.observer.statements.size());
		factory.dump();
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
	public ZDBInterfaceFactory createForTest() {
		// TODO Auto-generated method stub
		return null;
	}

}
