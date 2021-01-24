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

	public void pruneOtherSide(DValue tmp, DValue dvalFull, MemDBExecutor memDBInterface) {
		adjustOtherSide(tmp, dvalFull, memDBInterface);
	}
	
	//Note. here's how Struct and Relation work
	//Customer.addr. the field in DStructType has entry for addr with DStructType as type
	// so pair.type.isStructType is true
	//but when you get the value Customer.addr the field DValue is DRelation,
	// so inner.getType().isRelation is true
	//Summary: with types its a STRUCT and with values its a RELATION

	private void adjustOtherSide(DValue tmp, DValue dvalFull, MemDBExecutor memDBInterface) {
		DStructType dtype = dvalFull.asStruct().getType();
		DValue pkval = DValueHelper.findPrimaryKeyValue(tmp);
		for(String fieldName: dvalFull.asMap().keySet()) {
			TypePair pair = DValueHelper.findField(dtype, fieldName);
			if (pair.type.isStructShape()) {
				
				DValue possible = dvalFull.asMap().get(fieldName);
				if (possible == null) {
					continue;
				}
				DRelation updateRelation = possible.asRelation();
				
				//get list of all addresses whose .cust points to tmp (55)
				RelationInfo relinfo = DRuleHelper.findMatchingRuleInfo((DStructType) tmp.getType(), pair);
				List<DValue> allOthers = findOthers(pair, memDBInterface, relinfo, pkval);
				pruneRelations(allOthers, pair, relinfo, pkval, updateRelation);
			}
		}
	}

	private void pruneRelations(List<DValue> allOthers, TypePair pair, RelationInfo relinfo, DValue pkval, DRelation updateRelation) {
		if (relinfo.otherSide == null) {
			return;
		}
		String fieldName = relinfo.otherSide.fieldName;
		for(DValue dval: allOthers) {
			DValue otherpkval = DValueHelper.findPrimaryKeyValue(dval);
			DValue inner = dval.asStruct().getField(fieldName);
			if (inner != null) {
				//the idea here is that if we update Customer, then we need to check the far end (Address)
				//and remove any fks that are no longer correct.
				DRelation drel = inner.asRelation(); 
				
				//now see if updateRel contains otherpkval. If it does then leave this object alone
				//if it doesn't then prune
				List<DValue> newFKList = updateRelation.getMultipleKeys().stream().filter(x -> isMatchByStr(x, otherpkval)).collect(Collectors.toList());
				boolean needToPrune = newFKList.isEmpty();
				
//old				List<DValue> newFKList = drel.getMultipleKeys().stream().filter(x -> !isMatchByStr(x, pkval)).collect(Collectors.toList());
				if (needToPrune) {
					for(DValue fkval: drel.getMultipleKeys()) {
						if (isMatchByStr(fkval, pkval)) {
							drel.getMultipleKeys().remove(fkval);
							break;
						}
					}
					
					//and remove from fetched items too
					if (drel.haveFetched()) {
						List<DValue> newfetchL = new ArrayList<>();
						for(DValue ff: drel.getFetchedItems()) {
							DValue ffpk = DValueHelper.findPrimaryKeyValue(ff);
							if (!isMatchByStr(ffpk, pkval)) {
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
	private List<DValue> findOthers(TypePair pair, MemDBExecutor memDBInterface, RelationInfo relinfo, DValue pkval) {
		List<DValue> allFoundL = new ArrayList<>();
		MemDBTable tbl = memDBInterface.getTbl(pair.type.getName());
		if (tbl == null && relinfo.otherSide == null) {
			return allFoundL; //one-sided relation
		}
		
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