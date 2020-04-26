package org.delia.db.sql.fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.delia.core.FactoryService;
import org.delia.db.DBInterface;
import org.delia.db.QueryDetails;
import org.delia.db.QuerySpec;
import org.delia.db.SqlHelperFactory;
import org.delia.db.sql.StrCreator;
import org.delia.db.sql.prepared.SqlStatement;
import org.delia.db.sql.prepared.SqlStatementGroup;
import org.delia.db.sql.prepared.TableInfoHelper;
import org.delia.db.sql.table.TableInfo;
import org.delia.relation.RelationInfo;
import org.delia.rule.rules.RelationManyRule;
import org.delia.runner.VarEvaluator;
import org.delia.type.DRelation;
import org.delia.type.DStructType;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.type.TypePair;
import org.delia.util.DRuleHelper;
import org.delia.util.DValueHelper;
import org.delia.util.DeliaExceptionHelper;

//single use!!!
public class UpdateFragmentParser extends SelectFragmentParser {

	private boolean useAliases = true;
	private AssocTableReplacer assocTblReplacer;

	public UpdateFragmentParser(FactoryService factorySvc, DTypeRegistry registry, VarEvaluator varEvaluator, List<TableInfo> tblinfoL, DBInterface dbInterface, 
			SqlHelperFactory sqlHelperFactory, WhereFragmentGenerator whereGen) {
		super(factorySvc, registry, varEvaluator, tblinfoL, dbInterface, sqlHelperFactory, whereGen);
		this.assocTblReplacer = new AssocTableReplacer(factorySvc, registry, varEvaluator, tblinfoL, dbInterface, sqlHelperFactory, whereGen);
	}

