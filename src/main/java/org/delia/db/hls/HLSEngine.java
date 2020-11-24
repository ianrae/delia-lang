package org.delia.db.hls;

import java.util.Arrays;
import java.util.List;

import org.delia.compiler.ast.IntegerExp;
import org.delia.compiler.ast.QueryExp;
import org.delia.compiler.ast.QueryFieldExp;
import org.delia.compiler.ast.QueryFuncExp;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.queryresponse.LetSpan;
import org.delia.queryresponse.function.ZQueryResponseFunctionFactory;
import org.delia.type.DStructType;
import org.delia.type.DType;
import org.delia.type.DTypeRegistry;
import org.delia.type.TypePair;
import org.delia.util.DValueHelper;

public class HLSEngine extends ServiceBase {

		private QueryExp queryExp;
		private List<LetSpan> spanL;
		private DTypeRegistry registry;
		private DStructType mainStructType;
		private ZQueryResponseFunctionFactory fnFactory;

		public HLSEngine(FactoryService factorySvc, DTypeRegistry registry) {
			super(factorySvc);
			this.registry = registry;
			this.fnFactory = new ZQueryResponseFunctionFactory(factorySvc, null);
		}
		
		public HLSQueryStatement generateStatement(QueryExp queryExp, List<LetSpan> spanL) {
			this.queryExp = queryExp;
			this.spanL = spanL;
			this.mainStructType = (DStructType) registry.getType(queryExp.typeName);
			
			HLSQueryStatement hlstatement = new HLSQueryStatement();
			hlstatement.queryExp = queryExp;
			
			if (spanL.isEmpty()) {
				HLSQuerySpan hsltat = generateSpan(0, null);
				hlstatement.hlspanL.add(hsltat);
				return hlstatement;
			}
			
			//for some reason Customer[55].addr puts addr in span1.
//			if (spanL.size() == 1) {
//				LetSpan span1 = fixup(spanL.get(0));
//				if (span1 != null) {
//					spanL.add(0, span1); //insert as new first span
//				}
//			}
			
			int i = 0;
			for(LetSpan span: spanL) {
				HLSQuerySpan hlspan = generateSpan(i, span);
				chkSpan(hlspan);
				hlstatement.hlspanL.add(hlspan);
				i++;
			}
			return hlstatement;
		}
		

		private void chkSpan(HLSQuerySpan hlspan) {
			//rField may be field from next span. don't check here. TODO add check later
//			if (hlspan.rEl != null) {
//				chkField("R", hlspan, hlspan.rEl.rfieldPair);
//			}
			if (hlspan.fEl != null) {
				chkField("F", hlspan, hlspan.fEl.fieldPair);
			}
			
			if (hlspan.subEl != null) {
				for(String fieldName: hlspan.subEl.fetchL) {
					DValueHelper.throwIfFieldNotExist("SUB", fieldName, hlspan.fromType);
				}
			}
			
			if (hlspan.oloEl != null && hlspan.oloEl.orderBy != null) {
				DValueHelper.throwIfFieldNotExist("OLO", hlspan.oloEl.orderBy, hlspan.fromType);
			}
		}

		private void chkField(String prefix, HLSQuerySpan hlspan, TypePair pair) {
			DValueHelper.throwIfFieldNotExist(prefix, pair.name, hlspan.fromType);
		}

//		public LetSpan fixup(LetSpan span) {
//			HLSQuerySpan hlstat = new HLSQuerySpan();
//			hlstat.fromType = determineFromType(0);
//			hlstat.mtEl = new MTElement(hlstat.fromType);
//			hlstat.resultType = determineResultType(0);
//			hlstat.filEl = null;
//			
//			TypePair rfieldPair = findLastRField(0);
//			if (rfieldPair != null) {
//				LetSpan span1 = new LetSpan(mainStructType);
//				QueryFuncExp rqfe = findRFieldQFE(span, mainStructType);
////				span.qfeL.remove(rqfe);
////				span1.qfeL.add(rqfe);
//				span1.qresp = span.qresp;
////				span.startsWithScopeChange = true;
//				return span1;
//			}
//			return null;
//		}		
		
