package org.delia.repl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.delia.Delia;
import org.delia.DeliaSession;
import org.delia.api.MigrationAction;
import org.delia.builder.DeliaBuilder;
import org.delia.compiler.generate.DeliaGeneratePhase;
import org.delia.compiler.generate.SimpleFormatOutputGenerator;
import org.delia.db.schema.MigrationPlan;
import org.delia.db.sql.ConnectionDefinition;
import org.delia.log.Log;
import org.delia.log.LoggableBlob;
import org.delia.log.SimpleLog;
import org.delia.runner.DeliaException;
import org.delia.runner.QueryResponse;
import org.delia.runner.ResultValue;
import org.delia.type.BuiltInTypes;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.type.Shape;
import org.delia.util.DeliaExceptionHelper;
import org.delia.util.ResourceTextFileReader;
import org.delia.util.StringUtil;
import org.delia.util.TextFileReader;
import org.delia.zdb.DBInterfaceFactory;

public class ReplRunner  {
	private Log log = new SimpleLog();

	private Delia delia;
	private boolean isExternalDelia = false;
	private DBInterfaceFactory dbInterface;
	private DeliaSession mostRecentSess;

	private DeliaException mostRecentException;
	protected List<Cmd> allCmdsL = new ArrayList<>();
	private String loadSrc;

	private MigrationPlan currentMigrationPlan;

//	private ConnectionInfo connectionInfo;

	private String sessionName;

	protected ReplOutputWriter outWriter;

	private ConnectionDefinition connectionDef;
	public static boolean disableSQLLoggingDuringSchemaMigration = true;

	public ReplRunner(ConnectionDefinition connDef, ReplOutputWriter outWriter) {
		this.connectionDef = connDef;
		this.outWriter = outWriter;
		addAllCmds();
		restart(null);
	}
	public ReplRunner(DeliaSession externalDeliaSession, ReplOutputWriter outWriter) {
		this.connectionDef = null; //TODO: will this be a problem?
		this.outWriter = outWriter;
		addAllCmds();
		restart(externalDeliaSession);
	}
	protected void addAllCmds() {
		allCmdsL.add(new LoadCmd());
		allCmdsL.add(new RunFromResourceCmd()); //must be before runcmd
		allCmdsL.add(new RunCmd());
		allCmdsL.add(new GenerateMigrationPlanCmd());
		allCmdsL.add(new RunMigrationPlanCmd());
		allCmdsL.add(new ListTypesCmd());
		allCmdsL.add(new ListVarsCmd());
		allCmdsL.add(new CleanTablesCmd());
		allCmdsL.add(new ListDBTablesCmd());
		allCmdsL.add(new DBDeleteTableCmd());
		allCmdsL.add(new ContinueCmd());
		allCmdsL.add(new StatusCmd());
		allCmdsL.add(new DBLoggingCmd());
	}

	public void restart(DeliaSession externalDeliaSession) {
		if (externalDeliaSession == null) {
			this.delia = DeliaBuilder.withConnection(connectionDef).build();
		} else {
			this.delia = externalDeliaSession.getDelia();
			this.mostRecentSess = externalDeliaSession;
			this.isExternalDelia = true;
		}
		
		dbInterface = delia.getDBInterface();
		//TODO: are these ok for external delia? or do we need to reset at the end?
		dbInterface.getCapabilities().setRequiresSchemaMigration(true);
		dbInterface.enableSQLLogging(false);
//		if (dbInterface instanceof MemZDBInterfaceFactory) {
//			MemDBInterface memdb = (MemDBInterface) dbInterface;
//			memdb.createTablesAsNeededFlag = true;
//		}
		
		for(Cmd cmdx: allCmdsL) {
			CmdBase cmd = (CmdBase) cmdx;
			cmd.setFactorySvc(delia.getFactoryService());
			cmd.setOutputWriter(outWriter);
		}
		
		if (externalDeliaSession == null) {
			mostRecentSess = null;
		}
		mostRecentException = null;
		loadSrc = null;
		currentMigrationPlan = null;
		sessionName = null;
	}

