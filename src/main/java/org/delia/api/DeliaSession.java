package org.delia.api;

import org.delia.runner.DValueIterator;
import org.delia.runner.ExecutionState;
import org.delia.runner.ResultValue;
/**
 * The result of delia.beginExecution().
 * It holds registered types and other parts of delia program state.  
 * This session can be used to call delia.continueExecution()
 * which will compile and run some more delia statements in the 
 * context of the session.  
 * @author Ian Rae
 *
 */
public interface DeliaSession {
	boolean ok();
	ResultValue getFinalResult();
	ExecutionState getExecutionContext();
	void setInsertPrebuiltValueIterator(DValueIterator insertPrebuiltValueIterator); 
	DValueIterator getInsertPrebuiltValueIterator(); 
}