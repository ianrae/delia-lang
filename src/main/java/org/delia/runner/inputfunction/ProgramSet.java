package org.delia.runner.inputfunction;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.delia.compiler.ast.inputfunction.InputFunctionDefStatementExp;
import org.delia.type.DStructType;
import org.delia.type.DValue;

public class ProgramSet {
	public static class OutputSpec {
		public DStructType structType;
		public String alias;
		public ImportSpec ispec;
	}
	public Map<String,ProgramSpec> fieldMap = new ConcurrentHashMap<>(); //inputField,ProgramSpec
	public Map<DValue,String> syntheticMap = new ConcurrentHashMap<>(); //syntheticdvalue,inputField
	public HdrInfo hdr;
//	public List<DStructType> outputTypes = new ArrayList<>();
//	public List<String> outputAliases = new ArrayList<>(); //parallel to outputTypes
//	public List<ImportSpec> importSpecs = new ArrayList<>(); //parallel to outputTypes
	public List<OutputSpec> outputSpecs = new ArrayList<>(); //parallel to outputTypes
	public InputFunctionDefStatementExp inFnExp;
}