package org.delia.api;

import org.delia.runner.Runner;

/**
 * Used to customize a runner before it executes.
 * 
 * @author Ian Rae
 *
 */
public interface RunnerInitializer {

	void initialize(Runner runner);
}
