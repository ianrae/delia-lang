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
}
