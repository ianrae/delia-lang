package org.delia.app;


import org.delia.api.Delia;
import org.delia.bdd.MemBDDBase;
import org.delia.builder.ConnectionBuilder;
import org.delia.builder.ConnectionInfo;
import org.delia.builder.DeliaBuilder;
import org.delia.dao.DeliaDao;
import org.delia.db.DBType;


public class DaoTestBase extends MemBDDBase {
	
	
	//---

	protected DeliaDao createDao() {
		ConnectionInfo info = ConnectionBuilder.dbType(DBType.MEM).build();
		Delia delia = DeliaBuilder.withConnection(info).build();
		return new DeliaDao(delia);
	}

}
