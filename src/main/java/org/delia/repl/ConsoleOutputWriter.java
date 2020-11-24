package org.delia.repl;

public class ConsoleOutputWriter implements ReplOutputWriter {

	@Override
	public void output(String s) {
		System.out.println(s);
	}

}
