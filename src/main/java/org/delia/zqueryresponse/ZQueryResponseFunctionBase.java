package org.delia.zqueryresponse;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.delia.compiler.ast.Exp;
import org.delia.compiler.ast.IntegerExp;
import org.delia.compiler.ast.QueryFuncExp;
import org.delia.core.FactoryService;
import org.delia.queryresponse.QueryFuncContext;
import org.delia.runner.QueryResponse;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.util.DValueHelper;
import org.delia.valuebuilder.ScalarValueBuilder;

public abstract class ZQueryResponseFunctionBase implements ZQueryResponseFunction {

		protected DTypeRegistry registry;
		
		public ZQueryResponseFunctionBase(DTypeRegistry registry) {
			this.registry = registry;
		}
		public abstract QueryResponse process(QueryFuncExp qfe, QueryResponse qresp, QueryFuncContext ctx);
		
		protected String getStringArg(QueryFuncExp qfe, QueryFuncContext ctx) {
			String s = qfe.argL.get(0).strValue();
			return s;
		}
		protected int getIntArg(QueryFuncExp qfe, QueryFuncContext ctx) {
			Exp arg = qfe.argL.get(0);
			IntegerExp nexp = (IntegerExp) arg;
			return nexp.val;
		}
		
//		protected void ensureFieldExists(QueryResponse qresp, String fnName, String fieldName) {
//			if (CollectionUtils.isEmpty(qresp.dvalList)) {
//				return;
//			}
//			DValue dval = qresp.dvalList.get(0);
//			DValueHelper.throwIfFieldNotExist(fnName, fieldName, dval);
//		}
		protected void ensureFieldExists(List<DValue> dvalList, String fnName, String fieldName) {
			if (CollectionUtils.isEmpty(dvalList)) {
				return;
			}
			DValue dval = dvalList.get(0);
			DValueHelper.throwIfFieldNotExist(fnName, fieldName, dval);
		}
		
		protected void setSingletonResult(QueryResponse qresp, DValue dval) {
			qresp.dvalList = new ArrayList<>();
			qresp.dvalList.add(dval);
			//TODO: should we create a new qresp obj??
		}
		
		protected DValue buildIntVal(int max) {
			ScalarValueBuilder builder = new ScalarValueBuilder(null, registry);
			DValue dval = builder.buildInt(max);
			return dval;
		}
		protected DValue buildLongVal(long max) {
			ScalarValueBuilder builder = new ScalarValueBuilder(null, registry);
			DValue dval = builder.buildLong(max);
			return dval;
		}
		protected DValue buildNumberVal(double max) {
			ScalarValueBuilder builder = new ScalarValueBuilder(null, registry);
			DValue dval = builder.buildNumber(max);
			return dval;
		}
		protected DValue buildBoolVal(boolean b) {
			ScalarValueBuilder builder = new ScalarValueBuilder(null, registry);
			DValue dval = builder.buildBoolean(b);
			return dval;
		}
		protected DValue buildStringVal(String s) {
			ScalarValueBuilder builder = new ScalarValueBuilder(null, registry);
			DValue dval = builder.buildString(s);
			return dval;
		}
		protected DValue buildDateVal(Date dt, FactoryService factorySvc) {
			ScalarValueBuilder builder = new ScalarValueBuilder(factorySvc, registry);
			DValue dval = builder.buildDate(dt);
			return dval;
		}
		
	}