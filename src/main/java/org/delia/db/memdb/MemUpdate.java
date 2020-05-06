package org.delia.db.memdb;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.delia.core.DateFormatService;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.db.DBAccessContext;
import org.delia.db.DBException;
import org.delia.db.QuerySpec;
import org.delia.error.DeliaError;
import org.delia.type.DRelation;
import org.delia.type.DValue;
import org.delia.util.DValueHelper;

/**
 * Performs update
 * 
 * @author Ian Rae
 *
 */
public class MemUpdate extends ServiceBase {

	DateFormatService fmtSvc;

	public MemUpdate(FactoryService factorySvc) {
		super(factorySvc);
		this.fmtSvc = factorySvc.getDateFormatService();
	}

	public int doExecuteUpdate(QuerySpec spec, DValue dvalUpdate, Map<String, String> assocCrudMap, DBAccessContext dbctx, RowSelector selector, MemDBInterface memDBInterface) {
		MemDBTable tbl = selector.getTbl();
		List<DValue> dvalList = selector.match(tbl.rowL);
		String typeName = spec.queryExp.getTypeName();
		if (selector.wasError()) {
			DeliaError err = et.add("row-selector-error", String.format("xrow selector failed for type '%s'", typeName));
			throw new DBException(err);
		}

		if (CollectionUtils.isEmpty(dvalList)) {
			//nothing to do
			return 0;
		}

		//TODO: also need to validate with list. eq if two rows are setting same value
		for(DValue existing: dvalList) {
			memDBInterface.checkUniqueness(dvalUpdate, tbl, typeName, existing, true, dbctx);
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
					DValue clone = DValueHelper.mergeOne(dvalUpdate, tmp, assocCrudMap);
					if (assocCrudMap != null) {
						doAssocCrud(dvalUpdate, clone, assocCrudMap);
					}
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

	private void doAssocCrud(DValue dvalUpdate, DValue clone, Map<String, String> assocCrudMap) {
		for(String fieldName: assocCrudMap.keySet()) {
			DRelation drelSrc = dvalUpdate.asStruct().getField(fieldName).asRelation();
			DRelation drelDest = dvalUpdate.asStruct().getField(fieldName).asRelation();
			
			String action = assocCrudMap.get(fieldName);
			switch(action) {
			case "insert":
				drelDest.getMultipleKeys().addAll(drelSrc.getMultipleKeys());
			break;
			case "update":
				doUpdate(drelSrc, drelDest);
			break;
			case "delete":
			{
				for(DValue fk: drelSrc.getMultipleKeys()) {
					DValue srcFK = findIn(drelDest);
					if (srcFK != null) {
						drelSrc.getMultipleKeys().remove(srcFK);
					}
				}
			}
			break;
			default:
			break;
			}
		}
	}

	private DValue findIn(DRelation drelDest) {
		// TODO Auto-generated method stub
		return null;
	}

	private void doUpdate(DRelation drelSrc, DRelation drelDest) {
		// TODO Auto-generated method stub
		
	}
	
	
}