		public HLSQuerySpan generateSpan(int i, LetSpan span) {
			HLSQuerySpan hlstat = new HLSQuerySpan();
			hlstat.fromType = determineFromType(i);
			hlstat.mtEl = new MTElement(hlstat.fromType);
			hlstat.resultType = determineResultType(i);
			hlstat.filEl = (i > 0) ? null : new FILElement(queryExp);
			
			if (spanL.isEmpty()) {
				return hlstat;
			}
			
			TypePair rfieldPair = findLastRField(i);
			if (rfieldPair != null) {
				hlstat.rEl = new RElement(rfieldPair);
			}
			
			TypePair fieldPair = findLastField(i);
			if (fieldPair != null) {
				hlstat.fEl = new FElement(fieldPair);
			}
			
			fillGElements(i, hlstat.gElList);

			hlstat.subEl = buildSubEl(i);
			hlstat.oloEl = buildOLO(i);
			
			//adjustments
			if (hlstat.fEl != null && hlstat.subEl != null) {
				if (hlstat.subEl.containsFetch()) {
					hlstat.subEl.fetchL.clear(); //fetch not needed
					if (hlstat.subEl.isEmpty()) {
						hlstat.subEl = null; //remove completely
					}
				}
			}
			
			return hlstat;
		}

		private OLOElement buildOLO(int iStart) {
			OLOElement oloel = new OLOElement();
			boolean found = false;
			int i = 0;
			for(LetSpan span: spanL) {
				if (i < iStart) {
					i++;
					continue;
				} else if (i > iStart) {
					break;
				}
				
				for(QueryFuncExp qfe: span.qfeL) {
					if (qfe instanceof QueryFieldExp) {
					} else if (qfe.funcName.equals("orderBy")){
						String fieldName = qfe.argL.get(0).strValue();
						oloel.orderBy = fieldName;
						oloel.isAsc = qfe.argL.size() == 1 ? true : qfe.argL.get(1).strValue().equals("asc");
						found = true;
					} else if (qfe.funcName.equals("limit")){
						IntegerExp exp = (IntegerExp) qfe.argL.get(0);
						oloel.limit = exp.val;
						found = true;
					} else if (qfe.funcName.equals("offset")){
						IntegerExp exp = (IntegerExp) qfe.argL.get(0);
						oloel.offset = exp.val;
						found = true;
					}
				}
				i++;
			}
			return found ? oloel : null;
		}

		private SUBElement buildSubEl(int iStart) {
			SUBElement subel = new SUBElement();
			boolean found = false;
			int i = 0;
			for(LetSpan span: spanL) {
				if (i < iStart) {
					i++;
					continue;
				} else if (i > iStart) {
					break;
				}
				
				for(QueryFuncExp qfe: span.qfeL) {
					if (qfe instanceof QueryFieldExp) {
					} else if (qfe.funcName.equals("fetch")){
						String fieldName = qfe.argL.get(0).strValue();
						subel.fetchL.add(fieldName);
						found = true;
					} else if (qfe.funcName.equals("fks")){
						subel.allFKs = true;
						found = true;
					}
				}
				i++;
			}
			return found ? subel : null;
		}

		private void fillGElements(int iStart, List<GElement> gElList) {
			int i = 0;
			for(LetSpan span: spanL) {
				if (i < iStart) {
					i++;
					continue;
				} else if (i > iStart) {
					break;
				}
				
				for(QueryFuncExp qfe: span.qfeL) {
					if (qfe instanceof QueryFieldExp) {
					} else if (! isOLOFn(qfe) && !isSUBFn(qfe)){
						GElement gel = new GElement(qfe);
						gElList.add(gel);
					}
				}
				i++;
			}
		}

		private boolean isOLOFn(QueryFuncExp qfe) {
			String fnName = qfe.funcName;
			String[] ar = {"orderBy", "limit", "offset"};
			List<String> oloList = Arrays.asList(ar);
			return oloList.contains(fnName);
		}
		private boolean isSUBFn(QueryFuncExp qfe) {
			String fnName = qfe.funcName;
			String[] ar = {"fetch", "fks"};
			List<String> oloList = Arrays.asList(ar);
			return oloList.contains(fnName);
		}

