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

		fixupForParentFields(structType, upsertFrag);

		if (! useAliases) {
			removeAllAliases(upsertFrag);
		}

		return upsertFrag;
	}

}