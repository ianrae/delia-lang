package org.delia.error;

import java.util.List;

/**
 * Holds the set of errors that have occurred.
 * The main Delia object has an error tracker.
 * Some objects have their own error tracker so their errors
 * don't pollute the main error tracker.
 * 
 * @author Ian Rae
 *
 */
public interface ErrorTracker {
	int errorCount();
	boolean areNoErrors();
	void add(DeliaError err);
	void addAll(List<DeliaError> errL);
	DeliaError add(String id, String msg);

	void clear();
	void dump();
	List<DeliaError> getErrors();
	DeliaError getLastError();
	List<ValidationError> getValidationErrors();
	void copyErrorsFrom(ErrorTracker et, ErrorCopyFilter filter);
}