package org.delia.runner.inputfunction;

public class InputFunctionServiceOptions {
	public boolean ignoreRelationErrors; //true in level1 import
	public ExternalDataLoader externalLoader;
	public int numRowsToImport = Integer.MAX_VALUE;
}
