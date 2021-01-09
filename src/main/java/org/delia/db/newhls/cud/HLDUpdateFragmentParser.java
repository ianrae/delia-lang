package org.delia.db.newhls.cud;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.delia.assoc.DatIdMap;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.db.QueryDetails;
import org.delia.db.QuerySpec;
import org.delia.db.TableExistenceService;
import org.delia.db.hls.AliasInfo;
import org.delia.db.newhls.HLDAliasManager;
import org.delia.db.sql.QueryTypeDetector;
import org.delia.db.sql.StrCreator;
import org.delia.db.sql.fragment.AliasedFragment;
import org.delia.db.sql.fragment.AssocTableReplacer;
import org.delia.db.sql.fragment.DeleteStatementFragment;
import org.delia.db.sql.fragment.FieldFragment;
import org.delia.db.sql.fragment.FragmentHelper;
import org.delia.db.sql.fragment.FragmentParserService;
import org.delia.db.sql.fragment.InsertStatementFragment;
import org.delia.db.sql.fragment.OpFragment;
import org.delia.db.sql.fragment.RawFragment;
import org.delia.db.sql.fragment.SelectFragmentParser;
import org.delia.db.sql.fragment.SqlFragment;
import org.delia.db.sql.fragment.StatementFragmentBase;
import org.delia.db.sql.fragment.TableFragment;
import org.delia.db.sql.fragment.UpdateStatementFragment;
import org.delia.db.sql.fragment.WhereFragmentGenerator;
import org.delia.db.sql.fragment.WhereListHelper;
import org.delia.db.sql.prepared.SqlStatement;
import org.delia.db.sql.prepared.SqlStatementGroup;
import org.delia.db.sql.prepared.TableInfoHelper;
import org.delia.db.sql.table.TableInfo;
import org.delia.relation.RelationInfo;
import org.delia.rule.rules.RelationManyRule;
import org.delia.rule.rules.RelationOneRule;
import org.delia.type.DRelation;
import org.delia.type.DStructType;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.type.TypePair;
import org.delia.util.DRuleHelper;
import org.delia.util.DValueHelper;
import org.delia.util.DeliaExceptionHelper;

//single use!!!
public class HLDUpdateFragmentParser extends ServiceBase { //extends SelectFragmentParser {

	protected boolean useAliases = true;
	protected AssocTableReplacer assocTblReplacer;
	private DatIdMap datIdMap;
	private HLDWhereGen hldWhereGen;
	private HLDAliasManager aliasMgr;
	private DTypeRegistry registry;

	public HLDUpdateFragmentParser(FactoryService factorySvc, DatIdMap datIdMap, DTypeRegistry registry, 
			HLDWhereGen hdlWhereGen, HLDAliasManager aliasMgr, AssocTableReplacer assocTblReplacer) {
		super(factorySvc);
		this.assocTblReplacer = assocTblReplacer;
		this.datIdMap = datIdMap;
		this.registry = registry;
		this.hldWhereGen = hdlWhereGen;
		this.aliasMgr = aliasMgr;
	}

	public UpdateStatementFragment parseUpdate(QuerySpec spec, QueryDetails details, DValue partialVal, Map<String, String> assocCrudMap) {
		UpdateStatementFragment selectFrag = new UpdateStatementFragment();

		Map<String, DRelation> mmMap = new HashMap<>();

		//init tbl
		DStructType structType = getMainType(spec); 
		TableFragment tblFrag = createTable(structType, selectFrag);
		selectFrag.tblFrag = tblFrag;

		generateSetFields(spec, structType, selectFrag, partialVal, mmMap);
		initWhere(spec, structType, selectFrag);
		generateAssocUpdateIfNeeded(spec, structType, selectFrag, mmMap, assocCrudMap);

		//no min,max,etc in UPDATE

		generateUpdateFns(spec, structType, selectFrag);

		fixupForParentFields(structType, selectFrag);
		//			if (needJoin(spec, structType, selectFrag, details)) {
		//				//used saved join if we have one
		//				if (savedJoinedFrag == null) {
		//					addJoins(spec, structType, selectFrag, details);
		//				} else {
		//					selectFrag.joinFrag = savedJoinedFrag;
		//				}
		//			}

		if (! useAliases) {
			removeAllAliases(selectFrag);
			if (selectFrag.assocUpdateFrag != null) {
				removeAllAliases(selectFrag.assocUpdateFrag);
			}
		}

		return selectFrag;
	}

