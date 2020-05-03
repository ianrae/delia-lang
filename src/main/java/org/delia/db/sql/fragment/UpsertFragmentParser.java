package org.delia.db.sql.fragment;

import java.util.HashMap;
import java.util.Map;

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
public class UpsertFragmentParser extends SelectFragmentParser {

	protected boolean useAliases = true;

	public UpsertFragmentParser(FactoryService factorySvc, FragmentParserService fpSvc) {
		super(factorySvc, fpSvc);
	}

	//MERGE INTO Flight as a (id,wid) KEY(id) VALUES(?,?)  -- (55,22)
	public UpsertStatementFragment parseUpsert(QuerySpec spec, QueryDetails details, DValue partialVal, Map<String, String> assocCrudMap) {
		UpsertStatementFragment upsertFrag = new UpsertStatementFragment();

		Map<String, DRelation> mmMap = new HashMap<>();

		//init tbl
		DStructType structType = getMainType(spec); 
		TableFragment tblFrag = createTable(structType, upsertFrag);
		upsertFrag.tblFrag = tblFrag;

		generateKey(spec, upsertFrag, partialVal);
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

	/**
	 * Postgres doesn't like alias in UPDATE statements
	 * @param selectFrag
	 */
	protected void removeAllAliases(UpsertStatementFragment selectFrag) {
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

	protected void generateSetFields(QuerySpec spec, DStructType structType, UpsertStatementFragment updateFrag,
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
	protected boolean shouldGenerateFKConstraint(TypePair pair, DStructType dtype) {
		//key goes in child only
		RelationInfo info = DRuleHelper.findMatchingRuleInfo(dtype, pair);
		if (info != null && !info.isParent) {
			return true;
		}
		return false;
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
		stgroup.add(updateFrag.statement);
		
		return stgroup;
	}

	public void useAliases(boolean b) {
		this.useAliases = b;
	}
}