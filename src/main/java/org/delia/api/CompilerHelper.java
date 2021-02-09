package org.delia.api;

import java.util.List;

import org.delia.DeliaOptions;
import org.delia.compiler.DeliaCompiler;
import org.delia.compiler.ast.Exp;
import org.delia.core.FactoryService;
import org.delia.log.Log;
import org.delia.runner.InternalCompileState;
import org.delia.type.DTypeRegistry;
import org.delia.zdb.DBInterfaceFactory;

public class CompilerHelper {
	
	private Log log;
	private FactoryService factorySvc;
	private DeliaOptions deliaOptions;
	
	public CompilerHelper(DBInterfaceFactory dbInterface, Log log, FactoryService factorySvc, DeliaOptions options) {
		this.log = log;
		this.factorySvc = factorySvc;
		this.deliaOptions = options;
	}
	
	public List<Exp> compileDeliaSource(String src, boolean doPass3Flag) {
		DeliaCompiler compiler = createCompiler();
		if (deliaOptions.logSourceBeforeCompile) {
			log.log("delia-src: %s", src);
		}
		compiler.setDoPass3Flag(doPass3Flag);
		List<Exp> expL = compiler.parse(src);
		return expL;
	}
	public List<Exp> compileDeliaSource(String src, InternalCompileState execCtx) {
		DeliaCompiler compiler = createCompiler(execCtx);
		if (deliaOptions.logSourceBeforeCompile) {
			log.log("delia-src: %s", src);
		}
		compiler.setDoPass3Flag(false);
		List<Exp> expL = compiler.parse(src);
		return expL;
	}


	public void executePass3and4(InternalCompileState execCtx, String src, List<Exp> extL, DTypeRegistry registry) {
		DeliaCompiler compiler = createCompiler(execCtx);
		compiler.executePass3(src, extL);

		//and do pass4
		compiler.executePass4(src, extL, registry);
	}
	private DeliaCompiler createCompiler()  {
		return createCompiler(null);
	}
	public DeliaCompiler createCompiler(InternalCompileState execCtx)  {
		DeliaCompiler compiler = new DeliaCompiler(factorySvc, execCtx);
		return compiler;
	}
	

}