		private TypePair findLastField(int iStart) {
			DStructType currentType = mainStructType;
			TypePair lastField = null;
			int i = 0;
			for(LetSpan span: spanL) {
				if (i < iStart) {
					i++;
					continue;
				} else if (i > iStart) {
					break;
				}
				
				TypePair fieldPair = findFField(span, currentType);
				if (fieldPair != null) {
					lastField = fieldPair;
				}
				i++;
			}
			return lastField;
		}
		private TypePair findLastRField(int iStart) {
			DStructType currentType = mainStructType;
			TypePair lastRField = null;
			int i = 0;
			for(LetSpan span: spanL) {
				if (i < iStart) {
					i++;
					continue;
				} else if (i > iStart) {
					break;
				}
				
				TypePair rfieldPair = findRField(span, currentType);
				if (rfieldPair != null) {
					currentType = (DStructType) rfieldPair.type;
					lastRField = rfieldPair;
				}
				i++;
			}
			return lastRField;
		}

		private DStructType determineFromType(int iStart) {
			DStructType currentType = mainStructType;
			int i = 0;
			for(LetSpan span: spanL) {
				if (i < iStart) {
					i++;
					continue;
				} else if (i > iStart) {
					break;
				}
				
				TypePair rfieldPair = findRField(span, currentType);
				if (rfieldPair != null) {
					currentType = (DStructType) rfieldPair.type;
				}
				i++;
			}
			
			return currentType;
		}

		private DType determineResultType(int iStart) {
			DStructType currentType = mainStructType;
			DType resultType = currentType;
			
			int i = 0;
			for(LetSpan span: spanL) {
				if (i < iStart) {
					i++;
					continue;
				} else if (i > iStart) {
					break;
				}
				
				TypePair rfieldPair = findRField(span, currentType);
				if (rfieldPair != null) {
					currentType = (DStructType) rfieldPair.type;
					resultType = currentType;
				}
				
				TypePair fieldPair = findFField(span, currentType);
				if (fieldPair != null) {
					resultType = fieldPair.type;
				}
				
				DType gtype = findGType(span, currentType);
				if (gtype != null) {
					resultType = gtype;
				}
				i++;
			}
			
			return resultType;
		}

		private DType findGType(LetSpan span, DStructType currentType) {
			DType gtype = null;
			QueryFuncExp currentField = null;
			for(QueryFuncExp qfe: span.qfeL) {
				if (qfe instanceof QueryFieldExp) {
					currentField = qfe;
				} else {
					DType dtype = fnFactory.getResultType(qfe, currentType, currentField, registry);
					if (dtype != null) {
						gtype = dtype;
					}
				}
			}
			return gtype;
		}

		private TypePair findFField(LetSpan span, DStructType currentType) {
			for(QueryFuncExp qfe: span.qfeL) {
				if (qfe instanceof QueryFieldExp) {
					String fieldName = qfe.funcName;
					TypePair pair = DValueHelper.findField(currentType, fieldName);
					if (pair != null && !pair.type.isStructShape()) {
						return pair;
					}
				}
			}
			return null;
		}
		private TypePair findRField(LetSpan span, DStructType currentType) {
			for(QueryFuncExp qfe: span.qfeL) {
				if (qfe instanceof QueryFieldExp) {
					String fieldName = qfe.funcName;
					TypePair pair = DValueHelper.findField(currentType, fieldName);
					if (pair != null && pair.type.isStructShape()) {
						return pair;
					}
				}
			}
			return null;
		}
		private QueryFuncExp findRFieldQFE(LetSpan span, DStructType currentType) {
			for(QueryFuncExp qfe: span.qfeL) {
				if (qfe instanceof QueryFieldExp) {
					String fieldName = qfe.funcName;
					TypePair pair = DValueHelper.findField(currentType, fieldName);
					if (pair != null && pair.type.isStructShape()) {
						return qfe;
					}
				}
			}
			return null;
		}
		

		
		
	}