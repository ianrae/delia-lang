package org.delia.db.sql.fragment;

import java.util.ArrayList;
import java.util.List;

import org.delia.compiler.ast.IdentExp;
import org.delia.compiler.ast.QueryExp;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.db.DBAccessContext;
import org.delia.db.QueryDetails;
import org.delia.db.QuerySpec;
import org.delia.db.SpanHelper;
import org.delia.db.SqlHelperFactory;
import org.delia.db.TableExistenceService;
import org.delia.db.sql.QueryTypeDetector;
import org.delia.db.sql.SqlNameFormatter;
import org.delia.db.sql.prepared.SelectFuncHelper;
import org.delia.db.sql.prepared.TableInfoHelper;
import org.delia.db.sql.table.TableInfo;
import org.delia.db.sql.where.SqlWhereConverter;
import org.delia.db.sql.where.TypeDetails;
import org.delia.db.sql.where.WhereExpression;
import org.delia.db.sql.where.WhereOperand;
import org.delia.db.sql.where.WherePhrase;
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

public class FKHelper extends ServiceBase {

	private DTypeRegistry registry;
	private List<TableInfo> tblinfoL;
	private QueryTypeDetector queryDetectorSvc;
	private SqlWhereConverter whereConverter;
	private SqlNameFormatter nameFormatter;
	private SelectFuncHelper selectFnHelper;
	private TableFragmentMaker tableFragmentMaker;

	public FKHelper(FactoryService factorySvc, DTypeRegistry registry, List<TableInfo> tblinfoL, 
			SqlHelperFactory sqlHelperFactory, VarEvaluator varEvaluator, SpanHelper spanHelper) {
		super(factorySvc);
		this.registry = registry;
		this.tblinfoL = tblinfoL;
		
		DBAccessContext dbctx = new DBAccessContext(registry, varEvaluator);
		this.nameFormatter = sqlHelperFactory.createNameFormatter();
		this.queryDetectorSvc = sqlHelperFactory.createQueryTypeDetector(dbctx);
		this.whereConverter = sqlHelperFactory.createSqlWhereConverter(dbctx, queryDetectorSvc);
//		this.sqlgen = sqlHelperFactory.createPrepSqlGen(existSvc, dbctx);
		this.selectFnHelper = sqlHelperFactory.createSelectFuncHelper(dbctx, spanHelper);
//		this.pwheregen = new WhereFragmentGenerator(factorySvc, registry, varEvaluator);
	}

	private String tblName(String typeName) {
		return nameFormatter.convert(typeName);
	}

