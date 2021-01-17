package org.delia.zdb.mem;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.delia.core.DateFormatService;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.db.memdb.MemDBTable;
import org.delia.relation.RelationInfo;
import org.delia.type.DRelation;
import org.delia.type.DStructType;
import org.delia.type.DValue;
import org.delia.type.TypePair;
import org.delia.util.DRuleHelper;
import org.delia.util.DValueHelper;

/*
 * Relations are two-sides. If update or upsert modifies one side (eg Customer)
 * we need to find and modify all relation other side objects (eg. Address)
 * so that they are consistent. if A refers to B then B should refer to A.
 */
public class RelationPruner extends ServiceBase {

	DateFormatService fmtSvc;

	public RelationPruner(FactoryService factorySvc) {
		super(factorySvc);
	}

	public void pruneOtherSide(DValue tmp, DValue dvalFull, MemZDBExecutor memDBInterface) {
		adjustOtherSide(tmp, dvalFull, memDBInterface);
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
//				DValue existing = tmp.asStruct().getField(fieldName);
//				DRelation drel1 = existing == null ? null : existing.asRelation();
				
//				DValue newval = dvalFull.asStruct().getField(fieldName);
//				DRelation drel2 = newval.asRelation();
				
				//get list of all addresses whose .cust points to tmp (55)
				DValue pkval = DValueHelper.findPrimaryKeyValue(tmp);
				RelationInfo relinfo = DRuleHelper.findMatchingRuleInfo((DStructType) tmp.getType(), pair);
				List<DValue> allOthers = findOthers(pair, memDBInterface, relinfo, pkval);
				pruneRelations(allOthers, pair, relinfo, pkval);
			}
		}
	}

	private void pruneRelations(List<DValue> allOthers, TypePair pair, RelationInfo relinfo, DValue pkval) {
		String fieldName = relinfo.otherSide.fieldName;
		for(DValue dval: allOthers) {
			DValue inner = dval.asStruct().getField(fieldName);
			if (inner != null) {
				DRelation drel = inner.asRelation(); 
				List<DValue> newFKList = drel.getMultipleKeys().stream().filter(x -> !isMatchByStr(x, pkval)).collect(Collectors.toList());
				if (newFKList.size() != drel.getMultipleKeys().size()) {
					drel.getMultipleKeys().clear();
					drel.getMultipleKeys().addAll(newFKList);
					
					//and remove from fetched items too
					if (drel.haveFetched()) {
						List<DValue> newfetchL = new ArrayList<>();
						for(DValue ff: drel.getFetchedItems()) {
							if (!isMatchByStr(ff, pkval)) {
								newfetchL.add(ff);
							}
						}
						drel.getFetchedItems().clear();
						drel.getFetchedItems().addAll(newfetchL);
					}
				}
			}
		}
	}

	private boolean isMatchByStr(DValue dval1, DValue dval2) {
		String s1 = dval1.asString();
		String s2 = dval2.asString();
		return s1.equals(s2);
	}

	//TODO: this is very inefficient. improve!
	private List<DValue> findOthers(TypePair pair, MemZDBExecutor memDBInterface, RelationInfo relinfo, DValue pkval) {
		List<DValue> allFoundL = new ArrayList<>();
		MemDBTable tbl = memDBInterface.getTbl(pair.type.getName());
		for(DValue dval: tbl.rowL) {
			DValue inner = dval.asStruct().getField(relinfo.otherSide.fieldName);
			if (inner != null) {
				DRelation drel = inner.asRelation();
				for(DValue key: drel.getMultipleKeys()) {
					if (isMatchByStr(pkval, key)) {
						allFoundL.add(dval);
						break;
					}
				}
			}
		}
		return allFoundL;
	}

}