package org.delia.repl;

import org.delia.api.Delia;
import org.delia.db.DBInterface;
import org.delia.runner.ResultValue;

public class DBLoggingCmd extends CmdBase {
	public DBLoggingCmd() {
		super("db log", null);
	}
	@Override
	public Cmd isReplCmd(String src) {
		if (isMatch(src)) {
			Cmd cmd = new DBLoggingCmd();
			cmd.cmd = name;
			cmd.arg1 = parseArg1(src);
			return cmd;
		}
		return null;
	}

	@Override
	public ResultValue runCmd(Cmd cmd, ReplRunner runner) {
		Delia delia = runner.getDelia();
		DBInterface dbInterface = delia.getDBInterface();
		
		boolean b = false;
		if (cmd.arg1.equalsIgnoreCase("on")) {
			b = true;
		}
		log(String.format("sql logging: %b", b ? "on" : "off"));
		
		dbInterface.enableSQLLogging(b);
		return createEmptyRes();
	}
}