package org.delia.db.sql.fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.delia.core.FactoryService;
import org.delia.db.DBInterface;
import org.delia.db.SqlHelperFactory;
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
public class InsertFragmentParser extends SelectFragmentParser {

	private boolean useAliases = false;

	public InsertFragmentParser(FactoryService factorySvc, DTypeRegistry registry, VarEvaluator varEvaluator, List<TableInfo> tblinfoL, DBInterface dbInterface, 
			SqlHelperFactory sqlHelperFactory) {
		super(factorySvc, registry, varEvaluator, tblinfoL, dbInterface, sqlHelperFactory, null);
	}

	public InsertStatementFragment parseInsert(String typeName, DValue dval) {
		InsertStatementFragment insertFrag = new InsertStatementFragment();

		Map<String, DRelation> mmMap = new HashMap<>();

		//init tbl
		DStructType structType = getMainType(typeName); 
		TableFragment tblFrag = createTable(structType, insertFrag);
		insertFrag.tblFrag = tblFrag;

		generateSetFields(structType, insertFrag, dval, mmMap);
		generateAssocUpdateIfNeeded(structType, insertFrag, dval, mmMap);

		if (! useAliases) {
			removeAllAliases(insertFrag);
			if (insertFrag.assocInsertFragL != null) {
				for(InsertStatementFragment assocFrag: insertFrag.assocInsertFragL) {
					removeAllAliases(assocFrag);
				}
			}
		}

		return insertFrag;
	}

	/**
	 * Postgres doesn't like alias in UPDATE statements
	 * @param insertFrag
	 */
	private void removeAllAliases(InsertStatementFragment insertFrag) {
		for(FieldFragment ff: insertFrag.fieldL) {
			ff.alias = null;
		}
		insertFrag.tblFrag.alias = null;
	}

