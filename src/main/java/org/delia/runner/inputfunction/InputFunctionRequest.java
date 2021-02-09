package org.delia.runner.inputfunction;

import org.delia.Delia;
import org.delia.DeliaSession;

public class InputFunctionRequest {
	public ProgramSet progset;
	public Delia delia;
	public DeliaSession session;
	public int stopAfterErrorThreshold = Integer.MAX_VALUE;
	public ImportedValueListener importedValueListener;
}