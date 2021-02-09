package org.delia.app;


import org.delia.ConnectionStringBuilder;
import org.delia.Delia;
import org.delia.bdd.MemBDDBase;
import org.delia.builder.DeliaBuilder;
import org.delia.dao.DeliaGenericDao;
import org.delia.db.sql.ConnectionString;


public class DaoTestBase extends MemBDDBase {
	
	
	//---

	protected DeliaGenericDao createDao() {
		ConnectionString connStr = ConnectionStringBuilder.createMEM();
		Delia delia = DeliaBuilder.withConnection(connStr).build();
		delia.getOptions().logSourceBeforeCompile = true; //we want to see all src in log
		return new DeliaGenericDao(delia);
	}

}
