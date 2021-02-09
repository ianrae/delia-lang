package org.delia.repl;

import org.delia.Delia;
import org.delia.db.DBType;
import org.delia.db.RawStatementGenerator;
import org.delia.db.SqlStatement;
import org.delia.runner.ResultValue;
import org.delia.zdb.DBExecutor;
import org.delia.zdb.DBInterfaceFactory;
import org.delia.zdb.DBListingType;

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
		DBInterfaceFactory dbInterface = delia.getDBInterface();
		
		RawStatementGenerator gen = new RawStatementGenerator(delia.getFactoryService(), dbInterface.getDBType());
		String sql = gen.generateSchemaListing(DBListingType.ALL_TABLES);
		try(DBExecutor zexec = dbInterface.createExecutor()) {
			SqlStatement statement = new SqlStatement(null);
			statement.sql = sql;
			zexec.getDBConnection().execStatement(statement, null);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return createEmptyRes();
	}
}