	public ResultValue runFromFile(String path) {
		log.log("reading: %s", path);
		TextFileReader reader = new TextFileReader();
		List<String> lines = reader.readFile(path);
		String src = StringUtil.convertToSingleString(lines);
		this.sessionName = createSessionName(path);

		return this.executeReplCmdOrDelia(src);
	}
	public ResultValue continueFromFile(String path) {
		log.log("reading: %s", path);
		TextFileReader reader = new TextFileReader();
		List<String> lines = reader.readFile(path);
		String src = StringUtil.convertToSingleString(lines);

		return this.continueDelia(src);
	}
	public ResultValue runFromResource(String resPath) {
		log.log("reading: %s", resPath);
		ResourceTextFileReader r = new ResourceTextFileReader();
		String src = r.readAsSingleString(resPath);
//		String src = StringUtil.convertToSingleString(lines);
		this.sessionName = createSessionName(resPath);

		return this.executeReplCmdOrDelia(src);
	}

	public ResultValue loadFromFile(String path) {
		log.log("reading: %s", path);
		TextFileReader reader = new TextFileReader();
		List<String> lines = reader.readFile(path);
		String src = StringUtil.convertToSingleString(lines);

		this.loadSrc = src;
		this.sessionName = createSessionName(path);

		delia.getOptions().migrationAction = MigrationAction.DO_NOTHING;
		delia.getOptions().enableExecution = false;
		ResultValue res = executeReplCmdOrDelia(src);
		delia.getOptions().migrationAction = MigrationAction.MIGRATE;
		delia.getOptions().enableExecution = true;
		return res;
	}

	private String createSessionName(String path) {
		return FilenameUtils.getBaseName(path);
	}

	public ResultValue executeReplCmdOrDelia(String src) {
		src = src.trim();
		Cmd cmd = isReplCmd(src);
		if (cmd != null) {
			return runReplCmd(cmd);
		}
		
		//so it's delia source code
		if (! src.contains(" ") && ! src.contains("[")) {
			log.log("unknown command or statement");
			ResultValue res = new ResultValue();
			res.ok = true;
			res.val = null;
			return res;
		}
		
		if (inSession()) {
			return continueDelia(src);
		}
		
		return runDelia(src);
	}
	public ResultValue runDelia(String src) {
		mostRecentSess = null;
		//client.getOptions().disableSQLLoggingDuringSchemaMigration = disableSQLLoggingDuringSchemaMigration;
		ResultValue res = new ResultValue();
		try {
			DeliaSession sess = delia.beginSession(src);
			res = sess.getFinalResult();
			mostRecentSess = sess;
			
			Integer n = sess.getExecutionContext().registry.size();
			//log.log(n.toString());
			if (n  > DTypeRegistry.NUM_BUILTIN_TYPES) { //any types registered?
				this.loadSrc = src;
			}
			
		} catch (DeliaException e) {
			this.mostRecentException = e;
		}
		return res;
	}
	public ResultValue runMPlan(String src, MigrationPlan plan) {
		mostRecentSess = null;
		ResultValue res = new ResultValue();
		try {
			DeliaSession sess = delia.executeMigrationPlan(src, plan);
			mostRecentSess = sess;
			return sess.getFinalResult();
			
		} catch (DeliaException e) {
			this.mostRecentException = e;
		}
		return res;
	}
	private Cmd isReplCmd(String src) {

		for(Cmd ccc: allCmdsL) {
			ccc = ccc.isReplCmd(src);
			if (ccc != null) {
				return ccc;
			}
		}

		Cmd cmd = null;

		if (src.startsWith("? ")) {
			cmd = new InternalCmd();
			cmd.cmd = "?";
			cmd.arg1 = src.substring(cmd.cmd.length() + 1);
			return cmd;
		}
		return cmd;
	}
	private ResultValue runReplCmd(Cmd cmd) {
		String ss = cmd.cmd + ((cmd.arg1 == null) ? "" : " " + cmd.arg1);
		//log.log(ss);

		ResultValue res = null;
		try {
			res = doReplCmd(cmd);
		} catch (DeliaException e) {
			mostRecentException = e;
			res = new ResultValue();
		}
		return res;
	}
	private ResultValue doReplCmd(Cmd cmd) {
		if (!(cmd instanceof InternalCmd)) {
			return cmd.runCmd(cmd, this);
		}

		switch(cmd.cmd) {
		case "?":
		{
			String arg1 = cmd.arg1;
			if (!mostRecentSess.getExecutionContext().varMap.containsKey(arg1)) {
				DeliaExceptionHelper.throwError("repl", "can't find variable: " + arg1);
			} else {
				ResultValue res = mostRecentSess.getExecutionContext().varMap.get(arg1);
				res.varName = "?";
				res.ok = true;
				return res;
			}
		}
		break;
		default:
			break;
		}
		return null;
	}


