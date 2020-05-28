package org.delia.db.sql.where;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.delia.compiler.ast.Exp;
import org.delia.compiler.ast.FilterExp;
import org.delia.compiler.ast.FilterOpExp;
import org.delia.compiler.ast.FilterOpFullExp;
import org.delia.compiler.ast.IdentExp;
import org.delia.compiler.ast.QueryInExp;
import org.delia.compiler.astx.XNAFMultiExp;
import org.delia.compiler.astx.XNAFNameExp;
import org.delia.compiler.astx.XNAFSingleExp;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.db.QuerySpec;
import org.delia.db.sql.QueryType;
import org.delia.db.sql.QueryTypeDetector;
import org.delia.db.sql.SqlValue;
import org.delia.db.sql.SqlValuePair;
import org.delia.dval.DValueConverterService;
import org.delia.relation.RelationInfo;
import org.delia.runner.VarEvaluator;
import org.delia.type.DStructType;
import org.delia.type.DType;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.util.DRuleHelper;
import org.delia.util.DValueHelper;
import org.delia.util.DeliaExceptionHelper;

public class SqlWhereConverter extends ServiceBase {

	private DTypeRegistry registry;
	private QueryTypeDetector queryDetector;
	private VarEvaluator varEvaluator;
	private DValueConverterService dvalConverterSvc;

	public SqlWhereConverter(FactoryService factorySvc, DTypeRegistry registry, QueryTypeDetector queryTypeDetector, VarEvaluator varEvaluator) {
		super(factorySvc);
		this.registry = registry;
		this.queryDetector = queryTypeDetector == null ? new QueryTypeDetector(factorySvc, registry) : queryTypeDetector;
		this.varEvaluator = varEvaluator;
		this.dvalConverterSvc = new DValueConverterService(factorySvc);
	}
	
	public WhereExpression convert(QuerySpec spec) {
		
		if (QueryType.OP.equals(queryDetector.detectQueryType(spec))) {
			return addWhereClauseOp(spec.queryExp.filter, spec.queryExp.typeName);
		}
		return null;
	}
	
	public WhereExpression addWhereClauseOp(FilterExp filter, String typeName) {
		if (filter != null && filter.cond instanceof FilterOpFullExp) {
			FilterOpFullExp fexp = (FilterOpFullExp) filter.cond;
			return doFullExp(fexp, typeName);
		}
		return null;
	}
	private WhereExpression doFullExp(FilterOpFullExp fexp, String typeName) {
		if (fexp.opexp1 instanceof FilterOpExp) {
			FilterOpExp foexp = (FilterOpExp) fexp.opexp1;
			WherePhrase phrase = buildWherePhrase(foexp, typeName, fexp.negFlag);
			return phrase;
		} else if (fexp.opexp1 instanceof QueryInExp) {
			QueryInExp inexp = (QueryInExp) fexp.opexp1;
			InPhrase phrase = new InPhrase();
			phrase.valueL = inexp.listExp.valueL;
			
			Exp leftArg = resolveLeftArg(inexp);
			if (leftArg instanceof IdentExp) {
				TypeDetails details1 = new TypeDetails();
				getFromOpByFieldName(typeName, inexp.fieldName, details1);
				
				phrase.op1 = new WhereOperand();
				phrase.op1.typeDetails = details1;
				phrase.op1.exp = leftArg;
				return phrase;
			} else {
				TypeDetails details1 = new TypeDetails();
				//TODO this is hardcoded for u in [followers]. fix!!
				String fieldName = inexp.listExp.valueL.get(0).strValue(); 
				getFromOpByFieldName(typeName, fieldName, details1);
				
				phrase.op1 = new WhereOperand();
				phrase.op1.typeDetails = details1;
				phrase.op1.exp = leftArg;
				phrase.op1.isValue = true;
				return phrase;
			}
		} else if (fexp.opexp1 instanceof FilterOpFullExp) {
			LogicalPhrase lphrase = new LogicalPhrase();
			lphrase.isAnd = fexp.isAnd;
			
			FilterOpFullExp fullexp1 = (FilterOpFullExp) fexp.opexp1;
			lphrase.express1 = doFullExp(fullexp1, typeName);

			FilterOpFullExp fullexp2 = (FilterOpFullExp) fexp.opexp2;
			lphrase.express2 = doFullExp(fullexp2, typeName);
			return lphrase;
		} else {
			String name = fexp.opexp1 == null ? "nul" : fexp.opexp1.getClass().getSimpleName();
			DeliaExceptionHelper.throwError("unsupported-where-type", "Unsupported filter type '%s'", name);
			return null; 
		}
	}
	
