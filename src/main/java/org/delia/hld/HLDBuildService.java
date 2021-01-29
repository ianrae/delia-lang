package org.delia.hld;

import org.delia.compiler.ast.InsertStatementExp;
import org.delia.compiler.ast.QueryExp;
import org.delia.compiler.ast.UpdateStatementExp;
import org.delia.compiler.ast.UpsertStatementExp;
import org.delia.db.SqlStatementGroup;
import org.delia.hld.cud.HLDDeleteStatement;
import org.delia.hld.cud.HLDInsertStatement;
import org.delia.hld.cud.HLDUpdateStatement;
import org.delia.hld.cud.HLDUpsertStatement;
import org.delia.runner.DValueIterator;
import org.delia.runner.VarEvaluator;

public interface HLDBuildService {

	HLDQueryStatement fullBuildQuery(QueryExp queryExp, VarEvaluator varEvaluator);

	boolean canBuildQuery(QueryExp queryExp, VarEvaluator varEvaluator);

	HLDDeleteStatement fullBuildDelete(QueryExp queryExp);

	HLDInsertStatement fullBuildInsert(InsertStatementExp insertExp, VarEvaluator varEvaluator,
			DValueIterator insertPrebuiltValueIterator);

	HLDUpdateStatement fullBuildUpdate(UpdateStatementExp updateExp, VarEvaluator varEvaluator,
			DValueIterator insertPrebuiltValueIterator);

	HLDUpsertStatement fullBuildUpsert(UpsertStatementExp upsertExp, VarEvaluator varEvaluator,
			DValueIterator insertPrebuiltValueIterator);

	// -- sql generation --
	String generateRawSql(HLDQueryStatement hld);

	SqlStatementGroup generateSql(HLDQueryStatement hld);

	SqlStatementGroup generateSql(HLDDeleteStatement hlddel);

	SqlStatementGroup generateSql(HLDInsertStatement hldins);

	SqlStatementGroup generateSql(HLDUpdateStatement hldupdate);

	SqlStatementGroup generateSql(HLDUpsertStatement hldupsert);

}