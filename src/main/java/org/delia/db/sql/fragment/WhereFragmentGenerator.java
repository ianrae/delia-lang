//package org.delia.db.sql.fragment;
//
//import java.util.List;
//import java.util.StringJoiner;
//
//import org.apache.commons.collections.CollectionUtils;
//import org.delia.assoc.DatIdMap;
//import org.delia.compiler.ast.Exp;
//import org.delia.compiler.ast.FilterExp;
//import org.delia.compiler.ast.FilterOpFullExp;
//import org.delia.compiler.ast.IdentExp;
//import org.delia.compiler.astx.XNAFMultiExp;
//import org.delia.compiler.astx.XNAFNameExp;
//import org.delia.core.FactoryService;
//import org.delia.core.ServiceBase;
//import org.delia.db.QuerySpec;
//import org.delia.db.SqlStatement;
//import org.delia.db.ValueHelper;
//import org.delia.db.memdb.filter.filterfn.FilterFnRunner;
//import org.delia.db.sql.QueryTypeDetector;
//import org.delia.db.sql.where.InPhrase;
//import org.delia.db.sql.where.LogicalPhrase;
//import org.delia.db.sql.where.SqlWhereConverter;
//import org.delia.db.sql.where.WhereExpression;
//import org.delia.db.sql.where.WhereOperand;
//import org.delia.db.sql.where.WherePhrase;
//import org.delia.relation.RelationInfo;
//import org.delia.rule.rules.RelationManyRule;
//import org.delia.rule.rules.RelationOneRule;
//import org.delia.runner.VarEvaluator;
//import org.delia.type.BuiltInTypes;
//import org.delia.type.DStructType;
//import org.delia.type.DType;
//import org.delia.type.DTypeRegistry;
//import org.delia.type.DValue;
//import org.delia.type.Shape;
//import org.delia.type.TypePair;
//import org.delia.util.DRuleHelper;
//import org.delia.util.DValueHelper;
//import org.delia.util.DeliaExceptionHelper;
//import org.delia.valuebuilder.ScalarValueBuilder;
//
//public class WhereFragmentGenerator extends ServiceBase {
//	private DTypeRegistry registry;
//	private QueryTypeDetector queryDetectorSvc;
//	private ScalarValueBuilder dvalBuilder;
//	private SqlWhereConverter whereConverter;
//	private FilterFnRunner filterRunner;
//	private ValueHelper valueHelper;
//	private VarEvaluator varEvaluator;
//	public TableFragmentMaker tableFragmentMaker;
////	private DatIdMap datIdMap;
//
//	public WhereFragmentGenerator(FactoryService factorySvc, DTypeRegistry registry, VarEvaluator varEvaluator, DatIdMap datIdMap) {
//		super(factorySvc);
//		this.registry = registry;
//		this.queryDetectorSvc = new QueryTypeDetector(factorySvc, registry);
//		this.dvalBuilder = factorySvc.createScalarValueBuilder(registry);
//		this.whereConverter = new SqlWhereConverter(factorySvc, registry, queryDetectorSvc, varEvaluator);
//		this.filterRunner = new FilterFnRunner(registry);
//		this.valueHelper = new ValueHelper(factorySvc);
//		this.varEvaluator = varEvaluator;
////		this.datIdMap = datIdMap;
//	}
//
//
//	public void addWhereClauseOp(QuerySpec spec, DStructType structType, StatementFragmentBase selectFrag) {
//		doAddWhereClauseOp(spec, structType, selectFrag.statement, selectFrag);
//	}
//
//	public void addWhereClausePrimaryKey(QuerySpec spec, FilterExp filter, DStructType structType, StatementFragmentBase selectFrag) {
//		if (filter != null) {
//			TypePair keyPair = DValueHelper.findPrimaryKeyFieldPair(structType);
//			if (keyPair == null) {
//				//err!!
//				DeliaExceptionHelper.throwError("no-primary-key", "Type '%s' has no primary key. Cannot do query by primary key", structType.getName());
//				return;
//			}
//			SqlStatement statement = selectFrag.statement;
//			
//			DValue dval = null;
//			if (filter.cond instanceof IdentExp) {
//				IdentExp vv = (IdentExp) filter.cond;
//				String varName = vv.name();
//				List<DValue> dvalL = varEvaluator.lookupVar(varName);
//				if (dvalL == null) {
//					String msg = String.format("unknown var '%s' in primaryKey filter", varName);
//					DeliaExceptionHelper.throwError("unknown-var", msg);
//				} else if (dvalL.size() > 1) {
//					String msg = String.format("too many values (%d) in var '%s' in primaryKey filter", dvalL.size(), varName);
//					DeliaExceptionHelper.throwError("to-many-primary-key-vaues", msg);
//				} else {
//					dval = dvalL.get(0);
//				}
//			} else {
//				dval = valueInSql(keyPair.type.getShape(), filter.cond.strValue());
//			}
//			
//			statement.paramL.add(dval);
//			OpFragment opFrag = new OpFragment("=");
//			opFrag.left = FragmentHelper.buildFieldFrag(structType, selectFrag, keyPair);
//			opFrag.right = FragmentHelper.buildParam(selectFrag);
//			selectFrag.whereL.add(opFrag);
//		}
//	}
//	
//	protected DValue valueInSql(Shape shape, Object value) {
//		return valueHelper.valueInSql(shape, value, registry);
//	}
//
//	protected DType findFieldType(DStructType dtype, String fieldName) {
//		for(TypePair pair: dtype.getAllFields()) {
//			if (pair.name.equals(fieldName)) {
//				return pair.type;
//			}
//		}
//		return null;
//	}
//
//	
//	//------------------------------
//	protected void doAddWhereClauseOp(QuerySpec spec, DStructType structType, SqlStatement statement, StatementFragmentBase selectFrag) {
//		FilterExp filter = spec.queryExp.filter;
//		String typeName = structType.getName();
//		
//		if (filter != null && filter.cond instanceof FilterOpFullExp) {
//			WhereExpression express = whereConverter.addWhereClauseOp(filter, typeName);
//			if (express != null) {
//				addWhereClauseOpFromPhrase(spec, structType, express, statement, selectFrag);
//			}
//		} else {
//			String s = filter == null ? "null" : filter.getClass().getSimpleName();
//			DeliaExceptionHelper.throwError("bad-where-filter", "unknown filter class '%s'", s);
//		}
//	}
//
//
//	public void addWhereClauseOpFromPhrase(QuerySpec spec, DStructType structType, WhereExpression express, SqlStatement statement, StatementFragmentBase selectFrag) {
//		OpFragment opFrag = null;
//		
//		if (express instanceof WherePhrase) {
//			opFrag = doWherePhrase((WherePhrase) express, statement, selectFrag);
//		} else if (express instanceof LogicalPhrase) {
//			opFrag = doLogicalPhrase((LogicalPhrase) express, statement, selectFrag);
//		} else if (express instanceof InPhrase) {
//			opFrag = doInPhrase((InPhrase) express, structType, statement, selectFrag);
//		}
//		
//		if (opFrag != null) {
//			selectFrag.whereL.add(opFrag);
//		}
//	}
//	protected OpFragment doInPhrase(InPhrase phrase, DStructType structType, SqlStatement statement, StatementFragmentBase selectFrag) {
//		String op1 = operandToSql(phrase.op1, statement);
//		OpFragment opFK = handleInPhraseFK(structType, phrase, op1, selectFrag);
//		if (opFK != null) {
//			return opFK;
//		}
//		
//		StringJoiner joiner = new StringJoiner(",");
//		for(Exp exp: phrase.valueL) {
//			WhereOperand tmp = new WhereOperand();
//			tmp.exp = exp;
//			tmp.typeDetails = phrase.op1.typeDetails;
//			if (exp instanceof IdentExp) {
//				tmp.isValue = false;
//			} else {
//				tmp.isValue = true;
//			}
//			String s = operandToSql(tmp, statement);
//			joiner.add(s);
//		}
//
//		TableFragment tbl = selectFrag.tblFrag;
//		if (tbl == null) {
//			return null; 
//		} else {
//			String alias = phrase.op1.alias == null ? tbl.alias : phrase.op1.alias; 
//			String tmp = String.format("(%s)", joiner.toString());
//			
//			OpFragment opFrag = new OpFragment("IN");
//			opFrag.left = FragmentHelper.buildAliasedFrag(alias, op1);
//			opFrag.right = FragmentHelper.buildAliasedFrag(null, tmp);
//			return opFrag;
//		}
//	}
//	private OpFragment handleInPhraseFK(DStructType structType, InPhrase inphrase, String op1, StatementFragmentBase selectFrag) {
//		for(Exp exp: inphrase.valueL) {
//			if (exp instanceof IdentExp) {
//				TypePair pair = DValueHelper.findField(structType, exp.strValue());
//				if (pair != null) {
//					RelationInfo info = DRuleHelper.findMatchingRuleInfo(structType, pair);
//					if (info != null) {
//						OpFragment opFrag = new OpFragment("=");
//						opFrag.left = FragmentHelper.buildAliasedFrag(null, op1);
//						
//						FieldFragment ff = new FieldFragment();
//						ff.name = exp.strValue();
//						ff.structType = structType;
//						ff.alias = "zzz"; //tbl.alias;
////						selectFrag.hlsRemapList.add(ff);
//						opFrag.right = ff; //FragmentHelper.buildAliasedFrag(null, exp.strValue());
//						
//						return opFrag; //FUTURE handle more than one later. eg [followers,otherField]
//					}
//				}
//			}
//		}
//		return null;
//	}
//
//	protected OpFragment doLogicalPhrase(LogicalPhrase lphrase, SqlStatement statement, StatementFragmentBase selectFrag) {
//		OpFragment frag1 = doWherePhrase((WherePhrase) lphrase.express1, statement, selectFrag);
//		OpFragment frag2 = doWherePhrase((WherePhrase) lphrase.express2, statement, selectFrag);
//		
//		String sand = lphrase.isAnd ? "and" : "or";
//		
//		OpFragment opFrag = new OpFragment(sand);
//		opFrag.left = FragmentHelper.buildAliasedFrag(null, frag1.render());
//		opFrag.right = FragmentHelper.buildAliasedFrag(null, frag2.render());
//		return opFrag;
//	}
//
//	protected OpFragment doWherePhrase(WherePhrase phrase, SqlStatement statement, StatementFragmentBase selectFrag) {
//		String op = opToSql(phrase.op);
//		adjustYearStuff(phrase.op1, phrase.op2);
//
//		TableFragment tbl = selectFrag.tblFrag;
//		setPhraseAliasIfNeeded(phrase.op1, tbl);
//		setPhraseAliasIfNeeded(phrase.op2, tbl);
//		doImplicitFetchIfNeeded(phrase.op1, tbl, selectFrag);
//		doImplicitFetchIfNeeded(phrase.op2, tbl, selectFrag);
//		
//		String op1 = operandToSql(phrase.op1, statement);
//		String op2 = operandToSql(phrase.op2, statement);
//		
//		String snot = (phrase.notFlag) ? "NOT " : "";
//		if (tbl == null) {
//			return null; 
//		} else {
//			String alias;
//			if (!phrase.op1.isValue) {
//				alias = phrase.op1.alias == null ? tbl.alias : phrase.op1.alias; 
//				if (IsFn(phrase.op1)) {
//					alias = null;
//				}
//				
//				OpFragment opFrag = new OpFragment(op);
//				opFrag.left = FragmentHelper.buildAliasedFrag(alias, op1);
//				opFrag.leftNot = phrase.notFlag;
//				opFrag.right = FragmentHelper.buildAliasedFrag(null, op2);
//				return opFrag;
//			} else {
//				alias = phrase.op2.alias == null ? tbl.alias : phrase.op2.alias; 
//				if (IsFn(phrase.op2)) {
//					alias = null;
//				}
//				
//				OpFragment opFrag = new OpFragment(op);
//				opFrag.left = FragmentHelper.buildAliasedFrag(null, op1);
//				opFrag.right = FragmentHelper.buildAliasedFrag(alias, snot + op2);
//				opFrag.rightNot = phrase.notFlag;
//				return opFrag;
//			}
//		}
//	}
//
//
//	private void doImplicitFetchIfNeeded(WhereOperand op1, TableFragment tbl, StatementFragmentBase selectFrag) {
//		String possibleFieldName = this.getColumnName(op1.exp);
//		if (possibleFieldName != null) {
//			if (DValueHelper.fieldExists(tbl.structType, possibleFieldName)) {
//				RelationOneRule oneRule = DRuleHelper.findOneRule(tbl.structType, possibleFieldName);
//				if (oneRule != null && oneRule.relInfo.isParent) {
//					
//					//hls needs this
//					FieldFragment ff = new FieldFragment();
//					ff.name = possibleFieldName;
//					ff.structType = tbl.structType;
//					ff.alias = tbl.alias;
//					selectFrag.hlsRemapList.add(ff);
//					
//					DStructType farType = oneRule.relInfo.farType;
//					TableFragment otherTbl = selectFrag.findByTableName(farType.getName());
//					if (otherTbl == null) {
//						log.log("implicit(one) fetch %s.%s", farType.getName(), possibleFieldName);
//						tableFragmentMaker.createTable(farType, selectFrag);
//					}
//				}
//				
//				RelationManyRule manyRule = DRuleHelper.findManyRule(tbl.structType, possibleFieldName);
////				if (manyRule != null && manyRule.relInfo.isParent) {
//				if (manyRule != null) {
//					DStructType farType = manyRule.relInfo.farType;
//					
//					//hls needs this
//					FieldFragment ff = new FieldFragment();
//					ff.name = possibleFieldName;
//					ff.structType = tbl.structType;
//					ff.alias = tbl.alias;
//					selectFrag.hlsRemapList.add(ff);
//					
//					
//					TableFragment otherTbl = selectFrag.findByTableName(farType.getName());
//					if (otherTbl == null) {
//						log.log("implicit(many) fetch %s.%s", farType.getName(), possibleFieldName);
//						tableFragmentMaker.createTable(farType, selectFrag);
//					}
//				}
//			}
//		}
//	}
//
//
//	private void setPhraseAliasIfNeeded(WhereOperand op1, TableFragment tbl) {
//		if (op1.alias == null) {
//			op1.alias = tbl.alias; 
//		}
//	}
//
//
//	protected String opToSql(String op) {
//		switch(op) {
//		case "==":
//			return "=";
//		case "like":
//			return "LIKE";
//		default:
//			return op;
//		}
//	}
//	
//	protected boolean IsFn(WhereOperand val) {
//		if (!val.isValue) {
//			if (val.fnName != null) {
//				return true;
//			}
//		}
//		return false;
//	}
//	protected String operandToSql(WhereOperand val, SqlStatement statement) {
//		if (!val.isValue) {
//			if (val.fnName != null) {
//				return doFn(val);
//			}
//			return getColumnName(val.exp);
//		}
//		
//		if (val.typeDetails.dtype != null && val.typeDetails.dtype.isShape(Shape.DATE)) {
//			String s = val.exp.strValue(); //dateGenerator.convertDateStringToSQLTimestamp(val.exp.strValue());
//			statement.paramL.add(dvalBuilder.buildDate(s));
//			return "?";
//		}
//		
//		Object obj = extractObj(val.exp);
//		DValue dval = valueInSql(val.typeDetails.dtype.getShape(), obj);
//		statement.paramL.add(dval);
//		return "?";
//	}
//	
//	protected Object extractObj(Exp exp) {
//		return valueHelper.extractObj(exp);
//	}
//	
//
//	protected void adjustYearStuff(WhereOperand op1, WhereOperand op2) {
//		if (!op1.isValue) {
//			if (filterRunner.isDateFn(op1.fnName)) {
//				op2.typeDetails.dtype = registry.getType(BuiltInTypes.INTEGER_SHAPE);
//			}
//		}
//	}
//	protected String doFn(WhereOperand val) {
//		String alias = val.alias == null ? "" : val.alias + ".";
//		
//		//("yyyy-MM-dd'T'HH:mm:ss");
//		switch(val.fnName) {
//		case "year":
//		{
//			String s = this.getColumnName(val.exp);
//			return String.format("FORMATDATETIME(%s%s, 'yyyy')", alias, s);
//		}
//		case "month":
//		{
//			String s = this.getColumnName(val.exp);
//			return String.format("FORMATDATETIME(%s%s, 'MM')", alias, s);
//		}
//		case "day":
//		{
//			String s = this.getColumnName(val.exp);
//			return String.format("FORMATDATETIME(%s%s, 'dd')", alias, s);
//		}
//		case "hour":
//		{
//			String s = this.getColumnName(val.exp);
//			return String.format("FORMATDATETIME(%s%s, 'HH')", alias, s);
//		}
//		case "minute":
//		{
//			String s = this.getColumnName(val.exp);
//			return String.format("FORMATDATETIME(%s%s, 'mm')", alias, s);
//		}
//		case "second":
//		{
//			String s = this.getColumnName(val.exp);
//			return String.format("FORMATDATETIME(%s%s, 'ss')", alias, s);
//		}
//		default:
//			//err!!
//			break;
//		}
//		DeliaExceptionHelper.throwError("unknown-where-function", "Unknown filter function '%s'", val.fnName);
//		return null;
//	}
//	
//	protected String getColumnName(Exp op1) {
//		if (op1 instanceof XNAFMultiExp) {
//			XNAFMultiExp mexp = (XNAFMultiExp) op1;
//			if (!CollectionUtils.isEmpty(mexp.qfeL)) {
//				XNAFNameExp nexp = (XNAFNameExp) mexp.qfeL.get(0);
//				String fieldName = nexp.funcName;
//				return fieldName;
//			}
//		} else if (op1 instanceof IdentExp) {
//			return op1.strValue();
//		}
//		return null;
//	}
//	
//	
//}