package org.delia.db.sql.prepared;

import org.apache.commons.collections.CollectionUtils;
import org.delia.compiler.ast.QueryExp;
import org.delia.compiler.ast.QueryFieldExp;
import org.delia.compiler.ast.QueryFuncExp;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.db.QuerySpec;
import org.delia.db.SpanHelper;
import org.delia.type.BuiltInTypes;
import org.delia.type.DStructType;
import org.delia.type.DType;
import org.delia.type.DTypeRegistry;
import org.delia.util.DValueHelper;
import org.delia.util.DeliaExceptionHelper;

public class SelectFuncHelper extends ServiceBase {
	protected DTypeRegistry registry;
	private SpanHelper spanHelper;

	public SelectFuncHelper(FactoryService factorySvc, DTypeRegistry registry, SpanHelper spanHelper) {
		super(factorySvc);
		this.registry = registry;
		this.spanHelper = spanHelper;
	}
	
	public DType getSelectResultType(QuerySpec spec) {
		if (this.isCountPresent(spec)) {
			return registry.getType(BuiltInTypes.LONG_SHAPE);
		} else if (isExistsPresent(spec)) {
//			return registry.getType(BuiltInTypes.BOOLEAN_SHAPE);
			return registry.getType(BuiltInTypes.LONG_SHAPE);
		} else if (isMinPresent(spec)) {
			return determineFieldTypeForFn(spec, "min");
		} else if (isMaxPresent(spec)) {
			return determineFieldTypeForFn(spec, "max");
		} else if (isFirstPresent(spec) && findFieldUsingFn(spec, "first") != null) {
			return determineFieldTypeForFn(spec, "first");
		} else if (isLastPresent(spec) && findFieldUsingFn(spec, "last") != null) {
			return determineFieldTypeForFn(spec, "last");
		} else {
			String typeName = spec.queryExp.getTypeName();
			DStructType dtype = registry.findTypeOrSchemaVersionType(typeName);
			
			QueryFuncExp fieldExp = null;
			if (this.spanHelper != null) {
				fieldExp = spanHelper.isTargetAField();
				if (fieldExp != null) {
					return DValueHelper.findFieldType(dtype, fieldExp.funcName);
				}
			}
			
			return dtype;
		}
	}
	

