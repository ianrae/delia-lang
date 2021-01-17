package org.delia.zdb.mem;

import java.util.ArrayList;
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
import org.delia.db.hls.HLSSimpleQueryService;
import org.delia.db.memdb.MemDBTable;
import org.delia.db.memdb.RowSelector;
import org.delia.dval.DValueExConverter;
import org.delia.error.DeliaError;
import org.delia.relation.RelationInfo;
import org.delia.runner.FilterEvaluator;
import org.delia.type.DRelation;
import org.delia.type.DStructType;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.type.TypePair;
import org.delia.util.DRuleHelper;
import org.delia.util.DValueHelper;
import org.delia.util.DeliaExceptionHelper;

public class ZMemUpsert extends ServiceBase {

	DateFormatService fmtSvc;
	private DTypeRegistry registry;

	public ZMemUpsert(FactoryService factorySvc, DTypeRegistry registry) {
		super(factorySvc);
		this.registry = registry;
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
					adjustOtherSide(tmp, dvalFull, memDBInterface);
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
	
	//Note. here's how Struct and Relation work
	//Customer.addr. the field in DStructType has entry for addr with DStructType as type
	// so pair.type.isStructType is true
	//but when you get the value Customer.addr the field DValue is DRelation,
	// so inner.getType().isRelation is true

	private void adjustOtherSide(DValue tmp, DValue dvalFull, MemZDBExecutor memDBInterface) {
		DStructType dtype = dvalFull.asStruct().getType();
		for(String fieldName: dvalFull.asMap().keySet()) {
			TypePair pair = DValueHelper.findField(dtype, fieldName);
			if (pair.type.isStructShape()) {
				DValue existing = tmp.asStruct().getField(fieldName);
				DRelation drel1 = existing == null ? null : existing.asRelation();
				
				DValue newval = dvalFull.asStruct().getField(fieldName);
				DRelation drel2 = newval.asRelation();
				
				List<DValue> allOthers = findOthers(pair, tmp, memDBInterface);
				
				System.out.println("ss");
			}
		}
	}

	//TODO: this is very inefficient. improve!
	private List<DValue> findOthers(TypePair pair, DValue tmp, MemZDBExecutor memDBInterface) {
		List<DValue> allFoundL = new ArrayList<>();
		MemDBTable tbl = memDBInterface.getTbl(pair.type.getName());
		DValue pkval = DValueHelper.findPrimaryKeyValue(tmp);
		RelationInfo relinfo = DRuleHelper.findMatchingRuleInfo((DStructType) tmp.getType(), pair);
		for(DValue dval: tbl.rowL) {
			DValue inner = dval.asStruct().getField(relinfo.otherSide.fieldName);
			if (inner != null) {
				DRelation drel = inner.asRelation();
				for(DValue key: drel.getMultipleKeys()) {
					String s1 = pkval.asString();
					String s2 = key.asString();
					if (s1.equals(s2)) {
						allFoundL.add(dval);
						break;
					}
				}
			}
		}
		return allFoundL;
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