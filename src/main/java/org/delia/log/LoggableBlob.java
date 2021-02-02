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
	private byte[] byteArr;
	
	public LoggableBlob(String base64Str) {
		this.base64Str = base64Str;
	}
	public LoggableBlob(byte[] byteArr) {
		this.byteArr = byteArr;
	}
	//TODO: add byte array ctor later
	
	public String toLoggableHexString() {
		if (byteArr != null) {
			//TODO this is really inefficient. fix later!!
			String hex = BlobUtils.byteArrayToHexString(byteArr);
			String suffix = hex.length() > maxCharsToLog ? "..." : "";
			return hex.substring(0, maxCharsToLog) + suffix;
		}
		
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
		if (byteArr != null) {
			//TODO this is really inefficient. fix later!!
			String s = BlobUtils.toBase64(byteArr);
			int n = s.length() < maxCharsToLog ? s.length() : maxCharsToLog;
			String suffix = s.length() > maxCharsToLog ? "..." : "";
			return s.substring(0, n) + suffix;
		}
		
		if (base64Str == null) {
			return "null!";
		} else if (base64Str.length() < maxCharsToLog) {
			return base64Str;
		} else {
			return base64Str.substring(0, maxCharsToLog) + "...";
		}
	}
}
