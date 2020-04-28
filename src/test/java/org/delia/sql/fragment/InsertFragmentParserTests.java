package org.delia.sql.fragment;


import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.delia.api.Delia;
import org.delia.api.DeliaSessionImpl;
import org.delia.api.MigrationAction;
import org.delia.bddnew.NewBDDBase;
import org.delia.builder.ConnectionBuilder;
import org.delia.builder.ConnectionInfo;
import org.delia.builder.DeliaBuilder;
import org.delia.compiler.ast.DsonExp;
import org.delia.compiler.ast.Exp;
import org.delia.compiler.ast.InsertStatementExp;
import org.delia.compiler.ast.QueryExp;
import org.delia.core.FactoryService;
import org.delia.dao.DeliaDao;
import org.delia.db.DBInterface;
import org.delia.db.DBType;
import org.delia.db.QueryBuilderService;
import org.delia.db.QueryDetails;
import org.delia.db.QuerySpec;
import org.delia.db.SqlHelperFactory;
import org.delia.db.h2.H2SqlHelperFactory;
import org.delia.db.memdb.MemDBInterface;
import org.delia.db.sql.fragment.AssocTableReplacer;
import org.delia.db.sql.fragment.FieldFragment;
import org.delia.db.sql.fragment.FragmentHelper;
import org.delia.db.sql.fragment.InsertStatementFragment;
import org.delia.db.sql.fragment.OpFragment;
import org.delia.db.sql.fragment.SelectFragmentParser;
import org.delia.db.sql.fragment.SqlFragment;
import org.delia.db.sql.fragment.StatementFragmentBase;
import org.delia.db.sql.fragment.TableFragment;
import org.delia.db.sql.fragment.WhereFragmentGenerator;
import org.delia.db.sql.prepared.SqlStatement;
import org.delia.db.sql.prepared.SqlStatementGroup;
import org.delia.db.sql.table.TableInfo;
import org.delia.error.SimpleErrorTracker;
import org.delia.relation.RelationInfo;
import org.delia.rule.rules.RelationManyRule;
import org.delia.runner.ConversionResult;
import org.delia.runner.DsonToDValueConverter;
import org.delia.runner.Runner;
import org.delia.runner.RunnerImpl;
import org.delia.runner.VarEvaluator;
import org.delia.sprig.SprigService;
import org.delia.sprig.SprigServiceImpl;
import org.delia.type.DRelation;
import org.delia.type.DStructType;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.type.TypePair;
import org.delia.util.DRuleHelper;
import org.delia.util.DValueHelper;
import org.delia.util.DeliaExceptionHelper;
import org.junit.Before;
import org.junit.Test;


public class InsertFragmentParserTests extends NewBDDBase {
	
	//single use!!!
	public static class InsertFragmentParser extends SelectFragmentParser {

		private boolean useAliases = true;
		private AssocTableReplacer assocTblReplacer;

		public InsertFragmentParser(FactoryService factorySvc, DTypeRegistry registry, VarEvaluator varEvaluator, List<TableInfo> tblinfoL, DBInterface dbInterface, 
				SqlHelperFactory sqlHelperFactory, WhereFragmentGenerator whereGen, AssocTableReplacer assocTblReplacer) {
			super(factorySvc, registry, varEvaluator, tblinfoL, dbInterface, sqlHelperFactory, whereGen);
			this.assocTblReplacer = assocTblReplacer;
		}

