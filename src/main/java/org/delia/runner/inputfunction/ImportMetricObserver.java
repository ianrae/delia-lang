package org.delia.runner.inputfunction;

public interface ImportMetricObserver {
	void onRowStart(ImportSpec ispec, int rowNum);
	void onRowEnd(ImportSpec ispec, int rowNum, boolean success);

	void onNoMappingError(ImportSpec ispec, OutputFieldHandle ofh);
	void onMissingError(ImportSpec ispec, OutputFieldHandle ofh);
	void onInvalidError(ImportSpec ispec, OutputFieldHandle ofh);
	void onDuplicateError(ImportSpec ispec, OutputFieldHandle ofh);
	void onRelationError(ImportSpec ispec, OutputFieldHandle ofh);
}