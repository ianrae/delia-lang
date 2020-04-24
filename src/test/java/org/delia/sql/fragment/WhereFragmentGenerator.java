package org.delia.sql.fragment;

import java.util.List;
import java.util.StringJoiner;

import org.apache.commons.collections.CollectionUtils;
import org.delia.compiler.ast.Exp;
import org.delia.compiler.ast.FilterExp;
import org.delia.compiler.ast.FilterOpFullExp;
import org.delia.compiler.ast.IdentExp;
import org.delia.compiler.ast.QueryExp;
import org.delia.compiler.astx.XNAFMultiExp;
import org.delia.compiler.astx.XNAFNameExp;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.db.QuerySpec;
import org.delia.db.ValueHelper;
import org.delia.db.memdb.filter.filterfn.FilterFnRunner;
import org.delia.db.sql.QueryType;
import org.delia.db.sql.QueryTypeDetector;
import org.delia.db.sql.StrCreator;
import org.delia.db.sql.Table;
import org.delia.db.sql.prepared.SqlStatement;
import org.delia.db.sql.where.InPhrase;
import org.delia.db.sql.where.LogicalPhrase;
import org.delia.db.sql.where.SqlWhereConverter;
import org.delia.db.sql.where.WhereExpression;
import org.delia.db.sql.where.WhereOperand;
import org.delia.db.sql.where.WherePhrase;
import org.delia.runner.VarEvaluator;
import org.delia.sql.fragment.FragmentParserTests.OpFragment;
import org.delia.sql.fragment.FragmentParserTests.SelectStatementFragment;
import org.delia.type.BuiltInTypes;
import org.delia.type.DStructType;
import org.delia.type.DType;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.type.Shape;
import org.delia.type.TypePair;
import org.delia.util.DValueHelper;
import org.delia.util.DeliaExceptionHelper;
import org.delia.valuebuilder.ScalarValueBuilder;

public class WhereFragmentGenerator extends ServiceBase {
	private DTypeRegistry registry;
	private QueryTypeDetector queryDetectorSvc;
	private ScalarValueBuilder dvalBuilder;
	private SqlWhereConverter whereConverter;
	private FilterFnRunner filterRunner;
	private ValueHelper valueHelper;
	private VarEvaluator varEvaluator;

	public WhereFragmentGenerator(FactoryService factorySvc, DTypeRegistry registry, VarEvaluator varEvaluator) {
		super(factorySvc);
		this.registry = registry;
		this.queryDetectorSvc = new QueryTypeDetector(factorySvc, registry);
		this.dvalBuilder = factorySvc.createScalarValueBuilder(registry);
		this.whereConverter = new SqlWhereConverter(factorySvc, registry, queryDetectorSvc);
		this.filterRunner = new FilterFnRunner(registry);
		this.valueHelper = new ValueHelper(factorySvc);
		this.varEvaluator = varEvaluator;
	}


	protected void addWhereClauseOp(StrCreator sc, QuerySpec spec, String typeName, Table tbl, SqlStatement statement) {
		doAddWhereClauseOp(sc, spec, typeName, tbl, statement);
	}

	public void addWhereClausePrimaryKey(QuerySpec spec, FilterExp filter, DStructType structType, SelectStatementFragment selectFrag) {
		if (filter != null) {
			TypePair keyPair = DValueHelper.findPrimaryKeyFieldPair(structType);
			if (keyPair == null) {
				//err!!
				DeliaExceptionHelper.throwError("no-primary-key", "Type '%s' has no primary key. Cannot do query by primary key", structType.getName());
				return;
			}
			SqlStatement statement = selectFrag.statement;
			
			DValue dval = null;
			if (filter.cond instanceof IdentExp) {
				IdentExp vv = (IdentExp) filter.cond;
				String varName = vv.name();
				List<DValue> dvalL = varEvaluator.lookupVar(varName);
				if (dvalL == null) {
					String msg = String.format("unknown var '%s' in primaryKey filter", varName);
					DeliaExceptionHelper.throwError("unknown-var", msg);
				} else if (dvalL.size() > 1) {
					String msg = String.format("too many values (%d) in var '%s' in primaryKey filter", dvalL.size(), varName);
					DeliaExceptionHelper.throwError("to-many-primary-key-vaues", msg);
				} else {
					dval = dvalL.get(0);
				}
			} else {
				dval = valueInSql(keyPair.type.getShape(), filter.cond.strValue());
			}
			
			statement.paramL.add(dval);
			OpFragment opFrag = new OpFragment("=");
			opFrag.left = FragmentHelper.buildFieldFrag(structType, selectFrag, keyPair);
			opFrag.right = FragmentHelper.buildParam(selectFrag);
			selectFrag.whereL.add(opFrag);
		}
	}
	
