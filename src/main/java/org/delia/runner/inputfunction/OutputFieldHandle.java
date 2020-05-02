package org.delia.runner.inputfunction;

import org.delia.type.DStructType;
import org.delia.type.DValue;

public class OutputFieldHandle {
	public static final int NUM_METRICS = 5;
	public static final int INDEX_N = 0;
	public static final int INDEX_M = 1;
	public static final int INDEX_I = 2;
	public static final int INDEX_D = 3;
	public static final int INDEX_R = 4;

	public DStructType structType;
	public String fieldName;
	public int fieldIndex; //index of field in structType
	public int ifhIndex = -1; //if < 0 then is synthetic field
	//TODO: add list additional ifh indexes for combine(FIRSTNAME,'',LASTNAME)
	public int[] arMetrics; //for NMIDR error counters
	public DValue syntheticValue; //null or synthetic value
}