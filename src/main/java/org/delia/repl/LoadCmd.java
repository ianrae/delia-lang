package org.delia.repl;

import org.delia.runner.ResultValue;

public class LoadCmd extends CmdBase {
	public LoadCmd() {
		super("load", null);
	}
	public LoadCmd(LoadCmd obj) {
		super(obj);
	}
	@Override
	public Cmd isReplCmd(String src) {
		if (isMatch(src)) {
			Cmd cmd = new LoadCmd(this);
			cmd.cmd = name;
			cmd.arg1 = parseArg1(src);
			return cmd;
		}
		return null;
	}

	@Override
	public ResultValue runCmd(Cmd cmd, ReplRunner runner) {
		String path = cmd.arg1;
		if (! runner.doesFileExist(path)) {
			log("file-not-found: " + path);
			return createEmptyRes();
		}
		return runner.loadFromFile(path);
	}
}