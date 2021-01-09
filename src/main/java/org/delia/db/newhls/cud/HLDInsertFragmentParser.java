package org.delia.db.newhls.cud;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.delia.assoc.DatIdMap;
import org.delia.compiler.ast.QueryExp;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.db.QueryBuilderService;
import org.delia.db.QuerySpec;
import org.delia.db.TableExistenceService;
import org.delia.db.newhls.HLDWhereGenImpl;
import org.delia.db.sql.QueryType;
import org.delia.db.sql.QueryTypeDetector;
import org.delia.db.sql.fragment.AliasedFragment;
import org.delia.db.sql.fragment.FieldFragment;
import org.delia.db.sql.fragment.FragmentHelper;
import org.delia.db.sql.fragment.InsertStatementFragment;
import org.delia.db.sql.fragment.OpFragment;
import org.delia.db.sql.fragment.SqlFragment;
import org.delia.db.sql.fragment.StatementFragmentBase;
import org.delia.db.sql.fragment.TableFragment;
import org.delia.db.sql.fragment.UpdateStatementFragment;
import org.delia.db.sql.fragment.WhereFragmentGenerator;
import org.delia.db.sql.prepared.SqlStatement;
import org.delia.db.sql.prepared.SqlStatementGroup;
import org.delia.db.sql.table.TableInfo;
import org.delia.relation.RelationInfo;
import org.delia.rule.rules.RelationManyRule;
import org.delia.rule.rules.RelationOneRule;
import org.delia.runner.DoNothingVarEvaluator;
import org.delia.type.DRelation;
import org.delia.type.DStructType;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.type.PrimaryKey;
import org.delia.type.TypePair;
import org.delia.util.DRuleHelper;
import org.delia.util.DValueHelper;
import org.delia.util.DeliaExceptionHelper;

/**
 * Copy of InsertFragmentParser but seperated from SelectFragmentParser.
 * 
 * single use!!!
 * @author ian
 *
 */
public class HLDInsertFragmentParser extends ServiceBase { //extends SelectFragmentParser {

	private boolean useAliases = false;
	private DatIdMap datIdMap;
	protected DTypeRegistry registry;
	private HLDWhereGen hldWhereGen;

	public HLDInsertFragmentParser(FactoryService factorySvc, DatIdMap datIdMap, DTypeRegistry registry, HLDWhereGen hdlWhereGen) {
		super(factorySvc);
		this.registry = registry;
		this.datIdMap = datIdMap;
		this.hldWhereGen = hdlWhereGen;
	}

