package org.delia.db.sql.fragment;

import java.util.List;

import org.delia.compiler.ast.IntegerExp;
import org.delia.compiler.ast.QueryFuncExp;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.db.QueryDetails;
import org.delia.db.QuerySpec;
import org.delia.db.TableExistenceServiceImpl;
import org.delia.db.sql.QueryType;
import org.delia.db.sql.QueryTypeDetector;
import org.delia.db.sql.prepared.SelectFuncHelper;
import org.delia.db.sql.table.TableInfo;
import org.delia.relation.RelationInfo;
import org.delia.rule.rules.RelationManyRule;
import org.delia.rule.rules.RelationOneRule;
import org.delia.type.DStructType;
import org.delia.type.DTypeRegistry;
import org.delia.type.TypePair;
import org.delia.util.DRuleHelper;
import org.delia.util.DValueHelper;

//single use!!!
	public class FragmentParserBase extends ServiceBase implements TableFragmentMaker {
		protected int nextAliasIndex = 0;
		protected QueryTypeDetector queryDetectorSvc;
		protected DTypeRegistry registry;
		protected WhereFragmentGenerator whereGen;
		protected SelectFuncHelper selectFnHelper;
		protected TableExistenceServiceImpl existSvc;
		protected FKHelper fkHelper;
		protected JoinFragment savedJoinedFrag;
		protected List<TableInfo> tblinfoL;
		
		public FragmentParserBase(FactoryService factorySvc, FragmentParserService fpSvc) {
			super(factorySvc);
			
			this.registry = fpSvc.registry;
			this.queryDetectorSvc = fpSvc.createQueryTypeDetector();
			this.tblinfoL = fpSvc.tblinfoL;
			this.whereGen = fpSvc.whereGen; 
			this.selectFnHelper = fpSvc.createSelectFuncHelper(); 
			this.existSvc = fpSvc.createTableExistenceService(); 
			
			this.fkHelper = fpSvc.createFKHelper(); 
		}
		
		public void createAlias(AliasedFragment frag) {
			char ch = (char) ('a' + nextAliasIndex++);
			frag.alias = String.format("%c", ch);
		}

		
		protected boolean needJoinBase(QuerySpec spec, DStructType structType, StatementFragmentBase selectFrag, QueryDetails details) {
			QueryFuncExp qfexp = selectFnHelper.findFn(spec, "fetch");
			QueryFuncExp qfexp2 = selectFnHelper.findFn(spec, "fks");
			//TODO: later add fk
			//TODO: we need to distinguish which join. fix later
			if (qfexp != null || qfexp2 != null || selectFrag.aliasMap.size() > 1) {
				return true;
			}
			
			int numFields = selectFrag.fieldL.size();
			addJoins(spec, structType, selectFrag, details);
			if (selectFrag.joinFrag == null) {
				return false;
			}
			
			//remove the join-added fields
			while(selectFrag.fieldL.size() != numFields) {
				int n = selectFrag.fieldL.size();
				selectFrag.fieldL.remove(n - 1);
			}
			
			this.savedJoinedFrag = selectFrag.joinFrag;
			selectFrag.joinFrag = null; //clear
			
			//SELECT a.id,b.id as addr FROM Customer as a LEFT JOIN Address as b ON b.cust=a.id WHERE  a.id = ?  -- (55)
			String alias = savedJoinedFrag.joinTblFrag.alias;
			DStructType joinType = savedJoinedFrag.joinTblFrag.structType;
			
			//is join table mentioned anywhere
			boolean mentioned = false;
			for(FieldFragment ff: selectFrag.fieldL) {
				if (alias.equals(ff.alias) || (joinType != null && joinType.equals(ff.structType))) {
					mentioned = true;
					break;
				}
			}
			
			for(SqlFragment ff: selectFrag.whereL) {
				if (ff instanceof OpFragment) {
					OpFragment opff = (OpFragment) ff;
					if (alias.equals(opff.left.alias) || (alias.equals(opff.right.alias))) {
						mentioned = true;
						break;
					}
				}
			}
			
			if (mentioned) {
				log.log("need join..");
				return true;
			}
			return false;
		}

		protected void fixupForParentFields(DStructType structType, StatementFragmentBase selectFrag) {
//			public List<FieldFragment> fieldL = new ArsrayList<>();
//			public OrderByFragment orderByFrag = null;

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

		protected void addJoins(QuerySpec spec, DStructType structType, StatementFragmentBase selectFrag, QueryDetails details) {
			fkHelper.generateFKsQuery(spec, details, structType, selectFrag, this);
		}

		@Override
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
		@Override
		public TableFragment createAssocTable(StatementFragmentBase selectFrag, String tableName) {
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
		

		protected void doLimitIfPresent(QuerySpec spec, DStructType structType, StatementFragmentBase selectFrag) {
			QueryFuncExp qfexp = selectFnHelper.findFn(spec, "limit");
			if (qfexp == null) {
				return;
			}
			IntegerExp exp = (IntegerExp) qfexp.argL.get(0);
			Integer n = exp.val;

			LimitFragment frag = new LimitFragment(n);
			selectFrag.limitFrag = frag;
		}



		protected void addOrReplace(StatementFragmentBase selectFrag, FieldFragment fieldF) {
			selectFrag.fieldL.add(fieldF);
			// TODO Auto-generated method stub
			
		}

		protected void initFieldsAndWhere(QuerySpec spec, DStructType structType, StatementFragmentBase selectFrag) {
			
			QueryType queryType = queryDetectorSvc.detectQueryType(spec);
			switch(queryType) {
			case ALL_ROWS:
			{
//				addWhereExist(sc, spec);
			}
				break;
			case OP:
//				addWhereClauseOp(sc, spec, typeName, tbl, statement);
				whereGen.addWhereClauseOp(spec, structType, selectFrag);
				break;
			case PRIMARY_KEY:
			default:
			{
//				addWhereClausePrimaryKey(sc, spec, spec.queryExp.filter, typeName, tbl, statement);
				whereGen.addWhereClausePrimaryKey(spec, spec.queryExp.filter, structType, selectFrag);
			}
				break;
			}
		}

		protected DStructType getMainType(QuerySpec spec) {
			DStructType structType = (DStructType) registry.findTypeOrSchemaVersionType(spec.queryExp.typeName);
			return structType;
		}
		protected DStructType getMainType(String typeName) {
			DStructType structType = (DStructType) registry.findTypeOrSchemaVersionType(typeName);
			return structType;
		}

		protected FieldFragment buildStarFieldFrag(DStructType structType, StatementFragmentBase selectFrag) {
			TypePair pair = DValueHelper.findPrimaryKeyFieldPair(structType);
			if (pair == null) {
				FieldFragment fieldF = FragmentHelper.buildEmptyFieldFrag(structType, selectFrag);
				fieldF.isStar = true;
				return fieldF;
			}
			FieldFragment fieldF = FragmentHelper.buildFieldFrag(structType, selectFrag, pair);
			fieldF.isStar = true;
			return fieldF;
		}
		
//		protected FieldFragment buildFieldFrag(DStructType structType, SelectStatementFragment selectFrag, TypePair pair) {
//			return FragmentHelper.buildFieldFrag(structType, selectFrag, pair);
//		}

		public String renderSelect(SelectStatementFragment selectFrag) {
			selectFrag.statement.sql = selectFrag.render();
			return selectFrag.statement.sql;
		}
	}