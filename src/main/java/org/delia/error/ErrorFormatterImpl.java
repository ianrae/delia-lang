package org.delia.error;

import java.util.Locale;
import java.util.ResourceBundle;

import org.apache.commons.lang3.StringUtils;
import org.delia.log.DeliaLog;
import org.delia.log.LogLevel;
import org.delia.util.StringUtil;


public class ErrorFormatterImpl implements ErrorFormatter {

	private DeliaLog logger;
	private String currentBundlePath;
	private ResourceBundle currentBundle;
	private String deliaSrc;

	public ErrorFormatterImpl(DeliaLog log) {
		this.logger = log;
	}


	@Override
	public String format(DeliaError err) {
		return this.format(err, Locale.getDefault());
	}
	@Override
	public String format(DeliaError err, Locale locale) {
//		String dateStr = err.getTimestamp().toString();

		int lineNum = 0;
		if (err.getLoc() != null) {
			lineNum = err.getLoc().lineNum;
		}
		String textMessage = getTextMessage(err, locale);
//		String s = String.format("%s: [%s] - (%s) - %s", dateStr, err.getId(), err.getArea(), textMessage);
		//{fullpath to src file}:153: error: ')' expected
		String srcFileName = "line "; //TODO add later
		String s = String.format("%s%d: error: [%s] - %s", srcFileName, lineNum, err.getId(), textMessage);
		if (err.getLoc() != null && deliaSrc != null) {
			try{
				int lineIndex = err.getLoc().lineNum - 1;
				String[] ar = deliaSrc.split("\\n");
				String line = ar[lineIndex];
				String str = StringUtils.repeat(' ', err.getLoc().charOffset);
				String line2 = String.format("%s^", str);
				s = String.format("%s\n%s\n%s", s, line, line2);
			} catch (Exception e) {
			}
		}
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

	@Override
	public String getDeliaSrc() {
		return deliaSrc;
	}
	@Override
	public void setDeliaSrc(String deliaSrc) {
		this.deliaSrc = deliaSrc;
	}

}
