package org.delia.repl;

import org.delia.runner.ResultValue;

public abstract class Cmd {
	public String cmd;
	public String arg1;
	
	public abstract Cmd isReplCmd(String src);
	public abstract ResultValue runCmd(Cmd cmd, ReplRunner runner);
}