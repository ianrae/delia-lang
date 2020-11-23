package org.delia.repl;

import org.apache.commons.lang3.StringUtils;
import org.delia.api.Delia;
import org.delia.api.MigrationAction;
import org.delia.db.schema.MigrationPlan;
import org.delia.db.schema.SchemaType;
import org.delia.runner.ResultValue;

public class GenerateMigrationPlanCmd extends CmdBase {
	public GenerateMigrationPlanCmd() {
		super("migration generate", "mg");
		expectSpace = false;
	}
	public GenerateMigrationPlanCmd(GenerateMigrationPlanCmd obj) {
		super(obj);
	}
	@Override
	public Cmd isReplCmd(String src) {
		if (isMatch(src)) {
			Cmd cmd = new GenerateMigrationPlanCmd(this);
			cmd.cmd = name;
			return cmd;
		}
		return null;
	}

	@Override
	public ResultValue runCmd(Cmd cmd, ReplRunner runner) {
		String src = runner.getLoadSrc();
		if (StringUtils.isEmpty(src)) {
			log("You must define some types first, before you can generate a plan.");
			return createEmptyRes();
		}
		
		Delia delia = runner.getDelia();
		delia.getOptions().migrationAction = MigrationAction.GENERATE_MIGRATION_PLAN;
		delia.getOptions().enableExecution = false;
		ResultValue res = runner.runDelia(src);
		delia.getOptions().migrationAction = MigrationAction.MIGRATE;
		delia.getOptions().enableExecution = true;

		MigrationPlan migrationPlan = (MigrationPlan) res.val;
		runner.setCurrentMigrationPlan(migrationPlan);
		
		log("generated migration plan: ");
		for(SchemaType ss: migrationPlan.diffL) {
			log("  " + ss.getSummary());
		}
		log("");
		
		return res;
	}

	
}