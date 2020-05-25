package org.delia.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;


public class ResourceTextFileReader {

	public String readAsSingleString(String resourcePath) {
		String contents = null;

		try {
			contents = doReadAsSingleString(resourcePath);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return contents;
	}

	private String doReadAsSingleString(String resourcePath) throws Exception {
		InputStream in = this.getClass().getClassLoader().getResourceAsStream(resourcePath);
		if (in == null) {
			throw new IllegalArgumentException(String.format("can't find: %s", resourcePath));
		}
		StringBuilder sb = new StringBuilder();
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		if (in != null) {  
			String str;
			while ((str = reader.readLine()) != null) {
				sb.append(str);
				sb.append(StringUtil.eol());
			}                
		}		
		return sb.toString();
	}
}