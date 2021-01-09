package org.delia.runner.inputfunction;

import org.delia.api.Delia;
import org.delia.api.DeliaSession;

public class InputFunctionRequest {
	public ProgramSet progset;
	public Delia delia;
	public DeliaSession session;
	public int stopAfterErrorThreshold = Integer.MAX_VALUE;
	public ImportedValueListener importedValueListener;
}