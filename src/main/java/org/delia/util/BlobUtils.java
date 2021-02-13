package org.delia.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Base64;

public class BlobUtils {
	//https://www.baeldung.com/java-base64-encode-and-decode
	public static String toBase64(byte[] byteArr) {
		String encodedString = Base64.getEncoder().encodeToString(byteArr);
		return encodedString;
	}
	public static byte[] fromBase64(String encodedString) {
		byte[] decodedBytes = Base64.getDecoder().decode(encodedString);
		return decodedBytes;
	}
	
	public static InputStream toInputStream(byte[] byteArr) {
		InputStream is = new ByteArrayInputStream(byteArr);		
		return is;
	}
	
	public static byte[] hexStringToByteArray(String s) {
	    int len = s.length();
	    byte[] data = new byte[len / 2];
	    for (int i = 0; i < len; i += 2) {
	        data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
	                             + Character.digit(s.charAt(i+1), 16));
	    }
	    return data;
	}		
	public static String byteArrayToHexString(byte[] byteArr) {
		StringBuilder sb = new StringBuilder();
		for(byte bb: byteArr) {
			String s = String.format("%x", bb);
			sb.append(s);
		}
		return sb.toString();
	}		
	
	public static String base64ToHexString(String base64Str) {
		byte[] byteArr = BlobUtils.fromBase64(base64Str);
		String hex = BlobUtils.byteArrayToHexString(byteArr);
		return hex;
	}
}