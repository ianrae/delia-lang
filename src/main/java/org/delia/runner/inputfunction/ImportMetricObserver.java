package org.delia.runner.inputfunction;

public interface ImportMetricObserver {
	void onRowStart(ProgramSet progsec, int rowNum);
	void onRowEnd(ProgramSet progsec, int rowNum, boolean success);

	void onNoMappingError(ImportSpec ispec, OutputFieldHandle ofh);
	void onMissingError(ImportSpec ispec, OutputFieldHandle ofh);
	void onInvalidError(ImportSpec ispec, OutputFieldHandle ofh);
	void onDuplicateError(ImportSpec ispec, OutputFieldHandle ofh);
	void onRelationError(ImportSpec ispec, OutputFieldHandle ofh);
}