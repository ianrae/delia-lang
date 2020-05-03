package org.delia.db.sql.fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.delia.core.FactoryService;
import org.delia.db.QueryDetails;
import org.delia.db.QuerySpec;
import org.delia.db.sql.prepared.SqlStatement;
import org.delia.db.sql.prepared.SqlStatementGroup;
import org.delia.dval.DValueExConverter;
import org.delia.runner.FilterEvaluator;
import org.delia.type.DRelation;
import org.delia.type.DStructType;
import org.delia.type.DValue;
import org.delia.type.TypePair;
import org.delia.util.DRuleHelper;
import org.delia.util.DValueHelper;
import org.delia.util.DeliaExceptionHelper;

//single use!!!
public class UpsertFragmentParser extends UpdateFragmentParser {

	public UpsertFragmentParser(FactoryService factorySvc, FragmentParserService fpSvc, AssocTableReplacer assocTblReplacer) {
		super(factorySvc, fpSvc, assocTblReplacer);
	}

	public UpsertStatementFragment parseUpsert(QuerySpec spec, QueryDetails details, DValue partialVal, Map<String, String> assocCrudMap, boolean noUpdateFlag) {
		UpsertStatementFragment upsertFrag = new UpsertStatementFragment();

		Map<String, DRelation> mmMap = new HashMap<>();

		//init tbl
		DStructType structType = getMainType(spec); 
		TableFragment tblFrag = createTable(structType, upsertFrag);
		upsertFrag.tblFrag = tblFrag;

		generateKey(spec, upsertFrag, partialVal);
		generateSetFieldsUpsert(spec, structType, upsertFrag, partialVal, mmMap);
		initWhere(spec, structType, upsertFrag);
		generateAssocUpdateIfNeeded(spec, structType, upsertFrag, mmMap, assocCrudMap);
		//remove last
		int n = upsertFrag.statement.paramL.size();
		upsertFrag.statement.paramL.remove(n - 1);
		//no min,max,etc in UPDATE

		fixupForParentFields(structType, upsertFrag);
		
		return upsertFrag;
	}

	/**
	 * Postgres doesn't like alias in UPDATE statements
	 * @param selectFrag
	 */
	private void removeAllAliasesUpsert(UpsertStatementFragment selectFrag) {
		for(FieldFragment ff: selectFrag.fieldL) {
			ff.alias = null;
		}
		//			public List<SqlFragment> earlyL = new ArrayList<>();
		selectFrag.tblFrag.alias = null;
		//			public JoinFragment joinFrag; //TODO later a list
		for(SqlFragment ff: selectFrag.whereL) {
			if (ff instanceof OpFragment) {
				OpFragment opff = (OpFragment) ff;
				opff.left.alias = null;
				opff.right.alias = null;
			}
		}
	}

	protected void generateKey(QuerySpec spec, UpsertStatementFragment updateFrag, DValue partialVal) {
		TypePair keyPair = DValueHelper.findPrimaryKeyFieldPair(partialVal.getType());
		DValue inner = DValueHelper.findPrimaryKeyValue(partialVal);
		if (inner == null) {
			FilterEvaluator evaluator = spec.evaluator;
			DValueExConverter dvalConverter = new DValueExConverter(factorySvc, registry);
			String keyField = evaluator.getRawValue(); //we assume primary key. eg Customer[55]
			inner = dvalConverter.buildFromObject(keyField, keyPair.type);
		}
		String sql = String.format(" KEY(%s)", keyPair.name);
		updateFrag.keyFrag =  new RawFragment(sql);
		updateFrag.keyFieldName = keyPair.name;
		updateFrag.statement.paramL.add(inner);
	}
	public DValue getPrimaryKeyValue(QuerySpec spec, DValue partialVal) {
		TypePair keyPair = DValueHelper.findPrimaryKeyFieldPair(partialVal.getType());
		DValue inner = DValueHelper.findPrimaryKeyValue(partialVal);
		if (inner == null) {
			FilterEvaluator evaluator = spec.evaluator;
			DValueExConverter dvalConverter = new DValueExConverter(factorySvc, registry);
			String keyField = evaluator.getRawValue(); //we assume primary key. eg Customer[55]
			inner = dvalConverter.buildFromObject(keyField, keyPair.type);
		}
		return inner; 
	}

