package org.delia.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;

public class DirectoryUtil {

	public static String cleanupPath(String path) {
		if (path.contains("\\")) {
			path = path.replace('\\', '/');
		}
		return path;
	}
	public static boolean existsFile(String path) {
		path = cleanupPath(path);
		File f = new File(path);
		return f.exists();
	}
	public static boolean isDirectory(String path) {
		File f = new File(path);
		return f.exists() && f.isDirectory();
	}
	public static List<String> getFilesIn(String path, String extension) {
		if (!extension.startsWith("*.")) {
			extension = "*." + extension;
		}
		File directory = new File(path);
		Collection<File> coll = FileUtils.listFiles(directory, new WildcardFileFilter(extension), null);

		List<String> list = new ArrayList<>();
		for(File f: coll) {
			list.add(f.getAbsolutePath());
		}
		return list;
	}
	public static List<String> getFilesInRecursive(String path, String extension) {
		File directory = new File(path);
		
		final String[] SUFFIX = {extension};  // use the suffix to filter
		Collection<File> files = FileUtils.listFiles(directory, SUFFIX, true);
		
		List<String> list = new ArrayList<>();
		for(File f: files) {
			list.add(f.getAbsolutePath());
		}
		return list;
	}

	public static String getCurrentDir() {
		File f = new File(".");
		String s = f.getAbsolutePath();
		if (s.endsWith(".")) {
			s = s.substring(0, s.length() - 1);
		}
		return s;
	}
}