package org.delia.db.memdb;

import java.util.Map;

import org.delia.core.FactoryService;
import org.delia.db.DBAccessContext;
import org.delia.db.DBExecutor;
import org.delia.db.DBInterface;
import org.delia.db.InsertContext;
import org.delia.db.QueryContext;
import org.delia.db.QuerySpec;
import org.delia.db.SchemaContext;
import org.delia.db.RawDBExecutor;
import org.delia.db.TableExistenceService;
import org.delia.db.TableExistenceServiceImpl;
import org.delia.db.hls.HLSQueryStatement;
import org.delia.runner.FetchRunner;
import org.delia.runner.FetchRunnerImpl;
import org.delia.runner.QueryResponse;
import org.delia.type.DValue;
 
public class MemRawDBExecutor implements RawDBExecutor {

	private DBInterface dbInterface;
	private DBAccessContext dbctx;

	public MemRawDBExecutor(MemDBInterface memDBInterface, DBAccessContext ctx) {
		this.dbInterface = memDBInterface;
		this.dbctx = ctx;
	}
	public void forceDBInterface(DBInterface newOne) {
		this.dbInterface = newOne;
	}

	//close is from AutoClosable
	@Override
	public void close() {
//		conn.close(); no conn with mem db
	}
	@Override
	public DValue executeInsert(DValue dval, InsertContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public QueryResponse executeQuery(QuerySpec spec, QueryContext qtx) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public boolean execTableDetect(String tableName) {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public void createTable(String tableName) {
		// TODO Auto-generated method stub
		
	}

}