	public InsertStatementFragment parseInsert(String typeName, DValue dval) {
		InsertStatementFragment insertFrag = new InsertStatementFragment();

		Map<String, DRelation> mmMap = new HashMap<>();

		//init tbl
		DStructType structType = getMainType(typeName); 
		TableFragment tblFrag = createTable(structType, insertFrag);
		insertFrag.tblFrag = tblFrag;

		generateSetFields(structType, insertFrag, dval, mmMap);
		generateParentUpdateIfNeeded(structType, insertFrag, dval);
		generateAssocUpdateIfNeeded(structType, insertFrag, dval, mmMap);

		if (! useAliases) {
			removeAllAliases(insertFrag);
			if (insertFrag.assocInsertFragL != null) {
				for(InsertStatementFragment assocFrag: insertFrag.assocInsertFragL) {
					removeAllAliases(assocFrag);
				}
			}
			if (insertFrag.fkUpdateFragL != null) {
				for(UpdateStatementFragment assocFrag: insertFrag.fkUpdateFragL) {
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
	private void removeAllAliases(StatementFragmentBase insertFrag) {
		for(FieldFragment ff: insertFrag.fieldL) {
			ff.alias = null;
		}
		insertFrag.tblFrag.alias = null;
		for(SqlFragment ff: insertFrag.whereL) {
			if (ff instanceof OpFragment) {
				OpFragment opff = (OpFragment) ff;
				opff.left.alias = null;
				opff.right.alias = null;
			}
		}
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
				dvalToUse  = drel.getForeignKey(); 
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
				int x = fillTableInfoIfNeeded(tblinfoL, info);
				if (x < 0) {
					DeliaExceptionHelper.throwError("can't-find-assoc-tbl", "Can't find assoc table for '%s' and '%s'", info.nearType.getName(), info.farType.getName());
				}
				
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

	private int fillTableInfoIfNeeded(List<TableInfo> tblinfoL, RelationInfo info) {
		return existSvc.fillTableInfoIfNeeded(tblinfoL, info, datIdMap);
	}

	private boolean genAssocField(InsertStatementFragment insertFrag, InsertStatementFragment assocInsertFrag, DStructType structType, DValue mainDVal, DValue xdval, 
			RelationInfo info, SqlStatement statement) {

		String assocTblName = datIdMap.getAssocTblName(info.getDatId());
//		TableInfo tblinfo = TableInfoHelper.findTableInfoAssoc(this.tblinfoL, info.nearType, info.farType);
		assocInsertFrag.tblFrag = this.createAssocTable(assocInsertFrag, assocTblName);
		assocInsertFrag.paramStartIndex = insertFrag.statement.paramL.size();

		//struct is Address AddressCustomerAssoc
		//		if (assocTblName.startsWith(structType.getName())) {
		boolean flipped = datIdMap.isFlipped(info);
		if (!flipped) {
			genAssocTblInsertRows(assocInsertFrag, flipped, mainDVal, info.nearType, info.farType, xdval, info);
		} else {
			genAssocTblInsertRows(assocInsertFrag, flipped, mainDVal, info.farType, info.nearType, xdval, info);
		}
		return true;
	}

	private void genAssocTblInsertRows(InsertStatementFragment assocInsertFrag, boolean flipped, 
			DValue mainDVal, DStructType nearType, DStructType farType, DValue xdval, RelationInfo info) {
//		String assocTbl = datIdMap.getAssocTblName(info.getDatId());
//		String field1 = DatIdMapHelper.getAssocLeftField(nearType, assocTbl);
		TypePair keyPair1 = DValueHelper.findPrimaryKeyFieldPair(nearType);
		TypePair keyPair2 = DValueHelper.findPrimaryKeyFieldPair(farType);
		
		if (flipped) {
			String field2 = datIdMap.getAssocFieldFor(info);
			String field1 = datIdMap.getAssocOtherField(info);
			DValue pk = mainDVal.asStruct().getField(keyPair2.name);
			genxrow(assocInsertFrag, field1, keyPair1, xdval);
			genxrow(assocInsertFrag, field2, keyPair2, pk);
		} else {
			String field1 = datIdMap.getAssocFieldFor(info);
			String field2 = datIdMap.getAssocOtherField(info);
			DValue pk = mainDVal.asStruct().getField(keyPair1.name);
			genxrow(assocInsertFrag, field1, keyPair2, pk);
			genxrow(assocInsertFrag, field2, keyPair1, xdval);
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

	public SqlStatementGroup renderInsertGroup(InsertStatementFragment insertFrag) {
		SqlStatementGroup stgroup = new SqlStatementGroup();

		SqlStatement mainStatement = insertFrag.statement;
		mainStatement.sql = insertFrag.render();
		List<DValue> save = new ArrayList<>(mainStatement.paramL); //copy

		mainStatement.paramL.clear();
		stgroup.add(insertFrag.statement);
		if (insertFrag.assocInsertFragL != null) {
			for(InsertStatementFragment assocFrag: insertFrag.assocInsertFragL) {
				initMainParams(mainStatement, save, assocFrag);
			}
		}
		if (insertFrag.fkUpdateFragL != null) {
			for(UpdateStatementFragment assocFrag: insertFrag.fkUpdateFragL) {
				initMainParams(mainStatement, save, assocFrag);
			}
		}
		
		if (mainStatement.paramL.isEmpty()) {
			mainStatement.paramL.addAll(save);
			return stgroup; //no inner frags
		}

		if (insertFrag.assocInsertFragL != null) {
			for(InsertStatementFragment assocFrag: insertFrag.assocInsertFragL) {
				addIfNotNull(stgroup, assocFrag, save, nextStartIndex(insertFrag.assocInsertFragL));
				initMainParams(mainStatement, save, assocFrag);
			}
		}
		if (insertFrag.fkUpdateFragL != null) {
			for(UpdateStatementFragment assocFrag: insertFrag.fkUpdateFragL) {
				addIfNotNull(stgroup, assocFrag, save, nextStartIndex(insertFrag.fkUpdateFragL));
				initMainParams(mainStatement, save, assocFrag);
			}
		}

		return stgroup;
	}
	private int nextStartIndex(List<? extends StatementFragmentBase> fragL) {
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
	
	/**
	 * if insert statement include values for parent relations we need to add an update
	 * statement.
	 * @param structType - main type being inserted
	 * @param insertFrag - insert frag
	 * @param dval - values
	 */
	private void generateParentUpdateIfNeeded(DStructType structType, InsertStatementFragment insertFrag, DValue dval) {

		for(TypePair pair: structType.getAllFields()) {
			if (pair.type.isStructShape()) {
				DValue inner = dval.asStruct().getField(pair.name);
				if (inner == null) {
					continue;
				}
				
				if (! shouldGenerateFKConstraint(pair, structType)) {
					RelationOneRule ruleOne = DRuleHelper.findOneRule(structType, pair.name);
					if (ruleOne != null) {
						DValue pkval = DValueHelper.findPrimaryKeyValue(dval);
						RelationInfo info = ruleOne.relInfo;
						RelationInfo otherSide = ruleOne.relInfo.otherSide;
						DValue fkval = inner.asRelation().getForeignKey();
						
						addFkUpdateStatement(insertFrag, info, otherSide, pkval, fkval);
					} else {
						RelationManyRule ruleMany = DRuleHelper.findManyRule(structType, pair.name);
						if (ruleMany != null) {
							DValue pkval = DValueHelper.findPrimaryKeyValue(dval);
							RelationInfo info = ruleMany.relInfo;
							RelationInfo otherSide = ruleMany.relInfo.otherSide;
							//PrimaryKey pk = info.nearType.getPrimaryKey();

							for(DValue fkval: inner.asRelation().getMultipleKeys()) {
								addFkUpdateStatement(insertFrag, info, otherSide, pkval, fkval);
							}
						}						
					}
				} 
			}
		}
		
	}

	private void addFkUpdateStatement(InsertStatementFragment insertFrag, RelationInfo info, RelationInfo otherSide,
			DValue pkval, DValue fkval) {
		UpdateStatementFragment updateFrag = createUpdateFrag(info, insertFrag); 

		PrimaryKey pk = info.nearType.getPrimaryKey();
		TypePair tmp = new TypePair(otherSide.fieldName, pk.getKeyType());
		FieldFragment ff = FragmentHelper.buildFieldFrag(info.farType, insertFrag, tmp);
		updateFrag.setValuesL.add("?");
		updateFrag.fieldL.add(ff);
		updateFrag.statement.paramL.add(pkval); 
		
		addWhere(insertFrag, info.farType, fkval, updateFrag);
		addFKUpdateFrag(insertFrag, updateFrag);
	}

	private UpdateStatementFragment createUpdateFrag(RelationInfo info, InsertStatementFragment insertFrag) {
		UpdateStatementFragment updateFrag = new UpdateStatementFragment();
		TableFragment tblFrag = createTable(info.farType, insertFrag);
//		updateFrag.aliasMap.put(tblFrag.name, tblFrag);

		updateFrag.tblFrag = tblFrag;
		updateFrag.paramStartIndex = insertFrag.statement.paramL.size();
		return updateFrag;
	}

	private void addFKUpdateFrag(InsertStatementFragment insertFrag, UpdateStatementFragment updateFrag) {
		if (insertFrag.fkUpdateFragL == null) {
			insertFrag.fkUpdateFragL = new ArrayList<>();
		}
		insertFrag.fkUpdateFragL.add(updateFrag);
	}
	private void addWhere(InsertStatementFragment insertFrag, DStructType structType, DValue pkval, UpdateStatementFragment updateFrag) {
		QueryBuilderService builderSvc = factorySvc.getQueryBuilderService();
		QueryExp exp = builderSvc.createPrimaryKeyQuery(structType.getName(), pkval);
		QuerySpec spec = builderSvc.buildSpec(exp, new DoNothingVarEvaluator());
		
		initWhere(spec, structType, insertFrag);
		int n = insertFrag.whereL.size();
		SqlFragment opFrag = insertFrag.whereL.remove(n - 1);
		updateFrag.whereL.add(opFrag);
		updateFrag.statement.paramL.add(pkval); 
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
		createAlias(tblFrag);
		tblFrag.name = structType.getName();
		selectFrag.aliasMap.put(tblFrag.name, tblFrag);
		return tblFrag;
	}
	
	private int nextAliasIndex = 0;
	private QueryTypeDetector queryDetectorSvc = new QueryTypeDetector(factorySvc, registry);
	private WhereFragmentGenerator whereGen;
//	private SelectFuncHelper selectFnHelper;
	public TableExistenceService existSvc; //public as a hack
//	private FKHelper fkHelper;
//	private JoinFragment savedJoinedFrag;
	public List<TableInfo> tblinfoL; //public as a hack
//	private SpanHelper spanHelper;
	
	
	public void createAlias(AliasedFragment frag) {
		char ch = (char) ('a' + nextAliasIndex++);
		frag.alias = String.format("%c", ch);
	}
	
	protected void initWhere(QuerySpec spec, DStructType structType, StatementFragmentBase selectFrag) {
		//DeliaExceptionHelper.throwError("initWhere-not-impl", "");
		
		List<SqlFragment> fragL = hldWhereGen.createWhere(spec, structType, selectFrag.statement);
		selectFrag.whereL.addAll(fragL);
		
//		if (whereGen == null) {
//			whereGen = new WhereFragmentGenerator(factorySvc, registry, new DoNothingVarEvaluator(), datIdMap);
//		}
//		
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
	
	private TableFragment createAssocTable(StatementFragmentBase selectFrag, String tableName) {
		TableFragment tblFrag = selectFrag.findByTableName(tableName);
		if (tblFrag != null) {
			return tblFrag;
		}
		
		tblFrag = new TableFragment();
		tblFrag.structType = null;
		createAlias(tblFrag);
		tblFrag.name = tableName;
		selectFrag.aliasMap.put(tblFrag.name, tblFrag);
		return tblFrag;
	}
	
}