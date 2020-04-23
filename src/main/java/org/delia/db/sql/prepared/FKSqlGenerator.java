package org.delia.db.sql.prepared;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

import org.delia.compiler.ast.IdentExp;
import org.delia.compiler.ast.QueryExp;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.db.DBAccessContext;
import org.delia.db.QueryDetails;
import org.delia.db.QuerySpec;
import org.delia.db.SqlHelperFactory;
import org.delia.db.TableExistenceService;
import org.delia.db.sql.QueryTypeDetector;
import org.delia.db.sql.SqlNameFormatter;
import org.delia.db.sql.StrCreator;
import org.delia.db.sql.Table;
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

public class FKSqlGenerator extends ServiceBase {

	private DTypeRegistry registry;
	private int nextAliasIndex = 0;
	private List<TableInfo> tblinfoL;
	private QueryTypeDetector queryDetectorSvc;
	private SqlWhereConverter whereConverter;
	private SqlNameFormatter nameFormatter;
	private PreparedStatementGenerator sqlgen;
	private WhereClauseGenerator pwheregen;
	private SqlHelperFactory sqlHelperFactory;
	private SelectFuncHelper selectFnHelper;

	public FKSqlGenerator(FactoryService factorySvc, DTypeRegistry registry, List<TableInfo> tblinfoL, 
			SqlHelperFactory sqlHelperFactory, VarEvaluator varEvaluator, TableExistenceService existSvc) {
		super(factorySvc);
		this.registry = registry;
		this.tblinfoL = tblinfoL;
		this.sqlHelperFactory = sqlHelperFactory;
		
		DBAccessContext dbctx = new DBAccessContext(registry, varEvaluator);
		this.nameFormatter = sqlHelperFactory.createNameFormatter(dbctx);
		this.queryDetectorSvc = sqlHelperFactory.createQueryTypeDetector(dbctx);
		this.whereConverter = sqlHelperFactory.createSqlWhereConverter(dbctx, queryDetectorSvc);
		this.pwheregen = sqlHelperFactory.createPWhereGen(dbctx); 
		this.sqlgen = sqlHelperFactory.createPrepSqlGen(existSvc, dbctx);
		this.selectFnHelper = sqlHelperFactory.createSelectFuncHelper(dbctx);
	}

	private Table genTable(String typeName) {
		Table tbl = new Table();
		tbl.name = tblName(typeName);
		char ch = (char) ('a' + nextAliasIndex++);
		tbl.alias = String.format("%c", ch);
		return tbl;
	}
	private String tblName(String typeName) {
		return nameFormatter.convert(typeName);
	}

