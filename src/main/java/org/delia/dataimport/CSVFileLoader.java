package org.delia.dataimport;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.commons.io.FilenameUtils;
import org.delia.runner.inputfunction.LineObj;
import org.delia.util.DeliaExceptionHelper;

import au.com.bytecode.opencsv.CSVReader;


public class CSVFileLoader implements InputFileLoader {
	private String path;
	private CSVReader csvreader;
	private int lineNum;
	private String delim = ",";
	private boolean ignoreBlankLines = true;
	private String[] pendingNextLine;
	private int numHdrRows = 1;

	@Override
	public void init(String path) {
		this.path = path;
		open(path);
	}
	
	@Override
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
	@Override
	public void close() {
		if (csvreader != null) {
			try {
				csvreader.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	@Override
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

	@Override
	public String getFileName() {
		return FilenameUtils.getName(path);
	}

	@Override
	public LineObj readHdrRow() {
		if (numHdrRows <= 0) {
			return null;
		}
		
		if (! hasNext()) {
			return null;
		}
		return next();
	}
}