package org.delia.runner;

import java.util.List;

import org.delia.assoc.DatIdMap;
import org.delia.compiler.ast.Exp;
import org.delia.compiler.generate.DeliaGeneratePhase;
import org.delia.db.hld.HLDManager;
import org.delia.log.Log;
import org.delia.sprig.SprigService;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;

/**
 * We need an empty runner class for Migrate's use of InsertStatementRunner
 * 
 * @author irae
 *
 */
public class RunnerForMigrateInsert implements Runner {

	@Override
	public List<DValue> lookupVar(String varName) {
		return null;
	}

	@Override
	public String evalVarAsString(String varName, String typeName) {
		return null;
	}

	@Override
	public Log getLog() {
		return null;
	}

	@Override
	public DeliaGeneratePhase createGenerator() {
		return null;
	}

	@Override
	public InternalCompileState getCompileState() {
		return null;
	}

	@Override
	public ExecutionState getExecutionState() {
		return null;
	}

	@Override
	public boolean init(ExecutionState execState) {
		return false;
	}

	@Override
	public ResultValue executeOneStatement(Exp exp) {
		return null;
	}

	@Override
	public ResultValue executeProgram(List<Exp> expL) {
		return null;
	}

	@Override
	public boolean exists(String varName) {
		return false;
	}

	@Override
	public ResultValue getVar(String varName) {
		return null;
	}

	@Override
	public DTypeRegistry getRegistry() {
		return null;
	}

	@Override
	public TypeRunner createTypeRunner() {
		return null;
	}

	@Override
	public SprigService getSprigSvc() {
		return null;
	}

	@Override
	public void setInsertPrebuiltValueIterator(DValueIterator insertPrebuiltValueIterator) {
	}

	@Override
	public FetchRunner getPrebuiltFetchRunnerToUse() {
		return null;
	}

	@Override
	public void setPrebuiltFetchRunnerToUse(FetchRunner prebuiltFetchRunnerToUse) {
	}

	@Override
	public void setDatIdMap(DatIdMap datIdMap) {
	}

	@Override
	public void setHLDManager(HLDManager mgr) {
	}

}
