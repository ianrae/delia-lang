package org.delia.runner;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.delia.compiler.ast.UserFunctionDefStatementExp;

/**
 * Represents the compile state of a delia program.
 * Has all the declared types, and the variables
 * and user fns.
 * 
 * @author Ian Rae
 *
 */
public class InternalCompileState {
	public Map<String,TypeSpec> compiledTypeMap = new ConcurrentHashMap<>(); //value if list of fieldNames
	
	public Map<String,ResultValue> delcaredVarMap = new ConcurrentHashMap<>(); //this does NOT hold the runtime vars
	
	public Map<String,UserFunctionDefStatementExp> declaredUserFnMap = new ConcurrentHashMap<>();
	
	public InternalCompileState() {
	}
}