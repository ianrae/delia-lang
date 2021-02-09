package org.delia;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.List;

import org.delia.util.ResourceTextFileReader;
import org.delia.util.StringUtil;
import org.delia.util.TextFileReader;

public class DeliaLoader {

	public String fromFile(String path) throws IOException {
		ResourceTextFileReader r = new ResourceTextFileReader();
		String src = r.readAsSingleString(path);
		return src;
	}
	public String fromFile(File file) throws IOException {
		ResourceTextFileReader r = new ResourceTextFileReader();
		String src = r.readAsSingleString(file.getAbsolutePath());
		return src;
	}
	public String fromResource(String resourcePath) throws IOException {
		ResourceTextFileReader r = new ResourceTextFileReader();
		String src = r.readAsSingleString(resourcePath);
		return src;
	}
	public String fromReader(Reader reader) throws IOException {
		TextFileReader r = new TextFileReader();
		List<String> lines = r.ReadFileFromReader(reader);
		String src = StringUtil.flattenEx(lines, StringUtil.eol());
		return src;
	}
	public String fromInputStream(InputStream inStream) throws IOException {
		TextFileReader r = new TextFileReader();
		List<String> lines = r.ReadFileStream(inStream);
		String src = StringUtil.flattenEx(lines, StringUtil.eol());
		return src;
	}

}
