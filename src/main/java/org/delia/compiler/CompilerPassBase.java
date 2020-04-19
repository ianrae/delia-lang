package org.delia.compiler;

import java.util.List;

import org.delia.compiler.ast.Exp;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.error.DeliaError;
import org.delia.runner.InternalCompileState;

public abstract class CompilerPassBase extends ServiceBase {
	protected ErrorLineFinder errorLineFinder;
	protected InternalCompileState execCtx;
	
	public CompilerPassBase(FactoryService factorySvc, ErrorLineFinder errorLineFinder, InternalCompileState execCtx) {
		super(factorySvc);
		this.errorLineFinder = errorLineFinder;
		this.execCtx = execCtx; //may be null
	}

	protected DeliaError createError(String id, String errMsg, Exp exp) {
		int pos = exp.getPos();
		int lineNum = errorLineFinder.findLineNum(pos);
		String msg = String.format("[line %d char %d] %s", lineNum, pos, errMsg);
		DeliaError err = et.add(id, msg);
		err.setLineAndPos(lineNum, pos);
		return err;
	}
	
	public abstract CompilerResults process(List<Exp> list);
}