package org.delia.repl;

import org.delia.core.FactoryService;
import org.delia.runner.ResultValue;

public abstract class CmdBase extends Cmd {
	public String name;
	protected boolean expectSpace = true;
	private String shortName;
	protected int iParmStart;
	protected FactoryService factorySvc;
	
	public CmdBase(String name, String shortformName) {
		this.name = name;
		this.shortName = shortformName;
	}
	protected boolean isMatch(String src) {
		String s = name;
		if (expectSpace) {
			s = String.format("%s ", name);
		}
		if (src.startsWith(s)) {
			iParmStart = s.length();
			return true;
		} else if (shortName != null && src.startsWith(shortName)) {
			iParmStart = shortName.length() + 1;
			return true;
		}
		
		return false;
	}

	protected String parseArg1(String src) {
		String arg = src.substring(iParmStart);
		return arg;
	}

	@Override
	public ResultValue runCmd(Cmd cmd, ReplRunner runner) {
		String path = cmd.arg1;
		return runner.loadFromFile(path);
	}
	
	protected void log(String s) {
		System.out.println(s);
	}
	
	protected ResultValue createEmptyRes() {
		ResultValue res = new ResultValue();
		res.ok = true;
		return res;
	}
	public FactoryService getFactorySvc() {
		return factorySvc;
	}
	public void setFactorySvc(FactoryService factorySvc) {
		this.factorySvc = factorySvc;
	}
}