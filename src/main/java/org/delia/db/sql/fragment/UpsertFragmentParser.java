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
import org.delia.relation.RelationInfo;
import org.delia.type.DRelation;
import org.delia.type.DStructType;
import org.delia.type.DValue;
import org.delia.type.TypePair;
import org.delia.util.DRuleHelper;
import org.delia.util.DValueHelper;
import org.delia.util.DeliaExceptionHelper;

//single use!!!
public class UpsertFragmentParser extends SelectFragmentParser {

	private boolean useAliases = true;

	public UpsertFragmentParser(FactoryService factorySvc, FragmentParserService fpSvc) {
		super(factorySvc, fpSvc);
	}

	public UpsertStatementFragment parseUpsert(QuerySpec spec, QueryDetails details, DValue partialVal, Map<String, String> assocCrudMap) {
		UpsertStatementFragment selectFrag = new UpsertStatementFragment();

		Map<String, DRelation> mmMap = new HashMap<>();

		//init tbl
		DStructType structType = getMainType(spec); 
		TableFragment tblFrag = createTable(structType, selectFrag);
		selectFrag.tblFrag = tblFrag;

		generateSetFields(spec, structType, selectFrag, partialVal, mmMap);
		initWhere(spec, structType, selectFrag);
		generateKey(selectFrag, partialVal);
		//no min,max,etc in UPDATE

		//generateUpdateFns(spec, structType, selectFrag);

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
		}

		return selectFrag;
	}

	/**
	 * Postgres doesn't like alias in UPDATE statements
	 * @param selectFrag
	 */
	private void removeAllAliases(UpsertStatementFragment selectFrag) {
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
	
	private void generateKey(UpsertStatementFragment updateFrag, DValue partialVal) {
		DValue inner = DValueHelper.findPrimaryKeyValue(partialVal);
		String sql = String.format(" KEY(%s)", inner.asString());
		updateFrag.keyFrag =  new RawFragment(sql);
	}

	private void generateSetFields(QuerySpec spec, DStructType structType, UpsertStatementFragment updateFrag,
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
	private boolean shouldGenerateFKConstraint(TypePair pair, DStructType dtype) {
		//key goes in child only
		RelationInfo info = DRuleHelper.findMatchingRuleInfo(dtype, pair);
		if (info != null && !info.isParent) {
			return true;
		}
		return false;
	}
	
	private void generateUpsertFns(QuerySpec spec, DStructType structType, UpsertStatementFragment selectFrag) {
		//orderby supported only by MySQL which delia does not support
		//			this.doOrderByIfPresent(spec, structType, selectFrag);
		this.doLimitIfPresent(spec, structType, selectFrag);
		//			this.doOffsetIfPresent(spec, structType, selectFrag);
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
		
		if (mainStatement.paramL.isEmpty()) {
			mainStatement.paramL.addAll(save);
			return stgroup; //no inner frags
		}
		
		return stgroup;
	}

	private void initMainParams(SqlStatement mainStatement, List<DValue> saveL, List<StatementFragmentBase> allL, StatementFragmentBase innerFrag) {
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

	private void addIfNotNull(SqlStatementGroup stgroup, StatementFragmentBase innerFrag, List<DValue> paramL, List<StatementFragmentBase> allL) {
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
}