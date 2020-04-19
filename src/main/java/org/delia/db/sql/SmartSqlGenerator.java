//package org.delia.db.sql;
//
//import java.util.List;
//import java.util.StringJoiner;
//
//import org.delia.compiler.ast.FilterExp;
//import org.delia.compiler.ast.IdentExp;
//import org.delia.compiler.ast.QueryExp;
//import org.delia.core.FactoryService;
//import org.delia.core.ServiceBase;
//import org.delia.db.QueryDetails;
//import org.delia.db.QuerySpec;
//import org.delia.db.sql.table.TableInfo;
//import org.delia.db.sql.where.SqlWhereConverter;
//import org.delia.db.sql.where.TypeDetails;
//import org.delia.db.sql.where.WhereExpression;
//import org.delia.db.sql.where.WhereOperand;
//import org.delia.db.sql.where.WherePhrase;
//import org.delia.relation.RelationInfo;
//import org.delia.rule.rules.RelationManyRule;
//import org.delia.rule.rules.RelationOneRule;
//import org.delia.type.DStructType;
//import org.delia.type.DType;
//import org.delia.type.DTypeRegistry;
//import org.delia.type.TypePair;
//import org.delia.util.DRuleHelper;
//import org.delia.util.DValueHelper;
//
//public class SmartSqlGenerator extends ServiceBase {
//
//	private DTypeRegistry registry;
//	private SqlGenerator sqlgen;
//	private int nextAliasIndex = 0;
//	private List<TableInfo> tblinfoL;
//	private QueryTypeDetector queryDetectorSvc;
////	private SqlDateGenerator dateGenerator;
//	private SqlWhereConverter whereConverter;
//	private SqlNameFormatter nameFormatter;
//
//	public SmartSqlGenerator(FactoryService factorySvc, DTypeRegistry registry, List<TableInfo> tblinfoL, SqlNameFormatter nameFormatter) {
//		super(factorySvc);
//		this.registry = registry;
//		this.sqlgen = new SqlGenerator(factorySvc, registry, nameFormatter);
//		this.tblinfoL = tblinfoL;
//		this.nameFormatter = nameFormatter;
////		this.dateGenerator = new SqlDateGenerator(factorySvc, registry);
//		this.queryDetectorSvc = new QueryTypeDetector(factorySvc, registry);
//		this.whereConverter = new SqlWhereConverter(factorySvc, registry, queryDetectorSvc);
//	}
//
//	private Table genTable(String typeName) {
//		Table tbl = new Table();
//		tbl.name = tblName(typeName);
//		char ch = (char) ('a' + nextAliasIndex++);
//		tbl.alias = String.format("%c", ch);
//		return tbl;
//	}
//	private String tblName(String typeName) {
//		return nameFormatter.convert(typeName);
//	}
//	private String tblName(DType dtype) {
//		return nameFormatter.convert(dtype.getName());
//	}
//
//	public String generateFKsQuery(QuerySpec spec, QueryDetails details) {
//		StrCreator sc = new StrCreator();
//		QueryExp exp = spec.queryExp;
//
//		RelationOneRule rule = DRuleHelper.findOneRule(exp.getTypeName(), registry);
//		if (rule == null) {
//			RelationManyRule manyRule = DRuleHelper.findManyRule(exp.getTypeName(), registry);
//			if (manyRule != null) {
//				return generateFKsQueryMany(spec, exp, manyRule, details);
//			}
//			return sqlgen.generateQuery(spec); 
//		}
//
//		Table tbl = genTable(exp.getTypeName());
//		if (!rule.isParent()) {
//			sc.o("SELECT * FROM %s", tbl.name);
//			addWhereClauseIfNeeded(sc, spec, exp.filter, exp.getTypeName(), null);
//			sc.o(";");
//			return sc.str;
//		} 
//		Table tbl2 = genTable(rule.relInfo.farType.getName());
//
//		TypePair nearField = DValueHelper.findPrimaryKeyFieldPair(rule.relInfo.nearType);
//		String fields = genFields(exp.typeName, tbl, tbl2, rule.relInfo.fieldName, nearField);
//		sc.o("SELECT %s FROM %s", fields, tbl.name);
//
//		RelationOneRule farRule = DRuleHelper.findOneRule(rule.relInfo.farType.getName(), registry);
//		String onstr = String.format("%s.%s=%s.%s", tbl2.alias, farRule.relInfo.fieldName, tbl.alias, nearField.name);
//
//		//			TypePair farField = DValueHelper.findPrimaryKeyFieldPair(rule.relInfo.farType);
//		sc.o(" as %s LEFT JOIN %s ON %s", tbl.alias, tbl2.fmtAsStr(), onstr);
//		
//		//Customer[addr = 100]. can't do this because customer is parent and doesn't have fk
//		//must transform into Customer[b.cust
//		WhereExpression express = whereConverter.convert(spec);
//		if (express instanceof WherePhrase) {
//			WherePhrase phrase = (WherePhrase) express;
//			//TODO later also do op2
//			if (phrase.op1.typeDetails.isRelation && phrase.op2.typeDetails.isParent) {
//				WhereOperand replacement = new WhereOperand();
//				replacement.alias = tbl2.alias;
//				TypePair idpair = DValueHelper.findPrimaryKeyFieldPair(phrase.op1.typeDetails.dtype);
//				replacement.exp = new IdentExp(idpair.name);
//				replacement.typeDetails = new TypeDetails();
//				replacement.typeDetails.dtype = idpair.type;
//				
//				if (phrase.op1.isValue) {
//					phrase.op2 = replacement;
//				} else {
//					phrase.op1 = replacement;
//				}
//			}
//			this.queryDetectorSvc.addWhereClauseOpFromPhrase(sc, phrase, tbl);
//		} else {
//			addWhereClauseIfNeeded(sc, spec, exp.filter, exp.getTypeName(), tbl);
//		}
//		sc.o(";");
//		return sc.str;
//	}
//
//	private String generateFKsQueryMany(QuerySpec spec, QueryExp exp, RelationManyRule rule, QueryDetails details) {
//		StrCreator sc = new StrCreator();
//
//		Table tbl = genTable(exp.getTypeName());
//		Table tbl2 = genTable(rule.relInfo.farType.getName());
//
//
//		RelationOneRule farRule = DRuleHelper.findOneRule(rule.relInfo.farType.getName(), registry);
//		if (farRule == null) {
//			RelationManyRule xfarRule = DRuleHelper.findManyRule(rule.relInfo.farType.getName(), registry);
//			return doManyToMany(sc, spec, exp, xfarRule, tbl, tbl2, rule, details);
//		} else {
//			TypePair nearField = DValueHelper.findPrimaryKeyFieldPair(rule.relInfo.nearType);
//			String fields = genFields(exp.typeName, tbl, tbl2, rule.relInfo.fieldName, nearField);
//			sc.o("SELECT %s FROM %s", fields, tbl.name);
//			String onstr = String.format("%s.%s=%s.%s", tbl2.alias, farRule.relInfo.fieldName, tbl.alias, nearField.name);
//
//			//				TypePair farField = DValueHelper.findPrimaryKeyFieldPair(rule.relInfo.farType);
//			sc.o(" as %s LEFT JOIN %s ON %s", tbl.alias, tbl2.fmtAsStr(), onstr);
//			addWhereClauseIfNeeded(sc, spec, exp.filter, exp.getTypeName(), tbl);
//			sc.o(";");
//
//			details.mergeRows = true;
//			details.mergeOnField = rule.relInfo.fieldName;
//		}
//
//		return sc.str;
//	}
//
//	private String doManyToMany(StrCreator sc, QuerySpec spec, QueryExp exp, RelationManyRule farRule, Table tbl, Table tbl2, RelationManyRule otherRule, QueryDetails details) {
//		RelationInfo info = farRule.relInfo;
//		TableInfo tblinfo = findTableInfo(info.nearType, info.farType);
//
//		String assocField = tblinfo.tbl1.equals(tbl.name) ? "rightv" : "leftv";
//		String assocField2 = tblinfo.tbl1.equals(tbl.name) ? "leftv" : "rightv";
//		genJoin(sc, spec, info, tblinfo, tbl, otherRule, assocField, assocField2, exp);
//		details.mergeRows = true;
//		details.mergeOnField = otherRule.relInfo.fieldName;
//		return sc.str;
//	}
//
//	private void genJoin(StrCreator sc, QuerySpec spec, RelationInfo info, TableInfo tblinfo, Table tbl, RelationManyRule otherRule, String assocField, String assocField2, QueryExp exp) {
//		Table tblAssoc = genTable(tblinfo.assocTblName);
//		String typeName = info.farType.getName();
//		TypePair copy = new TypePair(assocField, null);
//		String fields = genFields(typeName, tbl, tblAssoc, otherRule.relInfo.fieldName, copy);
//		sc.o("SELECT %s FROM %s", fields, tbl.name);
//
//		TypePair pair = DValueHelper.findPrimaryKeyFieldPair(info.farType);
//		String onstr = String.format("%s.%s=%s.%s", tbl.alias, pair.name, tblAssoc.alias, assocField2);
//		sc.o(" as %s LEFT JOIN %s as %s ON %s", tbl.alias, tblAssoc.name, tblAssoc.alias, onstr);
//
//		addWhereClauseIfNeeded(sc, spec, exp.filter, typeName, tbl);
//		sc.o(";");
//	}
//	
//	private void addWhereClauseIfNeeded(StrCreator sc, QuerySpec spec, FilterExp filter, String typeName, Table tbl) {
//		if (filter == null) {
//			return;
//		}
//		
//		QueryType queryType = queryDetectorSvc.detectQueryType(spec);
//		switch(queryType) {
//		case ALL_ROWS:
//			break;
//		case OP:
//			addWhereClauseOp(sc, spec, typeName, tbl);
//			break;
//		case PRIMARY_KEY:
//		default:
//			addWhereClausePrimaryKey(sc, spec.queryExp.filter, typeName, tbl);
//			break;
//		}
//	}
//	
//	private void addWhereClauseOp(StrCreator sc, QuerySpec spec, String typeName, Table tbl) {
//		queryDetectorSvc.addWhereClauseOp(sc, spec, typeName, tbl);
//	}
//
//	private TableInfo findTableInfo(DStructType nearType, DStructType farType) {
//		for(TableInfo tblinfo: this.tblinfoL) {
//			if (tblinfo.assocTblName != null) {
//				if (tblinfo.tbl1.equals(nearType.getName()) &&
//						tblinfo.tbl2.equals(farType.getName())) {
//					return tblinfo;
//				}
//
//				if (tblinfo.tbl2.equals(nearType.getName()) &&
//						tblinfo.tbl1.equals(farType.getName())) {
//					return tblinfo;
//				}
//			}
//		}
//		return null;
//	}
//
//	private String genFields(String typeName, Table tbl, Table tbl2, String fieldName, TypePair nearField) {
//		//			sql = "SELECT c.id,a.id as addr FROM Customer as c JOIN Address as a ON a.cust=c.id WHERE c.id=55";
//		StringJoiner joiner = new StringJoiner(",");
//		DStructType structType = (DStructType) registry.getType(typeName);
//		for(TypePair pair: structType.getAllFields()) {
//			if (pair.type.isStructShape()) {
//				String s = String.format("%s.%s as %s", tbl2.alias, nearField.name, fieldName);
//				joiner.add(s);
//			} else {
//				String s = String.format("%s.%s", tbl.alias, pair.name);
//				joiner.add(s);
//			}
//
//		}
//		return joiner.toString();
//	}
//
//	private void addWhereClausePrimaryKey(StrCreator sc, FilterExp filter, String typeName, Table tbl) {
//		if (filter != null) {
//			DStructType type = (DStructType) registry.findTypeOrSchemaVersionType(typeName);
//			String keyField = DValueHelper.findUniqueField(type);
//			if (keyField == null) {
//				//err!!
//				return;
//			}
//
//			DType inner = sqlgen.findFieldType(type, keyField);
//			String val = sqlgen.valueInSql(inner.getShape(), filter.cond.strValue());
//			if (tbl == null) {
//				sc.o(" WHERE %s=%s", keyField, val);
//			} else {
//				sc.o(" WHERE %s.%s=%s", tbl.alias, keyField, val);
//			}
//		}
//	}
//}