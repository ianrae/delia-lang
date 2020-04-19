package org.delia.db.postgres;

import org.delia.core.FactoryService;
import org.delia.db.QuerySpec;
import org.delia.db.sql.prepared.WhereClauseGenerator;
import org.delia.db.sql.where.WhereOperand;
import org.delia.runner.VarEvaluator;
import org.delia.type.DTypeRegistry;

public class PostgresWhereClauseGenerator extends WhereClauseGenerator {

	public PostgresWhereClauseGenerator(FactoryService factorySvc, DTypeRegistry registry, VarEvaluator varEvaluator) {
		super(factorySvc, registry, varEvaluator);
	}
	
	protected String doFn(WhereOperand val) {
		//("yyyy-MM-dd'T'HH:mm:ss");
		switch(val.fnName) {
		case "year":
		{
			String s = this.getColumnName(val.exp);
			return String.format("date_part('year', %s)", s);
		}
		case "month":
		{
			String s = this.getColumnName(val.exp);
			return String.format("date_part('month', %s)", s);
		}
		case "day":
		{
			String s = this.getColumnName(val.exp);
			return String.format("date_part('day', %s)", s);
		}
		case "hour":
		{
			String s = this.getColumnName(val.exp);
			return String.format("date_part('hour', %s)", s);
		}
		case "minute":
		{
			String s = this.getColumnName(val.exp);
			return String.format("date_part('minute', %s)", s);
		}
		case "second":
		{
			String s = this.getColumnName(val.exp);
			return String.format("date_part('second', %s)", s);
		}
		default:
			//err!!
			break;
		}
		return "KKKKKKKKKKK"; //TODO fix
	}
	
	protected String whereWord(QuerySpec spec) {
//		if (this.selectFnHelper.isExistsPresent(spec)) {
////			return " WHERE EXISTS";
//			return " WHERE EXISTS";
//		} else {
			return " WHERE ";
//		}
	}

}