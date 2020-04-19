package org.delia.repl;

import java.util.ArrayList;
import java.util.List;

import org.delia.api.Delia;
import org.delia.api.MigrationAction;
import org.delia.core.FactoryService;
import org.delia.db.schema.MigrationPlan;
import org.delia.db.schema.SchemaType;
import org.delia.repl.migration.MigrationParser;
import org.delia.repl.migration.MigrationStep;
import org.delia.runner.ResultValue;
import org.delia.util.TextFileReader;

public class RunMigrationPlanCmd extends CmdBase {
			
	public RunMigrationPlanCmd() {
		super("migration run", "mr");
		expectSpace = false;
	}
	@Override
	public Cmd isReplCmd(String src) {
		if (isMatch(src)) {
			Cmd cmd = new RunMigrationPlanCmd();
			cmd.cmd = name;
			if (iParmStart > 0 && src.length() > iParmStart) {
				cmd.arg1 = parseArg1(src);
			}
			return cmd;
		}
		return null;
	}

	@Override
	public ResultValue runCmd(Cmd cmd, ReplRunner runner) {
		MigrationPlan plan = runner.getCurrentMigrationPlan();
		if (cmd.arg1 != null) {
			if (! runner.doesFileExist(cmd.arg1)) {
				log("file-not-found: " + cmd.arg1);
				return createEmptyRes();
			}
			
			plan = buildFromMigrationFile(cmd.arg1);
			if (plan == null) {
				return createEmptyRes();
			}
		}
		
		if (plan == null) {
			log("no migration plan. You need to run 'mg' command first");
			return createEmptyRes();
		} else if (plan.diffL.isEmpty()) {
			log("The plan has 0 steps. Nothing to do. Database is up to date.");
			return createEmptyRes();
		}
		
		String src = runner.getLoadSrc();

		Delia delia = runner.getDelia();
		delia.getOptions().migrationAction = MigrationAction.RUN_MIGRATION_PLAN;
		delia.getOptions().enableExecution = false;
		//TODO repl user needs way to edit plan and resubmit to repl
		ResultValue res = runner.runMPlan(src, plan);
		delia.getOptions().migrationAction = MigrationAction.MIGRATE;
		delia.getOptions().enableExecution = true;
		return res;
	}
	private MigrationPlan buildFromMigrationFile(String path) {
		log(String.format("reading: %s", path));
		TextFileReader reader = new TextFileReader();
		List<String> lines = reader.readFile(path);
		
		MigrationParser parser = new MigrationParser(factorySvc);
		List<MigrationStep> steps = new ArrayList<>();
		boolean b = parser.parse(lines, steps);
		if (! b) {
			log(String.format("migration file has %d syntax errors in it.", parser.getFailCount()));
			return null;
		}
		
		MigrationPlan plan = new MigrationPlan();
		plan.diffL = parser.convertToSchemaType(steps);
		return plan;
	}

}