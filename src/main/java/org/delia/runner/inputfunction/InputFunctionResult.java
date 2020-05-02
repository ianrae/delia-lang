package org.delia.runner.inputfunction;

import java.util.ArrayList;
import java.util.List;

import org.delia.error.DeliaError;

public class InputFunctionResult {
	public List<DeliaError> errors = new ArrayList<>();
	public int numRowsProcessed;
	public int numRowsInserted;
	public int numColumnsProcessedPerRow;
	public boolean wasHalted;
	public ProgramSet progset;
}