package org.delia.util;

import org.delia.compiler.ast.AST;
import org.delia.error.DeliaError;
import org.delia.runner.DeliaException;

import java.util.List;

public class DeliaExceptionHelper {

	public static void throwError(AST.Loc loc, String id, String fmt, Object... args) {
		String msg = String.format(fmt, args);
		DeliaError err = new DeliaError(id, msg);
		err.setLoc(loc);
		throw new DeliaException(err);
	}
	public static void throwError(String id, String fmt, Object... args) {
		String msg = String.format(fmt, args);
		DeliaError err = new DeliaError(id, msg);
		throw new DeliaException(err);
	}
	public static void throwError(DeliaError err) {
		throw new DeliaException(err);
	}
	public static void throwErrors(String id, List<DeliaError> errors) {
//		String msg = String.format(fmt, args);
		throw new DeliaException(errors);
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
	public static void throwUnknownTypeError(String typeName) {
		String msg = String.format("Type '%s' doesn't exist", typeName);
		DeliaError err = new DeliaError("unknown-type", msg);
		throw new DeliaException(err);
	}
	public static void throwUnknownFieldError(String typeName, String fieldName) {
		String msg = String.format("Type '%s', field '%s' doesn't exist", typeName, fieldName);
		DeliaError err = new DeliaError("unknown-field", msg);
		throw new DeliaException(err);
	}



}
