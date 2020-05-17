package org.delia.repl;

import org.delia.api.Delia;
import org.delia.db.DBAccessContext;
import org.delia.runner.ResultValue;
import org.delia.zdb.ZDBExecutor;
import org.delia.zdb.ZDBInterfaceFactory;

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
		ZDBInterfaceFactory dbInterface = delia.getDBInterface();
		
		try(ZDBExecutor exec = dbInterface.createExecutor()) {
			String tableName = cmd.arg1;
			if (exec.rawTableDetect(tableName)) {
				exec.deleteTable(cmd.arg1);
				log(String.format("deleted table '%s'", cmd.arg1));
			} else {
				log("can't find that table: " + tableName);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return createEmptyRes();
	}
}