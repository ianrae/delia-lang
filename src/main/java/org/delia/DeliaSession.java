package org.delia;

import org.delia.hld.dat.DatService;
import org.delia.runner.ExecutionState;
import org.delia.runner.ResultValue;
import org.delia.transaction.TransactionBody;
import org.delia.transaction.VoidTransactionBody;
import org.delia.type.DTypeRegistry;

import java.time.ZoneId;

/**
 * An instance of Delia that can be used from a single thread.
 * A Delia session contains the types and variables defined by the initial
 * Delia source code that was passed to delia.beginExecution().
 * <p>
 * The session is used to call delia.continueExecution() to do additional
 * database operations.
 * <p>
 * Use createChildSession() to create additional sessions that can be used
 * from other threads.  For example, a Spring web application would create
 * a main session at startup, and create a child session to handle each
 * HTTP request.
 *
 * @author Ian Rae
 */
public interface DeliaSession {
    boolean ok();

    ResultValue getFinalResult();

    ExecutionState getExecutionContext();

    DTypeRegistry getRegistry();

    //	void setRunnerIntiliazer(RunnerInitializer runnerInitializer);
//	RunnerInitializer getRunnerIntiliazer();
    Delia getDelia();

    DatService getDatIdMap(); //	DatIdMap getDatIdMap();

    //	Runner getMostRecentRunner();
    DeliaSession createChildSession();

    ZoneId getDefaultTimezone();

    <T> T runInTransaction(TransactionBody<T> body);

    void runInTransactionVoid(VoidTransactionBody body);

    DeliaOptions getSessionOptions(); //null except in child sessions
}