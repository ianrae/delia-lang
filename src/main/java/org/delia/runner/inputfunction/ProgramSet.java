package org.delia.runner.inputfunction;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.delia.compiler.ast.inputfunction.InputFunctionDefStatementExp;
import org.delia.type.DStructType;

public class ProgramSet {
	public static class OutputSpec {
		public DStructType structType;
		public String alias;
		public ImportSpec ispec;
	}
	public Map<String,ProgramSpec> fieldMap = new ConcurrentHashMap<>(); //inputField,ProgramSpec
	
	public HdrInfo hdr;
	public List<OutputSpec> outputSpecs = new ArrayList<>(); //parallel to outputTypes
	public InputFunctionDefStatementExp inFnExp;
}