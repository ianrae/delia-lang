package org.delia.util;

import org.delia.error.DeliaError;
import org.delia.runner.DeliaException;

public class DeliaExceptionHelper {

	public static void throwError(String id, String fmt, Object... args) {
		String msg = String.format(fmt, args);
		DeliaError err = new DeliaError(id, msg);
		throw new DeliaException(err);
	}
}
