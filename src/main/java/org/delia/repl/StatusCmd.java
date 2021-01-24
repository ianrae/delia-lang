package org.delia.repl;

import org.delia.api.Delia;
import org.delia.api.DeliaSession;
import org.delia.runner.ResultValue;
import org.delia.type.DTypeRegistry;
import org.delia.zdb.DBInterfaceFactory;

public class StatusCmd extends CmdBase {
	public StatusCmd() {
		super("status", null);
		expectSpace = false;
	}
	public StatusCmd(StatusCmd obj) {
		super(obj);
	}
	@Override
	public Cmd isReplCmd(String src) {
		if (isMatch(src)) {
			Cmd cmd = new StatusCmd(this);
			cmd.cmd = name;
			return cmd;
		}
		return null;
	}

	@Override
	public ResultValue runCmd(Cmd cmd, ReplRunner runner) {
		if (! runner.inSession()) {
			log("a session has not been created.");
			return createEmptyRes();
		}
		
		DeliaSession sess = runner.getMostRecentSess();
		Delia delia = runner.getDelia();
		DBInterfaceFactory dbInterface = delia.getDBInterface();
		
		int numTypes = sess.getExecutionContext().registry.size();
		numTypes -= DTypeRegistry.NUM_BUILTIN_TYPES;
		
		//TODO: fix this
//		DBInterfaceInternal dbi = (DBInterfaceInternal) dbInterface;
//		String summ = dbi.getConnectionSummary();
//		log(String.format("session: %s", runner.getSessionName()));
//		log(String.format("             database: %s - %s", dbInterface.getDBType().name(), summ));
//		log(String.format("      number of types: %d", numTypes));
//		log(String.format("  number of variables: %d", sess.getExecutionContext().varMap.size() - 1));
		
		return createEmptyRes();
	}
}