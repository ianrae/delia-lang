package org.delia.validation;

import org.delia.error.ErrorTracker;
import org.delia.runner.ResultValue;
import org.delia.type.DValue;

import java.util.List;

public interface ValidationRunner {

	boolean validateFieldsOnly(DValue dval);

	boolean validateDVal(DValue dval);

	void validateRelationRules(DValue dval);

	void propogateErrors(ResultValue res);
	void propogateErrors(ErrorTracker errorTracker);

	boolean validateDVals(List<DValue> dvalList);

	boolean validateDependentRules(DValue partialDVal);

	void enableRelationModifier(boolean b);

	boolean isPopulateFKsFlag();

	void setPopulateFKsFlag(boolean populateFKsFlag);

	void enableInsertFlag(boolean b);

	void enableUpsertFlag(boolean b);

	void setUpsertPKVal(DValue keyval);

	void setSoftMandatoryRelationFlag(boolean b);

}