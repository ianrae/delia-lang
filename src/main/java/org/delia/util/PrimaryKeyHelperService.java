package org.delia.util;

import java.util.Map;

import org.delia.compiler.ast.BooleanExp;
import org.delia.compiler.ast.Exp;
import org.delia.compiler.ast.FilterOpExp;
import org.delia.compiler.ast.FilterOpFullExp;
import org.delia.compiler.astx.XNAFMultiExp;
import org.delia.compiler.astx.XNAFNameExp;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.db.QuerySpec;
import org.delia.dval.DValueExConverter;
import org.delia.runner.FilterEvaluator;
import org.delia.type.DStructType;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.type.TypePair;

public class PrimaryKeyHelperService extends ServiceBase {

	private DTypeRegistry registry;

	public PrimaryKeyHelperService(FactoryService factorySvc, DTypeRegistry registry) {
		super(factorySvc);
		this.registry = registry;
	}

	public boolean addPrimaryKeyIfMissing(QuerySpec spec, DValue partialVal) {
		TypePair keyPair = DValueHelper.findPrimaryKeyFieldPair(partialVal.getType());
		DStructType dtype = (DStructType) partialVal.getType();
		if (spec.queryExp.filter.cond instanceof FilterOpFullExp) {
			String filterField = extractFieldName(spec);
			if (filterField != null) {
				//TODO: what to check here??
				return false;
			}
		} else if (dtype.fieldIsSerial(keyPair.name)) {
			DeliaExceptionHelper.throwError("upsert-filter-error", "cannot upsert using a serial primary key filter.");
		}
		
		DValue inner = DValueHelper.findPrimaryKeyValue(partialVal);
		if (inner == null) {
			if (spec.queryExp.filter.cond instanceof BooleanExp) {
				DeliaExceptionHelper.throwError("upsert-filter-error", "[true] not supported");
//			} else if (spec.queryExp.filter.cond instanceof FilterOpFullExp) {
//				DeliaExceptionHelper.throwError("upsert-filter-error", "only primary key filters are supported");
			}
			
			FilterEvaluator evaluator = spec.evaluator;

			DValueExConverter dvalConverter = new DValueExConverter(factorySvc, registry);
			String keyField = evaluator.getRawValue(); //we assume primary key. eg Customer[55]
			inner = dvalConverter.buildFromObject(keyField, keyPair.type);
			
			Map<String,DValue> map = partialVal.asMap();
			map.put(keyPair.name, inner);
			return true;
		} else {
			return false;
		}
	}
		
	private String extractFieldName(QuerySpec spec) {
		if (spec.queryExp.filter.cond instanceof FilterOpFullExp) {
			FilterOpFullExp fullexp = (FilterOpFullExp) spec.queryExp.filter.cond;
			if (fullexp.opexp1 instanceof FilterOpExp) {
				FilterOpExp foexp = (FilterOpExp) fullexp.opexp1;
				String fieldName = extractFieldNameFromFilterOp(foexp.op1);
				if (fieldName != null) {
					return fieldName;
				}
				return extractFieldNameFromFilterOp(foexp.op2);
			}
		}
		return null;
	}

	private String extractFieldNameFromFilterOp(Exp op1) {
		if (op1 instanceof XNAFMultiExp) {
			XNAFMultiExp xx = (XNAFMultiExp) op1;
			if (!xx.qfeL.isEmpty() && xx.qfeL.get(0) instanceof XNAFNameExp) {
				XNAFNameExp xne = (XNAFNameExp) xx.qfeL.get(0);
				if (xne.argL.isEmpty()) {
					return xne.funcName;
				}
			}
		}
		return null;
	}

	public void removePrimayKey(DValue dval) {
		TypePair keyPair = DValueHelper.findPrimaryKeyFieldPair(dval.getType());
		Map<String,DValue> map = dval.asMap();
		map.remove(keyPair.name);
	}
}
