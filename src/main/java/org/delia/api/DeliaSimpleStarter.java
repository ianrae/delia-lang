package org.delia.api;

import java.io.IOException;

import org.delia.ConnectionDefinitionBuilder;
import org.delia.Delia;
import org.delia.DeliaLoader;
import org.delia.DeliaSession;
import org.delia.builder.DeliaBuilder;
import org.delia.dao.DeliaGenericDao;
import org.delia.db.sql.ConnectionDefinition;
import org.delia.log.Log;
import org.delia.zdb.mem.MemDBInterfaceFactory;

/**
 * Initializes and runs delia using MEM db. 
 * Used for code generation.
 * 
 * @author Ian Rae
 *
 */
public class DeliaSimpleStarter {
	protected Delia delia;
	protected DeliaSession session;
	protected Log log;

	public DeliaSession execute(String deliaSrc) {
		DeliaGenericDao dao = createDao(); 
		log = delia.getLog();
		log.log("initial: " + deliaSrc);
		boolean b = dao.initialize(deliaSrc);
		if (! b) {
			return null; //error 
		}

		this.session = dao.getMostRecentSession();
		return session;
	}
	
	public DeliaGenericDao createDao() {
		ConnectionDefinition info = ConnectionDefinitionBuilder.createMEM();
		this.delia = DeliaBuilder.withConnection(info).build();
		MemDBInterfaceFactory memDBinterface = (MemDBInterfaceFactory) delia.getDBInterface();
		memDBinterface.createSingleMemDB();
		return new DeliaGenericDao(delia);
	}

	public DeliaSession executeFromResource(String resourcePath) throws IOException {
		DeliaLoader loader = new DeliaLoader();
		String src = loader.fromResource(resourcePath);
		return execute(src);
	}

}