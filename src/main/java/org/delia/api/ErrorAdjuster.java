package org.delia.api;

import java.util.ArrayList;
import java.util.List;

import org.delia.error.DeliaError;
import org.delia.error.DetailedError;

/**
 * Remove unnecessary duplicate errors.
 * 
 * @author Ian Rae
 *
 */
public class ErrorAdjuster {
	
	public List<DeliaError> adjustErrors(List<DeliaError> errL) {
		List<DeliaError> adjustedL = new ArrayList<>();
		
		for(DeliaError err: errL) {
			String target = "rule-relationMany";
			if (target.equals(err.getId())) {
				if (findMatch("rule-mandatory", err, errL)) {
					continue;
				}
			}
			adjustedL.add(err);
		}
		
		return adjustedL;
	}

	private boolean findMatch(String targetId, DeliaError targetErr, List<DeliaError> errL) {
		if (! (targetErr instanceof DetailedError)) {
			return false;
		}
		DetailedError target = (DetailedError) targetErr;
		
		for(DeliaError err: errL) {
			if (! targetId.equals(err.getId())) {
				continue;
			}
			
			if (err instanceof DetailedError) {
				DetailedError derr = (DetailedError) err;
				if (target.getTypeName().equals(derr.getTypeName()) && 
						target.getFieldName().equals(derr.getFieldName())) {
					return true;
				}
			}
		}
		return false;
	}

}
