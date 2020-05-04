package org.delia.db.sql.fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import org.delia.compiler.ast.BooleanExp;
import org.delia.compiler.ast.FilterOpFullExp;
import org.delia.core.FactoryService;
import org.delia.db.QueryDetails;
import org.delia.db.QuerySpec;
import org.delia.db.sql.prepared.SqlStatement;
import org.delia.db.sql.prepared.SqlStatementGroup;
import org.delia.dval.DValueExConverter;
import org.delia.relation.RelationInfo;
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
		logParams("a", upsertFrag);
		List<DValue> tmpL = new ArrayList<>(upsertFrag.statement.paramL);
//		upsertFrag.statement.paramL.clear();
		int nn = upsertFrag.statement.paramL.size();
		
		logParams("a1", upsertFrag);
		initWhere(spec, structType, upsertFrag);
		generateAssocUpdateIfNeeded(spec, structType, upsertFrag, mmMap, assocCrudMap);
		if (upsertFrag.statement.paramL.size() > nn) {
			upsertFrag.statement.paramL.remove(nn);
			if (upsertFrag.assocDeleteFrag != null) {
				upsertFrag.assocDeleteFrag.paramStartIndex--;
			}
			if (upsertFrag.assocMergeIntoFrag != null) {
				upsertFrag.assocMergeIntoFrag.paramStartIndex--;
			}
		}
		logParams("a2", upsertFrag);
//		//remove last
//		int n = upsertFrag.statement.paramL.size();
//		upsertFrag.statement.paramL.remove(n - 1);

		logParams("a3", upsertFrag);
//		upsertFrag.statement.paramL.addAll(tmpL);
		logParams("a4", upsertFrag);

		fixupForParentFields(structType, upsertFrag);
		
		
		return upsertFrag;
	}

	private void logParams(String title, UpsertStatementFragment upsertFrag) {
		StringJoiner joiner = new StringJoiner(",");
		for(DValue dval: upsertFrag.statement.paramL) {
			joiner.add(dval.asString());
		}
		log.log("%s: %s", title, joiner.toString());
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
			if (spec.queryExp.filter.cond instanceof BooleanExp) {
				DeliaExceptionHelper.throwError("upsert-filter-error", "[true] not supported");
			} else if (spec.queryExp.filter.cond instanceof FilterOpFullExp) {
				DeliaExceptionHelper.throwError("upsert-filter-error", "only primary key filters are supported");
			}
			
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

	@Override
	protected void buildUpdateAll(UpdateStatementFragment updateFrag, UpdateStatementFragment assocUpdateFrag, DStructType structType, Map<String, DRelation> mmMap, 
				String fieldName, RelationInfo info, String assocFieldName, String assocField2, String mainUpdateAlias, SqlStatement statement) {
		DeliaExceptionHelper.throwError("upsert-update-all-not-supported", "not supported");
	}
	@Override
	protected void buildUpdateOther(UpdateStatementFragment updateFrag, UpdateStatementFragment assocUpdateFrag, DStructType structType,
			Map<String, DRelation> mmMap, String fieldName, RelationInfo info, String assocFieldName, String assocField2,
			List<SqlFragment> existingWhereL, String mainUpdateAlias, SqlStatement statement) {
		DeliaExceptionHelper.throwError("upsert-update-other-not-supported", "not supported");
	}

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
		initMainParams(mainStatement, save, allL, updateFrag.assocMergeIntoFrag);
		
		if (mainStatement.paramL.isEmpty()) {
			mainStatement.paramL.addAll(save);
			return stgroup; //no inner frags
		}
		
		addIfNotNull(stgroup, updateFrag.assocUpdateFrag, save, allL);
		addIfNotNull(stgroup, updateFrag.assocDeleteFrag, save, allL);
		addIfNotNull(stgroup, updateFrag.assocMergeIntoFrag, save, allL);
		
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