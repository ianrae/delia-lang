package org.delia.error;

import java.util.Locale;
import java.util.ResourceBundle;

import org.delia.log.Log;
import org.delia.log.LogLevel;
import org.delia.util.StringUtil;


public class ErrorFormatterServiceImpl implements ErrorFormatterService {

//	@Configuration
//	public static class Config {
//		public static final String resourceBundlePath = ""; //set this to your desired bundle path
//	}		
	
	private Log logger;

	public ErrorFormatterServiceImpl(Log log) {
		this.logger = log;
	}
	
	private String currentBundlePath;
	private ResourceBundle currentBundle;
	
	@Override
	public String format(DeliaError err) {
		return this.format(err, Locale.getDefault());
	}
	@Override
	public String format(DeliaError err, Locale locale) {
		String dateStr = err.getTimestamp().toString();
		
		String textMessage = getTextMessage(err, locale);
		String s = String.format("%s: [%s] - (%s) - %s", dateStr, err.getId(), err.getArea(), textMessage);
		return s;
	}
	protected synchronized String getTextMessage(DeliaError err, Locale locale) {
		String textMessage = err.getMsg();
		String bundlePath = ""; //ctx.config().getString("resourceBundlePath");
		if (StringUtil.hasText(bundlePath)) {
			if (! bundlePath.equals(currentBundlePath)) {
				ResourceBundle bundle;
				try {
					logger.log("loading resource bundle '%s'", bundlePath);
					bundle = ResourceBundle.getBundle(bundlePath, locale);
					currentBundle = bundle;
					currentBundlePath = bundlePath;
				} catch (Exception e) {
					String errmsg = String.format("failed to load resource bundle '%s'", bundlePath);
					logger.logException(LogLevel.ERROR, errmsg, e);
				}
			}

			if (currentBundle != null && currentBundle.containsKey(err.getId())) {
				textMessage = currentBundle.getString(err.getId());
			}
		}
		return textMessage;
	}
	
	@Override
	public String formatValidationError(ValidationError err) {
		return formatValidationError(err, Locale.getDefault());
	}
	@Override
	public String formatValidationError(ValidationError err, Locale locale) {
		String textMessage = getTextMessage(err, locale);
		String s = String.format("%s", textMessage);
		//FUTURE: maybe include arg1,...
		return s;
	}
}
