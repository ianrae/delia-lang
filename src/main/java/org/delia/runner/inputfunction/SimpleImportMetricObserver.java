package org.delia.runner.inputfunction;

public class SimpleImportMetricObserver implements ImportMetricObserver {
	public int rowCounter; //num rows attempted
	public int failedRowCounter;
	public int successfulRowCounter;
	public int[] currentRowMetrics = new int[OutputFieldHandle.NUM_METRICS];

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
	public void onInvalidError(ImportSpec ispec, String outputFieldName) {
		OutputFieldHandle ofh = findOfh(ispec, outputFieldName);
		if (ofh == null) return;

		int k = OutputFieldHandle.INDEX_I;
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

	
}