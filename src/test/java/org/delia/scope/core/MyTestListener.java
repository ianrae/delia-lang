package org.delia.scope.core;

import java.lang.reflect.Method;

import org.apache.commons.lang3.StringUtils;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

public class MyTestListener extends RunListener {
		public ScopeTestRunResults results;
		
	    public void testRunStarted(Description description) throws Exception {
//	        System.out.println("Number of tests to execute: " + description.testCount());
	        if (results != null) {
	        	results.scope = getClassScopeValue(description);
	        }
	    }
	    public void testRunFinished(Result result) throws Exception {
	    }
	    public void testStarted(Description description) throws Exception {
	    }
	    public void testFinished(Description description) throws Exception {
	    	scopeExecution(description, true);
	    }

	    public void testFailure(Failure failure) throws Exception {
	    	scopeExecution(failure.getDescription(), false);
	    }

	    public void testAssumptionFailure(Failure failure) {
	    	scopeExecution(failure.getDescription(), false);
	    }

	    public void testIgnored(Description description) throws Exception {
	        //System.out.println("Ignored: " + description.getMethodName());
	    }
	    
	    //--helpers--
	    private void scopeExecution(Description desc, boolean pass) {
	        if (results != null) {
	        	String target = getMethodScopeTarget(desc);
	        	String value = getMethodScopeValue(desc);
	        	if (StringUtils.isNotEmpty(value)) {
	        		target = StringUtils.isNotEmpty(target) ? target : "";
	        		String s = String.format("%s:%s: %s", results.scope, target, value);
	        		ScopeResult res = new ScopeResult();
	        		res.pass = pass;
	        		res.scope = s;
	        		results.executions.add(res);
	        	}
	        }
	    	
	    }
	    private String getClassScopeValue(Description desc) {
        	Class<?> testClass = desc.getTestClass();
        	String testClassName = testClass.getSimpleName();
	        //System.out.println("TCCC: " + testClassName);
	        if (testClass.isAnnotationPresent(Scope.class)) {
        		Scope[] ar = testClass.getAnnotationsByType(Scope.class);
        		if (ar.length > 0) {
        			Scope scope = ar[0];
        			return scope.value();
        		}
	        }
	    	return null;
	    }
	    private String getMethodScopeValue(Description desc) {
        	Class<?> testClass = desc.getTestClass();
        	Method meth = findTestMethod(testClass, desc.getMethodName());
        	if (meth != null) {
        		Scope[] ar = meth.getAnnotationsByType(Scope.class);
        		if (ar.length > 0) {
        			Scope scope = ar[0];
        			return scope.value();
        		}

        	}
	    	return null;
	    }
	    private String getMethodScopeTarget(Description desc) {
        	Class<?> testClass = desc.getTestClass();
        	Method meth = findTestMethod(testClass, desc.getMethodName());
        	if (meth != null) {
        		Scope[] ar = meth.getAnnotationsByType(Scope.class);
        		if (ar.length > 0) {
        			Scope scope = ar[0];
        			return scope.target();
        		}

        	}
	    	return null;
	    }
	    private Method findTestMethod(Class<?> clazz, String methodName) {
            for (Method method : clazz.getMethods()) {
                if (method.getName().equals(methodName)) {
                	return method;
                }
            }
            return null;
	    }
	    
	}