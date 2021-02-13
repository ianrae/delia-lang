package org.delia.compiler;

import java.util.List;

import org.jparsec.error.ParserException;
import org.delia.compiler.ast.Exp;
import org.delia.compiler.parser.FullParser;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.error.DeliaError;
import org.delia.runner.DeliaException;
import org.delia.runner.InternalCompileState;
import org.delia.type.DTypeRegistry;

/**
 * Compiles Delia source code into an AST (Abstract Syntax Tree) object
 * repesented by an Exp object.
 * For example, a let statement is represented by a LetStatementExp.
 * 
 * @author Ian Rae
 *
 */
public class DeliaCompiler extends ServiceBase {
	private InternalCompileState execCtx;
	private boolean doPass3Flag = true;
	
	public DeliaCompiler(FactoryService factorySvc) {
		super(factorySvc);
	}
	public DeliaCompiler(FactoryService factorySvc, InternalCompileState execCtx) {
		super(factorySvc);
		this.execCtx = execCtx;
	}

	public  List<Exp> parse(String input) {
		List<Exp> list = null;
		ErrorLineFinder errorLineFinder = new ErrorLineFinder(input);
		
		try {
			list = FullParser.parse(input);
		} catch (ParserException e) {
			log.logError("ERR: %s", input);
			DeliaError err = et.add("parse-error", e.getMessage());
			throw new DeliaException(err);
		} catch (Exception e) {
			DeliaError err = et.add("pass2-error", e.getMessage());
			throw new DeliaException(err);
		}
		
		if (list != null) {
			Pass2Compiler pass2 = new Pass2Compiler(factorySvc, errorLineFinder, execCtx);
			CompilerResults pass2Res = pass2.process(list);
			if (! pass2Res.success()) {
				throw new DeliaException(pass2Res.errors);
			}
		}
		
		if (list != null && doPass3Flag) {
			Pass3Compiler pass3 = new Pass3Compiler(factorySvc, errorLineFinder, execCtx);
			CompilerResults pass3Res = pass3.process(list);
			if (! pass3Res.success()) {
				throw new DeliaException(pass3Res.errors);
			}
		}
		
		return list;
	}
	public void executePass3(String input, List<Exp> list) {
		ErrorLineFinder errorLineFinder = new ErrorLineFinder(input);
		if (list != null && doPass3Flag) {
			Pass3Compiler pass3 = new Pass3Compiler(factorySvc, errorLineFinder, execCtx);
			pass3.setBuildTypeMapFlag(false);
			CompilerResults pass3Res = pass3.process(list);
			if (! pass3Res.success()) {
				throw new DeliaException(pass3Res.errors);
			}
		}
	}
	public void executePass4(String input, List<Exp> list, DTypeRegistry registry) {
		ErrorLineFinder errorLineFinder = new ErrorLineFinder(input);
		if (list != null) {
			Pass4Compiler pass4 = new Pass4Compiler(factorySvc, errorLineFinder, execCtx, registry);
			CompilerResults pass3Res = pass4.process(list);
			if (! pass3Res.success()) {
				throw new DeliaException(pass3Res.errors);
			}
		}
	}
	public boolean isDoPass3Flag() {
		return doPass3Flag;
	}
	public void setDoPass3Flag(boolean doPass3Flag) {
		this.doPass3Flag = doPass3Flag;
	}
}