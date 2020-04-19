package org.delia.runner;

import java.util.List;

import org.delia.compiler.ast.Exp;
import org.delia.compiler.generate.DeliaGeneratePhase;
import org.delia.log.Log;
import org.delia.sprig.SprigService;
import org.delia.type.DTypeRegistry;

/**
 * The main runtime engine for Delia. Executes a compiled AST object
 * such as TypeStatementExp. 
 * Please create a new Runner object for each next execution.
 * 
 * This class is not thread-safe. Only use it as a local var.
 * @author Ian Rae
 *
 */
public interface Runner extends VarEvaluator {
	Log getLog();
	
	/**
	 * Get a generator which can be used to
	 * render DValues into xml,json,etc
	 * @return a generator
	 */
	DeliaGeneratePhase createGenerator();

	/**
	 * Get the compiler state. Used by
	 * compiler passes to verify and link
	 * the delia code.
	 * 
	 * @return the internal compile state
	 */
	InternalCompileState getCompileState();

	/**
	 * Get the new execution context. Used when
	 * you want to execute again with the
	 * same types (i.e. same registry).
	 * 
	 * @return the execution state
	 */
	ExecutionState getExecutionState();

	/**
	 * Initialize the runner.
	 * Should be called once after creating the Runner object.
	 * If we are continuing a previous run the execution state
	 * pass it in execState.
	 * If null is passed for execState then a new state will
	 * be created. 
	 * 
	 * @param execState - if null then runner will create a new execution state.
	 * @return success flag
	 */
	boolean init(ExecutionState execState);

	/**
	 * Execute a single statement. For advance use only.
	 * @param exp AST object to execute
	 * @return result value
	 */
	ResultValue executeOneStatement(Exp exp);

	/**
	 * Execute a single statement.
	 * @param expL AST object to execute
	 * @return result value
	 */
	ResultValue executeProgram(List<Exp> expL);

	/**
	 * Does the given variable exist.
	 * example: let x = Customer[true]
	 * would create a variable 'x'.
	 * 
	 * @param varName variable name
	 * @return true if exists
	 */
	boolean exists(String varName);
	/**
	 * Gets a variable's value
	 * 
	 * @param varName variable name
	 * @return null if not found
	 */
	ResultValue getVar(String varName);

	DTypeRegistry getRegistry();
	TypeRunner createTypeRunner();
	SprigService getSprigSvc();
	
}