package org.delia.runner;

import java.util.Date;
import java.util.List;

import org.delia.compiler.ast.Exp;
import org.delia.compiler.ast.IdentExp;
import org.delia.compiler.ast.ListExp;
import org.delia.compiler.ast.QueryExp;
import org.delia.core.DateFormatService;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.error.DeliaError;
import org.delia.type.DValue;
import org.delia.type.Shape;

/**
 * 		public QueryExp queryExp;
	public DValue resolvedFilterVars; //set at runtime. not-thread-safe. TODO -fix

 * @author Ian Rae
 *
 */
public class FilterEvaluator extends ServiceBase {
	public QueryExp queryExp;
	public DValue resolvedFilterVars; //set at runtime. not-thread-safe. TODO -fix
	private VarEvaluator varEvaluator;
	private DateFormatService fmtSvc;

	public FilterEvaluator(FactoryService factorySvc, VarEvaluator varEvaluator) {
		super(factorySvc);
		this.varEvaluator = varEvaluator;
		this.fmtSvc = factorySvc.getDateFormatService();
	}
	
	public void init(QueryExp queryExp) {
		this.queryExp = queryExp;
		if (queryExp.filter != null && queryExp.filter.cond instanceof IdentExp) {
			String varName = queryExp.filter.cond.strValue();
			
			List<DValue> list = varEvaluator.lookupVar(varName);
			if (list.isEmpty() || list.size() > 1) { //TODO later support 0 and > 1!!
				String msg = String.format("var eval failed: %s", varName);
				DeliaError err = et.add("var-eval-failed", msg);
				throw new DeliaException(err);
			}
			
			resolvedFilterVars = list.get(0);
		}
	}
	
	public boolean isEqualTo(DValue dval) {
		Object target = queryExp.filter.cond.strValue();
		if (resolvedFilterVars != null) {
			//TODO support int and other types
			target = resolvedFilterVars.asString();
		}
		return doIsEqualTo(dval, target);
	}
	private boolean doIsEqualTo(DValue dval, Object target) {
		if (dval.getType().isShape(Shape.DATE)) {
			Date dt = fmtSvc.parseLegacy(target.toString());
			return dval.asDate().equals(dt);
		} else {
			String tmp = dval.asString();
			if(tmp != null && tmp.equals(target)) {
				return true;
			}
			return false;
		}
	}

	public boolean isIn(DValue key, ListExp listExp) {
		for(Exp exp: listExp.valueL) {
			if (doIsEqualTo(key, exp.strValue())) {
				return true;
			}
		}
		return false;
	}
	
	public String getRawValue() {
		String target = queryExp.filter.cond.strValue();
		return target;
	}

}