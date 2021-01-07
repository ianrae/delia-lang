package org.delia.db.newhls;

import java.util.List;
import java.util.Optional;

import org.delia.db.hls.AliasInfo;
import org.delia.db.newhls.cond.FilterVal;
import org.delia.db.newhls.cond.OpFilterCond;
import org.delia.db.newhls.cond.SingleFilterCond;
import org.delia.relation.RelationCardinality;
import org.delia.relation.RelationInfo;
import org.delia.type.DStructType;
import org.delia.type.DType;
import org.delia.type.TypePair;
import org.delia.util.DRuleHelper;
import org.delia.util.DValueHelper;

/**
 * Determines which fields should be mentioned in the query.
 * For example, parent field of a relation should *not* be mentioned.
 * Includes fields of fetched types too.
 * @author ian
 *
 */
public class HLDFieldBuilder {
	private HLDAliasManager aliasMgr;

	public HLDFieldBuilder(HLDAliasManager aliasMgr) {
		this.aliasMgr = aliasMgr;
	}

	public void generateFieldsAndAliases(HLDQuery hld) {
		//TODO much more code needed here!
		addStructFields(hld, hld.fieldL);

		for(FetchSpec spec: hld.fetchL) {
			addFetchField(spec, hld);
		}

		assignAliases(hld);
	}
	
	private void addStructFields(HLDQuery hld, List<HLDField> fieldL) {
		DStructType fromType = hld.fromType;

		for(TypePair pair: fromType.getAllFields()) {
			if (pair.type.isStructShape()) {
				RelationInfo relinfo = DRuleHelper.findMatchingRuleInfo(fromType, pair);
				if (RelationCardinality.MANY_TO_MANY.equals(relinfo.cardinality)) {
					doManyToManyAddFKofJoins(fieldL, pair, relinfo, null, hld);
				} else if (!relinfo.isParent) {
					addField(fieldL, fromType, pair);
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
	private void doManyToManyAddFKofJoins(List<HLDField> fieldL, TypePair pair, RelationInfo relinfoA, JoinElement el, HLDQuery hld) {
		//			String assocTbl = datIdMap.getAssocTblName(relinfoA.getDatId()); 
		////			String fieldName = datIdMap.getAssocFieldFor(relinfoA);
		//			String fieldName = datIdMap.getAssocFieldFor(relinfoA.otherSide);
		//
		//			AliasInfo aliasInfo = aliasManager.getAssocAlias(relinfoA.nearType, relinfoA.fieldName, assocTbl);
		//			String s = aliasManager.buildFieldAlias(aliasInfo, fieldName);
		//			s = String.format("%s as %s", s, pair.name);
		//			RenderedField rff = addField(fieldL, null, fieldName, s);
		//			rff.isAssocField = true;
		//			rff.fieldGroup = new FieldGroup((el == null), el);
	}

	private void addFetchField(FetchSpec spec, HLDQuery hld) {
		if (spec.isFK) {
			DStructType reftype = (DStructType) DValueHelper.findFieldType(spec.structType, spec.fieldName);
			TypePair pkpair = DValueHelper.findPrimaryKeyFieldPair(reftype);
			Optional<JoinElement> optJoin = hld.joinL.stream().filter(x -> x.fetchSpec == spec).findAny(); //should only be one
			
			addField(hld.fieldL, reftype, pkpair).source = optJoin.get();
		} else {
			//TODO
		}
	}

	private void assignAliases(HLDQuery hld) {
		AliasInfo info = aliasMgr.createMainTableAlias(hld.fromType);
		hld.fromAlias = info.alias;
		for(HLDField rf: hld.fieldL) {
			if (rf.structType == hld.fromType) {
				rf.alias = info.alias;
			} else {
				if (rf.source instanceof JoinElement) {
					JoinElement el = (JoinElement) rf.source;
					if (el.aliasName == null) {
						AliasInfo info2 = aliasMgr.createMainTableAlias(el.relationField.fieldType);
						el.aliasName = info2.alias;
					}
					rf.alias = el.aliasName;
				}
				//TODO!!
			}
		}

		//now populate SYMBOL FilterdVal
		if (hld.filter instanceof SingleFilterCond) {
			SingleFilterCond sfc = (SingleFilterCond) hld.filter;
			doFilterPKVal(sfc.val1, hld);
		} else if (hld.filter instanceof OpFilterCond) {
			OpFilterCond ofc = (OpFilterCond) hld.filter;
			doFilterVal(ofc.val1, hld);
			doFilterVal(ofc.val2, hld);
		}
	}

	private void doFilterVal(FilterVal val1, HLDQuery hld) {
		if (val1.isSymbol()) {
			String fieldName = val1.exp.strValue();
			DType fieldType = DValueHelper.findFieldType(hld.fromType, fieldName);
			val1.structField = new StructField(hld.fromType, fieldName, fieldType);
			val1.alias = hld.fromAlias;
		}
	}
	private void doFilterPKVal(FilterVal val1, HLDQuery hld) {
		TypePair pkpair = DValueHelper.findPrimaryKeyFieldPair(hld.fromType);
		String fieldName = pkpair.name;
		val1.structField = new StructField(hld.fromType, fieldName, pkpair.type);
		val1.alias = hld.fromAlias;
	}

}