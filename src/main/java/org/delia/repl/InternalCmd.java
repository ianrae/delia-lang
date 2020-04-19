package org.delia.repl;

import org.delia.runner.ResultValue;

public class InternalCmd extends Cmd {
	public String cmd;
	public String arg1;
	
	public Cmd isReplCmd(String src) {
		return null;
	}
	public ResultValue runCmd(Cmd cmd, ReplRunner runner) {
		return null;
	}
}