	public void generateFKsQuery(QuerySpec spec, QueryDetails details, DStructType structType, 
				StatementFragmentBase selectFrag, TableFragmentMaker tableFragmentMaker) {
		this.tableFragmentMaker = tableFragmentMaker;
		QueryExp exp = spec.queryExp;
		
		TableFragment tbl = selectFrag.tblFrag;
		List<RelationOneRule> oneL = findAllOneRules(exp.getTypeName());
		List<RelationManyRule> manyL = findAllManyRules(exp.getTypeName());
		QueryAdjustment adjustment = addOtherPartsOfQuery(spec, exp.typeName);
		if (adjustment != null && adjustment.joinNotNeeded) {
			return; 
		}
//		if (adjustment != null && adjustment.isFirst) {
//			spec = this.selectFnHelper.doFirstFixup(spec, exp.typeName, tbl.alias);
//		} else if (adjustment != null && adjustment.isLast) {
//			spec = this.selectFnHelper.doLastFixup(spec, exp.typeName, tbl.alias);
//		}
		
//		RelationOneRule rule = DRuleHelper.findOneRule(exp.getTypeName(), registry);
		RelationOneRule rule = oneL.isEmpty() ? null : oneL.get(0);
		if (rule == null) {
//			RelationManyRule manyRule = DRuleHelper.findManyRule(exp.getTypeName(), registry);
			RelationManyRule manyRule = manyL.isEmpty() ? null : manyL.get(0);
			if (manyRule != null) {
				generateFKsQueryMany(spec, structType, exp, tbl, manyRule, details, selectFrag, adjustment);
//				sc.o(sql);
//				sqlgen.generateQueryFns(sc, spec, exp.typeName);
//				sc.o(";");
				return;
			}
			return; 
		}

		if (!rule.isParent()) {
			TypePair tmp = new TypePair(rule.relInfo.fieldName, null);
			genFields(structType, tbl, null, rule.relInfo.fieldName, tmp, selectFrag, adjustment);
			//sc.o("SELECT %s FROM %s as %s", fields, tbl.name, tbl.alias);
			
			TypePair nearField = DValueHelper.findPrimaryKeyFieldPair(rule.relInfo.nearType);
			TableFragment tbl2 = tableFragmentMaker.createTable(rule.relInfo.farType, selectFrag);
			JoinFragment joinFrag = new JoinFragment();
			joinFrag.joinTblFrag = tbl2;
			joinFrag.arg1 = FragmentHelper.buildFieldFrag(tbl.structType, selectFrag, rule.relInfo.fieldName);
			joinFrag.arg2 = FragmentHelper.buildFieldFragForTable(tbl2, selectFrag, nearField);
			selectFrag.joinFrag = joinFrag;
			return;
		} 
		TableFragment tbl2 = tableFragmentMaker.createTable(rule.relInfo.farType, selectFrag);

		TypePair nearField = DValueHelper.findPrimaryKeyFieldPair(rule.relInfo.nearType);
		genFields(structType, tbl, tbl2, rule.relInfo.fieldName, nearField, selectFrag, adjustment);
//		sc.o("SELECT %s FROM %s", fields, tbl.name);

		List<RelationOneRule> farL = findAllOneRules(rule.relInfo.farType.getName());
		RelationOneRule farRule = farL.isEmpty() ? null : farL.get(0);
//		String onstr = String.format("%s.%s=%s.%s", tbl2.alias, farRule.relInfo.fieldName, tbl.alias, nearField.name);

//		sc.o(" as %s LEFT JOIN %s ON %s", tbl.alias, tbl2.fmtAsStr(), onstr);
		JoinFragment joinFrag = new JoinFragment();
		joinFrag.joinTblFrag = tbl2;
		joinFrag.arg1 = FragmentHelper.buildFieldFrag(tbl2.structType, selectFrag, farRule.relInfo.fieldName);
		joinFrag.arg2 = FragmentHelper.buildFieldFragForTable(tbl, selectFrag, new TypePair(nearField.name, null));
		selectFrag.joinFrag = joinFrag;
//		sqlgen.generateQueryFns(sc, spec, exp.typeName);
		
		//Customer[addr = 100]. can't do this because customer is parent and doesn't have fk
		//must transform into Customer[b.cust
		WhereExpression express = whereConverter.convert(spec);
		if (express instanceof WherePhrase) {
			WherePhrase phrase = (WherePhrase) express;
			//TODO later also do op2
			if (phrase.op1.typeDetails.isRelation && phrase.op2.typeDetails.isParent) {
				WhereOperand replacement = new WhereOperand();
				replacement.alias = tbl2.alias;
				TypePair idpair = DValueHelper.findPrimaryKeyFieldPair(phrase.op1.typeDetails.dtype);
				replacement.exp = new IdentExp(idpair.name);
				replacement.typeDetails = new TypeDetails();
				replacement.typeDetails.dtype = idpair.type;
				
				if (phrase.op1.isValue) {
					phrase.op2 = replacement;
				} else {
					phrase.op1 = replacement;
				}
			}
			//this.queryDetectorSvc.addWhereClauseOpFromPhrase(sc, phrase, tbl);
//			pwheregen.addWhereClauseOpFromPhrase(spec, phrase, selectFrag.statement, selectFrag);
		} else {
//			this.pwheregen.addWhereClauseIfNeeded(sc, spec, exp.filter, exp.getTypeName(), tbl, statement);
		}
//		sc.o(";");
//		statement.sql = sc.str;
		return;
	}

