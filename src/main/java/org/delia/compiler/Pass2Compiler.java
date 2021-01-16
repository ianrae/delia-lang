package org.delia.compiler;

import java.util.List;

import org.delia.compiler.ast.ConfigureStatementExp;
import org.delia.compiler.ast.DeleteStatementExp;
import org.delia.compiler.ast.Exp;
import org.delia.compiler.ast.FilterOpExp;
import org.delia.compiler.ast.LetStatementExp;
import org.delia.compiler.ast.OptionExp;
import org.delia.compiler.ast.QueryExp;
import org.delia.compiler.ast.StructFieldExp;
import org.delia.compiler.ast.TypeStatementExp;
import org.delia.compiler.ast.UpdateStatementExp;
import org.delia.compiler.ast.UpsertStatementExp;
import org.delia.core.ConfigureService;
import org.delia.core.FactoryService;
import org.delia.db.memdb.filter.OP;
import org.delia.error.DeliaError;
import org.delia.runner.InternalCompileState;
import org.delia.type.BuiltInTypes;

/**
 * Enforce language rules that parser can't enforce.
 * Types are not yet available.
 * @author ian
 *
 */
public class Pass2Compiler extends CompilerPassBase {

	public Pass2Compiler(FactoryService factorySvc, ErrorLineFinder errorLineFinder, InternalCompileState execCtx) {
		super(factorySvc, errorLineFinder, execCtx);
	}

	@Override
	public CompilerResults process(List<Exp> list) {
		CompilerResults results = new CompilerResults();
		results.list = list;
		
		for(Exp exp: list) {
			if (exp instanceof LetStatementExp) {
				LetStatementExp letExp = (LetStatementExp) exp;
				checkLetStatement(results, letExp);
			} else if (exp instanceof TypeStatementExp) {
				TypeStatementExp typeExp = (TypeStatementExp) exp;
				checkTypeStatement(results, typeExp);
			} else if (exp instanceof ConfigureStatementExp) {
				ConfigureStatementExp typeExp = (ConfigureStatementExp) exp;
				checkConfigureStatement(results, typeExp);
			} else if (exp instanceof UpdateStatementExp) {
				UpdateStatementExp upExp = (UpdateStatementExp) exp;
				checkUpdateStatement(results, upExp);
			} else if (exp instanceof UpsertStatementExp) {
				UpsertStatementExp upExp = (UpsertStatementExp) exp;
				checkUpsertStatement(results, upExp);
			} else if (exp instanceof DeleteStatementExp) {
				checkDeleteStatement(results, (DeleteStatementExp) exp);
			}
		}
		return results;
	}

	/**
	 * We used to support update Customer, but is too dangerous. 
	 * Now you must suppy a filter, such as update Customer[true]
	 * @param results
	 * @param upExp
	 */
	private void checkUpdateStatement(CompilerResults results, UpdateStatementExp upExp) {
		doMissingFilterCheck(results, upExp.queryExp, upExp.typeName, upExp);
	}
	private void checkDeleteStatement(CompilerResults results, DeleteStatementExp upExp) {
		doMissingFilterCheck(results, upExp.queryExp, upExp.typeName, upExp);
	}
	private void doMissingFilterCheck(CompilerResults results, QueryExp queryExp, String typeName, Exp exp) {
		if (queryExp.filter == null) {
			String msg = String.format("update: filter is missing on '%s'", typeName);
			addError(results, "missing-filter", msg, exp);
		}
	}
	private void checkUpsertStatement(CompilerResults results, UpsertStatementExp upExp) {
		doMissingFilterCheck(results, upExp.queryExp, upExp.typeName, upExp);
		OptionExp optionExp = upExp.optionExp;
		if (optionExp != null && !optionExp.strValue().equals("noUpdate")) {
			String msg = String.format("upsert: unknown option '%s'", optionExp.strValue());
			addError(results, "configure-unknown-var", msg, upExp);
		}
	}

