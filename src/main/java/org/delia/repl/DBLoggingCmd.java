package org.delia.repl;

import org.delia.Delia;
import org.delia.runner.ResultValue;
import org.delia.zdb.DBInterfaceFactory;

public class DBLoggingCmd extends CmdBase {
	public DBLoggingCmd() {
		super("db log", null);
	}
	public DBLoggingCmd(DBLoggingCmd obj) {
		super(obj);
	}
	@Override
	public Cmd isReplCmd(String src) {
		if (isMatch(src)) {
			Cmd cmd = new DBLoggingCmd(this);
			cmd.cmd = name;
			cmd.arg1 = parseArg1(src);
			return cmd;
		}
		return null;
	}

	@Override
	public ResultValue runCmd(Cmd cmd, ReplRunner runner) {
		Delia delia = runner.getDelia();
		DBInterfaceFactory dbInterface = delia.getDBInterface();
		
		boolean b = false;
		if (cmd.arg1.equalsIgnoreCase("on")) {
			b = true;
		}
		log(String.format("sql logging: %b", b ? "on" : "off"));
		
		dbInterface.enableSQLLogging(b);
		return createEmptyRes();
	}
}