	public static class QueryAdjustment {
		public String fieldName;
		public String fmt;
		public boolean isCount;
		public boolean isFirst;
		public boolean isLast;
		public boolean joinNotNeeded;
		
		public QueryAdjustment(String fieldName, String fmt) {
			this.fieldName = fieldName;
			this.fmt = fmt;
		}
	}
	private QueryAdjustment addOtherPartsOfQuery(QuerySpec spec, String typeName) {
		QueryAdjustment adjustment = doAddOtherPartsOfQuery(spec, typeName);
		if (adjustment != null && !adjustment.isCount) {
			DStructType dtype = (DStructType) registry.findTypeOrSchemaVersionType(typeName);
			TypePair keypair = DValueHelper.findPrimaryKeyFieldPair(dtype);
			if (keypair != null && !keypair.name.equals(adjustment.fieldName)) {
				adjustment.joinNotNeeded = true;
			}
		}
		return adjustment;
	}
	private QueryAdjustment doAddOtherPartsOfQuery(QuerySpec spec, String typeName) {
		if (selectFnHelper.isCountPresent(spec) || selectFnHelper.isExistsPresent(spec)) {
			String fieldName = selectFnHelper.findFieldNameUsingFn(spec, "last");
			QueryAdjustment adjustment = new QueryAdjustment(fieldName, "COUNT(%s)");
			adjustment.joinNotNeeded = true;
//			adjustment.isCount = true;
			return adjustment;
		} else if (selectFnHelper.isMinPresent(spec)) {
			String fieldName = selectFnHelper.findFieldNameUsingFn(spec, "min");
			QueryAdjustment adjustment = new QueryAdjustment(fieldName, "MIN(%s)");
			return adjustment;
		} else if (selectFnHelper.isMaxPresent(spec)) {
			String fieldName = selectFnHelper.findFieldNameUsingFn(spec, "max");
			QueryAdjustment adjustment = new QueryAdjustment(fieldName, "MAX(%s)");
			return adjustment;
		} else if (selectFnHelper.isFirstPresent(spec)) {
			String fieldName = selectFnHelper.findFieldNameUsingFn(spec, "first");
			QueryAdjustment adjustment = new QueryAdjustment(fieldName, "");
			adjustment.isFirst = true;
			return adjustment;
		} else if (selectFnHelper.isLastPresent(spec)) {
			String fieldName = selectFnHelper.findFieldNameUsingFn(spec, "last");
			QueryAdjustment adjustment = new QueryAdjustment(fieldName, "");
			adjustment.isLast = true;
			return adjustment;
		} else {
		}
		return null;
	}

	//TOOD: fix this limitation!!!
	private List<RelationOneRule> findAllOneRules(String typeName) {
		List<RelationOneRule> rulesL = new ArrayList<>();
		DStructType structType = (DStructType) registry.findTypeOrSchemaVersionType(typeName);
		for(TypePair pair: structType.getAllFields()) {
			if (pair.type.isStructShape()) {
				RelationOneRule rule = DRuleHelper.findOneRule(typeName, pair.name, registry);
				if (rule != null) {
					rulesL.add(rule);
				}
			}
		}
		
		if (rulesL.size() > 1) {
			DeliaExceptionHelper.throwError("too-many-rules", "only one relation per type in current Delia version. found %d one rules", rulesL.size());
		}
		
		return rulesL;
	}
	private List<RelationManyRule> findAllManyRules(String typeName) {
		List<RelationManyRule> rulesL = new ArrayList<>();
		DStructType structType = (DStructType) registry.findTypeOrSchemaVersionType(typeName);
		for(TypePair pair: structType.getAllFields()) {
			if (pair.type.isStructShape()) {
				RelationManyRule rule = DRuleHelper.findManyRule(typeName, pair.name, registry);
				if (rule != null) {
					rulesL.add(rule);
				}
			}
		}
		
		if (rulesL.size() > 1) {
			DeliaExceptionHelper.throwError("too-many-rules", "only one relation per type in current Delia version. found %d many rules", rulesL.size());
		}
		return rulesL;
	}

