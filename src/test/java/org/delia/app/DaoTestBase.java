package org.delia.app;


import org.delia.Delia;
import org.delia.base.DBTestHelper;
import org.delia.bdd.MemBDDBase;
import org.delia.dao.DeliaGenericDao;


public class DaoTestBase extends MemBDDBase {
	
	
	//---

	protected DeliaGenericDao createDao() {
		Delia delia = DBTestHelper.createNewDelia();
		delia.getOptions().logSourceBeforeCompile = true; //we want to see all src in log
		return new DeliaGenericDao(delia);
	}

}
