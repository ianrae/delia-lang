package org.delia.db.newhls;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.delia.db.hls.AliasInfo;
import org.delia.db.newhls.cond.FilterVal;
import org.delia.db.newhls.cond.OpFilterCond;
import org.delia.db.newhls.cond.SingleFilterCond;
import org.delia.db.newhls.cond.SymbolChain;
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
		if (hld.finalField == null) {
			addStructFields(hld, hld.fieldL);
		} else {
			addFinalField(hld);
		}

		for(FetchSpec spec: hld.fetchL) {
			addFetchField(spec, hld);
		}

		assignAliases(hld);
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
		if (RelationCardinality.MANY_TO_MANY.equals(relinfo.cardinality)) {
			//doManyToManyAddFKofJoins(fieldL, pair, relinfo, null, hld);
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
		DStructType reftype = (DStructType) DValueHelper.findFieldType(spec.structType, spec.fieldName);
		Optional<JoinElement> optJoin = hld.joinL.stream().filter(x -> x.fetchSpec == spec).findAny(); //should only be one
		if (spec.isFK) {
			TypePair pkpair = DValueHelper.findPrimaryKeyFieldPair(reftype);
			
			addField(hld.fieldL, reftype, pkpair).source = optJoin.get();
		} else {
			for(TypePair pair: reftype.getAllFields()) {
				if (pair.type.isStructShape()) {
					RelationInfo relinfo = DRuleHelper.findMatchingRuleInfo(reftype, pair);
					if (RelationCardinality.MANY_TO_MANY.equals(relinfo.cardinality)) {
						//TODO doManyToManyAddFKofJoins(fieldL, pair, relinfo, null, hld);
					} else if (!relinfo.isParent) {
						addField(hld.fieldL, reftype, pair).source = optJoin.get();
					}
				} else {
					addField(hld.fieldL, reftype, pair).source = optJoin.get();
				}
			}
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
						AliasInfo info2 = aliasMgr.createFieldAlias(el.relationField.dtype, el.relationField.fieldName);
						el.aliasName = info2.alias;
						info2 = aliasMgr.createMainTableAlias(el.relationField.dtype);
						el.srcAlias = info2.alias;
						//TODO:this needs to be smarter. self-joins,multiple addr fields, etc
						//need to determine which instance of Customer this is!!
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
		
		//now do any other implicit joins
		for(JoinElement el: hld.joinL) {
			if (el.aliasName == null) {
				AliasInfo info2 = aliasMgr.createFieldAlias(el.relationField.dtype, el.relationField.fieldName);
				el.aliasName = info2.alias;
				info2 = aliasMgr.createMainTableAlias(el.relationField.dtype); //TODO fix later
				el.srcAlias = info2.alias;
				
				List<HLDField> fields = hld.fieldL.stream().filter(x -> x.source == el).collect(Collectors.toList());
				for(HLDField fld: fields) {
					fld.alias = info2.alias;
				}
			}
		}
		
		//and propogate alias to query fns
		for(QueryFnSpec fnspec: hld.funcL) {
			JoinElement el = findMatch(fnspec, hld);
			if (el != null) {
				if (el.relinfo.isParent) {
					fnspec.structField.alias = el.aliasName;
				} else if (el.relationField.dtype == hld.fromType) {
					fnspec.structField.alias = hld.fromAlias;
				} else {
					fnspec.structField.alias = el.aliasName;
				}
			} else {
				fnspec.structField.alias = hld.fromAlias;
			}
		}
	}

	private JoinElement findMatch(QueryFnSpec fnspec, HLDQuery hld) {
		if (fnspec.structField.fieldName == null) {
			return null;
		}
		for(JoinElement el: hld.joinL) {
			if (fnspec.structField.fieldName.equals(el.relationField.fieldName) && 
					fnspec.structField.dtype == el.relationField.dtype) {
				return el;
			}
		}
		return null;
	}

	private void doFilterVal(FilterVal val1, HLDQuery hld) {
		if (val1.isSymbol()) {
			String fieldName = val1.exp.strValue();
			DType fieldType = DValueHelper.findFieldType(hld.fromType, fieldName);
			val1.structField = new StructField(hld.fromType, fieldName, fieldType);
			val1.alias = hld.fromAlias;
		} else if (val1.isSymbolChain()) {
			String fieldName = val1.exp.strValue();
			SymbolChain chain = val1.asSymbolChain();
			DType fieldType = DValueHelper.findFieldType(chain.fromType, fieldName);
			val1.structField = new StructField(chain.fromType, fieldName, fieldType);
			AliasInfo info = aliasMgr.createFieldAlias(chain.fromType, fieldName);
			val1.alias = info.alias;
			JoinElement el = hld.findMatch(chain.fromType, fieldName, hld);
			if (el != null && el.aliasName == null) {
				el.aliasName = info.alias;
				info = aliasMgr.createMainTableAlias(el.relationField.dtype); //TODO fix later
				el.srcAlias = info.alias;
			}
		}
	}
	private void doFilterPKVal(FilterVal val1, HLDQuery hld) {
		TypePair pkpair = DValueHelper.findPrimaryKeyFieldPair(hld.fromType);
		String fieldName = pkpair.name;
		val1.structField = new StructField(hld.fromType, fieldName, pkpair.type);
		val1.alias = hld.fromAlias;
	}

}