	/**
	 * Postgres doesn't like alias in UPDATE statements
	 * @param selectFrag
	 */
	protected void removeAllAliases(UpdateStatementFragment selectFrag) {
		for(FieldFragment ff: selectFrag.fieldL) {
			ff.alias = null;
		}
		selectFrag.tblFrag.alias = null;
		for(SqlFragment ff: selectFrag.whereL) {
			if (ff instanceof OpFragment) {
				OpFragment opff = (OpFragment) ff;
				opff.left.alias = null;
				opff.right.alias = null;
			}
		}
	}

	private void generateSetFields(QuerySpec spec, DStructType structType, UpdateStatementFragment updateFrag,
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
				dvalToUse  = drel.getForeignKey(); 
			}

			FieldFragment ff = FragmentHelper.buildFieldFrag(structType, updateFrag, pair);
			updateFrag.setValuesL.add("?");
			updateFrag.fieldL.add(ff);
			updateFrag.statement.paramL.add(dvalToUse);

			index++;
		}
	}
	protected boolean shouldGenerateFKConstraint(TypePair pair, DStructType dtype) {
		//key goes in child only
		RelationInfo info = DRuleHelper.findMatchingRuleInfo(dtype, pair);
		if (info != null && !info.isParent) {
			return true;
		}
		return false;
	}
	protected void generateAssocUpdateIfNeeded(QuerySpec spec, DStructType structType,
			UpdateStatementFragment selectFrag, Map<String, DRelation> mmMap, Map<String, String> assocCrudMap) {
		if (mmMap.isEmpty()) {
			return;
		}

		for(String fieldName: mmMap.keySet()) {
			RelationManyRule ruleMany = DRuleHelper.findManyRule(structType, fieldName);
			if (ruleMany != null) {
				RelationInfo info = ruleMany.relInfo;
				String assocAction = assocCrudMap == null ? null: assocCrudMap.get(fieldName);
				if (assocAction == null) {
					selectFrag.assocUpdateFrag = new UpdateStatementFragment();
					genAssocField(selectFrag, selectFrag.assocUpdateFrag, structType, mmMap, fieldName, info, selectFrag.whereL, 
							selectFrag.tblFrag.alias, selectFrag.statement);
				} else {
					switch(assocAction) {
					case "insert":
						assocCrudInsert(selectFrag, structType, mmMap, fieldName, info, selectFrag.whereL, selectFrag.tblFrag.alias, selectFrag.statement);
						break;
					case "update":
						assocCrudUpdate(selectFrag, structType, mmMap, fieldName, info, selectFrag.whereL, selectFrag.tblFrag.alias, selectFrag.statement);
						break;
					case "delete":
						assocCrudDelete(selectFrag, structType, mmMap, fieldName, info, selectFrag.whereL, selectFrag.tblFrag.alias, selectFrag.statement);
						break;
					default:
						break;
					}
				}
			}
		}
	}
	
	private void assocCrudInsert(UpdateStatementFragment updateFrag, DStructType structType, Map<String, DRelation> mmMap, String fieldName, 
			RelationInfo info, List<SqlFragment> existingWhereL, String mainUpdateAlias, SqlStatement statement) {

		String action = "insert";
		chkIfCrudActionAllowed(action, updateFrag, fieldName, existingWhereL, info);
		
		DRelation drel = mmMap.get(fieldName);
		if (drel == null) {
			DeliaExceptionHelper.throwError("assoc-crud-insert-null-not-allowed", "update %s field %s action '%s' not allowed with null value", updateFrag.tblFrag.name, fieldName, action);
		}
		TableInfo tblinfo = TableInfoHelper.findTableInfoAssoc(this.tblinfoL, info.nearType, info.farType);
		TableFragment tblFrag = null;
		DValue mainDVal = statement.paramL.get(statement.paramL.size() - 1);
		for(DValue inner: drel.getMultipleKeys()) {
			InsertStatementFragment insertFrag = new InsertStatementFragment();
			if (tblFrag == null) {
				tblFrag = this.createAssocTable(insertFrag, tblinfo.assocTblName);
			} else {
				tblFrag = new TableFragment(tblFrag);
			}
			insertFrag.tblFrag = tblFrag;
			
			boolean reversed = tblinfo.tbl2.equalsIgnoreCase(structType.getName());

			updateFrag.assocCrudInsertL.add(insertFrag);
			assocTblReplacer.assocCrudInsert(updateFrag, insertFrag, structType, mainDVal, inner, info, mainUpdateAlias, statement, reversed);
		}
	}
	private void chkIfCrudActionAllowed(String action, UpdateStatementFragment updateFrag, String fieldName, List<SqlFragment> existingWhereL, RelationInfo info) {
		//only for update by primary id. FUTURE: later support more
		if (existingWhereL.isEmpty()) {
			log.logDebug("m-to-n:scenario1");
			DeliaExceptionHelper.throwError("assoc-crud-not-allowed", "update %s field %s action '%s' not allowed", updateFrag.tblFrag.name, fieldName, action);
			return;
		} else if (WhereListHelper.isOnlyPrimaryKeyQuery(existingWhereL, info.farType)) {
		} else {
			DeliaExceptionHelper.throwError("assoc-crud-not-allowed", "update %s field %s action '%s' not allowed", updateFrag.tblFrag.name, fieldName, action);
		}
	}

	private void assocCrudUpdate(UpdateStatementFragment updateFrag, DStructType structType, Map<String, DRelation> mmMap, String fieldName, 
			RelationInfo info, List<SqlFragment> existingWhereL, String mainUpdateAlias, SqlStatement statement) {

		String action = "update";
		chkIfCrudActionAllowed(action, updateFrag, fieldName, existingWhereL, info);
		
		DRelation drel = mmMap.get(fieldName);
		if (drel == null) {
			DeliaExceptionHelper.throwError("assoc-crud-update-null-not-allowed", "update %s field %s action '%s' not allowed with null value", updateFrag.tblFrag.name, fieldName, action);
		} else if (drel.getMultipleKeys().size() % 2 != 0) {
			DeliaExceptionHelper.throwError("assoc-crud-update-pairs-needed", "update %s field %s: udpate action requires pairs of values", updateFrag.tblFrag.name, fieldName);
		}
		
		TableInfo tblinfo = TableInfoHelper.findTableInfoAssoc(this.tblinfoL, info.nearType, info.farType);
		TableFragment tblFrag = null;
		DValue mainDVal = statement.paramL.get(statement.paramL.size() - 1);
		for(int i = 0; i < drel.getMultipleKeys().size(); i+= 2) {
			DValue oldVal = drel.getMultipleKeys().get(i);
			DValue newVal = drel.getMultipleKeys().get(i+1);
			
			UpdateStatementFragment innerUpdateFrag = new UpdateStatementFragment();
			if (tblFrag == null) {
				tblFrag = this.createAssocTable(innerUpdateFrag, tblinfo.assocTblName);
			} else {
				tblFrag = new TableFragment(tblFrag);
			}
			innerUpdateFrag.tblFrag = tblFrag;
			
			boolean reversed = tblinfo.tbl2.equalsIgnoreCase(structType.getName());

			updateFrag.assocCrudUpdateL.add(innerUpdateFrag);
			assocTblReplacer.assocCrudUpdate(updateFrag, innerUpdateFrag, structType, mainDVal, oldVal, newVal, info, mainUpdateAlias, statement, reversed);
		}
	}
	private void assocCrudDelete(UpdateStatementFragment updateFrag, DStructType structType, Map<String, DRelation> mmMap, String fieldName, 
			RelationInfo info, List<SqlFragment> existingWhereL, String mainUpdateAlias, SqlStatement statement) {

		String action = "delete";
		chkIfCrudActionAllowed(action, updateFrag, fieldName, existingWhereL, info);
		
		DRelation drel = mmMap.get(fieldName);
		if (drel == null) {
			DeliaExceptionHelper.throwError("assoc-crud-delete-null-not-allowed", "update %s field %s action '%s' not allowed with null value", updateFrag.tblFrag.name, fieldName, action);
		}
		TableInfo tblinfo = TableInfoHelper.findTableInfoAssoc(this.tblinfoL, info.nearType, info.farType);
		TableFragment tblFrag = null;
		DValue mainDVal = statement.paramL.get(statement.paramL.size() - 1);
		for(DValue inner: drel.getMultipleKeys()) {
			DeleteStatementFragment deleteFrage = new DeleteStatementFragment();
			if (tblFrag == null) {
				tblFrag = this.createAssocTable(deleteFrage, tblinfo.assocTblName);
			} else {
				tblFrag = new TableFragment(tblFrag);
			}
			deleteFrage.tblFrag = tblFrag;
			
			boolean reversed = tblinfo.tbl2.equalsIgnoreCase(structType.getName());

			updateFrag.assocCrudDeleteL.add(deleteFrage);
			assocTblReplacer.assocCrudDelete(updateFrag, deleteFrage, structType, mainDVal, inner, info, mainUpdateAlias, statement, reversed);
		}
	}
	
	private void genAssocField(UpdateStatementFragment updateFrag, UpdateStatementFragment assocUpdateFrag, DStructType structType, Map<String, DRelation> mmMap, String fieldName, 
			RelationInfo info, List<SqlFragment> existingWhereL, String mainUpdateAlias, SqlStatement statement) {
		//update assoctabl set leftv=x where rightv=y
		TableInfo tblinfo = TableInfoHelper.findTableInfoAssoc(this.tblinfoL, info.nearType, info.farType);
		assocUpdateFrag.tblFrag = this.createAssocTable(assocUpdateFrag, tblinfo.assocTblName);

		//struct is Address AddressCustomerAssoc
		String field1;
		String field2;
//		if (tblinfo.tbl1.equalsIgnoreCase(structType.getName())) {
		if (tblinfo.tbl1.equalsIgnoreCase(structType.getName())) {
			field1 = "rightv";
			field2 = "leftv";
		} else {
			field1 = "leftv";
			field2 = "rightv";
		}

		//3 scenarios here:
		// 1. updating all records in assoc table
		// 2. updating where filter by primaykey only
		// 3. updating where filter includes other fields (eg Customer.firstName) which may include primaryKey fields.
		if (existingWhereL.isEmpty()) {
			log.logDebug("m-to-n:scenario1");
			buildUpdateAll(updateFrag, assocUpdateFrag, structType, mmMap, fieldName, info, field1, field2, mainUpdateAlias, statement);
			return;
		} else if (WhereListHelper.isOnlyPrimaryKeyQuery(existingWhereL, info.farType)) {
			List<OpFragment> oplist = WhereListHelper.findPrimaryKeyQuery(existingWhereL, info.farType);
			log.logDebug("m-to-n:scenario2");
			buildUpdateByIdOnly(updateFrag, assocUpdateFrag, structType, mmMap, fieldName, info, field1, field2, oplist, statement);
		} else {
			log.logDebug("m-to-n:scenario3");
			buildUpdateOther(updateFrag, assocUpdateFrag, structType, mmMap, fieldName, info, field1, field2, existingWhereL, mainUpdateAlias, statement);
		}
	}


	protected void buildUpdateAll(UpdateStatementFragment updateFrag, UpdateStatementFragment assocUpdateFrag, DStructType structType, Map<String, DRelation> mmMap, 
				String fieldName, RelationInfo info, String assocFieldName, String assocField2, String mainUpdateAlias, SqlStatement statement) {
		if (assocTblReplacer != null) {
			log.logDebug("use assocTblReplacer");
			assocTblReplacer.buildUpdateAll(updateFrag, assocUpdateFrag, structType, mmMap, fieldName, info, assocFieldName, assocField2, mainUpdateAlias, statement);
		} else {
			buildAssocTblUpdate(assocUpdateFrag, structType, mmMap, fieldName, info, assocFieldName, statement);
		}		
	}
	protected void buildUpdateByIdOnly(UpdateStatementFragment updateFrag, UpdateStatementFragment assocUpdateFrag, DStructType structType,
			Map<String, DRelation> mmMap, String fieldName, RelationInfo info, String assocFieldName,
			String assocField2, List<OpFragment> oplist, SqlStatement statement) {
		
		if (assocTblReplacer != null) {
			log.logDebug("use assocTblReplacer");
			assocTblReplacer.buildUpdateByIdOnly(updateFrag, assocUpdateFrag, structType, mmMap, fieldName, info, assocFieldName, assocField2, statement);
		} else {
			int startingNumParams = statement.paramL.size();
			buildAssocTblUpdate(assocUpdateFrag, structType, mmMap, fieldName, info, assocFieldName, statement);

			List<OpFragment> clonedL = WhereListHelper.changeIdToAssocFieldName(false, oplist, info.farType, assocUpdateFrag.tblFrag.alias, assocField2);
			assocUpdateFrag.whereL.addAll(clonedL);
			
			int extra = statement.paramL.size() - startingNumParams;
			cloneParams(statement, clonedL, extra);
		}
	}
	protected void buildUpdateOther(UpdateStatementFragment updateFrag, UpdateStatementFragment assocUpdateFrag, DStructType structType,
			Map<String, DRelation> mmMap, String fieldName, RelationInfo info, String assocFieldName, String assocField2,
			List<SqlFragment> existingWhereL, String mainUpdateAlias, SqlStatement statement) {

		updateFrag.doUpdateLast = true; //in case we're updating any of the fields in the query
		if (assocTblReplacer != null) {
			log.logDebug("use assocTblReplacer");
			assocTblReplacer.buildUpdateOther(updateFrag, assocUpdateFrag, structType, mmMap, fieldName, info, assocFieldName, assocField2, existingWhereL, mainUpdateAlias, statement);
		} else {
			int startingNumParams = statement.paramL.size();
			buildAssocTblUpdate(assocUpdateFrag, structType, mmMap, fieldName, info, assocFieldName, statement);
	
			//update CAAssoc set rightv=100 where (select id from customer where lastname='smith')
			//Create a sub-select whose where list is a copy of the main update statement's where list.
			TypePair keyPair = DValueHelper.findPrimaryKeyFieldPair(info.nearType);
			StrCreator sc = new StrCreator();
			sc.o(" %s.%s IN", assocUpdateFrag.tblFrag.alias, assocField2);
			sc.o(" (SELECT %s FROM %s as %s WHERE", keyPair.name, info.nearType.getName(), mainUpdateAlias);
	
			List<OpFragment> clonedL = WhereListHelper.cloneWhereList(existingWhereL);
			for(OpFragment opff: clonedL) {
				sc.o(opff.render());
			}
			sc.o(")");
			RawFragment rawFrag = new RawFragment(sc.toString());
	
			assocUpdateFrag.whereL.add(rawFrag);
			int extra = statement.paramL.size() - startingNumParams;
			cloneParams(statement, clonedL, extra);
		}
	}
	
	private void cloneParams(SqlStatement statement, List<OpFragment> clonedL, int extra) {
		//clone params 
		int numToAdd = 0;
		for(SqlFragment ff: clonedL) {
			numToAdd += ff.getNumSqlParams();
		}
		
		int n = statement.paramL.size();
		log.logDebug("cloneParams %d %d", numToAdd, n);
		for(int i = 0; i < numToAdd; i++) {
			int k = n - (numToAdd - i) - extra;
			DValue previous = statement.paramL.get(k);
			statement.paramL.add(previous); //add copy
		}
	}
	protected void buildAssocTblUpdate(UpdateStatementFragment assocUpdateFrag, DStructType structType, Map<String, DRelation> mmMap, String fieldName, RelationInfo info, String assocFieldName, SqlStatement statement) {
		DRelation drel = mmMap.get(fieldName); //100
		DValue dvalToUse  = drel.getForeignKey(); 

		RelationInfo farInfo = info.otherSide; 
		TypePair pair2 = DValueHelper.findField(farInfo.nearType, farInfo.fieldName);
		TypePair rightPair = new TypePair(assocFieldName, pair2.type);
		FieldFragment ff = FragmentHelper.buildFieldFragForTable(assocUpdateFrag.tblFrag, assocUpdateFrag, rightPair);
		statement.paramL.add(dvalToUse);
		assocUpdateFrag.setValuesL.add("?");
		assocUpdateFrag.fieldL.add(ff);
	}


	private void generateUpdateFns(QuerySpec spec, DStructType structType, UpdateStatementFragment selectFrag) {
		//orderby supported only by MySQL which delia does not support
		//			this.doOrderByIfPresent(spec, structType, selectFrag);
		//TODO why would we limit on update????   this.doLimitIfPresent(spec, structType, selectFrag);
		//			this.doOffsetIfPresent(spec, structType, selectFrag);
	}


	//		protected void doOffsetIfPresent(QuerySpec spec, DStructType structType, SelectStatementFragment selectFrag) {
	//			QueryFuncExp qfexp = selectFnHelper.findFn(spec, "offset");
	//			if (qfexp == null) {
	//				return;
	//			}
	//			IntegerExp exp = (IntegerExp) qfexp.argL.get(0);
	//			Integer n = exp.val;
	//
	//			OffsetFragment frag = new OffsetFragment(n);
	//			selectFrag.offsetFrag = frag;
	//		}