	public UpdateStatementFragment parseUpdate(QuerySpec spec, QueryDetails details, DValue partialVal) {
		UpdateStatementFragment selectFrag = new UpdateStatementFragment();

		Map<String, DRelation> mmMap = new HashMap<>();

		//init tbl
		DStructType structType = getMainType(spec); 
		TableFragment tblFrag = createTable(structType, selectFrag);
		selectFrag.tblFrag = tblFrag;

		generateSetFields(spec, structType, selectFrag, partialVal, mmMap);
		initFieldsAndWhere(spec, structType, selectFrag);
		generateAssocUpdateIfNeeded(spec, structType, selectFrag, mmMap);

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
	private void removeAllAliases(UpdateStatementFragment selectFrag) {
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

	private void generateSetFields(QuerySpec spec, DStructType structType, UpdateStatementFragment selectFrag,
			DValue partialVal, Map<String, DRelation> mmMap) {
		//we assume partialVal same type as structType!! (or maybe a base class)

		int index = selectFrag.fieldL.size(); //setValuesL is parallel array to fieldL
		if (index != 0) {

			log.log("WHY FILLING INNNNNNNNN");
			for(int i = 0; i < index; i++) {
				selectFrag.setValuesL.add("????");
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
					if (inner != null) {
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

			FieldFragment ff = FragmentHelper.buildFieldFrag(structType, selectFrag, pair);
			selectFrag.setValuesL.add("?");
			selectFrag.fieldL.add(ff);
			selectFrag.statement.paramL.add(dvalToUse);

			index++;
		}
	}
	private boolean shouldGenerateFKConstraint(TypePair pair, DStructType dtype) {
		//key goes in child only
		RelationInfo info = DRuleHelper.findMatchingRuleInfo(dtype, pair);
		if (info != null && !info.isParent) {
			return true;
		}
		return false;
	}
	private void generateAssocUpdateIfNeeded(QuerySpec spec, DStructType structType,
			UpdateStatementFragment selectFrag, Map<String, DRelation> mmMap) {
		if (mmMap.isEmpty()) {
			return;
		}

		for(String fieldName: mmMap.keySet()) {
			RelationManyRule ruleMany = DRuleHelper.findManyRule(structType, fieldName);
			if (ruleMany != null) {
				RelationInfo info = ruleMany.relInfo;
				selectFrag.assocUpdateFrag = new UpdateStatementFragment();
				genAssocField(selectFrag, selectFrag.assocUpdateFrag, structType, mmMap, fieldName, info, selectFrag.whereL, 
						selectFrag.tblFrag.alias, selectFrag.statement);
			}
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
	private void buildUpdateByIdOnly(UpdateStatementFragment updateFrag, UpdateStatementFragment assocUpdateFrag, DStructType structType,
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
	private void buildUpdateOther(UpdateStatementFragment updateFrag, UpdateStatementFragment assocUpdateFrag, DStructType structType,
			Map<String, DRelation> mmMap, String fieldName, RelationInfo info, String assocFieldName, String assocField2,
			List<SqlFragment> existingWhereL, String mainUpdateAlias, SqlStatement statement) {

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
			RawFragment rawFrag = new RawFragment(sc.str);
	
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
		DValue dvalToUse  = drel.getForeignKey(); //TODO; handle composite keys later

		RelationInfo farInfo = DRuleHelper.findOtherSideMany(info.farType, structType);
		TypePair pair2 = DValueHelper.findField(farInfo.nearType, farInfo.fieldName);
		TypePair rightPair = new TypePair(assocFieldName, pair2.type);
		FieldFragment ff = FragmentHelper.buildFieldFragForTable(assocUpdateFrag.tblFrag, assocUpdateFrag, rightPair);
		statement.paramL.add(dvalToUse);
		assocUpdateFrag.setValuesL.add("?");
		assocUpdateFrag.fieldL.add(ff);
	}


	protected boolean needJoin(QuerySpec spec, DStructType structType, SelectStatementFragment selectFrag, QueryDetails details) {
		if (needJoinBase(spec, structType, selectFrag, details)) {
			return true;
		}

		//			if (selectFrag.joinFrag == null) {
		//				return false;
		//			}
		//
		//			String alias = savedJoinedFrag.joinTblFrag.alias;
		//
		//			boolean mentioned = false;
		//			if (selectFrag.orderByFrag != null) {
		//				if (alias.equals(selectFrag.orderByFrag.alias)) {
		//					mentioned = true;
		//				}
		//				for(OrderByFragment obff: selectFrag.orderByFrag.additionalL) {
		//					if (alias.equals(obff.alias)) {
		//						mentioned = true;
		//						break;
		//					}
		//				}
		//			}
		//
		//
		//			if (mentioned) {
		//				log.log("need join..");
		//				return true;
		//			}
		return false;
	}


	public void generateUpdateFns(QuerySpec spec, DStructType structType, UpdateStatementFragment selectFrag) {
		//orderby supported only by MySQL which delia does not support
		//			this.doOrderByIfPresent(spec, structType, selectFrag);
		this.doLimitIfPresent(spec, structType, selectFrag);
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

	public String renderUpdate(UpdateStatementFragment selectFrag) {
		if(selectFrag.setValuesL.isEmpty()) {
			selectFrag.statement.sql = ""; //nothing to do
			return selectFrag.statement.sql;
		}

		selectFrag.statement.sql = selectFrag.render();
		return selectFrag.statement.sql;
	}
	
	public SqlStatementGroup renderUpdateGroup(UpdateStatementFragment selectFrag) {
		SqlStatementGroup stgroup = new SqlStatementGroup();
		if(selectFrag.setValuesL.isEmpty()) {
			selectFrag.statement.sql = ""; //nothing to do
			return stgroup;
		}

		SqlStatement mainStatement = selectFrag.statement;
		mainStatement.sql = selectFrag.render();
		List<DValue> save = new ArrayList<>(mainStatement.paramL); //copy
		
		mainStatement.paramL.clear();
		stgroup.add(selectFrag.statement);
		initMainParams(mainStatement, save, selectFrag.assocUpdateFrag);
		initMainParams(mainStatement, save, selectFrag.assocDeleteFrag);
		initMainParams(mainStatement, save, selectFrag.assocMergeInfoFrag);
		addIfNotNull(stgroup, selectFrag.assocUpdateFrag, save, nextStartIndex(selectFrag.assocDeleteFrag, selectFrag.assocMergeInfoFrag));
		addIfNotNull(stgroup, selectFrag.assocDeleteFrag, save, nextStartIndex(selectFrag.assocMergeInfoFrag));
		addIfNotNull(stgroup, selectFrag.assocMergeInfoFrag, save, Integer.MAX_VALUE);
		return stgroup;
	}
	private int nextStartIndex(StatementFragmentBase... frags) {
		for(StatementFragmentBase frag: frags) {
			if (frag != null) {
				return frag.paramStartIndex;
			}
		}
		return Integer.MAX_VALUE;
	}

	private void initMainParams(SqlStatement mainStatement, List<DValue> saveL, StatementFragmentBase innerFrag) {
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

	private void addIfNotNull(SqlStatementGroup stgroup, StatementFragmentBase innerFrag, List<DValue> paramL, int nextStartIndex) {
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
}