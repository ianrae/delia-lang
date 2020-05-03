package org.delia.db.postgres;

import java.util.HashMap;
import java.util.Map;

import org.delia.core.FactoryService;
import org.delia.db.QueryDetails;
import org.delia.db.QuerySpec;
import org.delia.db.sql.fragment.FragmentParserService;
import org.delia.db.sql.fragment.TableFragment;
import org.delia.db.sql.fragment.UpsertFragmentParser;
import org.delia.db.sql.fragment.UpsertStatementFragment;
import org.delia.db.sql.prepared.SqlStatement;
import org.delia.type.DRelation;
import org.delia.type.DStructType;
import org.delia.type.DValue;

//single use!!!
public class PostgresUpsertFragmentParser extends UpsertFragmentParser {

	public PostgresUpsertFragmentParser(FactoryService factorySvc, FragmentParserService fpSvc) {
		super(factorySvc, fpSvc);
	}

	/**
	 * INSERT INTO customers (name, email) VALUES ('Microsoft','hotline@microsoft.com')
		ON CONFLICT (name) DO UPDATE 
	  SET email = EXCLUDED.email || ';' || customers.email;
	 */
	public UpsertStatementFragment parseUpsert(QuerySpec spec, QueryDetails details, DValue partialVal, Map<String, String> assocCrudMap) {
		UpsertStatementFragment upsertFrag = new UpsertStatementFragment();
		upsertFrag.sqlCmd = "INSERT INTO";
		upsertFrag.addOnConflictPhrase = true;
		this.useAliases = false;
		Map<String, DRelation> mmMap = new HashMap<>();

		//init tbl
		DStructType structType = getMainType(spec); 
		TableFragment tblFrag = createTable(structType, upsertFrag);
		upsertFrag.tblFrag = tblFrag;

		generateKey(spec, upsertFrag, partialVal);
		upsertFrag.keyFrag = null;
		
		generateSetFields(spec, structType, upsertFrag, partialVal, mmMap);
		initWhere(spec, structType, upsertFrag);
		//remove last
		int n = upsertFrag.statement.paramL.size();
		upsertFrag.statement.paramL.remove(n - 1);
		//no min,max,etc in UPDATE
		
		//		sc.o(" ON CONFLICT (leftv,rightv) DO UPDATE SET leftv = ?,rightv=?");


		fixupForParentFields(structType, upsertFrag);

		if (! useAliases) {
			removeAllAliases(upsertFrag);
		}

		//add params for the UPDATE SET.. part
		cloneParams(upsertFrag.statement, 0, upsertFrag.fieldL.size());
		return upsertFrag;
	}
	
	protected void cloneParams(SqlStatement statement, int startIndex, int numToAdd) {
		int n = statement.paramL.size();
		log.logDebug("cloneParams %d %d", numToAdd, n);
		for(int i = 0; i < numToAdd; i++) {
			int k = startIndex + n - (numToAdd - i);
			DValue previous = statement.paramL.get(k);
			statement.paramL.add(previous); //add copy
		}
	}

}