package org.delia.util;

import org.delia.error.DeliaError;
import org.delia.runner.DeliaException;

public class DeliaExceptionHelper {

	public static void throwError(String id, String fmt, Object... args) {
		String msg = String.format(fmt, args);
		DeliaError err = new DeliaError(id, msg);
		throw new DeliaException(err);
	}
	public static void throwError(DeliaError err) {
		throw new DeliaException(err);
	}
	public static DeliaError buildError(String id, String fmt, Object... args) {
		String msg = String.format(fmt, args);
		DeliaError err = new DeliaError(id, msg);
		return err;
	}
	public static void throwNotImplementedError(String fmt, Object... args) {
		String msg = String.format(fmt, args);
		DeliaError err = new DeliaError("not-implemented", msg);
		throw new DeliaException(err);
	}
	public static void throwUnknownFieldError(String typeName, String fieldName) {
		String msg = String.format("Type '%s', field '%s' doesn't exist", typeName, fieldName);
		DeliaError err = new DeliaError("unknown-field", msg);
		throw new DeliaException(err);
	}
	
	

}
