package org.delia.log;

import org.delia.util.BlobUtils;

/**
 * Blobs can be huge so we don't want to write them directly to the 
 * log. This class will write the first 100 bytes
 * @author ian
 *
 */
public class LoggableBlob {
	public int maxCharsToLog = 100;
	private String base64Str;
	
	public LoggableBlob(String base64Str) {
		this.base64Str = base64Str;
	}
	//TODO: add byte array ctor later
	
	public String toLoggableHexString() {
		if (base64Str == null) {
			return "null!";
		} else if (base64Str.length() < maxCharsToLog) {
			return BlobUtils.base64ToHexString(base64Str);
		} else {
			//TODO this is really inefficient. fix later!!
			String hex = BlobUtils.base64ToHexString(base64Str);
			return hex.substring(0, maxCharsToLog) + "...";
		}
	}

	@Override
	public String toString() {
		if (base64Str == null) {
			return "null!";
		} else if (base64Str.length() < maxCharsToLog) {
			return base64Str;
		} else {
			return base64Str.substring(0, maxCharsToLog) + "...";
		}
	}
}
