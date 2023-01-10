package org.delia.runner;

import org.delia.sprig.SprigService;
import org.delia.type.DTypeRegistry;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Holds the state of a delia program after execution.
 * Has all the registered types, the variables and their values, etc.
 *
 * @author Ian Rae
 */
public class ExecutionState {
    public DTypeRegistry registry;
    public Map<String, ResultValue> varMap = new ConcurrentHashMap<>();
//	public Map<String,UserFunctionDefStatementExp> userFnMap = new ConcurrentHashMap<>();
//	public Map<String,InputFunctionDefStatementExp> inputFnMap = new ConcurrentHashMap<>();
//	public DeliaGeneratePhase generator;
	public SprigService sprigSvc;
    public DeliaRunner deliaRunner;
    public boolean enableRemoveFks = true; //applies to MEM only
    public String currentSchema;

}