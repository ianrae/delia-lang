package org.delia.dataimport;


public class CSVInputFileLoaderFactory implements InputFileLoaderFactory {

	@Override
	public InputFileLoader create() {
		return new CSVFileLoader();
	}


}
