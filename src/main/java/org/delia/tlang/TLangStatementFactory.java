package org.delia.tlang;

import org.delia.compiler.ast.Exp;
import org.delia.compiler.astx.XNAFSingleExp;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.tlang.runner.TLangStatement;
import org.delia.tlang.statement.AbbreviateStatement;
import org.delia.tlang.statement.AsDateStatement;
import org.delia.tlang.statement.CombineStatement;
import org.delia.tlang.statement.FailsStatement;
import org.delia.tlang.statement.SubstringStatement;
import org.delia.tlang.statement.ToUpperStatement;
import org.delia.tlang.statement.TrimStatement;
import org.delia.util.DeliaExceptionHelper;

public class TLangStatementFactory extends ServiceBase {


	public TLangStatementFactory(FactoryService factorySvc) {
		super(factorySvc);
	}


	public TLangStatement create(XNAFSingleExp fieldOrFn) {
		String fnName = fieldOrFn.funcName;
		switch(fnName) {
		case "toUpperCase":
			return new ToUpperStatement(true);
		case "toLowerCase":
			return new ToUpperStatement(false);
		case "trim":
			return new TrimStatement();
		case "substring":
		{
			Exp arg1 = getArg(fieldOrFn, 0, true);
			Exp arg2 = getArg(fieldOrFn, 1, false);
			return new SubstringStatement(arg1, arg2);
		}
		case "abbreviate":
		{
			Exp arg1 = getArg(fieldOrFn, 0, true);
			Exp arg2 = getArg(fieldOrFn, 1, false);
			return new AbbreviateStatement(arg1, arg2);
		}
		case "asDate":
		{
			Exp arg1 = getArg(fieldOrFn, 0, true);
			return new AsDateStatement(arg1);
		}
		case "combine":
		{
			Exp arg1 = getArg(fieldOrFn, 0, true);
			Exp arg2 = getArg(fieldOrFn, 1, false);
			Exp arg3 = getArg(fieldOrFn, 2, false);
			return new CombineStatement(arg1, arg2, arg3);
		}
		case "fail":
			return new FailsStatement();
		default:
			DeliaExceptionHelper.throwError("tlang-unknown-fn", "Unknown function '%s'", fnName);
		}
		return null;
	}


	private Exp getArg(XNAFSingleExp fieldOrFn, int index, boolean mandatory) {
		if (index >= fieldOrFn.argL.size()) {
			if (mandatory) {
				DeliaExceptionHelper.throwError("tlang-missing-arg", "function '%s' missing an argument", fieldOrFn.funcName);
			}
			return null;
		}
		
		return fieldOrFn.argL.get(index);
	}

}