package org.delia.db.sql;

import java.util.StringJoiner;

import org.apache.commons.collections.CollectionUtils;
import org.delia.compiler.ast.BooleanExp;
import org.delia.compiler.ast.Exp;
import org.delia.compiler.ast.FilterOpFullExp;
import org.delia.compiler.ast.IdentExp;
import org.delia.compiler.ast.StringExp;
import org.delia.compiler.astx.XNAFMultiExp;
import org.delia.compiler.astx.XNAFNameExp;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.db.QuerySpec;
import org.delia.db.memdb.filter.filterfn.FilterFnRunner;
import org.delia.db.sql.where.InPhrase;
import org.delia.db.sql.where.LogicalPhrase;
import org.delia.db.sql.where.SqlWhereConverter;
import org.delia.db.sql.where.WhereExpression;
import org.delia.db.sql.where.WhereOperand;
import org.delia.db.sql.where.WherePhrase;
import org.delia.type.BuiltInTypes;
import org.delia.type.DTypeRegistry;
import org.delia.type.Shape;

public class QueryTypeDetector extends ServiceBase {

	private DTypeRegistry registry;
	private SqlDateGenerator dateGenerator;
	private SqlWhereConverter whereConverter;
	private FilterFnRunner filterRunner;

	public QueryTypeDetector(FactoryService factorySvc, DTypeRegistry registry) {
		super(factorySvc);
		this.registry = registry;
		this.dateGenerator = new SqlDateGenerator(factorySvc, registry);
		this.whereConverter = new SqlWhereConverter(factorySvc, registry, this);
		this.filterRunner = new FilterFnRunner(registry);
	}

	public QueryType detectQueryType(QuerySpec spec) {
		if (wantsAllRows(spec)) { 
			return QueryType.ALL_ROWS;
		} else if (spec.queryExp.filter.cond instanceof FilterOpFullExp) {
			return QueryType.OP;
		} else {
			return QueryType.PRIMARY_KEY;
		}
	}

	private boolean wantsAllRows(QuerySpec spec) {
		if (spec.queryExp.filter == null) {
			return true;
		} else if (spec.queryExp.filter.cond instanceof BooleanExp) {
			BooleanExp exp = (BooleanExp) spec.queryExp.filter.cond;
			return exp.val;
		}
		return false;
	}
	
	public void addWhereClauseOpFromPhrase(StrCreator sc, WhereExpression express, Table tbl) {
		String s = " WHERE ";
		if (express instanceof WherePhrase) {
			s += doWherePhrase(sc, (WherePhrase) express, tbl);
		} else if (express instanceof LogicalPhrase) {
			s += doLogicalPhrase(sc, (LogicalPhrase) express, tbl);
		} else if (express instanceof InPhrase) {
			s += doInPhrase(sc, (InPhrase) express, tbl);
		}
		//TODO: others??
		
		sc.addStr(s);
	}
	private String doInPhrase(StrCreator sc, InPhrase phrase, Table tbl) {
		String op1 = operandToSql(phrase.op1);
		StringJoiner joiner = new StringJoiner(",");
		for(Exp exp: phrase.valueL) {
			joiner.add(exp.strValue());
		}

		if (tbl == null) {
			return String.format("%s IN (%s)", op1, joiner.toString());
		} else {
			//TODO: how do we know which op1 or op2 needs the alias???
			String alias = phrase.op1.alias == null ? tbl.alias : phrase.op1.alias; 
			return String.format("%s.%s IN (%s)", alias, op1, joiner.toString());
		}
	}

	private String doLogicalPhrase(StrCreator sc, LogicalPhrase lphrase, Table tbl) {
		String s1 = doWherePhrase(sc, (WherePhrase) lphrase.express1, tbl);
		String s2 = doWherePhrase(sc, (WherePhrase) lphrase.express2, tbl);
		
		String sand = lphrase.isAnd ? "and" : "or";
		String s = String.format("%s %s %s", s1, sand, s2);
		return s;
	}

	private String doWherePhrase(StrCreator sc, WherePhrase phrase, Table tbl) {
		String op = opToSql(phrase.op);
		adjustYearStuff(phrase.op1, phrase.op2);
		String op1 = operandToSql(phrase.op1);
		String op2 = operandToSql(phrase.op2);
		
		String snot = (phrase.notFlag) ? "NOT " : "";
		if (tbl == null) {
			return String.format("%s%s %s %s", snot, op1, op, op2);
		} else {
			String alias;
			if (!phrase.op1.isValue) {
				alias = phrase.op1.alias == null ? tbl.alias : phrase.op1.alias; 
				return String.format("%s%s.%s %s %s", snot, alias, op1, op, op2);
			} else {
				alias = phrase.op2.alias == null ? tbl.alias : phrase.op2.alias; 
				return String.format("%s%s %s %s.%s", snot, op1, op, alias, op2);
			}
		}
	}


	public String opToSql(String op) {
		switch(op) {
		case "==":
			return "=";
		case "like":
			return "LIKE";
		default:
			return op;
		}
	}
	public String operandToSql(SqlValue val) {
		if (val.dtype != null && val.dtype.isShape(Shape.DATE)) {
			return dateGenerator.convertDateStringToSQLTimestamp(val.exp.strValue());
		}
		
		if (val.exp instanceof StringExp) {
			return String.format("'%s'", val.exp.strValue());
		}
		return val.exp.strValue();
	}
	public String operandToSql(WhereOperand val) {
		if (!val.isValue) {
			if (val.fnName != null) {
				return doFn(val);
			}
			return getColumnName(val.exp);
		}
		
		if (val.typeDetails.dtype != null && val.typeDetails.dtype.isShape(Shape.DATE)) {
			return dateGenerator.convertDateStringToSQLTimestamp(val.exp.strValue());
		}
		
		if (val.exp instanceof StringExp) {
			return String.format("'%s'", val.exp.strValue());
		}
		return val.exp.strValue();
	}

	private void adjustYearStuff(WhereOperand op1, WhereOperand op2) {
		if (!op1.isValue) {
			if (filterRunner.isDateFn(op1.fnName)) {
				op2.typeDetails.dtype = registry.getType(BuiltInTypes.INTEGER_SHAPE);
			}
		}
	}
	private String doFn(WhereOperand val) {
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
		case "date":
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
	
	private String getColumnName(Exp op1) {
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