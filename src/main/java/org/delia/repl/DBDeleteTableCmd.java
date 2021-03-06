package org.delia.repl;

import org.delia.Delia;
import org.delia.runner.ResultValue;
import org.delia.zdb.DBExecutor;
import org.delia.zdb.DBInterfaceFactory;

public class DBDeleteTableCmd extends CmdBase {
	public DBDeleteTableCmd() {
		super("db table delete", null);
	}
	public DBDeleteTableCmd(DBDeleteTableCmd obj) {
		super(obj);
	}
	@Override
	public Cmd isReplCmd(String src) {
		if (isMatch(src)) {
			Cmd cmd = new DBDeleteTableCmd(this);
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
		
		try(DBExecutor exec = dbInterface.createExecutor()) {
			String tableName = cmd.arg1;
			if (exec.rawTableDetect(tableName)) {
				exec.deleteTable(cmd.arg1);
				log(String.format("deleted table '%s'", cmd.arg1));
			} else {
				log("can't find that table: " + tableName);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		return createEmptyRes();
	}
}