//	public String renderUpdate(UpdateStatementFragment selectFrag) {
//		if(selectFrag.setValuesL.isEmpty()) {
//			selectFrag.statement.sql = ""; //nothing to do
//			return selectFrag.statement.sql;
//		}
//
//		selectFrag.statement.sql = selectFrag.render();
//		return selectFrag.statement.sql;
//	}
	
	public SqlStatementGroup renderUpdateGroup(UpdateStatementFragment updateFrag) {
		SqlStatementGroup stgroup = new SqlStatementGroup();
//		if(updateFrag.setValuesL.isEmpty()) {
//			updateFrag.statement.sql = ""; //nothing to do
//			return stgroup;
//		}

		SqlStatement mainStatement = updateFrag.statement;
		mainStatement.sql = updateFrag.render();
		List<DValue> save = new ArrayList<>(mainStatement.paramL); //copy
		
		List<StatementFragmentBase> allL = new ArrayList<>();
		mainStatement.paramL.clear();
		if (!updateFrag.setValuesL.isEmpty()) {
			stgroup.add(updateFrag.statement);
		} else {
			removeAliases(updateFrag.assocUpdateFrag);
			removeAliases(updateFrag.assocDeleteFrag);
			removeAliases(updateFrag.assocMergeIntoFrag);
			removeAliasesList(updateFrag.assocCrudInsertL);
			removeAliasesList(updateFrag.assocCrudDeleteL);
			removeAliasesList(updateFrag.assocCrudUpdateL);
		}
		
		initMainParams(mainStatement, save, allL, updateFrag.assocUpdateFrag);
		initMainParams(mainStatement, save, allL, updateFrag.assocDeleteFrag);
		initMainParams(mainStatement, save, allL, updateFrag.assocMergeIntoFrag);
		for(InsertStatementFragment insFrag: updateFrag.assocCrudInsertL) {
			initMainParams(mainStatement, save, allL, insFrag);
		}
		for(DeleteStatementFragment delFrag: updateFrag.assocCrudDeleteL) {
			initMainParams(mainStatement, save, allL, delFrag);
		}
		for(UpdateStatementFragment upFrag: updateFrag.assocCrudUpdateL) {
			initMainParams(mainStatement, save,  allL,upFrag);
		}
		
		if (mainStatement.paramL.isEmpty()) {
			mainStatement.paramL.addAll(save);
			return stgroup; //no inner frags
		}
		
		addIfNotNull(stgroup, updateFrag.assocUpdateFrag, save, allL);
		addIfNotNull(stgroup, updateFrag.assocDeleteFrag, save, allL);
		addIfNotNull(stgroup, updateFrag.assocMergeIntoFrag, save, allL);
		for(InsertStatementFragment insFrag: updateFrag.assocCrudInsertL) {
			addIfNotNull(stgroup, insFrag, save, allL);
		}
		for(DeleteStatementFragment delFrag: updateFrag.assocCrudDeleteL) {
			addIfNotNull(stgroup, delFrag, save, allL);
		}
		for(UpdateStatementFragment upFrag: updateFrag.assocCrudUpdateL) {
			addIfNotNull(stgroup, upFrag, save, allL);
		}
		
		if (updateFrag.doUpdateLast) {
			SqlStatement stat = stgroup.statementL.remove(0); //move first to last
//			List<DValue> firstParams = stat.paramL;
			stgroup.statementL.add(stat);
		}
		
		return stgroup;
	}

	private void removeAliases(StatementFragmentBase frag) {
		if (frag != null) {
			frag.tblFrag.alias = null;
			frag.aliasMap.clear();
			for(FieldFragment field: frag.fieldL) {
				field.alias = null;
			}
		}
	}
	private void removeAliasesList(List<? extends StatementFragmentBase> fragL) {
		for(StatementFragmentBase frag: fragL) {
			removeAliases(frag);
		}
	}

	protected void initMainParams(SqlStatement mainStatement, List<DValue> saveL, List<StatementFragmentBase> allL, StatementFragmentBase innerFrag) {
		if (innerFrag != null) {
			allL.add(innerFrag);
		}
		
		if (! mainStatement.paramL.isEmpty()) {
			return;
		}
		
		if (innerFrag != null) {
			for(int i = 0; i < innerFrag.paramStartIndex; i++) {
				DValue dval = saveL.get(i);
				mainStatement.paramL.add(dval);
			}
		}
	}

	protected void addIfNotNull(SqlStatementGroup stgroup, StatementFragmentBase innerFrag, List<DValue> paramL, List<StatementFragmentBase> allL) {
		int nextStartIndex = 0;
		int k = 0;
		for(StatementFragmentBase sfb: allL) {
			if (sfb == innerFrag) {
				if (k < allL.size() - 1) {
					nextStartIndex = allL.get(k+1).paramStartIndex;
				} else {
					nextStartIndex = Integer.MAX_VALUE;
				}
			}
			k++;
		}
		
		if (innerFrag != null) {
			SqlStatement stat = innerFrag.statement;
			stat.sql = innerFrag.render();
			//we are copying more than needed, but that's ok
			int n = nextStartIndex < paramL.size() ? nextStartIndex : paramL.size();
			for(int i = innerFrag.paramStartIndex; i < n; i++) {
				DValue dval = paramL.get(i);
				stat.paramL.add(dval);
			}
			stgroup.add(stat);
		}
	}

	public void useAliases(boolean b) {
		this.useAliases = b;
	}
	
	//added from base
	protected DStructType getMainType(String typeName) {
		DStructType structType = (DStructType) registry.findTypeOrSchemaVersionType(typeName);
		return structType;
	}
	public TableFragment createTable(DStructType structType, StatementFragmentBase selectFrag) {
		TableFragment tblFrag = selectFrag.findByTableName(structType.getName());
		if (tblFrag != null) {
			return tblFrag;
		}
		
		tblFrag = new TableFragment();
		tblFrag.structType = structType;
//		createAlias(tblFrag);
		AliasInfo info = this.aliasMgr.createMainTableAlias(structType);
		tblFrag.alias = info.alias;
		tblFrag.name = structType.getName();
		selectFrag.aliasMap.put(tblFrag.name, tblFrag);
		return tblFrag;
	}
	
