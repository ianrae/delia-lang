package org.delia.runner;

import org.delia.compiler.ast.Exp;
import org.delia.compiler.ast.TypeStatementExp;
import org.delia.core.FactoryService;
import org.delia.db.DBInterface;
import org.delia.error.DeliaError;
import org.delia.runner.ExecutionState;
import org.delia.runner.ResultValue;
import org.delia.runner.RunnerImpl;
import org.delia.type.DType;
import org.delia.typebuilder.TypeBuilder;

/**
 * For unit tests only.
 * 
 * This runner combines Runner and TypeRunner, which is how the code
 * was initially writtne.
 * 
 * @author Ian Rae
 *
 */
public class LegacyRunner extends RunnerImpl {
		//TODO remove legacyTypeMode

		public boolean legacyTypeMode;
		private TypeBuilder typeBuilder;

		public LegacyRunner(FactoryService factorySvc, DBInterface dbInterface) {
			super(factorySvc, dbInterface);
		}
		
		public boolean init(ExecutionState ctx) {
			boolean b = super.init(ctx);
			this.typeBuilder = new TypeBuilder(factorySvc, registry);
			return b;
		}
		
		public ResultValue executeOneStatement(Exp exp) {
			if (exp instanceof TypeStatementExp) {
				ResultValue res = new ResultValue();
				executeTypeStatement((TypeStatementExp)exp, res);
				return res;
			} else  {
				return super.executeOneStatement(exp);
			}
		}
		
		private void executeTypeStatement(TypeStatementExp exp, ResultValue res) {
			if (!legacyTypeMode) {
				throw new IllegalArgumentException("legacyTypeMode NOT SET!");
			}
			DType dtype = typeBuilder.createType(exp);
			//TODO: if futureL not-empty then re-run to handle forward delcs
			res.ok = dtype != null;
			if (! res.ok) {
				res.errors.addAll(typeBuilder.getErrorTracker().getErrors());
			}
		}

		private void addError(ResultValue res, String id, String msg) {
			DeliaError error = et.add(id, msg);
			res.errors.add(error);
			res.ok = false;
		}

	}