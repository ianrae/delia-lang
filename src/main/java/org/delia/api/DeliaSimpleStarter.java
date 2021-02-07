package org.delia.api;

import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import org.delia.base.UnitTestLog;
import org.delia.builder.ConnectionBuilder;
import org.delia.builder.ConnectionInfo;
import org.delia.builder.DeliaBuilder;
import org.delia.dao.DeliaGenericDao;
import org.delia.db.DBType;
import org.delia.log.Log;
import org.delia.util.StringUtil;
import org.delia.util.TextFileReader;
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
	protected Log log = new UnitTestLog();

	public DeliaSession execute(String deliaSrc) {
		log.log("initial: " + deliaSrc);
		
		DeliaGenericDao dao = createDao(); 
		boolean b = dao.initialize(deliaSrc);
		if (! b) {
			return null; //error 
		}

		this.session = dao.getMostRecentSession();
		return session;
	}
	
	public DeliaGenericDao createDao() {
		ConnectionInfo info = ConnectionBuilder.dbType(DBType.MEM).build();
		this.delia = DeliaBuilder.withConnection(info).build();
		MemDBInterfaceFactory memDBinterface = (MemDBInterfaceFactory) delia.getDBInterface();
		memDBinterface.createSingleMemDB();
		return new DeliaGenericDao(delia);
	}

	public DeliaSession execute(FileReader reader) throws IOException {
		TextFileReader r = new TextFileReader();
		List<String> lines = r.ReadFileFromReader(reader);
		String src = StringUtil.flattenEx(lines, StringUtil.eol());
		return execute(src);
	}

}