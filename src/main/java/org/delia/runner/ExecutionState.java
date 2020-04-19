package org.delia.runner;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.delia.compiler.ast.UserFunctionDefStatementExp;
import org.delia.compiler.generate.DeliaGeneratePhase;
import org.delia.sprig.SprigService;
import org.delia.type.DTypeRegistry;

/**
 * Holds the state of a delia program after execution.
 * Has all the registered types, the variables and their values, etc.
 * @author Ian Rae
 *
 */
public class ExecutionState {
	public DTypeRegistry registry;
	public Map<String,ResultValue> varMap = new ConcurrentHashMap<>();
	public Map<String,UserFunctionDefStatementExp> userFnMap = new ConcurrentHashMap<>();
	public DeliaGeneratePhase generator;
	public SprigService sprigSvc;
	
}