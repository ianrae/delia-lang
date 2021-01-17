package org.delia.zdb.mem;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.delia.compiler.ast.BooleanExp;
import org.delia.core.DateFormatService;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.db.DBException;
import org.delia.db.InsertContext;
import org.delia.db.QuerySpec;
import org.delia.db.memdb.MemDBTable;
import org.delia.db.memdb.RowSelector;
import org.delia.dval.DValueExConverter;
import org.delia.error.DeliaError;
import org.delia.runner.FilterEvaluator;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.type.TypePair;
import org.delia.util.DValueHelper;
import org.delia.util.DeliaExceptionHelper;

public class ZMemUpsert extends ServiceBase {

	DateFormatService fmtSvc;
	private DTypeRegistry registry;
	private RelationPruner relationPruner;

	public ZMemUpsert(FactoryService factorySvc, DTypeRegistry registry) {
		super(factorySvc);
		this.registry = registry;
		this.relationPruner = new RelationPruner(factorySvc);
	}

	public int doExecuteUpsert(QuerySpec spec, DValue dvalFull, Map<String, String> assocCrudMap, boolean noUpdateFlag, RowSelector selector, 
			MemZDBExecutor memDBInterface, ZStuff stuff) {
		MemDBTable tbl = selector.getTbl();
		List<DValue> dvalList = selector.match(tbl.rowL);
		String typeName = spec.queryExp.getTypeName();
		if (selector.wasError()) {
			DeliaError err = et.add("row-selector-error", String.format("row selector failed for type '%s'", typeName));
			throw new DBException(err);
		}
		if (dvalList.size() > 1) {
			DeliaError err = et.add("upsert-unique-violation", String.format("upsert filter must specify one row (at most). %d rows matched for type '%s'", dvalList.size(), typeName));
			throw new DBException(err);
		}
		if (spec.queryExp.filter.cond instanceof BooleanExp) {
			DeliaExceptionHelper.throwError("upsert-filter-error", "[true] not supported");
		}

		if (CollectionUtils.isEmpty(dvalList)) {
			//add primary key to dvalFull
			addPrimaryKey(spec, dvalFull, selector);

			ZMemInsert memInsert = new ZMemInsert(factorySvc);
			InsertContext ctx = new InsertContext(); //upsert not supported for serial primaryKey
			memInsert.doExecuteInsert(tbl, dvalFull, ctx, memDBInterface, stuff);
			return 1;
		} else if (noUpdateFlag) {
			return 0; //don't update
		}

		//TODO: also need to validate with list. eq if two rows are setting same value
		for(DValue existing: dvalList) {
			memDBInterface.checkUniqueness(dvalFull, tbl, typeName, existing, true);
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
					relationPruner.pruneOtherSide(tmp, dvalFull, memDBInterface);
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
	

	private void addPrimaryKey(QuerySpec spec, DValue dvalFull, RowSelector selector) {
		TypePair keyPair = DValueHelper.findPrimaryKeyFieldPair(dvalFull.getType());
		if (dvalFull.asStruct().getField(keyPair.name) != null) {
			return; //already has primary key
		} else if (DValueHelper.typeHasSerialPrimaryKey(dvalFull.getType())) {
			return; //don't add serial key
		}

		FilterEvaluator evaluator = spec.evaluator;
		DValueExConverter dvalConverter = new DValueExConverter(factorySvc, registry);
		String keyField = evaluator.getRawValue(); //we assume primary key. eg Customer[55]
		DValue inner = dvalConverter.buildFromObject(keyField, keyPair.type);

		Map<String, DValue> map = dvalFull.asMap();
		map.put(keyPair.name, inner);
	}
}