	private void generateFKsQueryMany(QuerySpec spec, DStructType structType, QueryExp exp, TableFragment tbl, RelationManyRule rule, QueryDetails details, 
			StatementFragmentBase selectFrag, QueryAdjustment adjustment) {
		TableFragment tbl2 = tableFragmentMaker.createTable(rule.relInfo.farType, selectFrag);

		List<RelationOneRule> farL = findAllOneRules(rule.relInfo.farType.getName());
//		RelationOneRule farRule = DRuleHelper.findOneRule(rule.relInfo.farType.getName(), registry);
		RelationOneRule farRule = farL.isEmpty() ? null : farL.get(0);
		if (farRule == null) {
			List<RelationManyRule> xfarL = findAllManyRules(rule.relInfo.farType.getName());
//			RelationManyRule xfarRule = DRuleHelper.findManyRule(rule.relInfo.farType.getName(), registry);
			RelationManyRule xfarRule = xfarL.isEmpty() ? null : xfarL.get(0);
			doManyToMany(spec, structType, exp, xfarRule, tbl, tbl2, rule, details, selectFrag, adjustment);
		} else {
			TypePair nearField = DValueHelper.findPrimaryKeyFieldPair(rule.relInfo.nearType);
			genFields(structType, tbl, tbl2, rule.relInfo.fieldName, nearField, selectFrag, adjustment);
			
//			sc.o("SELECT %s FROM %s", fields, tbl.name);
//			String onstr = String.format("%s.%s=%s.%s", tbl2.alias, farRule.relInfo.fieldName, tbl.alias, nearField.name);

//			sc.o(" as %s LEFT JOIN %s ON %s", tbl.alias, tbl2.fmtAsStr(), onstr);
//			pwheregen.addWhereClauseIfNeeded(sc, spec, exp.filter, exp.getTypeName(), tbl, statement);
			JoinFragment joinFrag = new JoinFragment();
			joinFrag.joinTblFrag = tbl2;
			joinFrag.arg1 = FragmentHelper.buildFieldFrag(tbl2.structType, selectFrag, farRule.relInfo.fieldName);
			joinFrag.arg2 = FragmentHelper.buildFieldFragForTable(tbl, selectFrag, new TypePair(nearField.name, null));
			selectFrag.joinFrag = joinFrag;

			details.mergeRows = true;
			details.mergeOnFieldL.add(rule.relInfo.fieldName);
		}
	}

	private void doManyToMany(QuerySpec spec, DStructType structType, QueryExp exp, RelationManyRule farRule, TableFragment tbl, TableFragment tbl2, RelationManyRule otherRule, 
					QueryDetails details, StatementFragmentBase selectFrag, QueryAdjustment adjustment)  {
		RelationInfo info = farRule.relInfo;
		TableInfo tblinfo = TableInfoHelper.findTableInfoAssoc(this.tblinfoL, info.nearType, info.farType);

		String actualTblName1 = this.tblName(tblinfo.tbl1); //postgres tables are lower-case
//		String actualTblName2 = this.tblName(tblinfo.tbl2);
		String assocField = actualTblName1.equalsIgnoreCase(tbl.name) ? "rightv" : "leftv";
		String assocField2 = actualTblName1.equalsIgnoreCase(tbl.name) ? "leftv" : "rightv";
		genJoin(spec, structType, info, tblinfo, tbl, otherRule, assocField, assocField2, exp, selectFrag, adjustment);
		details.mergeRows = true;
		details.isManyToMany = true;
		details.mergeOnFieldL.add(otherRule.relInfo.fieldName);
	}

