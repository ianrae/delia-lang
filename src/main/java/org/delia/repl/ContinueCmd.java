package org.delia.repl;

import org.delia.runner.ResultValue;

public class ContinueCmd extends CmdBase {
	public ContinueCmd() {
		super("continue", "c");
	}
	@Override
	public Cmd isReplCmd(String src) {
		if (isMatch(src)) {
			Cmd cmd = new ContinueCmd();
			cmd.cmd = name;
			cmd.arg1 = parseArg1(src);
			return cmd;
		}
		return null;
	}

	@Override
	public ResultValue runCmd(Cmd cmd, ReplRunner runner) {
		if (runner.getMostRecentSess() == null) {
			log("continue can only be called within asession.");
			log("use 'run <path>' to start a session.");
			return createEmptyRes();
		}
		String path = cmd.arg1;
		if (! runner.doesFileExist(path)) {
			log("file-not-found: " + path);
			return createEmptyRes();
		}
		return runner.continueFromFile(path);
	}
}