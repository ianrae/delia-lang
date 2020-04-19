package org.delia.valuebuilder;

import java.util.ArrayList;
import java.util.List;

import org.delia.error.DetailedError;
import org.delia.error.ErrorType;
import org.delia.type.DType;
import org.delia.type.DValue;


public abstract class DValueBuilder {
	protected List<DetailedError> valErrorList = new ArrayList<>();
	protected boolean finished;
	protected DValue newDVal;
	protected DType type;
	public String fieldName; //for logging errors only. can be null
	
	public abstract void buildFromString(String input);

	public boolean finish() {
		finished = true;
		onFinish();
		boolean ok = wasSuccessful();
		return ok;
	}
	
	protected abstract void onFinish();

	public boolean wasSuccessful() {
		return finished && valErrorList.isEmpty();
	}
	public List<DetailedError> getValidationErrors() {
		return valErrorList;
	}
	public DValue getDValue() {
		return newDVal;
	}
	
	private void addError(DetailedError err) {
	    this.valErrorList.add(err);
	}

	public void addParsingError(String msg, String inputText) {
		DetailedError nem = addOldErrorMsgZ(ErrorType.PARSINGERROR, msg);
		nem.setActualValue(inputText);
	}
	public void addParsingError(String msg, String inputText, String fieldName) {
		DetailedError nem = addOldErrorMsgZ(ErrorType.PARSINGERROR, msg);
		nem.setFieldName(fieldName);
		nem.setActualValue(inputText);
	}
	
    public DetailedError addOldErrorMsgZ(ErrorType errType, String message) {
        DetailedError err = new DetailedError(errType.name(), message);
        err.setErrorType(DetailedError.Type.IO_ERROR); //!!
        err.setErrorName(errType.name());
        err.setFieldName((fieldName == null) ? "?" : fieldName);
        err.setSrcFile("?");
        err.setTypeName("?");
        addError(err);
    	return err;
    }
	
	protected void addNoDataError(String msg) {
		addOldErrorMsgZ(ErrorType.NODATA, msg);
	}
	protected void addWrongTypeError(String s) {
		addOldErrorMsgZ(ErrorType.WRONGTYPE, String.format("wrong type - %s", s));
	}
    protected void addNoDataError() {
    	addOldErrorMsgZ(ErrorType.NODATA, "no data");
    }
	protected void addDuplicateFieldError(String msg, String fieldName) {
		addOldErrorMsgZ(ErrorType.DUPLICATEFIELD, msg).setFieldName(fieldName);
	}
	protected void addMissingFieldError(String msg, String fieldName) {
		addOldErrorMsgZ(ErrorType.MISSINGFIELD, msg).setFieldName(fieldName);
	}
	protected void addUnknownFieldError(String msg) {
		addOldErrorMsgZ(ErrorType.UNKNOWNFIELD, msg);
	}
	protected void addRefError(String msg) {
		addOldErrorMsgZ(ErrorType.REFERROR, msg);
	}

	public DType getType() {
		return type;
	}
}