	private void genJoin(QuerySpec spec, DStructType structType, RelationInfo info, TableInfo tblinfo, TableFragment tbl, RelationManyRule otherRule, String assocField, 
					String assocField2, QueryExp exp, StatementFragmentBase selectFrag, QueryAdjustment adjustment) {
		TableFragment tblAssoc = tableFragmentMaker.createAssocTable(selectFrag, tblinfo.assocTblName);
		TypePair copy = new TypePair(assocField, null);
		//TODO: fix adjustment
		genFields(structType, tbl, tblAssoc, otherRule.relInfo.fieldName, copy, selectFrag, adjustment);
		
		//sc.o("SELECT %s FROM %s", fields, tbl.name);

		TypePair pair = DValueHelper.findPrimaryKeyFieldPair(info.farType);
//		String onstr = String.format("%s.%s=%s.%s", tbl.alias, pair.name, tblAssoc.alias, assocField2);
//		sc.o(" as %s LEFT JOIN %s as %s ON %s", tbl.alias, tblAssoc.name, tblAssoc.alias, onstr);
		JoinFragment joinFrag = new JoinFragment();
		joinFrag.joinTblFrag = tblAssoc;
		joinFrag.arg1 = FragmentHelper.buildFieldFrag(tbl.structType, selectFrag, pair.name);
		TypePair p2 = new TypePair(assocField2, null); //TODO: fill in type later
		joinFrag.arg2 = FragmentHelper.buildFieldFragForTable(tblAssoc, selectFrag, p2);
		selectFrag.joinFrag = joinFrag;
		
		//pwheregen.addWhereClauseIfNeeded(sc, spec, exp.filter, typeName, tbl, statement);
	}
	


	private void genFields(DStructType structType, TableFragment tbl, TableFragment tbl2, String fieldName, TypePair nearField, 
			StatementFragmentBase selectFrag, QueryAdjustment adjustment) {
		//			sql = "SELECT c.id,a.id as addr FROM Customer as c JOIN Address as a ON a.cust=c.id WHERE c.id=55";
		
//		boolean haveDoneCount = false;
		for(TypePair pair: structType.getAllFields()) {
			FieldFragment ff = null;
			if (pair.type.isStructShape()) {
				if (tbl2 == null) {
					ff = FragmentHelper.buildFieldFrag(structType, selectFrag, new TypePair(nearField.name, null));
					ff.asName = fieldName;
//					s = String.format("%s as %s", nearField.name, fieldName);
				} else {
					ff = FragmentHelper.buildFieldFragForTable(tbl2, selectFrag, new TypePair(nearField.name, null));
					ff.asName = fieldName;
//					s = String.format("%s.%s as %s", tbl2.alias, nearField.name, fieldName);
				}
			} else {
				ff = FragmentHelper.buildFieldFrag(structType, selectFrag, pair.name);
//				s = String.format("%s.%s", tbl.alias, pair.name);
			}
			
			selectFrag.fieldL.add(ff);
			
//			if (adjustment != null) {
//				if (adjustment.isCount) {
//					TypePair keypair = DValueHelper.findPrimaryKeyFieldPair(structType);
//					if (keypair != null && !haveDoneCount) {
//						String ss = String.format("%s.%s", tbl.alias, keypair.name);
//						s = String.format(adjustment.fmt, ss);
//						haveDoneCount = true;
//						return s; //return only the adjustment
//					}
//				} else if (adjustment.isFirst || adjustment.isLast) {
//				} else if (adjustment.fieldName.equals(pair.name)) {
//					s = String.format(adjustment.fmt, s);
//					return s; //return only the adjustment
//				}
//			}
//			joiner.add(s);

		}
	}

}