	private void checkConfigureStatement(CompilerResults results, ConfigureStatementExp configExp) {
		log.log("config " + configExp.strValue());
		
		ConfigureService configSvc = factorySvc.getConfigureService();
		boolean b = configSvc.validate(configExp.varName);
		if (! b) {
			String msg = String.format("configure: unknown variable '%s'", configExp.varName);
			addError(results, "configure-unknown-var", msg, configExp);
		}
	}

	private void checkTypeStatement(CompilerResults results, TypeStatementExp typeExp) {
//		log.log("type " + typeExp.strValue());
		checkTypeName(typeExp, results);
		if (typeExp.structExp != null) {
			for(StructFieldExp sfe: typeExp.structExp.argL) {
				checkModifiers(sfe, results);
				
				if (isScalar(sfe)) {
					if (sfe.isRelation) {
						String msg = String.format("relation cannot be used with scalar types - field '%s'", sfe.fieldName);
						addError(results, "relation-wrong-type", msg, sfe);
					} else if (sfe.isMany) {
						String msg = String.format("'many' can only be used with relations - field '%s'", sfe.fieldName);
						addError(results, "many-error", msg, sfe);
					} else if (sfe.isOne) {
						String msg = String.format("'one' can only be used with relations - field '%s'", sfe.fieldName);
						addError(results, "many-error", msg, sfe);
					}
				} else {
					if (sfe.isMany && sfe.isOne) {
						String msg = String.format("cannot use 'one' and 'many' on same field - field '%s'", sfe.fieldName);
						addError(results, "one-and-many-error", msg, sfe);
					}
					if (sfe.isSerial) {
						String msg = String.format("cannot use 'serial' on relation fields - field '%s'", sfe.fieldName);
						addError(results, "serial-error", msg, sfe);
					}
				}
			}
		}
	}
	private void checkTypeName(TypeStatementExp typeExp, CompilerResults results) {
		if (BuiltInTypes.isBuiltInScalarType(typeExp.typeName)) {
			String msg = String.format("Type name already used - type'%s'", typeExp.typeName);
			addError(results, "typename.in.use", msg, typeExp);
		}			
	}

	private void checkModifiers(StructFieldExp qfe, CompilerResults results) {
		if (qfe.isOptional && qfe.isPrimaryKey) {
			String msg = String.format("optional and primaryKey cannot be used together - field '%s'", qfe.fieldName);
			addError(results, "optional-primarykey-not-allowed", msg, qfe);
		}			
		if (qfe.isOptional && qfe.isSerial) {
			String msg = String.format("optional and serial cannot be used together - field '%s'", qfe.fieldName);
			addError(results, "optional-serial-not-allowed", msg, qfe);
		}			

		if (qfe.isUnique && qfe.isPrimaryKey) {
			String msg = String.format("unique and primaryKey cannot be used together - field '%s'", qfe.fieldName);
			addError(results, "unique-primarykey-not-allowed", msg, qfe);
		}			
	}

	private void addError(CompilerResults results, String id, String msg, Exp exp) {
		DeliaError err = createError(id, msg, exp);
		results.errors.add(err);
	}

	private boolean isScalar(StructFieldExp qfe) {
		return BuiltInTypes.isBuiltInScalarType(qfe.typeName);
	}


	private void checkLetStatement(CompilerResults results, LetStatementExp letExp) {
//		log.log("a" + letExp.strValue());
		if (letExp.value instanceof QueryExp) {
			QueryExp qexp = (QueryExp) letExp.value;
			
			if (qexp.filter != null && qexp.filter.cond instanceof FilterOpExp) {
				FilterOpExp fexp = (FilterOpExp) qexp.filter.cond;
				
				OP op = OP.createFromString(fexp.op);
				if (op != null) {
//					if (fexp.op1 instanceof NullExp) {
//						String msg = String.format("null not allowed with '%s'", qexp.filter.strValue());
//						DangError err = createError("null-not-allowed", msg, qexp.filter);
//						results.errors.add(err);
//					}
//					if (fexp.op2 instanceof NullExp) {
//						String msg = String.format("null not allowed with '%s'", qexp.filter.strValue());
//						DangError err = createError("null-not-allowed", msg, qexp.filter);
//						results.errors.add(err);
//					}
				}
			}
		}
	}
}