	protected void generateSetFieldsUpsert(QuerySpec spec, DStructType structType, UpsertStatementFragment updateFrag,
			DValue partialVal, Map<String, DRelation> mmMap) {
		//we assume partialVal same type as structType!! (or maybe a base class)

		int index = updateFrag.fieldL.size(); //setValuesL is parallel array to fieldL
		if (index != 0) {

			log.log("WHY FILLING INNNNNNNNN");
			for(int i = 0; i < index; i++) {
				updateFrag.setValuesL.add("????");
			}
			DeliaExceptionHelper.throwError("unexpeced-fields-in-update", "should not occur");
		}

		for(String fieldName: partialVal.asMap().keySet()) {
			TypePair pair = DValueHelper.findField(structType, fieldName);

			if (pair.type.isStructShape()) {
				if (! shouldGenerateFKConstraint(pair, structType)) {
					continue;
				}
				if (DRuleHelper.isManyToManyRelation(pair, structType)) {
					DValue inner = partialVal.asStruct().getField(pair.name);
					if (inner == null) {
						mmMap.put(pair.name, null);
					} else {
						mmMap.put(pair.name, inner.asRelation());
					}
					continue;
				}
			}

			DValue inner = partialVal.asMap().get(fieldName);
			if (inner == null) {
				continue;
			}

			DValue dvalToUse = inner;
			if (inner.getType().isRelationShape()) {
				DRelation drel = inner.asRelation();
				dvalToUse  = drel.getForeignKey(); //TODO; handle composite keys later
			}

			FieldFragment ff = FragmentHelper.buildFieldFrag(structType, updateFrag, pair);
			updateFrag.setValuesL.add("?");
			updateFrag.fieldL.add(ff);
			updateFrag.statement.paramL.add(dvalToUse);

			index++;
		}
	}
//	private void generateAssocUpdateIfNeeded(QuerySpec spec, DStructType structType,
//			UpsertStatementFragment selectFrag, Map<String, DRelation> mmMap, Map<String, String> assocCrudMap) {
//		if (mmMap.isEmpty()) {
//			return;
//		}
//
//		for(String fieldName: mmMap.keySet()) {
//			RelationManyRule ruleMany = DRuleHelper.findManyRule(structType, fieldName);
//			if (ruleMany != null) {
//				RelationInfo info = ruleMany.relInfo;
//				String assocAction = assocCrudMap.get(fieldName);
//				if (assocAction == null) {
//					selectFrag.assocUpdateFrag = new UpsertStatementFragment();
//					genAssocField(selectFrag, selectFrag.assocUpdateFrag, structType, mmMap, fieldName, info, selectFrag.whereL, 
//							selectFrag.tblFrag.alias, selectFrag.statement);
//				} else {
//					//no assoc crud
//				}
//			}
//		}
//	}
	
//	private void chkIfCrudActionAllowed(String action, UpsertStatementFragment updateFrag, String fieldName, List<SqlFragment> existingWhereL, RelationInfo info) {
//		//only for update by primary id. TODO: later support more
////		List<OpFragment> oplist = null;
//		if (existingWhereL.isEmpty()) {
//			log.logDebug("m-to-n:scenario1");
//			DeliaExceptionHelper.throwError("assoc-crud-not-allowed", "update %s field %s action '%s' not allowed", updateFrag.tblFrag.name, fieldName, action);
//			return;
//		} else if (WhereListHelper.isOnlyPrimaryKeyQuery(existingWhereL, info.farType)) {
////			oplist = WhereListHelper.findPrimaryKeyQuery(existingWhereL, info.farType);
//		} else {
//			DeliaExceptionHelper.throwError("assoc-crud-not-allowed", "update %s field %s action '%s' not allowed", updateFrag.tblFrag.name, fieldName, action);
//		}
//	}

	
//	private void genAssocField(UpsertStatementFragment updateFrag, UpsertStatementFragment assocUpdateFrag, DStructType structType, Map<String, DRelation> mmMap, String fieldName, 
//			RelationInfo info, List<SqlFragment> existingWhereL, String mainUpdateAlias, SqlStatement statement) {
//		//update assoctabl set leftv=x where rightv=y
//		TableInfo tblinfo = TableInfoHelper.findTableInfoAssoc(this.tblinfoL, info.nearType, info.farType);
//		assocUpdateFrag.tblFrag = this.createAssocTable(assocUpdateFrag, tblinfo.assocTblName);
//
//		//struct is Address AddressCustomerAssoc
//		String field1;
//		String field2;
//		if (tblinfo.tbl1.equalsIgnoreCase(structType.getName())) {
//			field1 = "rightv";
//			field2 = "leftv";
//		} else {
//			field1 = "leftv";
//			field2 = "rightv";
//		}
//
//		//3 scenarios here:
//		// 1. updating all records in assoc table
//		// 2. updating where filter by primaykey only
//		// 3. updating where filter includes other fields (eg Customer.firstName) which may include primaryKey fields.
//		if (existingWhereL.isEmpty()) {
//			log.logDebug("m-to-n:scenario1");
//			buildUpdateAll(updateFrag, assocUpdateFrag, structType, mmMap, fieldName, info, field1, field2, mainUpdateAlias, statement);
//			return;
//		} else if (WhereListHelper.isOnlyPrimaryKeyQuery(existingWhereL, info.farType)) {
//			List<OpFragment> oplist = WhereListHelper.findPrimaryKeyQuery(existingWhereL, info.farType);
//			log.logDebug("m-to-n:scenario2");
//			buildUpdateByIdOnly(updateFrag, assocUpdateFrag, structType, mmMap, fieldName, info, field1, field2, oplist, statement);
//		} else {
//			log.logDebug("m-to-n:scenario3");
//			buildUpdateOther(updateFrag, assocUpdateFrag, structType, mmMap, fieldName, info, field1, field2, existingWhereL, mainUpdateAlias, statement);
//		}
//	}


//	protected void buildUpdateAll(UpsertStatementFragment updateFrag, UpsertStatementFragment assocUpdateFrag, DStructType structType, Map<String, DRelation> mmMap, 
//				String fieldName, RelationInfo info, String assocFieldName, String assocField2, String mainUpdateAlias, SqlStatement statement) {
//		if (assocTblReplacer != null) {
//			log.logDebug("use assocTblReplacer");
//			assocTblReplacer.buildUpdateAll(updateFrag, assocUpdateFrag, structType, mmMap, fieldName, info, assocFieldName, assocField2, mainUpdateAlias, statement);
//		} else {
//			buildAssocTblUpdate(assocUpdateFrag, structType, mmMap, fieldName, info, assocFieldName, statement);
//		}		
//	}
//	private void buildUpdateByIdOnly(UpsertStatementFragment updateFrag, UpsertStatementFragment assocUpdateFrag, DStructType structType,
//			Map<String, DRelation> mmMap, String fieldName, RelationInfo info, String assocFieldName,
//			String assocField2, List<OpFragment> oplist, SqlStatement statement) {
//		
//		if (assocTblReplacer != null) {
//			log.logDebug("use assocTblReplacer");
//			assocTblReplacer.buildUpdateByIdOnly(updateFrag, assocUpdateFrag, structType, mmMap, fieldName, info, assocFieldName, assocField2, statement);
//		} else {
//			int startingNumParams = statement.paramL.size();
//			buildAssocTblUpdate(assocUpdateFrag, structType, mmMap, fieldName, info, assocFieldName, statement);
//
//			List<OpFragment> clonedL = WhereListHelper.changeIdToAssocFieldName(false, oplist, info.farType, assocUpdateFrag.tblFrag.alias, assocField2);
//			assocUpdateFrag.whereL.addAll(clonedL);
//			
//			int extra = statement.paramL.size() - startingNumParams;
//			cloneParams(statement, clonedL, extra);
//		}
//	}
//	private void buildUpdateOther(UpsertStatementFragment updateFrag, UpsertStatementFragment assocUpdateFrag, DStructType structType,
//			Map<String, DRelation> mmMap, String fieldName, RelationInfo info, String assocFieldName, String assocField2,
//			List<SqlFragment> existingWhereL, String mainUpdateAlias, SqlStatement statement) {
//
//		updateFrag.doUpdateLast = true; //in case we're updating any of the fields in the query
//		if (assocTblReplacer != null) {
//			log.logDebug("use assocTblReplacer");
//			assocTblReplacer.buildUpdateOther(updateFrag, assocUpdateFrag, structType, mmMap, fieldName, info, assocFieldName, assocField2, existingWhereL, mainUpdateAlias, statement);
//		} else {
//			int startingNumParams = statement.paramL.size();
//			buildAssocTblUpdate(assocUpdateFrag, structType, mmMap, fieldName, info, assocFieldName, statement);
//	
//			//update CAAssoc set rightv=100 where (select id from customer where lastname='smith')
//			//Create a sub-select whose where list is a copy of the main update statement's where list.
//			TypePair keyPair = DValueHelper.findPrimaryKeyFieldPair(info.nearType);
//			StrCreator sc = new StrCreator();
//			sc.o(" %s.%s IN", assocUpdateFrag.tblFrag.alias, assocField2);
//			sc.o(" (SELECT %s FROM %s as %s WHERE", keyPair.name, info.nearType.getName(), mainUpdateAlias);
//	
//			List<OpFragment> clonedL = WhereListHelper.cloneWhereList(existingWhereL);
//			for(OpFragment opff: clonedL) {
//				sc.o(opff.render());
//			}
//			sc.o(")");
//			RawFragment rawFrag = new RawFragment(sc.str);
//	
//			assocUpdateFrag.whereL.add(rawFrag);
//			int extra = statement.paramL.size() - startingNumParams;
//			cloneParams(statement, clonedL, extra);
//		}
//	}
//	
//	private void cloneParams(SqlStatement statement, List<OpFragment> clonedL, int extra) {
//		//clone params 
//		int numToAdd = 0;
//		for(SqlFragment ff: clonedL) {
//			numToAdd += ff.getNumSqlParams();
//		}
//		
//		int n = statement.paramL.size();
//		log.logDebug("cloneParams %d %d", numToAdd, n);
//		for(int i = 0; i < numToAdd; i++) {
//			int k = n - (numToAdd - i) - extra;
//			DValue previous = statement.paramL.get(k);
//			statement.paramL.add(previous); //add copy
//		}
//	}
//	protected void buildAssocTblUpdate(UpsertStatementFragment assocUpdateFrag, DStructType structType, Map<String, DRelation> mmMap, String fieldName, RelationInfo info, String assocFieldName, SqlStatement statement) {
//		DRelation drel = mmMap.get(fieldName); //100
//		DValue dvalToUse  = drel.getForeignKey(); //TODO; handle composite keys later
//
//		RelationInfo farInfo = DRuleHelper.findOtherSideMany(info.farType, structType);
//		TypePair pair2 = DValueHelper.findField(farInfo.nearType, farInfo.fieldName);
//		TypePair rightPair = new TypePair(assocFieldName, pair2.type);
//		FieldFragment ff = FragmentHelper.buildFieldFragForTable(assocUpdateFrag.tblFrag, assocUpdateFrag, rightPair);
//		statement.paramL.add(dvalToUse);
//		assocUpdateFrag.setValuesL.add("?");
//		assocUpdateFrag.fieldL.add(ff);
//	}