//	private int nextAliasIndex = 0;
	private QueryTypeDetector queryDetectorSvc = new QueryTypeDetector(factorySvc, registry);
	private WhereFragmentGenerator whereGen;
//	private SelectFuncHelper selectFnHelper;
	public TableExistenceService existSvc; //public as a hack
//	private FKHelper fkHelper;
//	private JoinFragment savedJoinedFrag;
	public List<TableInfo> tblinfoL; //public as a hack
//	private SpanHelper spanHelper;
	
	
//	private void createAlias(AliasedFragment frag) {
//		char ch = (char) ('a' + nextAliasIndex++);
//		frag.alias = String.format("%c", ch);
//	}
	
	protected void initWhere(QuerySpec spec, DStructType structType, StatementFragmentBase selectFrag) {
		//DeliaExceptionHelper.throwError("initWhere-not-impl", "");

		List<SqlFragment> fragL = hldWhereGen.createWhere(spec, structType, selectFrag.statement, aliasMgr);
		selectFrag.whereL.addAll(fragL);
		useAliases = true;
		
		
//		QueryType queryType = queryDetectorSvc.detectQueryType(spec);
//		switch(queryType) {
//		case ALL_ROWS:
//		{
//		}
//			break;
//		case OP:
//			whereGen.addWhereClauseOp(spec, structType, selectFrag);
//			break;
//		case PRIMARY_KEY:
//		default:
//		{
//			whereGen.addWhereClausePrimaryKey(spec, spec.queryExp.filter, structType, selectFrag);
//		}
//			break;
//		}
	}
	
	public TableFragment createAssocTable(StatementFragmentBase selectFrag, String tableName) {
		TableFragment tblFrag = selectFrag.findByTableName(tableName);
		if (tblFrag != null) {
			return tblFrag;
		}
		
		tblFrag = new TableFragment();
		tblFrag.structType = null;
		String fieldName = "fixlater"; //TODO fix!!
		DStructType structType = null;
		AliasInfo info = this.aliasMgr.createAssocAlias(structType, fieldName, tableName);
		tblFrag.alias = info.alias;
		//createAlias(tblFrag);
		tblFrag.name = tableName;
		selectFrag.aliasMap.put(tblFrag.name, tblFrag);
		return tblFrag;
	}
	protected void fixupForParentFields(DStructType structType, StatementFragmentBase selectFrag) {
//		public List<FieldFragment> fieldL = new ArsrayList<>();
//		public OrderByFragment orderByFrag = null;

		for(SqlFragment frag: selectFrag.whereL) {
			if (frag instanceof OpFragment) {
				OpFragment opfrag = (OpFragment) frag;
				TableFragment tblFrag = selectFrag.findByAlias(opfrag.left.alias);
				if (tblFrag != null && tblFrag.structType.equals(structType)) {
					//this is the main type
					doParentFixup(opfrag.left, tblFrag, selectFrag);
				}
				
				tblFrag = selectFrag.findByAlias(opfrag.right.alias);
				if (tblFrag != null && tblFrag.structType.equals(structType)) {
					//this is the main type
					doParentFixup(opfrag.right, tblFrag, selectFrag);
				}
			}
		}
	}

	//SELECT a.id,b.id as addr FROM Customer as a LEFT JOIN Address as b ON b.cust=a.id WHERE  a.addr < ?  -- (111)
	//a.addr is parent. change to b.id
	protected void doParentFixup(AliasedFragment aliasFrag, TableFragment tblFrag, StatementFragmentBase selectFrag) {
		String fieldName = aliasFrag.name;
		RelationOneRule oneRule = DRuleHelper.findOneRule(tblFrag.structType.getName(), fieldName, registry);
		if (oneRule != null && oneRule.relInfo.isParent) {
			RelationInfo relInfo = oneRule.relInfo;
			if (aliasFrag.name.equals(relInfo.fieldName)) {
				changeToChild(aliasFrag, relInfo, selectFrag);
			}
		} else {
			RelationManyRule manyRule = DRuleHelper.findManyRule(tblFrag.structType.getName(), fieldName, registry);
			if (manyRule != null && manyRule.relInfo.isParent) {
				RelationInfo relInfo = manyRule.relInfo;
				changeToChild(aliasFrag, relInfo, selectFrag);
			}
		}
	}

	protected void changeToChild(AliasedFragment aliasFrag, RelationInfo relInfo, StatementFragmentBase selectFrag) {
		TableFragment otherSide = selectFrag.aliasMap.get(relInfo.farType.getName());
		TypePair pair = DValueHelper.findPrimaryKeyFieldPair(relInfo.farType);
		log.log("fixup %s.%s -> %s.%s", aliasFrag.alias, aliasFrag.name, otherSide.alias, pair.name);
		aliasFrag.alias = otherSide.alias;
		aliasFrag.name = pair.name;
	}
	protected DStructType getMainType(QuerySpec spec) {
		DStructType structType = (DStructType) registry.findTypeOrSchemaVersionType(spec.queryExp.typeName);
		return structType;
	}

}