	private Exp resolveLeftArg(QueryInExp inexp) {
		String varName = inexp.fieldName;
		List<DValue> dvalL = varEvaluator.lookupVar(varName);
		if (dvalL == null) {
			return new IdentExp(inexp.fieldName);
		} else if (dvalL.size() > 1) {
			String msg = String.format("too many values (%d) in 'in' expression '%s'", dvalL.size(), inexp.strValue());
			DeliaExceptionHelper.throwError("to-many-primary-key-vaues", msg);
			return null;
		} else {
			DValue dval = dvalL.get(0);
			return dvalConverterSvc.createExpFor(dval);
		}
	}

	private WherePhrase buildWherePhrase(FilterOpExp foexp, String typeName, boolean negFlag) {
		WherePhrase phrase = new WherePhrase();
		phrase.op = foexp.op; //delia op, not sql
		phrase.notFlag = negFlag;
		
		TypeDetails details1 = new TypeDetails();
		TypeDetails details2 = new TypeDetails();
		SqlValuePair vpair = detectType(typeName, foexp.op1, foexp.op2, details1, details2);
		TypeDetails detailsToUse = details1.dtype == null ? details2 : details1;

		WhereOperand oper = new WhereOperand();
		oper.typeDetails = detailsToUse;
		oper.exp = vpair.val1.exp;
		oper.isValue = determineValueFlag(foexp.op1);
		oper.fnName = determineFn(foexp.op1);
		phrase.op1 = oper;
		
		oper = new WhereOperand();
		oper.typeDetails = detailsToUse;
		oper.exp = vpair.val2.exp;
		oper.isValue = determineValueFlag(foexp.op2);
		oper.fnName = determineFn(foexp.op2);
		phrase.op2 = oper;
		return phrase;
	}

	private SqlValuePair detectType(String typeName, Exp op1, Exp op2, TypeDetails details1, TypeDetails details2) {
		SqlValuePair vpair = new SqlValuePair();
		
		SqlValue val1 = new SqlValue();
		SqlValue val2 = new SqlValue();
		
		val2.dtype = getFromOp(typeName, op1, details1);
		if (val2.dtype == null) {
			val1.dtype = getFromOp(typeName, op2, details2);
		}
		val1.exp = op1;
		val2.exp = op2;
		vpair.val1 = val1;
		vpair.val2 = val2;
		
		return vpair;
	}

	private boolean determineValueFlag(Exp op1) {
		if (op1 instanceof XNAFMultiExp) {
			return false;
		} 
		return true;
	}
	private String determineFn(Exp op1) {
		if (op1 instanceof XNAFMultiExp) {
			XNAFMultiExp mexp = (XNAFMultiExp) op1;
			if (mexp.qfeL.size() == 2) {
				XNAFSingleExp sexp = mexp.qfeL.get(1);
				return sexp.funcName;
			}
		} 
		return null;
	}

	private DType getFromOp(String typeName, Exp op1, TypeDetails details) {
		if (op1 instanceof XNAFMultiExp) {
			XNAFMultiExp mexp = (XNAFMultiExp) op1;
			if (!CollectionUtils.isEmpty(mexp.qfeL)) {
				XNAFNameExp nexp = (XNAFNameExp) mexp.qfeL.get(0);
				String fieldName = nexp.funcName;
				return getFromOpByFieldName(typeName, fieldName, details);
			}
		} 
		return null;
	}
	private DType getFromOpByFieldName(String typeName, String fieldName, TypeDetails details) {
		DStructType structType = (DStructType) registry.getType(typeName);

		RelationInfo relinfo = DRuleHelper.findRelinfoOneOrManyForField(structType, fieldName);
		if (relinfo != null) {
			details.isRelation = true;
			details.isParent = relinfo.isParent;
		}

		details.dtype = DValueHelper.findFieldType(structType, fieldName);
		return details.dtype;
	}
	
}