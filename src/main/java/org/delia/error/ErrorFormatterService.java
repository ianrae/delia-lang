package org.delia.error;

import java.util.Locale;


public interface ErrorFormatterService  {

	String format(DeliaError err);
	String format(DeliaError err, Locale locale);
	String formatValidationError(ValidationError err);
	String formatValidationError(ValidationError err, Locale locale);
}
