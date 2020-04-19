package org.delia.typebuilder;

import org.delia.error.DeliaError;

public class FutureDeclError extends DeliaError {
	public String baseTypeName;
	
	public FutureDeclError(String id, String msg) {
		super(id, msg);
	}
}
