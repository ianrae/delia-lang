package org.delia.dataimport;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.delia.runner.inputfunction.LineObj;
import org.delia.runner.inputfunction.LineObjIterator;
import org.delia.util.DeliaExceptionHelper;

import au.com.bytecode.opencsv.CSVReader;


public class CSVFileLoader implements LineObjIterator {
	private String path;
	private CSVReader csvreader;
	private int lineNum;
	private String delim = ",";
	private boolean ignoreBlankLines = true;
	private String[] pendingNextLine;
	private int numHdrRows = 1;

	public CSVFileLoader(String path) {
		this.path = path;
		open(path);
	}
	
	public void open(String path) {
		File f = new File(path);
		try {
			InputStream inputStream = new FileInputStream(f);
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
			char delimChar = getDelim();
			csvreader = new CSVReader(reader, delimChar);
			lineNum = 0;
		} catch (Exception e) {
			DeliaExceptionHelper.throwError("csv-file-open-failed", "Failed to open path '%s'", path);
		}

	}

	public char getDelim() {
		return delim == null ? ',' : delim.charAt(0);
	}

	@Override
	public boolean hasNext() {
		if (pendingNextLine != null) {
			return true;
		}
		
		try {
			pendingNextLine = csvreader.readNext();
			if (pendingNextLine != null) {
				return true;
			}
		} catch (IOException e) {
			DeliaExceptionHelper.throwError("csv-read-failed", "io error: %s", e.getMessage());
		}
		
		return false;
	}
	
	private LineObj doReadLine() throws IOException {
		if (pendingNextLine != null) {
			LineObj lineObj = new LineObj(pendingNextLine, lineNum++);
			pendingNextLine = null;
			return lineObj;
		}
		String[] nextLine = csvreader.readNext();
		if (nextLine == null) {
			return null;
		}

		return new LineObj(nextLine, lineNum++);
	}

	@Override
	public LineObj next() {
		LineObj lineObj = null;
		try {
			lineObj = doReadLine();
			
			if (ignoreBlankLines) {
				while (lineObj != null && lineObj.elements.length == 1 && lineObj.elements[0].isEmpty()) {
					lineObj = doReadLine();
				}
			}
		} catch (IOException e) {
			DeliaExceptionHelper.throwError("csv-read-failed", "io error: %s", e.getMessage());
		}
		
		return lineObj;
	}

	@Override
	public int getNumHdrRows() {
		return numHdrRows;
	}
}