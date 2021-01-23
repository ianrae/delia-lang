//package org.delia.db.postgres;
//
//import java.util.HashMap;
//import java.util.Map;
//
//import org.delia.core.FactoryService;
//import org.delia.db.QueryDetails;
//import org.delia.db.QuerySpec;
//import org.delia.db.sql.fragment.AssocTableReplacer;
//import org.delia.db.sql.fragment.FragmentParserService;
//import org.delia.db.sql.fragment.TableFragment;
//import org.delia.db.sql.fragment.UpsertFragmentParser;
//import org.delia.db.sql.fragment.UpsertStatementFragment;
//import org.delia.db.sql.prepared.SqlStatement;
//import org.delia.type.DRelation;
//import org.delia.type.DStructType;
//import org.delia.type.DValue;
//
////single use!!!
//public class PostgresUpsertFragmentParser extends UpsertFragmentParser {
//
//	public PostgresUpsertFragmentParser(FactoryService factorySvc, FragmentParserService fpSvc, AssocTableReplacer assocTblReplacer) {
//		super(factorySvc, fpSvc, assocTblReplacer);
//	}
//
//	/**
//	 * INSERT INTO customers (name, email) VALUES ('Microsoft','hotline@microsoft.com')
//		ON CONFLICT (name) DO UPDATE 
//	  SET email = EXCLUDED.email || ';' || customers.email;
//	 */
//	@Override
//	public UpsertStatementFragment parseUpsert(QuerySpec spec, QueryDetails details, DValue partialVal, Map<String, String> assocCrudMap, boolean noUpdateFlag) {
//		UpsertStatementFragment upsertFrag = new UpsertStatementFragment();
//		upsertFrag.sqlCmd = "INSERT INTO";
//		upsertFrag.addOnConflictPhrase = true;
//		upsertFrag.noUpdateFlag = noUpdateFlag;
//		this.useAliases = false;
//		Map<String, DRelation> mmMap = new HashMap<>();
//
//		//init tbl
//		DStructType structType = getMainType(spec); 
//		TableFragment tblFrag = createTable(structType, upsertFrag);
//		upsertFrag.tblFrag = tblFrag;
//
//		generateKey(spec, upsertFrag, partialVal);
//		upsertFrag.keyFrag = null;
//		generateSetFieldsUpsert(spec, structType, upsertFrag, partialVal, mmMap);
//		//add params for the UPDATE SET.. part
//		if (! noUpdateFlag) {
//			cloneParams(upsertFrag.statement, 0, upsertFrag.fieldL.size());
//		}
//
//		int nn = upsertFrag.statement.paramL.size();
//		
//		initWhere(spec, structType, upsertFrag);
//		generateAssocUpdateIfNeeded(spec, structType, upsertFrag, mmMap, assocCrudMap);
//		if (upsertFrag.statement.paramL.size() > nn) {
//			upsertFrag.statement.paramL.remove(nn);
//			if (upsertFrag.assocDeleteFrag != null) {
//				upsertFrag.assocDeleteFrag.paramStartIndex--;
//			}
//			if (upsertFrag.assocMergeIntoFrag != null) {
//				upsertFrag.assocMergeIntoFrag.paramStartIndex--;
//			}
//		}
//
//		fixupForParentFields(structType, upsertFrag);
//
//		if (! useAliases) {
//			removeAllAliases(upsertFrag);
//		}
//		return upsertFrag;
//	}
//	
//	protected void cloneParams(SqlStatement statement, int startIndex, int numToAdd) {
//		int n = statement.paramL.size();
//		log.logDebug("cloneParams %d %d", numToAdd, n);
//		for(int i = 0; i < numToAdd; i++) {
//			int k = startIndex + n - (numToAdd - i);
//			DValue previous = statement.paramL.get(k);
//			statement.paramL.add(previous); //add copy
//		}
//	}
//
//}