	public ResultValue continueDelia(String src) {
		delia.getOptions().disableSQLLoggingDuringSchemaMigration = disableSQLLoggingDuringSchemaMigration;
		ResultValue res = new ResultValue();
		try {
			res = delia.continueExecution(src, mostRecentSess);
		} catch (DeliaException e) {
			this.mostRecentException = e;
		}
		return res;
	}

	public String toReplResult(ResultValue res) {
		if (res.ok) {
			if (res.val == null) {
				return "";
			}

			if (res.val instanceof QueryResponse) {
				String valStr = doQueryResp(res);
				String typeName = getType(res);
				if (res.varName.equals("?")) {
					return valStr;
				}

				if (valStr.isEmpty()) {
					if (res.varName.equals("$$")) {
						String s = String.format("null (0 records found)");
						return s;
					} else {
						String s = String.format("created variable %s = null (0 records found)", res.varName);
						return s;
					}
				} else if (res.varName.equals("$$")) {
					String s = String.format("result: (%s) = %s", typeName, valStr);
					return s;
				} else {
					String s = String.format("created variable %s (%s) = %s", res.varName, typeName, valStr);
					return s;
				}
			} else if (res.val instanceof MigrationPlan) {
				MigrationPlan plan = (MigrationPlan) res.val;
				if (plan.diffL.isEmpty()) {
					String s = String.format("plan has 0 steps. The database is up to date.", plan.diffL.size());
					return s;
				} else {
					String s = String.format("plan has %d steps.", plan.diffL.size());
					return s;
					
				}
			}
			DValue dval = (DValue) res.val;
			String valStr;
			if (dval.getType().isBlobShape()) {
				LoggableBlob lb = new LoggableBlob(dval.asBlob().getByteArray());
				valStr = lb.toString();
			} else {
				valStr = dval.getType().isShape(Shape.STRING) ? String.format("'%s'", dval.asString()) : dval.asString();
			}
			//				String s = String.format("OK: %s (%s)", valStr, shapeStr(dval));
			if (res.varName.equals("?")) {
				return valStr;
			}
			String s = String.format("created variable %s (%s) = %s", res.varName, shapeStr(dval), valStr);
			return s;
		} else {
			String s = "Delia execution failed.\n";
			String tmp = String.format("  error: %s", mostRecentException.getMessage());
			tmp = tmp.replace("\n", "\n    ");
			s += tmp;
			s += "\n";
			return s;
		}
	}
	private String getType(ResultValue res) {
		QueryResponse qresp = (QueryResponse) res.val;
		if (qresp.dvalList.isEmpty()) {
			return "??";
		} else {
			DValue dval = qresp.dvalList.get(0);
			return dval.getType().getName();
		}
	}

	private String doQueryResp(ResultValue res) {
		QueryResponse qresp = (QueryResponse) res.val;
		SimpleFormatOutputGenerator gen = new SimpleFormatOutputGenerator();
		gen.includeVPrefix = false;
//		gen.truncateLargeBlob = true;

		DeliaGeneratePhase phase = mostRecentSess.getExecutionContext().generator;

		boolean multiple = qresp.dvalList.size() > 1;
		String resultStr = multiple ? "[" : "";
		for(DValue dval: qresp.dvalList) {
			gen.outputL.clear();
			boolean b = phase.generateValue(gen, dval, "a");
			if (!b) {
				return "sdfsdf";
			}
			String s = StringUtil.convertToSingleString(gen.outputL);
			int pos = s.indexOf('{');
			if (pos >= 0) {
				s = s.substring(pos);
			}
			resultStr += s;
		}

		resultStr += multiple ? "]" : "";
		return resultStr;
	}

	private Object shapeStr(DValue dval) {
		String s = BuiltInTypes.convertDTypeNameToDeliaName(dval.getType().getName());
		return s;
	}

	public DeliaSession getMostRecentSess() {
		return mostRecentSess;
	}
	public boolean inSession() {
		return mostRecentSess != null;
	}

	public Delia getDelia() {
		return delia;
	}

	public MigrationPlan getCurrentMigrationPlan() {
		return currentMigrationPlan;
	}

	public void setCurrentMigrationPlan(MigrationPlan currentMigrationPlan) {
		this.currentMigrationPlan = currentMigrationPlan;
	}

	public String getLoadSrc() {
		return loadSrc;
	}

	public void setLoadSrc(String loadSrc) {
		this.loadSrc = loadSrc;
	}

	public boolean doesFileExist(String path) {
		File f = new File(path);
		return f.exists();
	}

	public String getSessionName() {
		return sessionName;
	}
}