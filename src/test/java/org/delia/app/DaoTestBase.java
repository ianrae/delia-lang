package org.delia.app;


import org.delia.api.Delia;
import org.delia.bdd.MemBDDBase;
import org.delia.builder.ConnectionBuilder;
import org.delia.builder.ConnectionInfo;
import org.delia.builder.DeliaBuilder;
import org.delia.dao.DeliaGenericDao;
import org.delia.db.DBType;


public class DaoTestBase extends MemBDDBase {
	
	
	//---

	protected DeliaGenericDao createDao() {
		ConnectionInfo info = ConnectionBuilder.dbType(DBType.MEM).build();
		Delia delia = DeliaBuilder.withConnection(info).build();
		delia.getOptions().logCompileSrc = true; //we want to see all src in log
		return new DeliaGenericDao(delia);
	}

}
