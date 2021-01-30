package org.delia.hld.cud;

import org.delia.db.SqlStatementGroup;

public interface HLDToSQLConverter {

	SqlStatementGroup generate(HLDInsertStatement hldins);

	SqlStatementGroup generate(HLDUpdateStatement hldupdate);

	SqlStatementGroup generate(HLDUpsertStatement hldupsert);

	SqlStatementGroup generate(HLDDeleteStatement hld);

}