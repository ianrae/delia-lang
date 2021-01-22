package org.delia.db.newhls;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.delia.relation.RelationInfo;
import org.delia.type.DStructType;
import org.delia.type.TypePair;
import org.delia.util.DRuleHelper;
import org.delia.util.DValueHelper;
import org.delia.util.DeliaExceptionHelper;

/**
 * Determines which fields should be mentioned in the query.
 * For example, parent field of a relation should *not* be mentioned.
 * Includes fields of fetched types too.
 * @author ian
 *
 */
public class HLDFieldBuilder {
	
	public void generateFields(HLDQuery hld) {
		if (hld.finalField == null) {
			addStructFields(hld, hld.fieldL);
		} else {
			addFinalField(hld);
		}

		for(FetchSpec spec: hld.fetchL) {
			addFetchField(spec, hld);
		}
		
		//remove duplicates
		List<HLDField> doomedL = new ArrayList<>();
		int index = 0;
		for(HLDField fld: hld.fieldL) {
			for(int k = index+1; k < hld.fieldL.size(); k++) {
				HLDField inner = hld.fieldL.get(k); 
				if (inner == fld) {
					continue;
				}
				if (fld.structType == inner.structType) {
					if (StringUtils.equals(fld.fieldName, inner.fieldName)) {
//						System.out.println("removing dup!! " + inner.fieldName);
//						if (inner.source == null && fld.source != null) {
//							doomedL.add(inner);
//						} else {
//							doomedL.add(fld);
//						}
						break;
					}
				}
			}
			index++;
		}
		for(HLDField doomed: doomedL) {
			hld.fieldL.remove(doomed);
		}
	}
	
	private void addFinalField(HLDQuery hld) {
		JoinElement throughChainEl = findThroughChainEl(hld.finalField, hld);
		StructField sf = hld.finalField.structField;
		DStructType fromType = sf.dtype;
		TypePair pair = DValueHelper.findField(fromType, sf.fieldName);
		if (!pair.type.isStructShape()) {
			addField(hld.fieldL, fromType, pair).source = throughChainEl;
			return;
		}
		
		if (hld.finalField.rf != null) { //has throughchain?
			RelationField rf = hld.finalField.rf;
			TypePair pairx = new TypePair(rf.fieldName, rf.fieldType);
			addField(hld.fieldL, rf.dtype, pairx).source = throughChainEl;
			return;
		}
		
		JoinElement el = hld.findMatch(fromType, sf.fieldName, hld);
		RelationInfo relinfo = DRuleHelper.findMatchingRuleInfo(fromType, pair);
		if (relinfo.isManyToMany()) {
			addField(hld.fieldL, fromType, pair).source = el;
		} else if (relinfo.isParent) {
			TypePair other = new TypePair(relinfo.otherSide.fieldName, relinfo.nearType);
			addField(hld.fieldL, relinfo.otherSide.nearType, other).source = el;
		} else {
			addField(hld.fieldL, fromType, pair).source = el;
		}
	}

	private JoinElement findThroughChainEl(FinalField finalField, HLDQuery hld) {
		if (hld.finalField.rf != null) { //has throughchain?
			RelationField rf = hld.finalField.rf;
			JoinElement el = hld.findMatch(rf.dtype, rf.fieldName, hld);
			return el;
		}
		return null;
	}

	private void addStructFields(HLDQuery hld, List<HLDField> fieldL) {
		DStructType fromType = hld.fromType;

		for(TypePair pair: fromType.getAllFields()) {
			if (pair.type.isStructShape()) {
				RelationInfo relinfo = DRuleHelper.findMatchingRuleInfo(fromType, pair);
				//only load fk fields if fks or fetch
				JoinElement el = hld.findMatch(relinfo, hld);
				if (relinfo.notContainsFK()) {
					boolean fetchingThisField = el == null ? false : el.usedForFetch();
					if (fetchingThisField) {
						addField(fieldL, fromType, pair).source = el;
					}
				} else {
					addField(fieldL, fromType, pair).source = el;
				}
			} else {
				addField(fieldL, fromType, pair);
			}
		}
	}

	private HLDField addField(List<HLDField> fieldL, DStructType fromType, TypePair pair) {
		HLDField rf = new HLDField();
		rf.structType = fromType;
		rf.fieldName = pair.name;
		rf.fieldType = pair.type;
		//			rf.groupName = "__MAINGROUP__";
		fieldL.add(rf);
		return rf;
	}

	private void addFetchField(FetchSpec spec, HLDQuery hld) {
		DStructType reftype = (DStructType) DValueHelper.findFieldType(spec.structType, spec.fieldName);
		if (reftype == null) {
			DeliaExceptionHelper.throwUnknownFieldError(spec.structType.getName(), spec.fieldName);
		}
		
		Optional<JoinElement> optJoin = hld.joinL.stream().filter(x -> x.fetchSpec == spec).findAny(); //should only be one
		if (spec.isFK) {
			TypePair pkpair = DValueHelper.findPrimaryKeyFieldPair(reftype);
			RelationInfo relinfo = DRuleHelper.findMatchingRuleInfo(spec.structType, new TypePair(spec.fieldName, null));
			//only get fk fields if is a join for it
			JoinElement el = optJoin.orElse(null);
			if (el == null) { //due to upgrade the fetchspec may have been cleared
				el = findMatch(spec, hld.joinL);
			}
			
			if (!relinfo.isManyToMany() || (el != null && el.usedForFK())) {
				addField(hld.fieldL, reftype, pkpair).source = el;
			}
		} else {
			for(TypePair pair: reftype.getAllFields()) {
				if (pair.type.isStructShape()) {
					RelationInfo relinfo = DRuleHelper.findMatchingRuleInfo(reftype, pair);
					
					//only get fk fields if is a join for it
					JoinElement el = optJoin.orElse(null);
					if (relinfo.isManyToMany() && el != null) {
						String f1 = el.relinfo.fieldName;
						String f1a = el.relinfo.otherSide.fieldName;
						String f2 = relinfo.fieldName;
						if (f2.equals(f1) || f2.equals(f1a)) {
							addField(hld.fieldL, reftype, pair).source = optJoin.get();
						}
					} else if (!relinfo.isParent && !relinfo.isManyToMany()) {
						addField(hld.fieldL, reftype, pair).source = optJoin.get();
					}
				} else {
					addField(hld.fieldL, reftype, pair).source = optJoin.get();
				}
			}
		}
	}

	private JoinElement findMatch(FetchSpec spec, List<JoinElement> joinL) {
		for(JoinElement el: joinL) {
			if (el.relationField.dtype == spec.structType) {
				if (el.relationField.fieldName.equals(spec.fieldName)) {
					return el;
				}
			}
		}
		return null;
	}

}