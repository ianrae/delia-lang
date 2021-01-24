package org.delia.zdb.mem;

import org.delia.compiler.ast.BooleanExp;
import org.delia.compiler.ast.Exp;
import org.delia.compiler.ast.FilterExp;
import org.delia.compiler.ast.FilterOpFullExp;
import org.delia.compiler.ast.IdentExp;
import org.delia.compiler.ast.QueryExp;
import org.delia.compiler.ast.QueryFieldExp;
import org.delia.compiler.ast.QueryFuncExp;
import org.delia.compiler.ast.QueryInExp;
import org.delia.core.DateFormatService;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.db.QuerySpec;
import org.delia.dval.DValueConverterService;
import org.delia.runner.QueryResponse;
import org.delia.type.DValue;
import org.delia.util.DValueHelper;

/**
 * On some queries such as [u in [followers]] we need to do a pre-query first to
 * resolve followers into a list of ids.
 * @author ian
 *
 */
public class PreSpecService extends ServiceBase {

	DateFormatService fmtSvc;
	public boolean createTablesAsNeededFlag = true;
//	private MemZDBInterfaceFactory dbInterface;
	private DValueConverterService dvalConverterSvc;

	public PreSpecService(FactoryService factorySvc, MemDBInterfaceFactory dbInterface) {
		super(factorySvc);
//		this.dbInterface = dbInterface;
		this.log = factorySvc.getLog();
		this.et = factorySvc.getErrorTracker();
		this.fmtSvc = factorySvc.getDateFormatService();
		this.dvalConverterSvc = new DValueConverterService(factorySvc);
	}

	//handle things like [55 in [followers]]. need to resolve followers to a list of ids
	public QuerySpec getPreQuery(QuerySpec spec, QuerySpec preQuerySpec, QueryResponse qresp0) {
		if (preQuerySpec == null) {
			if (spec.queryExp.filter != null && spec.queryExp.filter.cond instanceof FilterOpFullExp) {
				FilterOpFullExp fofe = (FilterOpFullExp) spec.queryExp.filter.cond;
				if (fofe.isSingleQueryInExp()) {
					QueryInExp inExp = (QueryInExp) fofe.opexp1;
					for(Exp exp: inExp.listExp.valueL) {
						if (exp instanceof IdentExp) {
							//TODO later support multiple idents in list. we only support one for now.
							QuerySpec preSpec = new QuerySpec();
							preSpec.evaluator = spec.evaluator;
							FilterExp filter = new FilterExp(99, new BooleanExp(true));
							preSpec.queryExp = new QueryExp(0, new IdentExp(spec.queryExp.typeName), filter, null);
							QueryFuncExp qfe = new QueryFieldExp(99, new IdentExp(exp.strValue()));
							preSpec.queryExp.qfelist.add(qfe);
							return preSpec;
						}
					}
				}
			}
		} else {
			//hack. we assume was the followers thing. TODO fix when are more than one
			if (spec.queryExp.filter != null && spec.queryExp.filter.cond instanceof FilterOpFullExp) {
				FilterOpFullExp fofe = (FilterOpFullExp) spec.queryExp.filter.cond;
				if (fofe.isSingleQueryInExp()) {
					QueryInExp inExp = (QueryInExp) fofe.opexp1;
					inExp.listExp.valueL.clear();
					for(DValue inner: qresp0.dvalList) {
						DValue pk = DValueHelper.findPrimaryKeyValue(inner);
						Exp exp = dvalConverterSvc.createExpFor(pk);
						inExp.listExp.valueL.add(exp);
					}
				}
			}
		}
		
		return null;
	}

}