	private void generateSetFields(DStructType structType, InsertStatementFragment insertFrag,
			DValue dval, Map<String, DRelation> mmMap) {
		//we assume dVal same type as structType!! (or maybe a base class)

		int index = insertFrag.fieldL.size(); //setValuesL is parallel array to fieldL
		if (index != 0) {

			log.log("WHY FILLING INNNNNNNNN");
			for(int i = 0; i < index; i++) {
				insertFrag.setValuesL.add("????");
			}
			DeliaExceptionHelper.throwError("unexpeced-fields-in-insert", "should not occur");
		}

		for(TypePair pair: structType.getAllFields()) {
			if (pair.type.isStructShape()) {
				if (! shouldGenerateFKConstraint(pair, structType)) {
					continue;
				}
				if (DRuleHelper.isManyToManyRelation(pair, structType)) {
					DValue inner = dval.asStruct().getField(pair.name);
					if (inner == null) {
						mmMap.put(pair.name, null);
					} else {
						mmMap.put(pair.name, inner.asRelation());
					}
					continue;
				}
			}

			DValue inner = dval.asMap().get(pair.name);
			if (inner == null) {
				continue;
			}

			if (structType.fieldIsSerial(pair.name)) {
				DeliaExceptionHelper.throwError("serial-value-cannot-be-provided", "Type %s, field %s - do not specify a value for a serial field", structType.getName(), pair.name);
			}
			
			DValue dvalToUse = inner;
			if (inner.getType().isRelationShape()) {
				DRelation drel = inner.asRelation();
				dvalToUse  = drel.getForeignKey(); //TODO; handle composite keys later
			}

			FieldFragment ff = FragmentHelper.buildFieldFrag(structType, insertFrag, pair);
			insertFrag.setValuesL.add("?");
			insertFrag.fieldL.add(ff);
			insertFrag.statement.paramL.add(dvalToUse);

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
	private void generateAssocUpdateIfNeeded(DStructType structType,
			InsertStatementFragment insertFrag, DValue dval, Map<String, DRelation> mmMap) {
		if (mmMap.isEmpty()) {
			return;
		}

		for(String fieldName: mmMap.keySet()) {
			RelationManyRule ruleMany = DRuleHelper.findManyRule(structType, fieldName);
			if (ruleMany != null) {
				if (mmMap.get(fieldName) == null) {
					continue;
				}
				
				RelationInfo info = ruleMany.relInfo;
				DRelation drel = mmMap.get(fieldName);
				for(DValue xdval: drel.getMultipleKeys()) {
					InsertStatementFragment assocFrag = new InsertStatementFragment();
					boolean wereSome = genAssocField(insertFrag, assocFrag, structType, dval, xdval, info,  
							insertFrag.statement);
					if (wereSome) {
						if (insertFrag.assocInsertFragL == null) {
							insertFrag.assocInsertFragL = new ArrayList<>();
						}
						insertFrag.assocInsertFragL.add(assocFrag);
					}
				}
			}
		}
	}

	private boolean genAssocField(InsertStatementFragment insertFrag, InsertStatementFragment assocInsertFrag, DStructType structType, DValue mainDVal, DValue xdval, 
			RelationInfo info, SqlStatement statement) {

		TableInfo tblinfo = TableInfoHelper.findTableInfoAssoc(this.tblinfoL, info.nearType, info.farType);
		assocInsertFrag.tblFrag = this.createAssocTable(assocInsertFrag, tblinfo.assocTblName);
		assocInsertFrag.paramStartIndex = insertFrag.statement.paramL.size();

		//struct is Address AddressCustomerAssoc
		if (tblinfo.tbl1.equalsIgnoreCase(structType.getName())) {
			genAssocTblInsertRows(assocInsertFrag, true, mainDVal, info.nearType, info.farType, xdval, info);
		} else {
			genAssocTblInsertRows(assocInsertFrag, false, mainDVal, info.farType, info.nearType, xdval, info);
		}
		return true;
	}

	private void genAssocTblInsertRows(InsertStatementFragment assocInsertFrag, boolean mainDValFirst, 
			DValue mainDVal, DStructType farType, DStructType nearType, DValue xdval, RelationInfo info) {
		TypePair keyPair1 = DValueHelper.findPrimaryKeyFieldPair(info.farType);
		TypePair keyPair2 = DValueHelper.findPrimaryKeyFieldPair(info.nearType);
		if (mainDValFirst) {
			genxrow(assocInsertFrag, "leftv", keyPair1, mainDVal);
			genxrow(assocInsertFrag, "rightv", keyPair2, xdval);
		} else {
			genxrow(assocInsertFrag, "leftv", keyPair1, xdval);
			genxrow(assocInsertFrag, "rightv", keyPair2, mainDVal);
		}
	}

	private void genxrow(InsertStatementFragment assocInsertFrag, String assocFieldName, TypePair keyPair1, DValue dval) {
		TypePair tmpPair = new TypePair(assocFieldName, keyPair1.type);
		FieldFragment ff = FragmentHelper.buildFieldFragForTable(assocInsertFrag.tblFrag, assocInsertFrag, tmpPair);
		assocInsertFrag.setValuesL.add("?");
		assocInsertFrag.fieldL.add(ff);
		assocInsertFrag.statement.paramL.add(dval);
	}

	public String renderInsert(InsertStatementFragment selectFrag) {
		selectFrag.statement.sql = selectFrag.render();
		return selectFrag.statement.sql;
	}

	public SqlStatementGroup renderInsertGroup(InsertStatementFragment selectFrag) {
		SqlStatementGroup stgroup = new SqlStatementGroup();

		SqlStatement mainStatement = selectFrag.statement;
		mainStatement.sql = selectFrag.render();
		List<DValue> save = new ArrayList<>(mainStatement.paramL); //copy

		mainStatement.paramL.clear();
		stgroup.add(selectFrag.statement);
		if (selectFrag.assocInsertFragL != null) {
			for(InsertStatementFragment assocFrag: selectFrag.assocInsertFragL) {
				initMainParams(mainStatement, save, assocFrag);
			}
		}
		if (mainStatement.paramL.isEmpty()) {
			mainStatement.paramL.addAll(save);
			return stgroup; //no inner frags
		}

		if (selectFrag.assocInsertFragL != null) {
			for(InsertStatementFragment assocFrag: selectFrag.assocInsertFragL) {
				addIfNotNull(stgroup, assocFrag, save, nextStartIndex(selectFrag.assocInsertFragL));
				initMainParams(mainStatement, save, assocFrag);
			}
		}

		return stgroup;
	}
	private int nextStartIndex(List<InsertStatementFragment> fragL) {
		for(StatementFragmentBase frag: fragL) {
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
}