	protected DType determineFieldTypeForFn(QuerySpec spec, String fnName) {
		QueryFieldExp fieldExp = findFieldUsingFn(spec, fnName);
		String typeName = spec.queryExp.getTypeName();
		DStructType dtype = registry.findTypeOrSchemaVersionType(typeName);
		String fieldName = fieldExp.funcName;
		DType fieldType = DValueHelper.findFieldType(dtype, fieldName);
		if (fieldType == null) {
			DeliaExceptionHelper.throwError("unknown-field", "%s - can't find field '%s' in type '%s'", fnName, fieldName, typeName);
		}
		return fieldType;
	}

//	public void doOrderByIfPresent(StrCreator sc, QuerySpec spec, String typeName) {
//		QueryFuncExp qfexp = findFn(spec, "orderBy");
//		if (qfexp == null) {
//			return;
//		}
//		
//		doInnerOrderBy(sc, spec, typeName, qfexp);
//	}
//	public void doInnerOrderBy(StrCreator sc, QuerySpec spec, String typeName, QueryFuncExp qfexp) {
//		DStructType structType = (DStructType) registry.getType(typeName);
//		StringJoiner joiner = new StringJoiner(",");
//		boolean isDesc = false;
//		for(Exp exp : qfexp.argL) {
//			if (exp instanceof IdentExp) {
//				isDesc = exp.strValue().equals("desc");
//			} else {
//				String fieldName = exp.strValue();
//				if (fieldName.contains(".")) {
//					fieldName = StringUtils.substringAfter(fieldName, ".");
//				}
//				if (! DValueHelper.fieldExists(structType, fieldName)) {
//					DeliaExceptionHelper.throwError("unknown-field", "type '%s' does not have field '%s'. Invalid orderBy parameter", typeName, fieldName);
//				}
//				joiner.add(exp.strValue());
//			}
//		}
//		String s = String.format(" ORDER BY %s", joiner.toString());
//		sc.o(s);
//		if (isDesc) {
//			sc.o(" DESC");
//		}
//	}
//	
//	public void doOffsetIfPresent(StrCreator sc, QuerySpec spec, String typeName) {
//		QueryFuncExp qfexp = findFn(spec, "offset");
//		if (qfexp == null) {
//			return;
//		}
//		
//		IntegerExp exp = (IntegerExp) qfexp.argL.get(0);
//		Integer n = exp.val;
//		String s = String.format(" OFFSET %d", n);
//		sc.o(s);
//	}
//	public void doLimitIfPresent(StrCreator sc, QuerySpec spec, String typeName) {
//		QueryFuncExp qfexp = findFn(spec, "limit");
//		if (qfexp == null) {
//			return;
//		}
//		
//		IntegerExp exp = (IntegerExp) qfexp.argL.get(0);
//		Integer n = exp.val;
//		String s = String.format(" LIMIT %d", n);
//		sc.o(s);
//	}
//	public QuerySpec doFirstFixup(QuerySpec specOriginal, String typeName, String alias) {
//		QuerySpec spec = doLastFixup(specOriginal, typeName, alias, true);
//		QueryFuncExp limitFn = this.findFn(spec, "limit");
//		if (limitFn != null) {
//			spec.queryExp.qfelist.remove(limitFn);
//		}
//		
//		QueryFuncExp qfexp1 = new QueryFuncExp(99, new IdentExp("limit"), null, false);
//		qfexp1.argL.add(new IntegerExp(1));
//
//		spec.queryExp.qfelist.add(qfexp1);
//		return spec;
//	}
//	public QuerySpec doLastFixup(QuerySpec specOriginal, String typeName, String alias) {
//		return doLastFixup(specOriginal, typeName, alias, false);
//	}
//	public QuerySpec doLastFixup(QuerySpec specOriginal, String typeName, String alias, boolean asc) {
//		DType dtype = registry.findTypeOrSchemaVersionType(typeName);
//		QueryFieldExp possibleFieldExp = findFieldUsingFn(specOriginal, "last");
//		TypePair pair;
//		if (possibleFieldExp == null) {
//			pair = DValueHelper.findPrimaryKeyFieldPair(dtype);
//		} else {
//			pair = DValueHelper.findField(dtype, possibleFieldExp.funcName);
//		}
//		
//		
//		if (pair == null) { 
//			DeliaExceptionHelper.throwError("last-requires-sortable-field", "last() requires an orderBy() function or a primary key in type '%s'", typeName);
//			return null;
//		} else {
//			QuerySpec spec = makeCopy(specOriginal);
//			QueryFuncExp qfexp1 = new QueryFuncExp(99, new IdentExp("orderBy"), null, false);
//			String s = alias == null ? pair.name : String.format("%s.%s", alias, pair.name);
//			QueryFieldExp qfe = new QueryFieldExp(99, new IdentExp(s));
//			qfexp1.argL.add(qfe);
//			if (! asc) {
//				IdentExp exp1 = new IdentExp("desc");
//				qfexp1.argL.add(exp1);
//			}
//			
//			QueryFuncExp qfexpAlreadyInList = findFn(spec, "last");
//			int index = 0;
//			boolean done = false;
//			for(QueryFuncExp qfexp: spec.queryExp.qfelist) {
//				if (index < spec.queryExp.qfelist.size() - 1) {
//					if (qfexp == qfexpAlreadyInList) {
//						//insert before
//						spec.queryExp.qfelist.add(index, qfexp1);
//						done = true;
//						break;
//					}
//				}
//				index++;
//			}
//			
//			if (! done) {
//				spec.queryExp.qfelist.add(qfexp1);
//			}
//			return spec;
//		}
//	}
//	protected QuerySpec makeCopy(QuerySpec spec) {
//		QuerySpec copy = new QuerySpec();
//		copy.evaluator = spec.evaluator;
//		
//		QueryExp qfe = spec.queryExp;
//		List<QueryFuncExp> qfelist = new ArrayList<>();
//		qfelist.addAll(qfe.qfelist);
//		copy.queryExp = new QueryExp(qfe.pos, new IdentExp(qfe.typeName), qfe.filter, null);
//		copy.queryExp.qfelist = qfelist;
//		
//		return copy;
//	}

//	public boolean isOrderByPresent(QuerySpec spec) {
//		QueryFuncExp qfexp = findFn(spec, "orderBy");
//		return qfexp != null;
//	}
	public boolean isCountPresent(QuerySpec spec) {
		QueryFuncExp qfexp = findFn(spec, "count");
		return qfexp != null;
	}
	public boolean isExistsPresent(QuerySpec spec) {
		QueryFuncExp qfexp = findFn(spec, "exist");
		return qfexp != null;
	}
	public boolean isMinPresent(QuerySpec spec) {
		QueryFuncExp qfexp = findFn(spec, "min");
		return qfexp != null;
	}
	public boolean isMaxPresent(QuerySpec spec) {
		QueryFuncExp qfexp = findFn(spec, "max");
		return qfexp != null;
	}
	public boolean isFirstPresent(QuerySpec spec) {
		QueryFuncExp qfexp = findFn(spec, "first");
		return qfexp != null;
	}
	public boolean isLastPresent(QuerySpec spec) {
		QueryFuncExp qfexp = findFn(spec, "last");
		return qfexp != null;
	}
	//ith done in java for now
//	public boolean isIthPresent(QuerySpec spec) {
//		QueryFuncExp qfexp = findFn(spec, "ith");
//		return qfexp != null;
//	}
	
	public QueryFuncExp findFn(QuerySpec spec, String targetFnName) {
		QueryExp queryExp = spec.queryExp;

		if (CollectionUtils.isNotEmpty(queryExp.qfelist)) {
			for(QueryFuncExp qfexp: queryExp.qfelist) {

				if (qfexp instanceof QueryFieldExp) {
				} else { //it's a fn to run
					String fnName = qfexp.funcName;
					if (fnName.equals(targetFnName)) {
						return qfexp;
					}
				}
			}
		}
		return null;
	}
	
	public String findFieldNameUsingFn(QuerySpec spec, String targetFnName) {
		QueryFieldExp prev = findFieldUsingFn(spec, targetFnName);
		if (prev == null) {
			return null;
		}
		
		determineFieldTypeForFn(spec, targetFnName); //only used for field existence check
		return prev.funcName;
	}
	
	protected QueryFieldExp findFieldUsingFn(QuerySpec spec, String targetFnName) {
		QueryExp queryExp = spec.queryExp;

		QueryFieldExp prev = null;
		if (CollectionUtils.isNotEmpty(queryExp.qfelist)) {
			for(QueryFuncExp qfexp: queryExp.qfelist) {

				if (qfexp instanceof QueryFieldExp) {
					prev = (QueryFieldExp) qfexp;
				} else { //it's a fn to run
					String fnName = qfexp.funcName;
					if (fnName.equals(targetFnName)) {
						return prev;
					}
				}
			}
		}
		return null;
	}

}