	public SqlStatement generateFKsQuery(QuerySpec spec, QueryDetails details) {
		StrCreator sc = new StrCreator();
		QueryExp exp = spec.queryExp;
		SqlStatement statement = new SqlStatement();
		
		Table tbl = genTable(exp.getTypeName());
		List<RelationOneRule> oneL = findAllOneRules(exp.getTypeName());
		List<RelationManyRule> manyL = findAllManyRules(exp.getTypeName());
		QueryAdjustment adjustment = addOtherPartsOfQuery(spec, exp.typeName);
		if (adjustment != null && adjustment.joinNotNeeded) {
			return sqlgen.generateQuery(spec); 
		}
		if (adjustment != null && adjustment.isFirst) {
			spec = this.selectFnHelper.doFirstFixup(spec, exp.typeName, tbl.alias);
		} else if (adjustment != null && adjustment.isLast) {
			spec = this.selectFnHelper.doLastFixup(spec, exp.typeName, tbl.alias);
		}
		
//		RelationOneRule rule = DRuleHelper.findOneRule(exp.getTypeName(), registry);
		RelationOneRule rule = oneL.isEmpty() ? null : oneL.get(0);
		if (rule == null) {
//			RelationManyRule manyRule = DRuleHelper.findManyRule(exp.getTypeName(), registry);
			RelationManyRule manyRule = manyL.isEmpty() ? null : manyL.get(0);
			if (manyRule != null) {
				sc = new StrCreator();
				String sql = generateFKsQueryMany(spec, exp, manyRule, details, statement, adjustment);
				sc.o(sql);
				sqlgen.generateQueryFns(sc, spec, exp.typeName);
				sc.o(";");
				statement.sql = sc.str;
				return statement;
			}
			return sqlgen.generateQuery(spec); 
		}

		if (!rule.isParent()) {
			sc.o("SELECT * FROM %s", tbl.name);
			this.pwheregen.addWhereClauseIfNeeded(sc, spec, exp.filter, exp.getTypeName(), null, statement);
			sc.o(";");
			statement.sql = sc.str;
			return statement;
		} 
		Table tbl2 = genTable(rule.relInfo.farType.getName());

		TypePair nearField = DValueHelper.findPrimaryKeyFieldPair(rule.relInfo.nearType);
		String fields = genFields(exp.typeName, tbl, tbl2, rule.relInfo.fieldName, nearField, adjustment);
		sc.o("SELECT %s FROM %s", fields, tbl.name);

		List<RelationOneRule> farL = findAllOneRules(rule.relInfo.farType.getName());
//		RelationOneRule farRule = DRuleHelper.findOneRule(rule.relInfo.farType.getName(), registry);
		RelationOneRule farRule = farL.isEmpty() ? null : farL.get(0);
		String onstr = String.format("%s.%s=%s.%s", tbl2.alias, farRule.relInfo.fieldName, tbl.alias, nearField.name);

		//			TypePair farField = DValueHelper.findPrimaryKeyFieldPair(rule.relInfo.farType);
		sc.o(" as %s LEFT JOIN %s ON %s", tbl.alias, tbl2.fmtAsStr(), onstr);
		sqlgen.generateQueryFns(sc, spec, exp.typeName);
		
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
			pwheregen.addWhereClauseOpFromPhrase(sc, spec, phrase, tbl, statement);
		} else {
			this.pwheregen.addWhereClauseIfNeeded(sc, spec, exp.filter, exp.getTypeName(), tbl, statement);
		}
		sc.o(";");
		statement.sql = sc.str;
		return statement;
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
			DStructType dtype = (DStructType) registry.getType(typeName);
			TypePair keypair = DValueHelper.findPrimaryKeyFieldPair(dtype);
			if (keypair != null && !keypair.name.equals(adjustment.fieldName)) {
				adjustment.joinNotNeeded = true;
			}
		}
		return adjustment;
	}
	private QueryAdjustment doAddOtherPartsOfQuery(QuerySpec spec, String typeName) {
		if (selectFnHelper.isCountPresent(spec) || selectFnHelper.isExistsPresent(spec)) {
			QueryAdjustment adjustment = new QueryAdjustment(null, "COUNT(%s)");
			adjustment.isCount = true;
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
		DStructType structType = (DStructType) registry.getType(typeName);
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
		DStructType structType = (DStructType) registry.getType(typeName);
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

	private String generateFKsQueryMany(QuerySpec spec, QueryExp exp, RelationManyRule rule, QueryDetails details, SqlStatement statement, QueryAdjustment adjustment) {
		StrCreator sc = new StrCreator();

		Table tbl = genTable(exp.getTypeName());
		Table tbl2 = genTable(rule.relInfo.farType.getName());

		List<RelationOneRule> farL = findAllOneRules(rule.relInfo.farType.getName());
//		RelationOneRule farRule = DRuleHelper.findOneRule(rule.relInfo.farType.getName(), registry);
		RelationOneRule farRule = farL.isEmpty() ? null : farL.get(0);
		if (farRule == null) {
			List<RelationManyRule> xfarL = findAllManyRules(rule.relInfo.farType.getName());
//			RelationManyRule xfarRule = DRuleHelper.findManyRule(rule.relInfo.farType.getName(), registry);
			RelationManyRule xfarRule = xfarL.isEmpty() ? null : xfarL.get(0);
			return doManyToMany(sc, spec, exp, xfarRule, tbl, tbl2, rule, details, statement);
		} else {
			TypePair nearField = DValueHelper.findPrimaryKeyFieldPair(rule.relInfo.nearType);
			String fields = genFields(exp.typeName, tbl, tbl2, rule.relInfo.fieldName, nearField, adjustment);
			sc.o("SELECT %s FROM %s", fields, tbl.name);
			String onstr = String.format("%s.%s=%s.%s", tbl2.alias, farRule.relInfo.fieldName, tbl.alias, nearField.name);

			//				TypePair farField = DValueHelper.findPrimaryKeyFieldPair(rule.relInfo.farType);
			sc.o(" as %s LEFT JOIN %s ON %s", tbl.alias, tbl2.fmtAsStr(), onstr);
			pwheregen.addWhereClauseIfNeeded(sc, spec, exp.filter, exp.getTypeName(), tbl, statement);

			details.mergeRows = true;
			details.mergeOnField = rule.relInfo.fieldName;
		}

		return sc.str;
	}

	private String doManyToMany(StrCreator sc, QuerySpec spec, QueryExp exp, RelationManyRule farRule, Table tbl, Table tbl2, RelationManyRule otherRule, 
					QueryDetails details, SqlStatement statement) {
		RelationInfo info = farRule.relInfo;
		TableInfo tblinfo = TableInfoHelper.findTableInfoAssoc(this.tblinfoL, info.nearType, info.farType);

		String actualTblName1 = this.tblName(tblinfo.tbl1); //postgres tables are lower-case
//		String actualTblName2 = this.tblName(tblinfo.tbl2);
		String assocField = actualTblName1.equals(tbl.name) ? "rightv" : "leftv";
		String assocField2 = actualTblName1.equals(tbl.name) ? "leftv" : "rightv";
		genJoin(sc, spec, info, tblinfo, tbl, otherRule, assocField, assocField2, exp, statement);
		details.mergeRows = true;
		details.mergeOnField = otherRule.relInfo.fieldName;
		return sc.str;
	}

	private void genJoin(StrCreator sc, QuerySpec spec, RelationInfo info, TableInfo tblinfo, Table tbl, RelationManyRule otherRule, String assocField, 
					String assocField2, QueryExp exp, SqlStatement statement) {
		Table tblAssoc = genTable(tblinfo.assocTblName);
		String typeName = info.farType.getName();
		TypePair copy = new TypePair(assocField, null);
		//TODO: fix adjustment
		String fields = genFields(typeName, tbl, tblAssoc, otherRule.relInfo.fieldName, copy, null);
		sc.o("SELECT %s FROM %s", fields, tbl.name);

		TypePair pair = DValueHelper.findPrimaryKeyFieldPair(info.farType);
		String onstr = String.format("%s.%s=%s.%s", tbl.alias, pair.name, tblAssoc.alias, assocField2);
		sc.o(" as %s LEFT JOIN %s as %s ON %s", tbl.alias, tblAssoc.name, tblAssoc.alias, onstr);

		pwheregen.addWhereClauseIfNeeded(sc, spec, exp.filter, typeName, tbl, statement);
		sc.o(";");
	}
	


	private String genFields(String typeName, Table tbl, Table tbl2, String fieldName, TypePair nearField, QueryAdjustment adjustment) {
		//			sql = "SELECT c.id,a.id as addr FROM Customer as c JOIN Address as a ON a.cust=c.id WHERE c.id=55";
		StringJoiner joiner = new StringJoiner(",");
		DStructType structType = (DStructType) registry.getType(typeName);
		
		boolean haveDoneCount = false;
		for(TypePair pair: structType.getAllFields()) {
			String s;
			if (pair.type.isStructShape()) {
				s = String.format("%s.%s as %s", tbl2.alias, nearField.name, fieldName);
			} else {
				s = String.format("%s.%s", tbl.alias, pair.name);
			}
			
			if (adjustment != null) {
				if (adjustment.isCount) {
					TypePair keypair = DValueHelper.findPrimaryKeyFieldPair(structType);
					if (keypair != null && !haveDoneCount) {
						String ss = String.format("%s.%s", tbl.alias, keypair.name);
						s = String.format(adjustment.fmt, ss);
						haveDoneCount = true;
					}
				} else if (adjustment.isFirst || adjustment.isLast) {
				} else if (adjustment.fieldName.equals(pair.name)) {
					s = String.format(adjustment.fmt, s);
				}
			}
			joiner.add(s);

		}
		return joiner.toString();
	}

}