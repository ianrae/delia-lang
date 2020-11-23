package org.delia.repl;

import org.delia.runner.ResultValue;

public class RunCmd extends CmdBase {
	public RunCmd() {
		super("run", "r");
	}
	public RunCmd(RunCmd obj) {
		super(obj);
	}
	@Override
	public Cmd isReplCmd(String src) {
		if (isMatch(src)) {
			Cmd cmd = new RunCmd(this);
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
		String path = cmd.arg1;
		if (! runner.doesFileExist(path)) {
			log("file-not-found: " + path);
			return createEmptyRes();
		}
		return runner.runFromFile(path);
	}
}