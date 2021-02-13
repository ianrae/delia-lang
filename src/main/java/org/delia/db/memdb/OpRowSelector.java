package org.delia.db.memdb;

import java.util.ArrayList;
import java.util.List;

import org.delia.compiler.ast.Exp;
import org.delia.compiler.ast.FilterOpExp;
import org.delia.compiler.ast.FilterOpFullExp;
import org.delia.compiler.ast.IdentExp;
import org.delia.compiler.astx.XNAFMultiExp;
import org.delia.core.DateFormatService;
import org.delia.core.FactoryService;
import org.delia.db.QuerySpec;
import org.delia.db.memdb.filter.InEvaluator;
import org.delia.db.memdb.filter.MultiOpEvaluator;
import org.delia.db.memdb.filter.OpEvaluator;
import org.delia.db.memdb.filter.OpFactory;
import org.delia.error.ErrorTracker;
import org.delia.runner.FilterEvaluator;
import org.delia.type.DStructType;
import org.delia.type.DType;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.util.DValueHelper;

public class OpRowSelector extends RowSelectorBase {
		private OpEvaluator evaluator;
		private DateFormatService fmtSvc;
		private FactoryService factorySvc;
		private FilterEvaluator filterEvaluator;
		
		public OpRowSelector(DateFormatService fmtSvc, FactoryService factorySvc, FilterEvaluator evaluator2) {
			this.fmtSvc = fmtSvc;
			this.factorySvc = factorySvc;
			this.filterEvaluator = evaluator2;
		}

		@Override
		public void init(ErrorTracker et, QuerySpec spec, DStructType dtype, DTypeRegistry registry) {
			super.init(et, spec, dtype, registry);
			
			FilterOpFullExp fullexp = (FilterOpFullExp) filter.cond;
			
			if (fullexp.isSingleFilterOpExp()) {
				initSingleOpExpression(fullexp, dtype);
			} else if (fullexp.isSingleQueryInExp()) {
				initSingleInExpression(fullexp, dtype);
			} else {
				//AND, OR, etc
				initMultipleOpExpression(fullexp, dtype);
			}
		}
		
		private void initMultipleOpExpression(FilterOpFullExp fullexp, DStructType dtype) {
			this.evaluator = new MultiOpEvaluator(fullexp, dtype, registry, fmtSvc, factorySvc);
		}

		private void initSingleOpExpression(FilterOpFullExp fullexp, DStructType dtype) {
			FilterOpExp foexp = (FilterOpExp) fullexp.opexp1;
			
			Exp xop1 = foexp.getFirstArg();
			Exp xop2 = foexp.getSecondArg();
			
			//handle int-long issues by determining field type
			DType op1HintType = null;
			DType op2HintType = null;
			if (xop1 instanceof IdentExp) {
				String fieldOrVarOrFn = ((IdentExp)xop1).strValue();
				op1HintType = DValueHelper.findFieldType(dtype, fieldOrVarOrFn);
			}
			if (xop2 instanceof IdentExp) {
				String fieldOrVarOrFn = ((IdentExp)xop2).strValue();
				op2HintType = DValueHelper.findFieldType(dtype, fieldOrVarOrFn);
			}
			
			OpFactory factory = new OpFactory(registry, fmtSvc, factorySvc, dtype);
			this.evaluator = factory.create(foexp.op, xop1, xop2, op1HintType, op2HintType, fullexp.negFlag);
			//support id < 10 and also 10 < id
			boolean reversed = (xop2 instanceof IdentExp) || (xop2 instanceof XNAFMultiExp);
			if (reversed) {
				evaluator.setRightVar(xop1);
			} else {
				evaluator.setRightVar(xop2);
			}
		}
		private void initSingleInExpression(FilterOpFullExp fullexp, DStructType dtype) {
			this.evaluator = new InEvaluator(fullexp, dtype, filterEvaluator);
		}


		@Override
		public List<DValue> match(List<DValue> list) {
			List<DValue> resultL = new ArrayList<>();
			for(DValue dval: list) {
				boolean b = evaluator.match(dval);
				if (b) {
					resultL.add(dval);
				}
			}
			return resultL;
		}
	}