package org.delia.runner.inputfunction;

public interface ImportMetricObserver {
	void onRowStart(ProgramSet progsec, int rowNum);
	void onRowEnd(ProgramSet progsec, int rowNum, boolean success);

	void onNoMappingError(ImportSpec ispec, String outputFieldName);
	void onMissingError(ImportSpec ispec, String outputFieldName);
	void onInvalid1Error(ImportSpec ispec, String outputFieldName);
	void onInvalid2Error(ImportSpec ispec, String outputFieldName);
	void onDuplicateError(ImportSpec ispec, String outputFieldName);
	void onRelationError(ImportSpec ispec, String outputFieldName);
	
	void onLoadedExternally(ImportSpec ispec, int size);
}