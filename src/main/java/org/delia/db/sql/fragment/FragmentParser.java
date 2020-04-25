package org.delia.db.sql.fragment;

import java.util.List;
import java.util.StringJoiner;

import org.apache.commons.lang3.StringUtils;
import org.delia.compiler.ast.Exp;
import org.delia.compiler.ast.IdentExp;
import org.delia.compiler.ast.IntegerExp;
import org.delia.compiler.ast.QueryFuncExp;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.db.DBAccessContext;
import org.delia.db.DBInterface;
import org.delia.db.QueryDetails;
import org.delia.db.QuerySpec;
import org.delia.db.SqlHelperFactory;
import org.delia.db.TableExistenceServiceImpl;
import org.delia.db.sql.QueryType;
import org.delia.db.sql.QueryTypeDetector;
import org.delia.db.sql.prepared.SelectFuncHelper;
import org.delia.db.sql.table.TableInfo;
import org.delia.relation.RelationInfo;
import org.delia.rule.rules.RelationManyRule;
import org.delia.rule.rules.RelationOneRule;
import org.delia.runner.VarEvaluator;
import org.delia.type.DStructType;
import org.delia.type.DTypeRegistry;
import org.delia.type.TypePair;
import org.delia.util.DRuleHelper;
import org.delia.util.DValueHelper;
import org.delia.util.DeliaExceptionHelper;

