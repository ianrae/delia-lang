package org.delia.api;

import org.delia.assoc.DatIdMap;
import org.delia.runner.ExecutionState;
import org.delia.runner.ResultValue;
import org.delia.runner.Runner;
/**
 * An instance of Delia that can be used from a single thread.
 * A Delia session contains the types and variables defined by the initial
 * Delia source code that was passed to delia.beginExecution().
 * 
 * The session is used to call delia.continueExecution() to do additional
 * database operations.
 * 
 * Use createChildSession() to create additional sessions that can be used
 * from other threads.  For example, a Spring web application would create
 * a main session at startup, and create a child session to handle each
 * HTTP request.  
 * @author Ian Rae
 *
 */
public interface DeliaSession {
	boolean ok();
	ResultValue getFinalResult();
	ExecutionState getExecutionContext();
	void setRunnerIntiliazer(RunnerInitializer runnerInitializer);
	RunnerInitializer getRunnerIntiliazer();
	Delia getDelia();
	DatIdMap getDatIdMap();
	Runner getMostRecentRunner();
}