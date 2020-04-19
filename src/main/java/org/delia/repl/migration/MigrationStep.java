package org.delia.repl.migration;

public class MigrationStep {
	public String name;
	public String arg1;
	public String arg2;
	
	public MigrationStep(String name, String arg1) {
		this.name = name;
		this.arg1 = arg1;
	}
	public MigrationStep(String name, String arg1, String arg2) {
		this.name = name;
		this.arg1 = arg1;
		this.arg2 = arg2;
	}
}