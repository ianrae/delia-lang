package org.delia.repl;

import org.delia.core.FactoryService;
import org.delia.runner.ResultValue;

public abstract class CmdBase extends Cmd {
	public String name;
	protected boolean expectSpace = true;
	protected String shortName;
	protected int iParmStart;
	protected FactoryService factorySvc;
	private ReplOutputWriter outWriter;
	
	public CmdBase(String name, String shortformName) {
		this.name = name;
		this.shortName = shortformName;
	}
	public CmdBase(CmdBase obj) {
		this.name = obj.name;
		this.shortName = obj.shortName;
		this.expectSpace = obj.expectSpace;
		this.factorySvc = obj.factorySvc;
		this.outWriter = obj.outWriter;
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
		outWriter.output(s);
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
	public void setOutputWriter(ReplOutputWriter outWriter) {
		this.outWriter = outWriter;
	}
}