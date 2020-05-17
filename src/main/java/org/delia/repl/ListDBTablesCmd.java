package org.delia.repl;

import org.delia.api.Delia;
import org.delia.db.DBInterface;
import org.delia.db.h2.H2DBInterface;
import org.delia.db.postgres.PostgresDBInterface;
import org.delia.runner.ResultValue;
import org.delia.zdb.ZDBInterfaceFactory;

public class ListDBTablesCmd extends CmdBase {
	public ListDBTablesCmd() {
		super("db table list", "ldb");
		expectSpace = false;
	}
	@Override
	public Cmd isReplCmd(String src) {
		if (isMatch(src)) {
			Cmd cmd = new ListDBTablesCmd();
			cmd.cmd = name;
			return cmd;
		}
		return null;
	}

	@Override
	public ResultValue runCmd(Cmd cmd, ReplRunner runner) {
		Delia delia = runner.getDelia();
		ZDBInterfaceFactory dbInterface = delia.getDBInterface();
		
		if (dbInterface instanceof H2DBInterface) {
			H2DBInterface h2db = (H2DBInterface) dbInterface;
			h2db.enumerateAllTables(delia.getLog());
		}
		if (dbInterface instanceof PostgresDBInterface) {
			PostgresDBInterface pgdb = (PostgresDBInterface) dbInterface;
			pgdb.enumerateAllTables(delia.getLog());
		}

		return createEmptyRes();
	}
}