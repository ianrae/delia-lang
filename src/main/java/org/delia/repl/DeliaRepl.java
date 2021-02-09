package org.delia.repl;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.delia.DeliaSession;
import org.delia.db.sql.ConnectionString;
import org.delia.runner.ResultValue;

public class DeliaRepl {
	ReplRunner runner;
	private boolean shouldQuit = false;
	private String baseDir;
//	private ConnectionInfo connectionInfo;
	private ConnectionString connectionDef;
	
	public DeliaRepl(ConnectionString connectionDef, String baseDir) {
		this.baseDir = baseDir;
		this.connectionDef = connectionDef;
		this.runner = new ReplRunner(connectionDef, new ConsoleOutputWriter());
	}

	public void run()  {
		log(String.format("Delia REPL - dbType: %s", connectionDef.dbType.name()));
		log("");
		while(! shouldQuit) {
			try {
				doRunOne();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	private void doRunOne() throws Exception  {
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		//			System.out.println("");
		String prefix = buildPrefix(); //(mostRecentRes == null) ? "(no session)" : "sess";
		System.out.print(String.format("%s> ", prefix));

		String input = reader.readLine();
		input = input.trim();
		if (isQuit(input)) {
			shouldQuit = true;
			return;
		}

		if (input.trim().isEmpty()) {
			return;
		}

		if (input.equals("restart")) {
			runner.restart(null);
			return;
		}

		//hack
		if (input.equals("d2")) {
			input = "run delia2.txt";
		}

		input = doPathFixup(input, "run");
		input = doPathFixup(input, "load");
		input = doPathFixup(input, "r");
		input = doPathFixup(input, "continue");
		input = doPathFixup(input, "c");
		input = doPathFixup(input, "load");
		input = doPathFixup(input, "migration run");
		input = doPathFixup(input, "mr");

		ResultValue res;
		boolean inSess = runner.inSession();
		res = runner.executeReplCmdOrDelia(input);

		//log("");
		String output = runner.toReplResult(res);
		if (res.ok) {
			if (! inSess && runner.inSession()) {
				output += String.format("\nOK - session '%s' created.\n", runner.getSessionName());
			} else {
				output += "\nOK\n";
			}
		}
		log(output);
	}

	private boolean isQuit(String input) {
		input = input.toLowerCase();
		if (input.equals("q") || input.equals("quit") || input.equals("exit")) {
			return true;
		}
		return false;
	}

	private String buildPrefix() {
		DeliaSession sess = runner.getMostRecentSess();
		String name = runner.getSessionName();
		name = name == null ? "session" : name;
		String prefix = (sess == null) ? "(no session)" : name;
		return prefix;
	}

	private void log(String s) {
		System.out.println(s);
	}

	private String doPathFixup(String input, String cmd) {
		int n = cmd.length() + 1;
		if (input.startsWith(cmd + " ")) {
			String s = input.substring(n);
			if (!input.contains("/") && !input.contains("\\")) {
				input = cmd + " " + baseDir + s;
			}
		}
		return input;
	}
}