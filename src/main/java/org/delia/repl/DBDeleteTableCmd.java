package org.delia.repl;

import org.delia.api.Delia;
import org.delia.db.DBAccessContext;
import org.delia.db.DBExecutor;
import org.delia.db.DBInterface;
import org.delia.runner.ResultValue;

public class DBDeleteTableCmd extends CmdBase {
	public DBDeleteTableCmd() {
		super("db table delete", null);
	}
	@Override
	public Cmd isReplCmd(String src) {
		if (isMatch(src)) {
			Cmd cmd = new DBDeleteTableCmd();
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
		DBAccessContext dbctx = new DBAccessContext(null, null);
		DBExecutor exec = dbInterface.createExector(dbctx);
		
		try {
			String tableName = cmd.arg1;
			if (exec.execTableDetect(tableName)) {
				exec.deleteTable(cmd.arg1);
				log(String.format("deleted table '%s'", cmd.arg1));
			} else {
				log("can't find that table: " + tableName);
			}
		} finally {
			exec.close();
		}
		
		return createEmptyRes();
	}
}