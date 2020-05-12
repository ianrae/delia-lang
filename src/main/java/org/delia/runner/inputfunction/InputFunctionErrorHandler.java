package org.delia.runner.inputfunction;


import java.util.List;

import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.error.DeliaError;
import org.delia.error.DetailedError;
import org.delia.runner.DeliaException;
import org.delia.type.DStructType;
import org.delia.type.DValue;


public class InputFunctionErrorHandler extends ServiceBase {
	private ImportMetricObserver metricsObserver;
	private InputFunctionServiceOptions options;

	public InputFunctionErrorHandler(FactoryService factorySvc, ImportMetricObserver observer, InputFunctionServiceOptions options) {
		super(factorySvc);
		this.metricsObserver = observer;
		this.options = options;
	}

	public void handleException(DeliaException ex, DStructType structType, InputFunctionRequest request, InputFunctionResult fnResult, int lineNum, List<DeliaError> errL) {
		fnResult.numFailedRowInserts++;
		DeliaError err = ex.getLastError();
		boolean addErrorFlag = true;

		if (errIdIStartsWith(err, "rule-")) {
			addErrorFlag = !options.ignoreRelationErrors;
			if (metricsObserver != null && err instanceof DetailedError) {
				ImportSpec ispec = findImportSpec(request, structType);
				if (err.getId().equals("rule-relationOne") || err.getId().equals("rule-relationOne")) {
					DetailedError derr = (DetailedError) err;
					metricsObserver.onRelationError(ispec, derr.getFieldName());
				} else {
					DetailedError derr = (DetailedError) err;
					metricsObserver.onInvalid2Error(ispec, derr.getFieldName());
				}
			}
		} else if (errIdIs(err, "duplicate-unique-value")) {
			if (metricsObserver != null && err instanceof DetailedError) {
				DetailedError derr = (DetailedError) err;
				//TODO this will fails for h2 and postgres. fix them!
				ImportSpec ispec = findImportSpec(request, structType);
				metricsObserver.onDuplicateError(ispec, derr.getFieldName());
			}
		}

		if (addErrorFlag) {
			addError(ex, errL, lineNum);
		}
	}	

	private boolean errIdIs(DeliaError err, String target) {
		if (err.getId() != null && err.getId().equals(target)) {
			return true;
		}
		return false;
	}

	private boolean errIdIStartsWith(DeliaError err, String target) {
		if (err.getId() != null && err.getId().startsWith(target)) {
			return true;
		}
		return false;
	}

	private ImportSpec findImportSpec(InputFunctionRequest request, DStructType structType) {
		for(ProgramSet.OutputSpec ospec: request.progset.outputSpecs) {
			if (ospec.structType == structType) {
				return ospec.ispec;
			}
		}
		return null;
	}

	private void addError(DeliaError err, List<DeliaError> errL, int lineNum) {
		err.setLineAndPos(lineNum, 0);
		errL.add(err);
		log.logError("error: %s", err.toString());
	}
	private void addError(DeliaException e, List<DeliaError> errL, int lineNum) {
		DeliaError err = e.getLastError();
		addError(err, errL, lineNum);
	}

}