package org.delia.zdb;

import static org.junit.Assert.assertEquals;

import org.delia.api.Delia;
import org.delia.bdd.NewBDDBase;
import org.delia.builder.ConnectionBuilder;
import org.delia.builder.ConnectionInfo;
import org.delia.builder.DeliaBuilder;
import org.delia.dao.DeliaDao;
import org.delia.db.DBInterface;
import org.delia.db.DBType;
import org.delia.db.memdb.MemDBInterface;
import org.junit.Before;
import org.junit.Test;

public class ZDBTests  extends NewBDDBase {

	@Test
	public void testTool() {
		assertEquals(1,2);
	}

	// --
	private DeliaDao dao;
	private Delia delia;

	@Before
	public void init() {
		this.dao = createDao();
		this.delia = dao.getDelia();
	}

	private DeliaDao createDao() {
		ConnectionInfo info = ConnectionBuilder.dbType(DBType.MEM).build();
		Delia delia = DeliaBuilder.withConnection(info).build();
		return new DeliaDao(delia);
	}

	@Override
	public DBInterface createForTest() {
		return new MemDBInterface();
	}
}
