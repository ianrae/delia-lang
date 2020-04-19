package org.delia.db.memdb;
//package org.dang.db.memdb;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import org.dang.compiler.ast.QueryInExp;
//import org.dang.db.QuerySpec;
//import org.dang.error.ErrorTracker;
//import org.dang.type.DStructType;
//import org.dang.type.DValue;
//
//public class InSelector extends RowSelectorBase {
//	
//	private QueryInExp inExp;
//
//	@Override
//	public void init(ErrorTracker et, QuerySpec spec, DStructType dtype) {
//		super.init(et, spec, dtype);
//		
//		this.inExp = (QueryInExp) spec.queryExp.filter.cond;
//		
//		this.keyField = inExp.fieldName;
//		if (this.keyField == null) {
//			//err!!
//			wasError = true;
//		}
//	}
//
//	@Override
//	public List<DValue> match(List<DValue> list) {
//		if (keyField == null) {
//			wasError = true;
//			//err!!
//			return null;
//		} else {
//			List<DValue> resultL = new ArrayList<>();
//			for(DValue dval: list) {
//				DValue key = dval.asStruct().getField(keyField);
//				if (key == null) {
//					wasError = true;
//					//err!!
//					return resultL;
//				}
//				
//				if (spec.evaluator.isIn(key, inExp.listExp)) {
//					resultL.add(dval); //only one row
//				}
//			}
//			return resultL;
//		}
//	}
//}