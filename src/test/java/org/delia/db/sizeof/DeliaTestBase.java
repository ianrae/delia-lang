package org.delia.db.sizeof;


import static org.junit.Assert.assertEquals;

import org.delia.Delia;
import org.delia.DeliaSession;
import org.delia.api.DeliaSessionImpl;
import org.delia.assoc.CreateNewDatIdVisitor;
import org.delia.base.DBTestHelper;
import org.delia.base.UnitTestLog;
import org.delia.dao.DeliaGenericDao;
import org.delia.log.Log;
import org.delia.runner.BlobLoader;
import org.delia.runner.DeliaException;
import org.delia.runner.ResultValue;
import org.delia.util.render.ObjectRendererImpl;
import org.delia.zdb.mem.MemDBInterfaceFactory;

/**
 * Base class for tests using Delia and DeliaSession
 * @author Ian Rae
 *
 */
public abstract class DeliaTestBase  { 
	
	
	//-------------------------
	protected Delia delia;
	protected DeliaSession session;
	protected Log log = new UnitTestLog();
	protected BlobLoader blobLoader;

	protected abstract String buildSrc();

	protected DeliaSessionImpl execute(String src) {
		String initialSrc = buildSrc();
		log.log("initial: " + initialSrc);
		
		DeliaGenericDao dao = createDao(); 
		dao.setBlobLoader(blobLoader);
		boolean b = dao.initialize(initialSrc);
		assertEquals(true, b);

		Delia delia = dao.getDelia();
		this.session = dao.getMostRecentSession();
		log.log("src: %s", src);
		ResultValue res = delia.continueExecution(src, session);
		
		DeliaSessionImpl sessimpl = (DeliaSessionImpl) session;
		return sessimpl;
	}
	protected DeliaSessionImpl continueExecution(String src) {
		log.log("src: %s", src);
		ResultValue res = delia.continueExecution(src, session);
		
		DeliaSessionImpl sessimpl = (DeliaSessionImpl) session;
		return sessimpl;
	}
	
	protected DeliaGenericDao createDao() {
		this.delia = DBTestHelper.createNewDelia();
		MemDBInterfaceFactory memDBinterface = (MemDBInterfaceFactory) delia.getDBInterface();
		memDBinterface.createSingleMemDB();
		CreateNewDatIdVisitor.hackFlag = true;
		
		return new DeliaGenericDao(delia);
	}
	protected void executeFail(String src, String expectedErrId) {
		boolean ok = false;
		try {
			execute(src);
			ok = true;
		} catch (DeliaException e) {
			log.log("exception: %s", e.getMessage());
			assertEquals(expectedErrId, e.getLastError().getId());
		}
		assertEquals(false, ok);
	}
	protected void dumpObj(String title, Object obj) {
		log.log(title);
		ObjectRendererImpl ori = new ObjectRendererImpl();
		String json = ori.render(obj);
		log.log(json);
	}

}
