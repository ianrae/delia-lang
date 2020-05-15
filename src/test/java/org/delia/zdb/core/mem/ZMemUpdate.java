package org.delia.zdb.core.mem;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.delia.core.DateFormatService;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.db.DBException;
import org.delia.db.QuerySpec;
import org.delia.db.memdb.MemDBTable;
import org.delia.db.memdb.RowSelector;
import org.delia.error.DeliaError;
import org.delia.type.BuiltInTypes;
import org.delia.type.DRelation;
import org.delia.type.DType;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.util.DValueHelper;
import org.delia.util.DeliaExceptionHelper;
import org.delia.valuebuilder.RelationValueBuilder;

//========
public class ZMemUpdate extends ServiceBase {

	DateFormatService fmtSvc;
	private DTypeRegistry registry;

	public ZMemUpdate(FactoryService factorySvc, DTypeRegistry registry) {
		super(factorySvc);
		this.registry = registry;
		this.fmtSvc = factorySvc.getDateFormatService();
	}

	public int doExecuteUpdate(QuerySpec spec, DValue dvalUpdate, Map<String, String> assocCrudMap, RowSelector selector, MemZDBExecutor memDBInterface) {
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
			memDBInterface.checkUniqueness(dvalUpdate, tbl, typeName, existing, true);
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
			DRelation src = dvalUpdate.asStruct().getField(fieldName).asRelation();
			DRelation dest = getOrCreateRelation(clone, fieldName); 

			String action = assocCrudMap.get(fieldName);
			switch(action) {
			case "insert":
				dest.getMultipleKeys().addAll(src.getMultipleKeys());
				break;
			case "update":
				doUpdate(src, dest);
				break;
			case "delete":
			{
				for(DValue fk: src.getMultipleKeys()) {
					DValue srcFK = findIn(dest, fk);
					if (srcFK != null) {
						dest.getMultipleKeys().remove(srcFK);
					}
				}
				//empty relation not allowed, so delete entire relation if mepty
				if (dest.getMultipleKeys().isEmpty()) {
					clone.asMap().remove(fieldName);
				}
			}
			break;
			default:
				break;
			}
		}
	}

	private DRelation getOrCreateRelation(DValue clone, String fieldName) {
		DValue tmp = clone.asStruct().getField(fieldName);
		if (tmp == null) {
			DValue dval = createRelation(clone, fieldName);
			clone.asMap().put(fieldName, dval);
			return dval.asRelation();
		} else {
			return tmp.asRelation();
		}
	}

	//create empty relation
	private DValue createRelation(DValue clone, String fieldName) {
		DType relType = registry.getType(BuiltInTypes.RELATION_SHAPE);
		DType farEndType = DValueHelper.findFieldType(clone.getType(), fieldName);
		String typeName = farEndType.getName();
		RelationValueBuilder builder = new RelationValueBuilder(relType, typeName, registry);
		builder.buildEmptyRelation();
		boolean b = builder.finish();
		if (!b) {
			DeliaExceptionHelper.throwError("relation-create-failed-assocCrud", "Type '%s': Failed to create empty relation", typeName);
			return null;
		} else {
			DValue dval = builder.getDValue();
			return dval;
		}
	}

	private DValue findIn(DRelation drelDest, DValue target) {
		String s2 = target.asString();
		for(DValue dval: drelDest.getMultipleKeys()) {
			//str compare for now
			String s1 = dval.asString();
			if (s1.equals(s2)) {
				return dval;
			}
		}
		return null;
	}

	private void doUpdate(DRelation drelSrc, DRelation drelDest) {
		//process pairs (old,new)
		for(int i = 0; i < drelSrc.getMultipleKeys().size(); i += 2) {
			DValue currentVal = drelSrc.getMultipleKeys().get(i);
			DValue newVal = drelSrc.getMultipleKeys().get(i+1);

			DValue existingVal = findIn(drelDest, currentVal);
			if (existingVal != null) {	
				drelDest.getMultipleKeys().remove(existingVal);
				drelDest.getMultipleKeys().add(newVal);
			}
		}
	}
}