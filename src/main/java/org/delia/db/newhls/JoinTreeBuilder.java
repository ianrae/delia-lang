package org.delia.db.newhls;

import java.util.List;
import java.util.Optional;

import org.delia.db.newhls.cond.FilterFunc;
import org.delia.db.newhls.cond.FilterVal;
import org.delia.db.newhls.cond.OpFilterCond;
import org.delia.relation.RelationCardinality;
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

		for(QueryFnSpec fnspec: hld.funcL) {
			//if parent does .orderBy('addr') then we need a join.
			if (fnspec.filterFn.fnName.equals("orderBy")) {
				addOrderBy(fnspec, hld.joinL);
			}
		}

		//TODO: add throughChain

		if (hld.filter instanceof OpFilterCond) {
			OpFilterCond ofc = (OpFilterCond) hld.filter;
			addImplicitJoin(hld.fromType, ofc.val1, hld.joinL);
			addImplicitJoin(hld.fromType, ofc.val2, hld.joinL);
		}
		//TODO: do like and IN filters too, and AndOr
	}

	private void addImplicitJoin(DStructType fromType, FilterVal fval, List<JoinElement> resultL) {
		if (fval.isSymbol()) {
			String fieldName = fval.asSymbol();
			addFieldJoinIfNeeded(fromType, fieldName, resultL);
		} else if (fval.isFn()) {
			FilterFunc filterFn = fval.asFunc();
			for(FilterVal inner: filterFn.argL) {
				addImplicitJoin(fromType, inner, resultL); //*** recursion ***
			}
		}
	}

	private void addFieldJoinIfNeeded(DStructType fromType, String fieldName, List<JoinElement> resultL) {
		TypePair pair = DRuleHelper.findMatchingStructPair(fromType, fieldName);
		if (pair != null) {
			JoinElement el = buildElement(fromType, fieldName, (DStructType) pair.type);
			switch(el.relinfo.cardinality) {
			case ONE_TO_ONE:
			case ONE_TO_MANY:
				if (el.relinfo.isParent) {
					addElement(el, resultL);
				}
				break;
			case MANY_TO_MANY:
				addElement(el, resultL);
				break;
			}
		}
	}

	private void addOrderBy(QueryFnSpec fnspec, List<JoinElement> resultL) {
		DStructType structType = fnspec.structField.dtype;
		String fieldName = fnspec.structField.fieldName;
		TypePair pair = DRuleHelper.findMatchingPair(structType, fieldName);
		//ignore sort by scalar fields
		if (pair != null && pair.type instanceof DStructType) {
			//				addElement(structType, fieldName, (DStructType) pair.type, resultL);
			JoinElement el = buildElement(structType, fieldName, (DStructType) pair.type);
			addElement(el, resultL);
		}
	}

	private void addFKs(FetchSpec spec, List<JoinElement> resultL) {
		DStructType structType = spec.structType;
		for(TypePair pair: structType.getAllFields()) {
			if (pair.type.isStructShape()) {
				RelationInfo relinfo = DRuleHelper.findMatchingRuleInfo(structType, pair);
				if (relinfo.isParent || RelationCardinality.MANY_TO_MANY.equals(relinfo.cardinality)) {
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
	private void addElement(JoinElement el, List<JoinElement> resultL) {
		String target = el.toString();
		Optional<JoinElement> optExisting = resultL.stream().filter(x -> x.toString().equals(target)).findAny();
		if (optExisting.isPresent()) {
//			if (el.usedForFK()) {
//				optExisting.get().usedForFK = true; //propogate
//			}
//			if (el.usedForFetch) {
//				optExisting.get().usedForFetch = true; //propogate
//			}
			return;
		}

		resultL.add(el);
	}
}