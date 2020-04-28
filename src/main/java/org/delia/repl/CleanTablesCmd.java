package org.delia.repl;

import org.delia.api.Delia;
import org.delia.db.DBType;
import org.delia.db.h2.test.H2TestCleaner;
import org.delia.runner.ResultValue;

public class CleanTablesCmd extends CmdBase {
	public CleanTablesCmd() {
		super("clean tables", null);
		expectSpace = false;
	}
	@Override
	public Cmd isReplCmd(String src) {
		if (isMatch(src)) {
			Cmd cmd = new CleanTablesCmd();
			cmd.cmd = name;
			return cmd;
		}
		return null;
	}

	@Override
	public ResultValue runCmd(Cmd cmd, ReplRunner runner) {
		Delia delia = runner.getDelia();
		if (!delia.getDBInterface().getDBType().equals(DBType.H2))
		{
			log("error: this command is only avalailable for H2.");
			return createEmptyRes();
		}
		H2TestCleaner cleaner = new H2TestCleaner(DBType.H2);
		cleaner.deleteKnownTables(delia.getFactoryService(), delia.getDBInterface());
		return createEmptyRes();
	}

}