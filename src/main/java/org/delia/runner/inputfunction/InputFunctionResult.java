package org.delia.runner.inputfunction;

import java.util.ArrayList;
import java.util.List;

import org.delia.error.DeliaError;

public class InputFunctionResult {
	public List<DeliaError> errors = new ArrayList<>();
	public String filename; //csv
	public int numRowsProcessed;
	public int numRowsInserted;
	public int numFailedRowInserts;
	public int numColumnsProcessedPerRow;
	public boolean wasHalted;
	public ProgramSet progset;
}