	public String renderUpsert(UpsertStatementFragment selectFrag) {
		if(selectFrag.setValuesL.isEmpty()) {
			selectFrag.statement.sql = ""; //nothing to do
			return selectFrag.statement.sql;
		}

		selectFrag.statement.sql = selectFrag.render();
		return selectFrag.statement.sql;
	}
	
	public SqlStatementGroup renderUpsertGroup(UpsertStatementFragment updateFrag) {
		SqlStatementGroup stgroup = new SqlStatementGroup();
		if(updateFrag.setValuesL.isEmpty()) {
			updateFrag.statement.sql = ""; //nothing to do
			return stgroup;
		}

		SqlStatement mainStatement = updateFrag.statement;
		mainStatement.sql = updateFrag.render();
		List<DValue> save = new ArrayList<>(mainStatement.paramL); //copy
		
		List<StatementFragmentBase> allL = new ArrayList<>();
		mainStatement.paramL.clear();
		stgroup.add(updateFrag.statement);
		initMainParams(mainStatement, save, allL, updateFrag.assocUpdateFrag);
		initMainParams(mainStatement, save, allL, updateFrag.assocDeleteFrag);
		initMainParams(mainStatement, save, allL, updateFrag.assocMergeInfoFrag);
		
		if (mainStatement.paramL.isEmpty()) {
			mainStatement.paramL.addAll(save);
			return stgroup; //no inner frags
		}
		
		addIfNotNull(stgroup, updateFrag.assocUpdateFrag, save, allL);
		addIfNotNull(stgroup, updateFrag.assocDeleteFrag, save, allL);
		addIfNotNull(stgroup, updateFrag.assocMergeInfoFrag, save, allL);
		
		if (updateFrag.doUpdateLast) {
			SqlStatement stat = stgroup.statementL.remove(0); //move first to last
//			List<DValue> firstParams = stat.paramL;
			stgroup.statementL.add(stat);
		}
		
		return stgroup;
	}


	public void useAliases(boolean b) {
		this.useAliases = b;
	}
}