package org.delia.db.hls.join;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.delia.compiler.ast.Exp;
import org.delia.compiler.ast.FilterOpExp;
import org.delia.compiler.ast.FilterOpFullExp;
import org.delia.compiler.ast.QueryExp;
import org.delia.compiler.ast.QueryFuncExp;
import org.delia.compiler.astx.XNAFMultiExp;
import org.delia.compiler.astx.XNAFNameExp;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.queryresponse.LetSpan;
import org.delia.relation.RelationCardinality;
import org.delia.relation.RelationInfo;
import org.delia.type.DStructType;
import org.delia.type.DTypeRegistry;
import org.delia.type.TypePair;
import org.delia.util.DRuleHelper;

public class JoinTreeEngine extends ServiceBase {
	private DTypeRegistry registry;
	//	private ZQueryResponseFunctionFactory fnFactory;

	public JoinTreeEngine(FactoryService factorySvc, DTypeRegistry registry) {
		super(factorySvc);
		this.registry = registry;

		//		this.fnFactory = new ZQueryResponseFunctionFactory(factorySvc, null); //fetchRunner not needed here
	}

	public List<JTElement> parse(QueryExp queryExp, List<LetSpan> spanL) {
		List<JTElement> resultL = new ArrayList<>();
		DStructType structType = (DStructType) registry.getType(queryExp.typeName);
		for(LetSpan span: spanL) {
			if (!span.qfeL.isEmpty()) {
				for( QueryFuncExp qfe:span.qfeL) {
					if (qfe.funcName.equals("fks")) {
						addFKs(span, resultL);
					} else if (qfe.funcName.equals("fetch")) {
						addFetch(span, qfe, resultL);
					} else if (qfe.funcName.equals("orderBy")) {
						addOrderBy(span, qfe, resultL);
					} else {
						String fieldName = qfe.funcName;
						TypePair pair = DRuleHelper.findMatchingStructPair(structType, fieldName);
						if (pair != null) {
							addElement(structType, fieldName, (DStructType) pair.type, resultL);
						}
					}
				}
			}
		}

		if (queryExp.filter != null && queryExp.filter.cond instanceof FilterOpFullExp) {
			FilterOpFullExp fofe = (FilterOpFullExp) queryExp.filter.cond;
			if (fofe.opexp1 instanceof FilterOpExp) {
				doFilterOpExp(fofe.opexp1, structType, resultL);
			}
			if (fofe.opexp2 instanceof FilterOpExp) {
				doFilterOpExp(fofe.opexp1, structType, resultL);
			}
		}

		return resultL;
	}


	private void doFilterOpExp(Exp opexp1, DStructType structType, List<JTElement> resultL) {
		FilterOpExp foe = (FilterOpExp) opexp1;
		doXNAFMultiExp(foe.op1, structType, resultL);
		doXNAFMultiExp(foe.op2, structType, resultL);
	}

	private void doXNAFMultiExp(Exp op, DStructType structType, List<JTElement> resultL) {
		if (! (op instanceof XNAFMultiExp)) {
			return;
		}
		XNAFMultiExp xx = (XNAFMultiExp) op;
		if (!xx.qfeL.isEmpty() && xx.qfeL.get(0) instanceof XNAFNameExp) {
			XNAFNameExp xne = (XNAFNameExp) xx.qfeL.get(0);

			String fieldName = xne.strValue();
			TypePair pair = DRuleHelper.findMatchingStructPair(structType, fieldName);
			if (pair != null) {

				JTElement el = buildElement(structType, fieldName, (DStructType) pair.type);
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
	}

	private void addFetch(LetSpan span, QueryFuncExp qfe, List<JTElement> resultL) {
		DStructType structType = (DStructType) span.dtype;
		String fieldName = qfe.argL.get(0).strValue();
		TypePair pair = DRuleHelper.findMatchingStructPair(structType, fieldName);
		if (pair != null) {
			JTElement el = addElement(structType, fieldName, (DStructType) pair.type, resultL);
			el.usedForFetch = true;
		}
	}

	private void addFKs(LetSpan span, List<JTElement> resultL) {
		DStructType structType = (DStructType) span.dtype;
		for(TypePair pair: structType.getAllFields()) {
			if (pair.type.isStructShape()) {
				RelationInfo relinfo = DRuleHelper.findMatchingRuleInfo(structType, pair);
				if (relinfo.isParent || RelationCardinality.MANY_TO_MANY.equals(relinfo.cardinality)) {
					JTElement el = buildElement((DStructType) span.dtype, pair.name, (DStructType) pair.type);
					el.usedForFK = true;
					addElement(el, resultL);
				}
			}
		}
	}

	private void addOrderBy(LetSpan span, QueryFuncExp qfe, List<JTElement> resultL) {
		DStructType structType = (DStructType) span.dtype;
		String fieldName = qfe.argL.get(0).strValue();
		TypePair pair = DRuleHelper.findMatchingPair(structType, fieldName);
		//ignore sort by scalar fields
		if (pair != null && pair.type instanceof DStructType) {
			addElement(structType, fieldName, (DStructType) pair.type, resultL);
		}
	}

	private JTElement buildElement(DStructType dtype, String field, DStructType fieldType) {
		JTElement el = new JTElement();
		el.dtype = dtype;
		el.fieldName = field;
		el.fieldType = fieldType;
		el.relinfo = DRuleHelper.findMatchingRuleInfo(dtype, el.createPair());
		return el;
	}

	private JTElement addElement(DStructType dtype, String field, DStructType fieldType, List<JTElement> resultL) {
		JTElement el = buildElement(dtype, field, fieldType);
		addElement(el, resultL);
		return el;
	}
	private void addElement(JTElement el, List<JTElement> resultL) {
		String target = el.toString();
		Optional<JTElement> optExisting = resultL.stream().filter(x -> x.toString().equals(target)).findAny();
		if (optExisting.isPresent()) {
			if (el.usedForFK) {
				optExisting.get().usedForFK = true; //propogate
			}
			if (el.usedForFetch) {
				optExisting.get().usedForFetch = true; //propogate
			}
			return;
		}

		resultL.add(el);
	}

}