package org.delia.db.newhls;

import java.util.List;
import java.util.Optional;

import org.delia.db.newhls.cond.FilterFunc;
import org.delia.db.newhls.cond.FilterVal;
import org.delia.db.newhls.cond.OpFilterCond;
import org.delia.db.newhls.cond.SymbolChain;
import org.delia.relation.RelationInfo;
import org.delia.type.DStructType;
import org.delia.type.TypePair;
import org.delia.util.DRuleHelper;

/**
 * Detects all the explicit and implicit joins needed by a query.
 * Builds the contents of hld.joinL
 * @author ian
 *
 */
public class JoinTreeBuilder {
	public void generateJoinTree(HLDQuery hld) {
		for(FetchSpec spec: hld.fetchL) {
			if (spec.isFK) {
				addFKs(spec, hld.joinL);
			} else {
				addFetch(spec, hld.joinL);
			}
		}
		
		for(RelationField rf: hld.throughChain) {
			addThroughChain(rf, hld);
		}
		
		if (hld.finalField != null) {
			addFinalFieldFetchIfNeeded(hld);
		}

		for(QueryFnSpec fnspec: hld.funcL) {
			if (fnspec.filterFn.fnName.equals("orderBy")) {
				//if parent does .orderBy('addr') then we need a join.
				addOrderBy(fnspec, hld.joinL);
			}
		}

		if (hld.filter instanceof OpFilterCond) {
			OpFilterCond ofc = (OpFilterCond) hld.filter;
			addImplicitJoin(hld.fromType, ofc.val1, hld.joinL);
			addImplicitJoin(hld.fromType, ofc.val2, hld.joinL);
		}
		//TODO: do like and IN filters too, and AndOr
	}

	private void addThroughChain(RelationField rf, HLDQuery hld) {
		addFieldJoinIfNeeded(rf.dtype, rf.fieldName, hld.joinL, true);
	}

	private void addFinalFieldFetchIfNeeded(HLDQuery hld) {
		StructField sf = hld.finalField.structField;
		if (! sf.fieldType.isStructShape()) {
			return;
		}
		
		addFieldJoinIfNeeded(sf.dtype, sf.fieldName, hld.joinL);
	}

	private void addImplicitJoin(DStructType fromType, FilterVal fval, List<JoinElement> resultL) {
		if (fval.isSymbol()) {
			String fieldName = fval.asSymbol();
			addFieldJoinIfNeeded(fromType, fieldName, resultL);
		} else if (fval.isSymbolChain()) {
			String fieldName = fval.asString();
			SymbolChain chain = fval.asSymbolChain();
			chain.el = addFieldJoinIfNeeded(chain.fromType, fieldName, resultL, true);
		} else if (fval.isFn()) {
			//do args
			FilterFunc filterFn = fval.asFunc();
			for(FilterVal inner: filterFn.argL) {
				addImplicitJoin(fromType, inner, resultL); //*** recursion ***
			}
		}
	}

	private void addFieldJoinIfNeeded(DStructType fromType, String fieldName, List<JoinElement> resultL) {
		addFieldJoinIfNeeded(fromType, fieldName, resultL, false);
	}
	private JoinElement addFieldJoinIfNeeded(DStructType fromType, String fieldName, List<JoinElement> resultL, boolean forceAdd) {
		TypePair pair = DRuleHelper.findMatchingStructPair(fromType, fieldName);
		if (pair != null) {
			JoinElement el = buildElement(fromType, fieldName, (DStructType) pair.type);
			if (forceAdd) {
				return addElement(el, resultL);
			} else {
				if (el.relinfo.notContainsFK()) {
					return addElement(el, resultL);
				}
			}
		}
		return null;
	}

	private void addOrderBy(QueryFnSpec fnspec, List<JoinElement> resultL) {
		DStructType structType = fnspec.structField.dtype;
		String fieldName = fnspec.structField.fieldName;
		TypePair pair = DRuleHelper.findMatchingPair(structType, fieldName);
		//ignore sort by scalar fields
		if (pair != null && pair.type instanceof DStructType) {
			//				addElement(structType, fieldName, (DStructType) pair.type, resultL);
			JoinElement el = buildElement(structType, fieldName, (DStructType) pair.type);
			if (el.relinfo.notContainsFK()) {
				addElement(el, resultL);
			}
		}
	}

	private void addFKs(FetchSpec spec, List<JoinElement> resultL) {
		DStructType structType = spec.structType;
		for(TypePair pair: structType.getAllFields()) {
			if (pair.type.isStructShape() && pair.name.equals(spec.fieldName)) {
				RelationInfo relinfo = DRuleHelper.findMatchingRuleInfo(structType, pair);
				if (relinfo.notContainsFK()) {
					JoinElement el = buildElement(structType, pair.name, (DStructType) pair.type);
					el.fetchSpec = spec;
					addElement(el, resultL);
				}
			}
		}
	}
	private void addFetch(FetchSpec spec, List<JoinElement> resultL) {
		DStructType structType = spec.structType;
		String fieldName = spec.fieldName;
		TypePair pair = DRuleHelper.findMatchingStructPair(structType, fieldName);
		if (pair != null) {
			JoinElement el = buildElement(structType, pair.name, (DStructType) pair.type);
			el.fetchSpec = spec;
			addElement(el, resultL);
		}
	}
	private JoinElement buildElement(DStructType dtype, String field, DStructType fieldType) {
		JoinElement el = new JoinElement();
		el.relationField = new RelationField(dtype, field, fieldType);
		el.relinfo = DRuleHelper.findMatchingRuleInfo(dtype, el.createPair());
		return el;
	}
	private JoinElement addElement(JoinElement el, List<JoinElement> resultL) {
		Optional<JoinElement> optExisting = resultL.stream().filter(x -> isRelMatch(x, el)).findAny();
		if (optExisting.isPresent()) {
			if (optExisting.get().usedForFK()) {
				if (el.usedForFetch()) {  //full fetch?
					optExisting.get().fetchSpec.isFK = false; //upgrade from fk to fetch
				} else if (el.fetchSpec == null) {
					optExisting.get().fetchSpec.isFK = false; //upgrade
				}
			}
			return optExisting.get();
		}

		resultL.add(el);
		return el;
	}
	private boolean isRelMatch(JoinElement x, JoinElement el) {
		RelationField rf1 = x.relationField;
		RelationField rf2 = el.relationField;
		if (rf1.dtype == rf2.dtype && rf1.fieldName.equals(rf2.fieldName)) {
			return true;
		}
		return false;
	}
}