	protected String whereWord(QuerySpec spec) {
//		if (this.selectFnHelper.isExistsPresent(spec)) {
////			return " WHERE EXISTS";
//			return " WHERE EXISTS";
//		} else {
			return " WHERE ";
//		}
	}
	

	protected DValue valueInSql(Shape shape, Object value) {
		return valueHelper.valueInSql(shape, value, registry);
	}

	protected DType findFieldType(DStructType dtype, String fieldName) {
		for(TypePair pair: dtype.getAllFields()) {
			if (pair.name.equals(fieldName)) {
				return pair.type;
			}
		}
		return null;
	}

	
	//------------------------------
	protected void doAddWhereClauseOp(StrCreator sc, QuerySpec spec, String typeName, Table tbl, SqlStatement statement) {
		FilterExp filter = spec.queryExp.filter;
		
		if (filter != null && filter.cond instanceof FilterOpFullExp) {
			WhereExpression express = whereConverter.addWhereClauseOp(filter, typeName);
			if (express != null) {
				addWhereClauseOpFromPhrase(sc, spec, express, tbl, statement);
			}
		} else {
			sc.o("JJJJJJJJJJJJJJJ"); //TODO
		}
	}
	public void addWhereClauseOpFromPhrase(StrCreator sc, QuerySpec spec, WhereExpression express, Table tbl, SqlStatement statement) {
		String s = whereWord(spec);
		if (express instanceof WherePhrase) {
			s += doWherePhrase(sc, (WherePhrase) express, tbl, statement);
		} else if (express instanceof LogicalPhrase) {
			s += doLogicalPhrase(sc, (LogicalPhrase) express, tbl, statement);
		} else if (express instanceof InPhrase) {
			s += doInPhrase(sc, (InPhrase) express, tbl, statement);
		}
		//TODO: others??
		
		sc.addStr(s);
	}
	protected String doInPhrase(StrCreator sc, InPhrase phrase, Table tbl, SqlStatement statement) {
		String op1 = operandToSql(phrase.op1, statement);
		StringJoiner joiner = new StringJoiner(",");
		for(Exp exp: phrase.valueL) {
//			joiner.add(exp.strValue());
//			String s = operandToSql(phrase.op1, statement);
			
			WhereOperand tmp = new WhereOperand();
			tmp.exp = exp;
			tmp.isValue = true;
			tmp.typeDetails = phrase.op1.typeDetails;
			String s = operandToSql(tmp, statement);
			joiner.add(s);
		}

		if (tbl == null) {
			return String.format("%s IN (%s)", op1, joiner.toString());
		} else {
			//TODO: how do we know which op1 or op2 needs the alias???
			String alias = phrase.op1.alias == null ? tbl.alias : phrase.op1.alias; 
			Table tmp = new Table(alias, op1);
			return String.format("%s IN (%s)", tmp.toString(), joiner.toString());
		}
	}

	protected String doLogicalPhrase(StrCreator sc, LogicalPhrase lphrase, Table tbl, SqlStatement statement) {
		String s1 = doWherePhrase(sc, (WherePhrase) lphrase.express1, tbl, statement);
		String s2 = doWherePhrase(sc, (WherePhrase) lphrase.express2, tbl, statement);
		
		String sand = lphrase.isAnd ? "and" : "or";
		String s = String.format("%s %s %s", s1, sand, s2);
		return s;
	}