//single use!!!
	public class FragmentParser extends ServiceBase implements TableFragmentMaker {
		protected int nextAliasIndex = 0;
		protected QueryTypeDetector queryDetectorSvc;
		protected DTypeRegistry registry;
//		protected ScalarValueBuilder dvalBuilder;
//		protected ValueHelper valueHelper;
//		protected VarEvaluator varEvaluator;
		protected WhereFragmentGenerator whereGen;
		protected SelectFuncHelper selectFnHelper;
		protected TableExistenceServiceImpl existSvc;
		protected FKHelper fkHelper;
		protected JoinFragment savedJoinedFrag;
		
		public FragmentParser(FactoryService factorySvc, DTypeRegistry registry, VarEvaluator varEvaluator, List<TableInfo> tblinfoL, DBInterface dbInterface, 
					SqlHelperFactory sqlHelperFactory, WhereFragmentGenerator whereGen) {
			super(factorySvc);
			this.registry = registry;
			this.queryDetectorSvc = new QueryTypeDetector(factorySvc, registry);
			
//			this.dvalBuilder = factorySvc.createScalarValueBuilder(registry);
//			this.whereConverter = new SqlWhereConverter(factorySvc, registry, queryDetectorSvc);
//			this.filterRunner = new FilterFnRunner(registry);
//			this.valueHelper = new ValueHelper(factorySvc);
//			this.varEvaluator = varEvaluator;
			this.whereGen = whereGen; 
//			this.selectFnHelper = new SelectFuncHelper(new DBAccessContext(registry, varEvaluator));
			this.selectFnHelper = new SelectFuncHelper(factorySvc, registry);
			this.existSvc = new TableExistenceServiceImpl(dbInterface, new DBAccessContext(registry, varEvaluator));
			
			this.fkHelper = new FKHelper(factorySvc, registry, tblinfoL, sqlHelperFactory, varEvaluator, existSvc);
		}
		
		public void createAlias(AliasedFragment frag) {
			char ch = (char) ('a' + nextAliasIndex++);
			frag.alias = String.format("%c", ch);
		}

		public SelectStatementFragment parseSelect(QuerySpec spec, QueryDetails details) {
			SelectStatementFragment selectFrag = new SelectStatementFragment();
			
			//init tbl
			DStructType structType = getMainType(spec); 
			TableFragment tblFrag = createTable(structType, selectFrag);
			selectFrag.tblFrag = tblFrag;
			
			initFields(spec, structType, selectFrag);
//			addJoins(spec, structType, selectFrag, details);
			addFns(spec, structType, selectFrag);

			generateQueryFns(spec, structType, selectFrag);
			
			fixupForParentFields(structType, selectFrag);
			if (needJoin(spec, structType, selectFrag, details)) {
				//used saved join if we have one
				if (savedJoinedFrag == null) {
					addJoins(spec, structType, selectFrag, details);
				} else {
					selectFrag.joinFrag = savedJoinedFrag;
				}
			}

			if (selectFrag.fieldL.isEmpty()) {
				FieldFragment fieldF = buildStarFieldFrag(structType, selectFrag); //new FieldFragment();
				selectFrag.fieldL.add(fieldF);
			}
			
			
			return selectFrag;
		}
		
		protected boolean needJoin(QuerySpec spec, DStructType structType, SelectStatementFragment selectFrag, QueryDetails details) {
			if (needJoinBase(spec, structType, selectFrag, details)) {
				return true;
			}
			
			if (selectFrag.joinFrag == null) {
				return false;
			}

			String alias = savedJoinedFrag.joinTblFrag.alias;
			
			boolean mentioned = false;
			if (selectFrag.orderByFrag != null) {
				if (alias.equals(selectFrag.orderByFrag.alias)) {
					mentioned = true;
				}
				for(OrderByFragment obff: selectFrag.orderByFrag.additionalL) {
					if (alias.equals(obff.alias)) {
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
		
		public void generateQueryFns(QuerySpec spec, DStructType structType, SelectStatementFragment selectFrag) {
			this.doOrderByIfPresent(spec, structType, selectFrag);
			this.doLimitIfPresent(spec, structType, selectFrag);
			this.doOffsetIfPresent(spec, structType, selectFrag);
		}
		
		protected void doOrderByIfPresent(QuerySpec spec, DStructType structType, SelectStatementFragment selectFrag) {
			QueryFuncExp qfexp = selectFnHelper.findFn(spec, "orderBy");
			if (qfexp == null) {
				return;
			}
			
			StringJoiner joiner = new StringJoiner(",");
			boolean isDesc = false;
			for(Exp exp : qfexp.argL) {
				if (exp instanceof IdentExp) {
					isDesc = exp.strValue().equals("desc");
				} else {
					String fieldName = exp.strValue();
					if (fieldName.contains(".")) {
						fieldName = StringUtils.substringAfter(fieldName, ".");
					}
					if (! DValueHelper.fieldExists(structType, fieldName)) {
						DeliaExceptionHelper.throwError("unknown-field", "type '%s' does not have field '%s'. Invalid orderBy parameter", structType.getName(), fieldName);
					}
					
					String alias = FragmentHelper.findAlias(structType, selectFrag);
					joiner.add(String.format("%s.%s", alias, fieldName));
				}
			}

			String asc = isDesc ? "desc" : null;
			OrderByFragment frag = FragmentHelper.buildRawOrderByFrag(structType, joiner.toString(), asc, selectFrag);
			addToOrderBy(frag, selectFrag);
		}
		protected void addToOrderBy(OrderByFragment frag, SelectStatementFragment selectFrag) {
			OrderByFragment orderByFrag = selectFrag.orderByFrag;
			if (orderByFrag == null) {
				selectFrag.orderByFrag = frag;
			} else {
				//only add if different
				if (areEqualOrderBy(selectFrag.orderByFrag, frag)) {
					return;
				}
				for(OrderByFragment obf: selectFrag.orderByFrag.additionalL) {
					if (areEqualOrderBy(obf, frag)) {
						return;
					}
				}
				
				OrderByFragment tmp = selectFrag.orderByFrag; //swap
				selectFrag.orderByFrag = frag;
				selectFrag.orderByFrag.additionalL.add(tmp);
			}
		}
		protected boolean areEqualOrderBy(OrderByFragment orderByFrag, OrderByFragment frag) {
			if((frag.alias != null &&frag.alias.equals(orderByFrag.alias)) && frag.name.equals(orderByFrag.name)) {
				if (frag.asc != null && frag.asc.equals(orderByFrag.asc)) {
					return true;
				} else if (frag.asc == null && orderByFrag.asc == null) {
					return true;
				}
			}
			return false;
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
		protected void doOffsetIfPresent(QuerySpec spec, DStructType structType, SelectStatementFragment selectFrag) {
			QueryFuncExp qfexp = selectFnHelper.findFn(spec, "offset");
			if (qfexp == null) {
				return;
			}
			IntegerExp exp = (IntegerExp) qfexp.argL.get(0);
			Integer n = exp.val;

			OffsetFragment frag = new OffsetFragment(n);
			selectFrag.offsetFrag = frag;
		}


		protected void addFns(QuerySpec spec, DStructType structType, SelectStatementFragment selectFrag) {
			//TODO: for now we implement exist using count(*). improve later
			if (selectFnHelper.isCountPresent(spec) || selectFnHelper.isExistsPresent(spec)) {
				genCount(spec, structType, selectFrag);
			} else if (selectFnHelper.isMinPresent(spec)) {
				genMin(spec, structType, selectFrag);
			} else if (selectFnHelper.isMaxPresent(spec)) {
				genMax(spec, structType, selectFrag);
			} else if (selectFnHelper.isFirstPresent(spec)) {
				genFirst(spec, structType, selectFrag);
			} else if (selectFnHelper.isLastPresent(spec)) {
				genLast(spec, structType, selectFrag);
			} else {
//				sc.o("SELECT * FROM %s", typeName);
			}
		}

		protected void genCount(QuerySpec spec, DStructType structType, SelectStatementFragment selectFrag) {
			String fieldName = selectFnHelper.findFieldNameUsingFn(spec, "count");
			selectFrag.clearFieldList();
			if (fieldName == null) {
				FieldFragment fieldF = buildStarFieldFrag(structType, selectFrag); //new FieldFragment();
				fieldF.fnName = "COUNT";
				selectFrag.fieldL.add(fieldF);
			} else {
				addFnField("COUNT", fieldName, structType, selectFrag);
			}
		}
		protected void genMin(QuerySpec spec, DStructType structType, SelectStatementFragment selectFrag) {
			selectFrag.clearFieldList();
			String fieldName = selectFnHelper.findFieldNameUsingFn(spec, "min");
			addFnField("MIN", fieldName, structType, selectFrag);
		}
		protected void genMax(QuerySpec spec, DStructType structType, SelectStatementFragment selectFrag) {
			selectFrag.clearFieldList();
			String fieldName = selectFnHelper.findFieldNameUsingFn(spec, "max");
			addFnField("MAX", fieldName, structType, selectFrag);
		}
		protected void genFirst(QuerySpec spec, DStructType structType, SelectStatementFragment selectFrag) {
			String fieldName = selectFnHelper.findFieldNameUsingFn(spec, "first");
			AliasedFragment top = FragmentHelper.buildAliasedFrag(null, "TOP 1 ");
			selectFrag.earlyL.add(top);
			selectFrag.clearFieldList();
			if (fieldName == null) {
				FieldFragment fieldF = buildStarFieldFrag(structType, selectFrag); 
				selectFrag.fieldL.add(fieldF);
			} else {
				FieldFragment fieldF = FragmentHelper.buildFieldFrag(structType, selectFrag, fieldName);
				selectFrag.fieldL.add(fieldF);
			}
		}
		protected void genLast(QuerySpec spec, DStructType structType, SelectStatementFragment selectFrag) {
			String fieldName = selectFnHelper.findFieldNameUsingFn(spec, "last");
			AliasedFragment top = FragmentHelper.buildAliasedFrag(null, "TOP 1 ");
			selectFrag.earlyL.add(top);
			selectFrag.clearFieldList();
			if (fieldName == null) {
				forceAddOrderByPrimaryKey(structType, selectFrag, "desc");
			} else {
				FieldFragment fieldF = FragmentHelper.buildFieldFrag(structType, selectFrag, fieldName);
				selectFrag.fieldL.add(fieldF);
				forceAddOrderByField(structType, fieldName, "desc", selectFrag);
			}
		}


		protected void forceAddOrderByPrimaryKey(DStructType structType, SelectStatementFragment selectFrag, String asc) {
			TypePair pair = DValueHelper.findPrimaryKeyFieldPair(structType);
			if (pair == null) {
				return; //no primary key
			}
			forceAddOrderByField(structType, pair.name, asc, selectFrag);
		}
		protected void forceAddOrderByField(DStructType structType, String fieldName, String asc, SelectStatementFragment selectFrag) {
			OrderByFragment orderByFrag = FragmentHelper.buildOrderByFrag(structType, fieldName, asc, selectFrag);
			selectFrag.orderByFrag = orderByFrag;
		}

		protected void addFnField(String fnName, String fieldName, DStructType structType, SelectStatementFragment selectFrag) {
			FieldFragment fieldF = FragmentHelper.buildFieldFrag(structType, selectFrag, fieldName);
			if (fieldF == null) {
				DeliaExceptionHelper.throwError("unknown-field", "Field %s.%s unknown in %s function", structType.getName(), fieldName, fnName);
			}
			fieldF.fnName = fnName;
			addOrReplace(selectFrag, fieldF);
		}

		protected void addOrReplace(SelectStatementFragment selectFrag, FieldFragment fieldF) {
			selectFrag.fieldL.add(fieldF);
			// TODO Auto-generated method stub
			
		}

		protected void initFields(QuerySpec spec, DStructType structType, StatementFragmentBase selectFrag) {
			
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