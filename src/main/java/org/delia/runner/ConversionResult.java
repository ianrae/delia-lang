package org.delia.runner;

import org.delia.error.ErrorTracker;
import org.delia.type.DValue;

import java.util.HashMap;
import java.util.Map;

public class ConversionResult {
	public DValue dval;
	public ErrorTracker localET;
	public Map<String,DValue> extraMap = new HashMap<>(); //ok for thread safety. short-lived obj 
	public Map<String, String> assocCrudMap;
}