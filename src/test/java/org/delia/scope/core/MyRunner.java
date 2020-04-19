package org.delia.scope.core;

import org.delia.base.UnitTestLog;
import org.delia.log.Log;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;

public class MyRunner extends BlockJUnit4ClassRunner {
	public static boolean enableScopeProcessing;
	public static ScopeTestRunResults scopeResults;
	private Log log = new UnitTestLog();

	public MyRunner(Class<?> klass) throws InitializationError {
        super(klass);
    }
 
//    @Override
//    protected Statement methodInvoker(FrameworkMethod method, Object test) {
//        //System.out.println("invoking: " + method.getName());
//        return super.methodInvoker(method, test);
//    }

	@Override
	public void run(RunNotifier notifier) {
		MyTestListener listener = new MyTestListener();
		if (enableScopeProcessing) {
			scopeResults = new ScopeTestRunResults();
			listener.results = scopeResults;
			notifier.addListener(listener);
		}
		notifier.fireTestRunStarted(getDescription());
		super.run(notifier);
		
		if (enableScopeProcessing) {
			System.out.println("---");
			for(ScopeResult res: listener.results.executions) {
				log.log("[%b] %s", res.pass, res.scope);
			}
		}
		notifier.removeListener(listener);
	}
}