package org.delia.runner.inputfunction;

import org.delia.type.DStructType;
import org.delia.type.DValue;

public class OutputFieldHandle {
	public static final int NUM_METRICS = 6;
	public static final int INDEX_N = 0; //not-in-mapping. struct field not mentioned in input function.
	public static final int INDEX_M = 1; //missing. input data was null
	public static final int INDEX_I1 = 2;
	public static final int INDEX_I2 = 3;
	public static final int INDEX_D = 4;
	public static final int INDEX_R = 5;

	public DStructType structType;
	public String fieldName;
	public int fieldIndex; //index of field in structType
	public int ifhIndex = -1; //if < 0 then is synthetic field
	//TODO: add list additional ifh indexes for combine(FIRSTNAME,'',LASTNAME)
	public int[] arMetrics; //for NMIDR error counters
	public DValue syntheticValue; //null or synthetic value
	public String syntheticFieldName;
}