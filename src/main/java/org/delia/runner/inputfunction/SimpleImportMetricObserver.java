package org.delia.runner.inputfunction;

import java.util.Map;
import java.util.TreeMap;

public class SimpleImportMetricObserver implements ImportMetricObserver {
	public int rowCounter; //num rows attempted
	public int failedRowCounter;
	public int successfulRowCounter;
	public int[] currentRowMetrics = new int[OutputFieldHandle.NUM_METRICS];
	public Map<String,Integer> externalLoadMap = new TreeMap<>();

	@Override
	public void onRowStart(ProgramSet progsec, int rowNum) {
		rowCounter++;
	}

	@Override
	public void onRowEnd(ProgramSet progsec, int rowNum, boolean success) {
		if (! success) {
			failedRowCounter++;
		} else {
			successfulRowCounter++;
		}
		
		int n = 0;
		for(int i = 0; i < currentRowMetrics.length; i++) {
			n += currentRowMetrics[i];
		}
		//n is number of errors for this row
	}

	@Override
	public void onNoMappingError(ImportSpec ispec, String outputFieldName) {
		OutputFieldHandle ofh = findOfh(ispec, outputFieldName);
		if (ofh == null) {
			for(OutputFieldHandle tmp: ispec.unMappedOfhList) {
				if (tmp.fieldName.equals(outputFieldName)) {
					ofh = tmp;
					break;
				}
			}
		}
		
		int k = OutputFieldHandle.INDEX_N;
		currentRowMetrics[k]++;
		ofh.arMetrics[k]++;
	}

	@Override
	public void onMissingError(ImportSpec ispec, String outputFieldName) {
		OutputFieldHandle ofh = findOfh(ispec, outputFieldName);
		if (ofh == null) return;
		
		int k = OutputFieldHandle.INDEX_M;
		currentRowMetrics[k]++;
		ofh.arMetrics[k]++;
	}

	@Override
	public void onInvalid1Error(ImportSpec ispec, String outputFieldName) {
		OutputFieldHandle ofh = findOfh(ispec, outputFieldName);
		if (ofh == null) return;

		int k = OutputFieldHandle.INDEX_I1;
		currentRowMetrics[k]++;
		ofh.arMetrics[k]++;
	}
	@Override
	public void onInvalid2Error(ImportSpec ispec, String outputFieldName) {
		OutputFieldHandle ofh = findOfh(ispec, outputFieldName);
		if (ofh == null) return;

		int k = OutputFieldHandle.INDEX_I2;
		currentRowMetrics[k]++;
		ofh.arMetrics[k]++;
	}

	@Override
	public void onDuplicateError(ImportSpec ispec, String outputFieldName) {
		OutputFieldHandle ofh = findOfh(ispec, outputFieldName);
		if (ofh == null) return;
		
		int k = OutputFieldHandle.INDEX_D;
		currentRowMetrics[k]++;
		ofh.arMetrics[k]++;
	}

	@Override
	public void onRelationError(ImportSpec ispec, String outputFieldName) {
		OutputFieldHandle ofh = findOfh(ispec, outputFieldName);
		if (ofh == null) return;
		
		int k = OutputFieldHandle.INDEX_R;
		currentRowMetrics[k]++;
		ofh.arMetrics[k]++;
	}
	
	private OutputFieldHandle findOfh(ImportSpec ispec, String outputFieldName) {
		for(OutputFieldHandle ofh: ispec.ofhList) {
			if (ofh.fieldName.equals(outputFieldName)) {
				return ofh;
			}
		}
		return null;
	}

	@Override
	public void onLoadedExternally(ImportSpec ispec, int size) {
		String typeName = ispec.structType.getName();
		Integer n = externalLoadMap.get(typeName);
		if (n == null) {
			n = 0;
		}
		externalLoadMap.put(typeName, n + size);
	}
}