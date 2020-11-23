package org.delia.repl;

import org.delia.runner.ResultValue;

public class RunFromResourceCmd extends CmdBase {
	public RunFromResourceCmd() {
		super("runres", "rr");
	}
	public RunFromResourceCmd(RunFromResourceCmd obj) {
		super(obj);
	}
	@Override
	public Cmd isReplCmd(String src) {
		if (isMatch(src)) {
			Cmd cmd = new RunFromResourceCmd();
			cmd.cmd = name;
			cmd.arg1 = parseArg1(src);
			return cmd;
		}
		return null;
	}

	@Override
	public ResultValue runCmd(Cmd cmd, ReplRunner runner) {
		if (runner.getMostRecentSess() != null) {
			log("run can only be called once per session.");
			log("  Either use 'continue' to execute a Delia file within this session.");
			log("  or use 'restart' to end this session.");
			return createEmptyRes();
		}
		String resPath = cmd.arg1;
		return runner.runFromResource(resPath);
	}
}