		public InsertStatementFragment parseInsert(String typeName, QueryDetails details, DValue partialVal) {
			InsertStatementFragment selectFrag = new InsertStatementFragment();

			Map<String, DRelation> mmMap = new HashMap<>();

			//init tbl
			DStructType structType = getMainType(typeName); 
			TableFragment tblFrag = createTable(structType, selectFrag);
			selectFrag.tblFrag = tblFrag;

			generateSetFields(structType, selectFrag, partialVal, mmMap);
			generateAssocUpdateIfNeeded(structType, selectFrag, mmMap);

			fixupForParentFields(structType, selectFrag);

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
		private void removeAllAliases(InsertStatementFragment selectFrag) {
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

		private void generateSetFields(DStructType structType, InsertStatementFragment selectFrag,
				DValue dval, Map<String, DRelation> mmMap) {
			//we assume partialVal same type as structType!! (or maybe a base class)

			int index = selectFrag.fieldL.size(); //setValuesL is parallel array to fieldL
			if (index != 0) {

				log.log("WHY FILLING INNNNNNNNN");
				for(int i = 0; i < index; i++) {
					selectFrag.setValuesL.add("????");
				}
				DeliaExceptionHelper.throwError("unexpeced-fields-in-update", "should not occur");
			}

			for(String fieldName: dval.asMap().keySet()) {
				TypePair pair = DValueHelper.findField(structType, fieldName);

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

				DValue inner = dval.asMap().get(fieldName);
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
		private void generateAssocUpdateIfNeeded(DStructType structType,
				InsertStatementFragment selectFrag, Map<String, DRelation> mmMap) {
			if (mmMap.isEmpty()) {
				return;
			}

			for(String fieldName: mmMap.keySet()) {
				RelationManyRule ruleMany = DRuleHelper.findManyRule(structType, fieldName);
				if (ruleMany != null) {
					RelationInfo info = ruleMany.relInfo;
					selectFrag.assocUpdateFrag = new InsertStatementFragment();
//					genAssocField(selectFrag, selectFrag.assocUpdateFrag, structType, mmMap, fieldName, info, selectFrag.whereL, 
//							selectFrag.tblFrag.alias, selectFrag.statement);
				}
			}
		}

//		private void genAssocField(InsertStatementFragment updateFrag, InsertStatementFragment assocUpdateFrag, DStructType structType, Map<String, DRelation> mmMap, String fieldName, 
//				RelationInfo info, List<SqlFragment> existingWhereL, String mainUpdateAlias, SqlStatement statement) {
//			//update assoctabl set leftv=x where rightv=y
//			TableInfo tblinfo = TableInfoHelper.findTableInfoAssoc(this.tblinfoL, info.nearType, info.farType);
//			assocUpdateFrag.tblFrag = this.createAssocTable(assocUpdateFrag, tblinfo.assocTblName);
//
//			//struct is Address AddressCustomerAssoc
//			String field1;
//			String field2;
//			if (tblinfo.tbl1.equalsIgnoreCase(structType.getName())) {
//				field1 = "rightv";
//				field2 = "leftv";
//			} else {
//				field1 = "leftv";
//				field2 = "rightv";
//			}
//
//			//3 scenarios here:
//			// 1. updating all records in assoc table
//			// 2. updating where filter by primaykey only
//			// 3. updating where filter includes other fields (eg Customer.firstName) which may include primaryKey fields.
//			if (existingWhereL.isEmpty()) {
//				log.logDebug("m-to-n:scenario1");
//				buildUpdateAll(updateFrag, assocUpdateFrag, structType, mmMap, fieldName, info, field1, field2, mainUpdateAlias, statement);
//				return;
//			} else if (WhereListHelper.isOnlyPrimaryKeyQuery(existingWhereL, info.farType)) {
//				List<OpFragment> oplist = WhereListHelper.findPrimaryKeyQuery(existingWhereL, info.farType);
//				log.logDebug("m-to-n:scenario2");
//				buildUpdateByIdOnly(updateFrag, assocUpdateFrag, structType, mmMap, fieldName, info, field1, field2, oplist, statement);
//			} else {
//				log.logDebug("m-to-n:scenario3");
//				buildUpdateOther(updateFrag, assocUpdateFrag, structType, mmMap, fieldName, info, field1, field2, existingWhereL, mainUpdateAlias, statement);
//			}
//		}

//
//		protected void buildUpdateAll(InsertStatementFragment updateFrag, InsertStatementFragment assocUpdateFrag, DStructType structType, Map<String, DRelation> mmMap, 
//					String fieldName, RelationInfo info, String assocFieldName, String assocField2, String mainUpdateAlias, SqlStatement statement) {
//			if (assocTblReplacer != null) {
//				log.logDebug("use assocTblReplacer");
//				assocTblReplacer.buildUpdateAll(updateFrag, assocUpdateFrag, structType, mmMap, fieldName, info, assocFieldName, assocField2, mainUpdateAlias, statement);
//			} else {
//				buildAssocTblUpdate(assocUpdateFrag, structType, mmMap, fieldName, info, assocFieldName, statement);
//			}		
//		}
//		private void buildUpdateByIdOnly(InsertStatementFragment updateFrag, InsertStatementFragment assocUpdateFrag, DStructType structType,
//				Map<String, DRelation> mmMap, String fieldName, RelationInfo info, String assocFieldName,
//				String assocField2, List<OpFragment> oplist, SqlStatement statement) {
//			
//			if (assocTblReplacer != null) {
//				log.logDebug("use assocTblReplacer");
//				assocTblReplacer.buildUpdateByIdOnly(updateFrag, assocUpdateFrag, structType, mmMap, fieldName, info, assocFieldName, assocField2, statement);
//			} else {
//				int startingNumParams = statement.paramL.size();
//				buildAssocTblUpdate(assocUpdateFrag, structType, mmMap, fieldName, info, assocFieldName, statement);
//
//				List<OpFragment> clonedL = WhereListHelper.changeIdToAssocFieldName(false, oplist, info.farType, assocUpdateFrag.tblFrag.alias, assocField2);
//				assocUpdateFrag.whereL.addAll(clonedL);
//				
//				int extra = statement.paramL.size() - startingNumParams;
//				cloneParams(statement, clonedL, extra);
//			}
//		}
//		private void buildUpdateOther(InsertStatementFragment updateFrag, InsertStatementFragment assocUpdateFrag, DStructType structType,
//				Map<String, DRelation> mmMap, String fieldName, RelationInfo info, String assocFieldName, String assocField2,
//				List<SqlFragment> existingWhereL, String mainUpdateAlias, SqlStatement statement) {
//
//			updateFrag.doUpdateLast = true; //in case we're updating any of the fields in the query
//			if (assocTblReplacer != null) {
//				log.logDebug("use assocTblReplacer");
//				assocTblReplacer.buildUpdateOther(updateFrag, assocUpdateFrag, structType, mmMap, fieldName, info, assocFieldName, assocField2, existingWhereL, mainUpdateAlias, statement);
//			} else {
//				int startingNumParams = statement.paramL.size();
//				buildAssocTblUpdate(assocUpdateFrag, structType, mmMap, fieldName, info, assocFieldName, statement);
//		
//				//update CAAssoc set rightv=100 where (select id from customer where lastname='smith')
//				//Create a sub-select whose where list is a copy of the main update statement's where list.
//				TypePair keyPair = DValueHelper.findPrimaryKeyFieldPair(info.nearType);
//				StrCreator sc = new StrCreator();
//				sc.o(" %s.%s IN", assocUpdateFrag.tblFrag.alias, assocField2);
//				sc.o(" (SELECT %s FROM %s as %s WHERE", keyPair.name, info.nearType.getName(), mainUpdateAlias);
//		
//				List<OpFragment> clonedL = WhereListHelper.cloneWhereList(existingWhereL);
//				for(OpFragment opff: clonedL) {
//					sc.o(opff.render());
//				}
//				sc.o(")");
//				RawFragment rawFrag = new RawFragment(sc.str);
//		
//				assocUpdateFrag.whereL.add(rawFrag);
//				int extra = statement.paramL.size() - startingNumParams;
//				cloneParams(statement, clonedL, extra);
//			}
//		}
//		
//		private void cloneParams(SqlStatement statement, List<OpFragment> clonedL, int extra) {
//			//clone params 
//			int numToAdd = 0;
//			for(SqlFragment ff: clonedL) {
//				numToAdd += ff.getNumSqlParams();
//			}
//			
//			int n = statement.paramL.size();
//			log.logDebug("cloneParams %d %d", numToAdd, n);
//			for(int i = 0; i < numToAdd; i++) {
//				int k = n - (numToAdd - i) - extra;
//				DValue previous = statement.paramL.get(k);
//				statement.paramL.add(previous); //add copy
//			}
//		}
//		protected void buildAssocTblUpdate(InsertStatementFragment assocUpdateFrag, DStructType structType, Map<String, DRelation> mmMap, String fieldName, RelationInfo info, String assocFieldName, SqlStatement statement) {
//			DRelation drel = mmMap.get(fieldName); //100
//			DValue dvalToUse  = drel.getForeignKey(); //TODO; handle composite keys later
//
//			RelationInfo farInfo = DRuleHelper.findOtherSideMany(info.farType, structType);
//			TypePair pair2 = DValueHelper.findField(farInfo.nearType, farInfo.fieldName);
//			TypePair rightPair = new TypePair(assocFieldName, pair2.type);
//			FieldFragment ff = FragmentHelper.buildFieldFragForTable(assocUpdateFrag.tblFrag, assocUpdateFrag, rightPair);
//			statement.paramL.add(dvalToUse);
//			assocUpdateFrag.setValuesL.add("?");
//			assocUpdateFrag.fieldL.add(ff);
//		}
//


		public String renderInsert(InsertStatementFragment selectFrag) {
			if(selectFrag.setValuesL.isEmpty()) {
				selectFrag.statement.sql = ""; //nothing to do
				return selectFrag.statement.sql;
			}

			selectFrag.statement.sql = selectFrag.render();
			return selectFrag.statement.sql;
		}
		
		public SqlStatementGroup renderInsertGroup(InsertStatementFragment selectFrag) {
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
			if (mainStatement.paramL.isEmpty()) {
				mainStatement.paramL.addAll(save);
				return stgroup; //no inner frags
			}
			
			addIfNotNull(stgroup, selectFrag.assocUpdateFrag, save, nextStartIndex(selectFrag.assocDeleteFrag, selectFrag.assocMergeInfoFrag));
			addIfNotNull(stgroup, selectFrag.assocDeleteFrag, save, nextStartIndex(selectFrag.assocMergeInfoFrag));
			addIfNotNull(stgroup, selectFrag.assocMergeInfoFrag, save, Integer.MAX_VALUE);
			
//			if (selectFrag.doUpdateLast) {
//				SqlStatement stat = stgroup.statementL.remove(0); //move first to last
//				List<DValue> firstParams = stat.paramL;
//				stgroup.statementL.add(stat);
//				
////				//swap param lists too
////				SqlStatement newFirst = stgroup.statementL.get(0);
////				List<DValue> tmp = stat.paramL;
////				newFirst.paramL = firstParams;
////				stat.paramL = tmp;
//			}
//			
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
	
	
	///AAAB

	@Test
	public void testPrimaryKey() {
		String src = buildSrc();
		src += "\n insert Flight {field1: 1, field2: 10}";
//		src += "\n insert Flight {field1: 2, field2: 20}";
		
		InsertStatementExp insertStatementExp = buildFromSrc(src);
		DValue dval = convertToDVal(insertStatementExp);
		InsertStatementFragment selectFrag = buildUpdateFragment(insertStatementExp, dval); 
		
		runAndChk(selectFrag, "xxxUPDATE Flight as a SET a.field2 = ? WHERE a.field1 = ?");
	}
	
//	@Test
//	public void testPrimaryKeyNoAlias() {
//		String src = buildSrc();
//		src += " update Flight[1] {field2: 111}";
//		
//		UpdateStatementExp insertStatementExp = buildFromSrc(src);
//		DValue dval = convertToDVal(insertStatementExp);
//		useAliasesFlag = false;
//		InsertStatementFragment selectFrag = buildUpdateFragment(insertStatementExp, dval); 
//		
//		runAndChk(selectFrag, "UPDATE Flight SET field2 = ? WHERE field1 = ?");
//	}
//
//	@Test
//	public void testAllRows() {
//		String src = buildSrc();
//		src += " update Flight[true] {field2: 111}";
//		
//		UpdateStatementExp insertStatementExp = buildFromSrc(src);
//		DValue dval = convertToDVal(insertStatementExp);
//		InsertStatementFragment selectFrag = buildUpdateFragment(insertStatementExp, dval); 
//		
//		runAndChk(selectFrag, "UPDATE Flight as a SET a.field2 = ?");
//	}
//
//	@Test
//	public void testOp() {
//		String src = buildSrc();
//		src += " update Flight[field1 > 0] {field2: 111}";
//		
//		UpdateStatementExp insertStatementExp = buildFromSrc(src);
//		DValue dval = convertToDVal(insertStatementExp);
//		InsertStatementFragment selectFrag = buildUpdateFragment(insertStatementExp, dval); 
//		
//		runAndChk(selectFrag, "UPDATE Flight as a SET a.field2 = ? WHERE a.field1 > ?");
//	}
//	@Test
//	public void testOpNoPrimaryKey() {
//		String src = buildSrcNoPrimaryKey();
//		src += " update Flight[field1 > 0] {field2: 111}";
//		
//		UpdateStatementExp insertStatementExp = buildFromSrc(src);
//		DValue dval = convertToDVal(insertStatementExp);
//		InsertStatementFragment selectFrag = buildUpdateFragment(insertStatementExp, dval); 
//		
//		runAndChk(selectFrag, "UPDATE Flight as a SET a.field2 = ? WHERE a.field1 > ?");
//	}
//
//	@Test
//	public void testBasic() {
//		String src = buildSrc();
//		src += " update Flight[1] {field2: 111}";
//		
//		UpdateStatementExp insertStatementExp = buildFromSrc(src);
//		DValue dval = convertToDVal(insertStatementExp);
//		InsertStatementFragment selectFrag = buildUpdateFragment(insertStatementExp, dval); 
//		
//		runAndChk(selectFrag, "UPDATE Flight as a SET a.field2 = ? WHERE a.field1 = ?");
//	}
//	
//	@Test
//	public void testOneToOne() {
//		String src = buildSrcOneToOne();
//		src += "\n  update Customer[55] {wid: 333}";
//
//		UpdateStatementExp insertStatementExp = buildFromSrc(src);
//		DValue dval = convertToDVal(insertStatementExp, "Customer");
//		InsertStatementFragment selectFrag = buildUpdateFragment(insertStatementExp, dval); 
//		
//		runAndChk(selectFrag, "UPDATE Customer as a SET a.wid = ? WHERE a.id = ?");
//	}
//	@Test
//	public void testOneToOneParent() {
//		String src = buildSrcOneToOne();
//		src += "\n  update Customer[55] {wid: 333, addr:100}";
//
//		UpdateStatementExp insertStatementExp = buildFromSrc(src);
//		DValue dval = convertToDVal(insertStatementExp, "Customer");
//		InsertStatementFragment selectFrag = buildUpdateFragment(insertStatementExp, dval); 
//		
//		runAndChk(selectFrag, "UPDATE Customer as a SET a.wid = ? WHERE a.id = ?");
//	}
//	@Test
//	public void testOneToOneChild() {
//		String src = buildSrcOneToOne();
//		src += "\n  update Address[100] {z: 6, cust:55}";
//
//		UpdateStatementExp insertStatementExp = buildFromSrc(src);
//		DValue dval = convertToDVal(insertStatementExp, "Address");
//		InsertStatementFragment selectFrag = buildUpdateFragment(insertStatementExp, dval); 
//		
//		runAndChk(selectFrag, "UPDATE Address as a SET a.cust = ?, a.z = ? WHERE a.id = ?");
//	}
//	
//	@Test
//	public void testOneToMany() {
//		String src = buildSrcOneToMany();
//		src += "\n  update Customer[55] {wid: 333}";
//
//		UpdateStatementExp insertStatementExp = buildFromSrc(src);
//		DValue dval = convertToDVal(insertStatementExp, "Customer");
//		InsertStatementFragment selectFrag = buildUpdateFragment(insertStatementExp, dval); 
//		
//		runAndChk(selectFrag, "UPDATE Customer as a SET a.wid = ? WHERE a.id = ?");
//	}
//	@Test
//	public void testOneToManyParent() {
//		String src = buildSrcOneToMany();
//		src += "\n  update Customer[55] {wid: 333, addr:100}";
//
//		UpdateStatementExp insertStatementExp = buildFromSrc(src);
//		DValue dval = convertToDVal(insertStatementExp, "Customer");
//		InsertStatementFragment selectFrag = buildUpdateFragment(insertStatementExp, dval); 
//		
//		runAndChk(selectFrag, "UPDATE Customer as a SET a.wid = ? WHERE a.id = ?");
//	}
//	@Test
//	public void testOneToManyChild() {
//		String src = buildSrcOneToMany();
//		src += "\n  update Address[100] {z: 6, cust:55}";
//
//		UpdateStatementExp insertStatementExp = buildFromSrc(src);
//		DValue dval = convertToDVal(insertStatementExp, "Address");
//		InsertStatementFragment selectFrag = buildUpdateFragment(insertStatementExp, dval); 
//		
//		runAndChk(selectFrag, "UPDATE Address as a SET a.cust = ?, a.z = ? WHERE a.id = ?");
//	}
	
	
	//---
	private Delia delia;
	private FactoryService factorySvc;
	private DTypeRegistry registry;
	private Runner runner;
	private QueryBuilderService queryBuilderSvc;
	private QueryDetails details = new QueryDetails();
	private boolean useAliasesFlag = true;
	private InsertFragmentParser fragmentParser;
	private String sqlLine1;
	private String sqlLine2;


	@Before
	public void init() {
	}

	private DeliaDao createDao() {
		ConnectionInfo info = ConnectionBuilder.dbType(DBType.MEM).build();
		Delia delia = DeliaBuilder.withConnection(info).build();
		return new DeliaDao(delia);
	}

	private String buildSrc() {
		String src = "type Flight struct {field1 int unique, field2 int } end";
		return src;
	}
	private String buildSrcNoPrimaryKey() {
		String src = "type Flight struct {field1 int, field2 int } end";
		src += "\n insert Flight {field1: 1, field2: 10}";
		src += "\n insert Flight {field1: 2, field2: 20}";
		return src;
	}
	private String buildSrcOneToOne() {
		String src = " type Customer struct {id int unique, wid int, relation addr Address optional one parent } end";
		src += "\n type Address struct {id int unique, z int, relation cust Customer optional one } end";
		src += "\n  insert Customer {id: 55, wid: 33}";
		src += "\n  insert Address {id: 100, z:5, cust: 55 }";
		return src;
	}
	private String buildSrcOneToMany() {
		String src = " type Customer struct {id int unique, wid int, relation addr Address optional many } end";
		src += "\n type Address struct {id int unique, z int, relation cust Customer optional one } end";
		src += "\n  insert Customer {id: 55, wid: 33}";
		src += "\n  insert Address {id: 100, z:5, cust: 55 }";
		src += "\n  insert Address {id: 101, z:6, cust: 55 }";
		return src;
	}
	private String buildSrcManyToMany() {
		String src = " type Customer struct {id int unique, wid int, relation addr Address optional many } end";
		src += "\n type Address struct {id int unique, z int, relation cust Customer optional many } end";
		src += "\n  insert Customer {id: 55, wid: 33}";
		src += "\n  insert Customer {id: 56, wid: 34}";
		src += "\n  insert Address {id: 100, z:5, cust: [55,56] }";
		return src;
	}

	@Override
	public DBInterface createForTest() {
		return new MemDBInterface();
	}

	private QuerySpec buildQuery(QueryExp exp) {
		QuerySpec spec= queryBuilderSvc.buildSpec(exp, runner);
		return spec;
	}

	private InsertFragmentParser createFragmentParser(DeliaDao dao, String src, List<TableInfo> tblInfoL) {
		boolean b = dao.initialize(src);
		assertEquals(true, b);

		this.delia = dao.getDelia();
		this.factorySvc = delia.getFactoryService();
		this.registry = dao.getRegistry();
		this.runner = new RunnerImpl(factorySvc, dao.getDbInterface());

		InsertFragmentParser parser = createParser(dao, tblInfoL); 
		this.queryBuilderSvc = factorySvc.getQueryBuilderService();

		return parser;
	}
	private InsertFragmentParser createParser(DeliaDao dao) {
		List<TableInfo> tblinfoL = createTblInfoL(); 
		return createParser(dao, tblinfoL);
	}
	private InsertFragmentParser createParser(DeliaDao dao, List<TableInfo> tblinfoL) {
		SqlHelperFactory sqlHelperFactory = new H2SqlHelperFactory(factorySvc);
		
		WhereFragmentGenerator whereGen = new WhereFragmentGenerator(factorySvc, registry, runner);
	    AssocTableReplacer assocTblReplacer = new AssocTableReplacer(factorySvc, registry, runner, tblinfoL, dao.getDbInterface(), sqlHelperFactory, whereGen);
		InsertFragmentParser parser = new InsertFragmentParser(factorySvc, registry, runner, tblinfoL, dao.getDbInterface(), sqlHelperFactory, whereGen, assocTblReplacer);
		whereGen.tableFragmentMaker = parser;
		return parser;
	}

	private List<TableInfo> createTblInfoL() {
		List<TableInfo> tblinfoL = new ArrayList<>();
		TableInfo info = new  TableInfo("Address", "AddressCustomerAssoc");
		info.tbl1 = "Address";
		info.tbl2 = "Customer";
		//public String fieldName;
		tblinfoL.add(info);
		return tblinfoL;
	}
	private List<TableInfo> createTblInfoLOtherWay() {
		List<TableInfo> tblinfoL = new ArrayList<>();
		TableInfo info = new  TableInfo("Customer", "CustomerAddressAssoc");
		info.tbl1 = "Customer";
		info.tbl2 = "Address";
		//public String fieldName;
		tblinfoL.add(info);
		return tblinfoL;
	}

	private void runAndChk(InsertStatementFragment selectFrag, String expected) {
		String sql = fragmentParser.renderInsert(selectFrag);
		log.log(sql);
		assertEquals(expected, sql);
	}
	private void runAndChkLine(int lineNum, InsertStatementFragment selectFrag, String expected) {
		String sql = fragmentParser.renderInsert(selectFrag);
		log.log(sql);
		if (lineNum == 1) {
			String[] ar = sql.split("\n");
			this.sqlLine1 = ar[0];
			this.sqlLine2 = ar[1];
			assertEquals(expected, sqlLine1);
		}
	}
	private void chkLine(int lineNum, InsertStatementFragment selectFrag, String expected) {
		if (lineNum == 2) {
			assertEquals(expected, sqlLine2);
		}
	}

	private InsertStatementFragment buildUpdateFragment(InsertStatementExp exp, DValue dval) {
		fragmentParser.useAliases(useAliasesFlag);
		InsertStatementFragment selectFrag = fragmentParser.parseInsert(exp.typeName, details, dval);
		return selectFrag;
	}

	private InsertStatementExp buildFromSrc(String src) {
		List<TableInfo> tblinfoL = this.createTblInfoL();
		return buildFromSrc(src, tblinfoL);
	}
	private InsertStatementExp buildFromSrc(String src, List<TableInfo> tblinfoL) {
		DeliaDao dao = createDao(); 
		Delia xdelia = dao.getDelia();
		xdelia.getOptions().migrationAction = MigrationAction.GENERATE_MIGRATION_PLAN;
		dao.getDbInterface().getCapabilities().setRequiresSchemaMigration(true);
		log.log(src);
		this.fragmentParser = createFragmentParser(dao, src, tblinfoL); 
		
		//		List<Exp> expL = dao.getMostRecentSess().
		DeliaSessionImpl sessImpl = (DeliaSessionImpl) dao.getMostRecentSess();
		InsertStatementExp insertStatementExp = null;
		for(Exp exp: sessImpl.expL) {
			if (exp instanceof InsertStatementExp) {
				insertStatementExp = (InsertStatementExp) exp;
			}
		}
		return insertStatementExp;
	}

	private ConversionResult buildPartialValue(DStructType dtype, DsonExp dsonExp) {
		ConversionResult cres = new ConversionResult();
		cres.localET = new SimpleErrorTracker(log);
		SprigService sprigSvc = new SprigServiceImpl(factorySvc, registry);
		DsonToDValueConverter converter = new DsonToDValueConverter(factorySvc, cres.localET, registry, null, sprigSvc);
		cres.dval = converter.convertOnePartial(dtype.getName(), dsonExp);
		return cres;
	}
	private DValue convertToDVal(InsertStatementExp insertStatementExp) {
		return convertToDVal(insertStatementExp, "Flight");
	}
	private DValue convertToDVal(InsertStatementExp insertStatementExp, String typeName) {
		DStructType structType = (DStructType) registry.getType(typeName);
		ConversionResult cres = buildPartialValue(structType, insertStatementExp.dsonExp);
		assertEquals(0, cres.localET.errorCount());
		return cres.dval;
	}

}