	protected String doWherePhrase(StrCreator sc, WherePhrase phrase, Table tbl, SqlStatement statement) {
		String op = opToSql(phrase.op);
		adjustYearStuff(phrase.op1, phrase.op2);
		String op1 = operandToSql(phrase.op1, statement);
		String op2 = operandToSql(phrase.op2, statement);
		
		String snot = (phrase.notFlag) ? "NOT " : "";
		if (tbl == null) {
			return String.format("%s%s %s %s", snot, op1, op, op2);
		} else {
			String alias;
			if (!phrase.op1.isValue) {
				alias = phrase.op1.alias == null ? tbl.alias : phrase.op1.alias; 
				Table tmp = new Table(alias, op1);
				return String.format("%s%s %s %s", snot, tmp.toString(), op, op2);
			} else {
				alias = phrase.op2.alias == null ? tbl.alias : phrase.op2.alias; 
				Table tmp = new Table(alias, op2);
				return String.format("%s%s %s %s", snot, op1, op, tmp.toString());
			}
		}
	}


	protected String opToSql(String op) {
		switch(op) {
		case "==":
			return "=";
		case "like":
			return "LIKE";
		default:
			return op;
		}
	}
	protected String operandToSql(WhereOperand val, SqlStatement statement) {
		if (!val.isValue) {
			if (val.fnName != null) {
				return doFn(val);
			}
			return getColumnName(val.exp);
		}
		
		if (val.typeDetails.dtype != null && val.typeDetails.dtype.isShape(Shape.DATE)) {
			String s = val.exp.strValue(); //dateGenerator.convertDateStringToSQLTimestamp(val.exp.strValue());
			statement.paramL.add(dvalBuilder.buildDate(s));
			return "?";
		}
		
//		if (val.exp instanceof StringExp) {
////			String s = String.format("'%s'", val.exp.strValue());
//			statement.paramL.add(dvalBuilder.buildString(val.exp.strValue()));
//			return "?";
//		}
		
		Object obj = extractObj(val.exp);
		DValue dval = valueInSql(val.typeDetails.dtype.getShape(), obj);
		statement.paramL.add(dval);
		return "?";
	}
	
	protected Object extractObj(Exp exp) {
		return valueHelper.extractObj(exp);
	}
	

	protected void adjustYearStuff(WhereOperand op1, WhereOperand op2) {
		if (!op1.isValue) {
			if (filterRunner.isDateFn(op1.fnName)) {
				op2.typeDetails.dtype = registry.getType(BuiltInTypes.INTEGER_SHAPE);
			}
		}
	}
	protected String doFn(WhereOperand val) {
		//("yyyy-MM-dd'T'HH:mm:ss");
		switch(val.fnName) {
		case "year":
		{
			String s = this.getColumnName(val.exp);
			return String.format("FORMATDATETIME(%s, 'yyyy')", s);
		}
		case "month":
		{
			String s = this.getColumnName(val.exp);
			return String.format("FORMATDATETIME(%s, 'MM')", s);
		}
		case "day":
		{
			String s = this.getColumnName(val.exp);
			return String.format("FORMATDATETIME(%s, 'dd')", s);
		}
		case "hour":
		{
			String s = this.getColumnName(val.exp);
			return String.format("FORMATDATETIME(%s, 'HH')", s);
		}
		case "minute":
		{
			String s = this.getColumnName(val.exp);
			return String.format("FORMATDATETIME(%s, 'mm')", s);
		}
		case "second":
		{
			String s = this.getColumnName(val.exp);
			return String.format("FORMATDATETIME(%s, 'ss')", s);
		}
		default:
			//err!!
			break;
		}
		return "KKKKKKKKKKK"; //TODO fix
	}
	
	protected String getColumnName(Exp op1) {
		if (op1 instanceof XNAFMultiExp) {
			XNAFMultiExp mexp = (XNAFMultiExp) op1;
			if (!CollectionUtils.isEmpty(mexp.qfeL)) {
				XNAFNameExp nexp = (XNAFNameExp) mexp.qfeL.get(0);
				String fieldName = nexp.funcName;
				return fieldName;
			}
		} else if (op1 instanceof IdentExp) {
			return op1.strValue();
		}
		return null;
	}
	
	
}