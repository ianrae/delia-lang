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
	public class FragmentParser extends ServiceBase {
		private int nextAliasIndex = 0;
		private QueryTypeDetector queryDetectorSvc;
		private DTypeRegistry registry;
//		private ScalarValueBuilder dvalBuilder;
//		private ValueHelper valueHelper;
//		private VarEvaluator varEvaluator;
		private WhereFragmentGenerator whereGen;
		private SelectFuncHelper selectFnHelper;
		private TableExistenceServiceImpl existSvc;
		private FKHelper fkHelper;
		
		public FragmentParser(FactoryService factorySvc, DTypeRegistry registry, VarEvaluator varEvaluator, List<TableInfo> tblinfoL, DBInterface dbInterface, SqlHelperFactory sqlHelperFactory) {
			super(factorySvc);
			this.registry = registry;
			this.queryDetectorSvc = new QueryTypeDetector(factorySvc, registry);
			
//			this.dvalBuilder = factorySvc.createScalarValueBuilder(registry);
//			this.whereConverter = new SqlWhereConverter(factorySvc, registry, queryDetectorSvc);
//			this.filterRunner = new FilterFnRunner(registry);
//			this.valueHelper = new ValueHelper(factorySvc);
//			this.varEvaluator = varEvaluator;
			this.whereGen = new WhereFragmentGenerator(factorySvc, registry, varEvaluator);
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
			addJoins(spec, structType, selectFrag, details);
			addFns(spec, structType, selectFrag);

			generateQueryFns(spec, structType, selectFrag);
			
			if (selectFrag.fieldL.isEmpty()) {
				FieldFragment fieldF = buildStarFieldFrag(structType, selectFrag); //new FieldFragment();
				selectFrag.fieldL.add(fieldF);
			}
			
			fixupForParentFields(structType, selectFrag);
			
			return selectFrag;
		}
		
		private void fixupForParentFields(DStructType structType, SelectStatementFragment selectFrag) {
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
		private void doParentFixup(AliasedFragment aliasFrag, TableFragment tblFrag, SelectStatementFragment selectFrag) {
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

		private void changeToChild(AliasedFragment aliasFrag, RelationInfo relInfo, SelectStatementFragment selectFrag) {
			TableFragment otherSide = selectFrag.aliasMap.get(relInfo.farType.getName());
			TypePair pair = DValueHelper.findPrimaryKeyFieldPair(relInfo.farType);
			log.log("fixup %s.%s -> %s.%s", aliasFrag.alias, aliasFrag.name, otherSide.alias, pair.name);
			aliasFrag.alias = otherSide.alias;
			aliasFrag.name = pair.name;
		}

		private void addJoins(QuerySpec spec, DStructType structType, SelectStatementFragment selectFrag, QueryDetails details) {
			fkHelper.generateFKsQuery(spec, details, structType, selectFrag, this);
		}

		public TableFragment createTable(DStructType structType, SelectStatementFragment selectFrag) {
			TableFragment tblFrag = new TableFragment();
			tblFrag.structType = structType;
			createAlias(tblFrag);
			tblFrag.name = structType.getName();
			selectFrag.aliasMap.put(tblFrag.name, tblFrag);
			return tblFrag;
		}
		public TableFragment createAssocTable(SelectStatementFragment selectFrag, String tableName) {
			TableFragment tblFrag = new TableFragment();
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
		
		private void doOrderByIfPresent(QuerySpec spec, DStructType structType, SelectStatementFragment selectFrag) {
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
			OrderByFragment orderByFrag = selectFrag.orderByFrag;
			if (orderByFrag == null) {
				selectFrag.orderByFrag = frag;
			} else {
				OrderByFragment tmp = selectFrag.orderByFrag; //swap
				selectFrag.orderByFrag = frag;
				selectFrag.orderByFrag.additionalL.add(tmp);
			}
		}
		private void doLimitIfPresent(QuerySpec spec, DStructType structType, SelectStatementFragment selectFrag) {
			QueryFuncExp qfexp = selectFnHelper.findFn(spec, "limit");
			if (qfexp == null) {
				return;
			}
			IntegerExp exp = (IntegerExp) qfexp.argL.get(0);
			Integer n = exp.val;

			LimitFragment frag = new LimitFragment(n);
			selectFrag.limitFrag = frag;
		}
		private void doOffsetIfPresent(QuerySpec spec, DStructType structType, SelectStatementFragment selectFrag) {
			QueryFuncExp qfexp = selectFnHelper.findFn(spec, "offset");
			if (qfexp == null) {
				return;
			}
			IntegerExp exp = (IntegerExp) qfexp.argL.get(0);
			Integer n = exp.val;

			OffsetFragment frag = new OffsetFragment(n);
			selectFrag.offsetFrag = frag;
		}


		private void addFns(QuerySpec spec, DStructType structType, SelectStatementFragment selectFrag) {
			//TODO: for now we implement exist using count(*). improve later
			if (selectFnHelper.isCountPresent(spec) || selectFnHelper.isExistsPresent(spec)) {
				String fieldName = selectFnHelper.findFieldNameUsingFn(spec, "count");
				selectFrag.clearFieldList();
				if (fieldName == null) {
					FieldFragment fieldF = buildStarFieldFrag(structType, selectFrag); //new FieldFragment();
					fieldF.fnName = "COUNT";
					selectFrag.fieldL.add(fieldF);
				} else {
					addFnField("COUNT", fieldName, structType, selectFrag);
				}
			} else if (selectFnHelper.isMinPresent(spec)) {
				selectFrag.clearFieldList();
				String fieldName = selectFnHelper.findFieldNameUsingFn(spec, "min");
				addFnField("MIN", fieldName, structType, selectFrag);
			} else if (selectFnHelper.isMaxPresent(spec)) {
				selectFrag.clearFieldList();
				String fieldName = selectFnHelper.findFieldNameUsingFn(spec, "max");
				addFnField("MAX", fieldName, structType, selectFrag);
			} else if (selectFnHelper.isFirstPresent(spec)) {
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
			} else if (selectFnHelper.isLastPresent(spec)) {
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
			} else {
//				sc.o("SELECT * FROM %s", typeName);
			}
		}

		private void forceAddOrderByPrimaryKey(DStructType structType, SelectStatementFragment selectFrag, String asc) {
			TypePair pair = DValueHelper.findPrimaryKeyFieldPair(structType);
			if (pair == null) {
				return; //no primary key
			}
			forceAddOrderByField(structType, pair.name, asc, selectFrag);
		}
		private void forceAddOrderByField(DStructType structType, String fieldName, String asc, SelectStatementFragment selectFrag) {
			OrderByFragment orderByFrag = FragmentHelper.buildOrderByFrag(structType, fieldName, asc, selectFrag);
			selectFrag.orderByFrag = orderByFrag;
		}

		private void addFnField(String fnName, String fieldName, DStructType structType, SelectStatementFragment selectFrag) {
			FieldFragment fieldF = FragmentHelper.buildFieldFrag(structType, selectFrag, fieldName);
			if (fieldF == null) {
				DeliaExceptionHelper.throwError("unknown-field", "Field %s.%s unknown in %s function", structType.getName(), fieldName, fnName);
			}
			fieldF.fnName = fnName;
			addOrReplace(selectFrag, fieldF);
		}

		private void addOrReplace(SelectStatementFragment selectFrag, FieldFragment fieldF) {
			selectFrag.fieldL.add(fieldF);
			// TODO Auto-generated method stub
			
		}

		private void initFields(QuerySpec spec, DStructType structType, SelectStatementFragment selectFrag) {
			
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

		private DStructType getMainType(QuerySpec spec) {
			DStructType structType = (DStructType) registry.findTypeOrSchemaVersionType(spec.queryExp.typeName);
			return structType;
		}

		private FieldFragment buildStarFieldFrag(DStructType structType, SelectStatementFragment selectFrag) {
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
		
//		private FieldFragment buildFieldFrag(DStructType structType, SelectStatementFragment selectFrag, TypePair pair) {
//			return FragmentHelper.buildFieldFrag(structType, selectFrag, pair);
//		}

		public String render(SelectStatementFragment selectFrag) {
			selectFrag.statement.sql = selectFrag.render();
			return selectFrag.statement.sql;
		}
	}