package org.delia.db.postgres;

import org.delia.compiler.ast.IdentExp;
import org.delia.compiler.ast.IntegerExp;
import org.delia.compiler.ast.QueryFieldExp;
import org.delia.compiler.ast.QueryFuncExp;
import org.delia.core.FactoryService;
import org.delia.db.QuerySpec;
import org.delia.db.sql.prepared.SelectFuncHelper;
import org.delia.type.DType;
import org.delia.type.DTypeRegistry;
import org.delia.type.TypePair;
import org.delia.util.DValueHelper;
import org.delia.util.DeliaExceptionHelper;

public class PostgresSelectFuncHelper extends SelectFuncHelper {

	public PostgresSelectFuncHelper(FactoryService factorySvc, DTypeRegistry registry) {
		super(factorySvc, registry);
	}

	public QuerySpec doFirstFixup(QuerySpec specOriginal, String typeName) {
		QuerySpec spec = makeCopy(specOriginal);
		QueryFuncExp limitFn = this.findFn(spec, "limit");
		if (limitFn != null) {
			spec.queryExp.qfelist.remove(limitFn);
		}
		
		QueryFuncExp qfexp1 = new QueryFuncExp(99, new IdentExp("limit"), null, false);
		qfexp1.argL.add(new IntegerExp(1));

		spec.queryExp.qfelist.add(qfexp1);
		return spec;
	}
	public QuerySpec doLastFixup(QuerySpec specOriginal, String typeName) {
		QuerySpec spec = doFirstFixup(specOriginal, typeName);
		DType dtype = registry.findTypeOrSchemaVersionType(typeName);
		TypePair pair = DValueHelper.findPrimaryKeyFieldPair(dtype);
		if (pair == null) { 
			DeliaExceptionHelper.throwError("last-requires-sortable-field", "last() requires an orderBy() function or a primary key in type '%s'", typeName);
			return null;
		} else {
			QueryFuncExp qfexp1 = new QueryFuncExp(99, new IdentExp("orderBy"), null, false);
			QueryFieldExp qfe = new QueryFieldExp(99, new IdentExp(pair.name));
			IdentExp exp1 = new IdentExp("desc");
			qfexp1.argL.add(qfe);
			qfexp1.argL.add(exp1);
			
			QueryFuncExp qfexpAlreadyInList = findFn(spec, "last");
			int index = 0;
			boolean done = false;
			for(QueryFuncExp qfexp: spec.queryExp.qfelist) {
				if (index < spec.queryExp.qfelist.size() - 1) {
					if (qfexp == qfexpAlreadyInList) {
						//insert before
						spec.queryExp.qfelist.add(index, qfexp1);
						done = true;
						break;
					}
				}
				index++;
			}
			
			if (! done) {
				spec.queryExp.qfelist.add(qfexp1);
			}
			return spec;
		}
	}

}