package org.delia.db.memdb;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.delia.core.DateFormatService;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.db.DBAccessContext;
import org.delia.db.DBException;
import org.delia.db.InsertContext;
import org.delia.db.QuerySpec;
import org.delia.db.memdb.MemDBInterface.Stuff;
import org.delia.error.DeliaError;
import org.delia.type.DValue;
import org.delia.util.DValueHelper;

/**
 * Performs update
 * 
 * @author Ian Rae
 *
 */
public class MemUpsert extends ServiceBase {

	DateFormatService fmtSvc;

	public MemUpsert(FactoryService factorySvc) {
		super(factorySvc);
		this.fmtSvc = factorySvc.getDateFormatService();
	}

	public int doExecuteUpsert(QuerySpec spec, DValue dvalFull, Map<String, String> assocCrudMap, DBAccessContext dbctx, RowSelector selector, 
				MemDBInterface memDBInterface, Stuff stuff) {
		MemDBTable tbl = selector.getTbl();
		List<DValue> dvalList = selector.match(tbl.rowL);
		String typeName = spec.queryExp.getTypeName();
		if (selector.wasError()) {
			DeliaError err = et.add("row-selector-error", String.format("xrow selector failed for type '%s'", typeName));
			throw new DBException(err);
		}

		if (CollectionUtils.isEmpty(dvalList)) {
			//nothing to do
			MemInsert memInsert = new MemInsert(factorySvc);
			InsertContext ctx = new InsertContext(); //fix!!
			memInsert.doExecuteInsert(tbl, dvalFull, ctx, dbctx, memDBInterface, stuff);
			return 0;
		}

		//TODO: also need to validate with list. eq if two rows are setting same value
		for(DValue existing: dvalList) {
			memDBInterface.checkUniqueness(dvalFull, tbl, typeName, existing, true, dbctx);
		}

		//TODO if dvalUpdate contains the primary key then do uniqueness check

		//update one or more matching dvals
		int numRowsAffected = dvalList.size();
		for(int i = 0; i < tbl.rowL.size(); i++) {
			DValue dd = tbl.rowL.get(i);
			//this is very inefficient if rowL large. TODO fix
			//if dd is one of the matching rows, then clone it and
			//replace it in tbl
			for(DValue tmp: dvalList) {
				if (tmp == dd) {
					DValue clone = DValueHelper.mergeOne(dvalFull, tmp);
					dvalList.remove(tmp);
					tbl.rowL.set(i, clone);
					break;
				}
			}

			if (dvalList.isEmpty()) {
				break; //no need to keep searching
			}
		}
		return numRowsAffected;
	}
	
	
}