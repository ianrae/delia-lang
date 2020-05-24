package org.delia.db.postgres;

import org.delia.core.FactoryService;
import org.delia.db.sql.fragment.WhereFragmentGenerator;
import org.delia.db.sql.where.WhereOperand;
import org.delia.runner.VarEvaluator;
import org.delia.type.DTypeRegistry;
import org.delia.util.DeliaExceptionHelper;

public class PostgresWhereFragmentGenerator extends WhereFragmentGenerator {

	public PostgresWhereFragmentGenerator(FactoryService factorySvc, DTypeRegistry registry, VarEvaluator varEvaluator) {
		super(factorySvc, registry, varEvaluator);
	}

	protected String doFn(WhereOperand val) {
		String alias = val.alias == null ? "" : val.alias + ".";
		
		//("yyyy-MM-dd'T'HH:mm:ss");
		switch(val.fnName) {
		case "year":
		{
			String s = this.getColumnName(val.exp);
			return String.format("date_part('year', %s%s)", alias, s);
		}
		case "month":
		{
			String s = this.getColumnName(val.exp);
			return String.format("date_part('month', %s%s)", alias, s);
		}
		case "day":
		{
			String s = this.getColumnName(val.exp);
			return String.format("date_part('day', %s%s)", alias, s);
		}
		case "hour":
		{
			String s = this.getColumnName(val.exp);
			return String.format("date_part('hour', %s%s)", alias, s);
		}
		case "minute":
		{
			String s = this.getColumnName(val.exp);
			return String.format("date_part('minute', %s%s)", alias, s);
		}
		case "second":
		{
			String s = this.getColumnName(val.exp);
			return String.format("date_part('second', %s%s)", alias, s);
		}
		default:
			//err!!
			break;
		}
		DeliaExceptionHelper.throwError("unknown-where-function", "Unknown filter function '%s'", val.fnName);
		return null;
	}
}