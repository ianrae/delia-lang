package org.delia.scope.core;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

public abstract class MyScopeTestsBase {
	public List<String> allTypes;
//	public List<String> allListTypes;
//	public List<String> allArrayTypes;
	public List<String> errors = new ArrayList<>();
	protected ScopeTestRunResults scopeResults;
	public List<ScopeResult> observedL = new ArrayList<>();
	
	public abstract boolean checkResults(ScopeTestRunResults scopeResults);

	protected void checkObserved() {
		for(ScopeResult res: scopeResults.executions) {
			if (! observedL.contains(res)) {
				errors.add(String.format("%s UNKNOWN", res.scope));
			}
		}
	}
	protected void checkAll() {
		for(String type: allTypes) {
			for(String inner: allTypes) {
				String s = String.format("%s:: %s", type, inner);
				ScopeResult res = findTarget(s);
				if (inner.equals(type)) {
					addToObserved(res);
					continue;
				}
				addErrorIfFailed("checkAll", res, type, inner);
			}
		}
	}
	protected void checkListAll() {
		for(String type: allTypes) {
			for(String inner: allTypes) {
				String s = String.format("List<%s>:: List<%s>", type, inner);
				ScopeResult res = findTarget(s);
				if (inner.equals(type)) {
					addToObserved(res);
					continue;
				}
				addErrorIfFailed("checkListAll", res, type, inner);
			}
		}
	}
	protected void checkPrimitive(String mainType, String primitiveType) {
		for(String type: allTypes) {
			String target = String.format("%s:%s", mainType, primitiveType);
			
			String s = String.format("%s: %s", target, type);
			ScopeResult res = findTarget(s);
			if (mainType.startsWith(type)) {
				addToObserved(res);
				continue;
			}
			addErrorIfFailed("checkPrimitives", res, target, type);
		}
	}

	protected void ensureHappened(String testName) {
		for(String type: allTypes) {
			ScopeResult res = find(type, testName);
			addErrorIfFailed("", res, type, testName);
		}
	}

	protected ScopeResult find(String type, String testName) {
		String s = String.format("%s:: %s", type, testName);
		return findTarget(s);
	}
	protected ScopeResult findTarget(String target) {
		for(ScopeResult res: scopeResults.executions) {
			if (res.scope.equals(target)) {
				return res;
			}
		}
		return null;
	}
	protected ScopeResult findTargetStartsWith(String target) {
		for(ScopeResult res: scopeResults.executions) {
			if (res.scope.startsWith(target)) {
				return res;
			}
		}
		return null;
	}
	private void addToObserved(ScopeResult res) {
		if (res != null) {
			observedL.add(res);
		}
	}

	protected void addErrorIfFailed(String name, ScopeResult res, String s1, String s2) {
		observedL.add(res);
		String title = StringUtils.isEmpty(name) ? "" : String.format("(%s)", name);
		if (res == null) {
			errors.add(String.format("%s:: %s MISSING %s", s1, s2, title));
		} else if (!res.pass) {
			errors.add(String.format("%s:: %s FAILED %s", s1, s2, title));
		}
	}

	public void dump() {
		log(String.format("--%d errors", errors.size()));
		for(String err: errors) {
			log(err);
		}
	}
	protected static void log(String s) {
		System.out.println(s);
	}

}
