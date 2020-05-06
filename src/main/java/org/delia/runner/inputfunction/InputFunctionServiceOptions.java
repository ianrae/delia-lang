package org.delia.runner.inputfunction;

public class InputFunctionServiceOptions {
	public boolean ignoreRelationErrors; //true in level1 import
	public ExternalDataLoader externalLoader;
	public int numRowsToImport = Integer.MAX_VALUE;
	public boolean logDetails;
	public boolean useInsertStatement;
}
