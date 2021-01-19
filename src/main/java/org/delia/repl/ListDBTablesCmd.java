package org.delia.repl;

import org.delia.api.Delia;
import org.delia.db.DBType;
import org.delia.db.h2.DBListingType;
import org.delia.db.sql.prepared.RawStatementGenerator;
import org.delia.db.sql.prepared.SqlStatement;
import org.delia.runner.ResultValue;
import org.delia.zdb.ZDBExecutor;
import org.delia.zdb.ZDBInterfaceFactory;

public class ListDBTablesCmd extends CmdBase {
	public ListDBTablesCmd() {
		super("db table list", "ldb");
		expectSpace = false;
	}
	public ListDBTablesCmd(ListDBTablesCmd obj) {
		super(obj);
	}
	@Override
	public Cmd isReplCmd(String src) {
		if (isMatch(src)) {
			Cmd cmd = new ListDBTablesCmd(this);
			cmd.cmd = name;
			return cmd;
		}
		return null;
	}

	@Override
	public ResultValue runCmd(Cmd cmd, ReplRunner runner) {
		Delia delia = runner.getDelia();
		ZDBInterfaceFactory dbInterface = delia.getDBInterface();
		
		RawStatementGenerator gen = new RawStatementGenerator(delia.getFactoryService(), dbInterface.getDBType());
		String sql = gen.generateSchemaListing(DBListingType.ALL_TABLES);
		try(ZDBExecutor zexec = dbInterface.createExecutor()) {
			SqlStatement statement = new SqlStatement(null);
			statement.sql = sql;
			zexec.getDBConnection().execStatement(statement, null);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return createEmptyRes();
	}
}