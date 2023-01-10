package org.delia.runner;

import org.delia.compiler.*;
import org.delia.compiler.ast.AST;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.db.DBType;
import org.delia.hld.HLDFirstPassResults;
import org.delia.util.DeliaExceptionHelper;

public class AdditionalCompilerPasses extends ServiceBase {

    public AdditionalCompilerPasses(FactoryService factorySvc) {
        super(factorySvc);
    }

    public void runAdditionalCompilerPasses(AST.DeliaScript script, HLDFirstPassResults firstPassResults, String currentSchema, DBType dbType, String defaultSchema) {
        Pass1Compiler pass1Compiler = new Pass1Compiler(factorySvc, firstPassResults, dbType, script.errorFormatter, defaultSchema);
        CompilerPassResults passResult = pass1Compiler.process(script, currentSchema);
        if (!passResult.success()) {
            log.logError("Pass1 failed: %d errors", passResult.errors.size());
            //et.addAll(passResult.errors);
            DeliaExceptionHelper.throwErrors("compile-pass1-failed", passResult.errors);
        }
        Pass2Compiler pass2Compiler = new Pass2Compiler(factorySvc, firstPassResults, dbType, script.errorFormatter);
        passResult = pass2Compiler.process(script, currentSchema);
        if (!passResult.success()) {
            log.logError("Pass2 failed: %d errors", passResult.errors.size());
            //et.addAll(passResult.errors);
            DeliaExceptionHelper.throwErrors("compile-pass2-failed", passResult.errors);
        }
        Pass3Compiler pass3Compiler = new Pass3Compiler(factorySvc, firstPassResults, dbType, script.errorFormatter);
        passResult = pass3Compiler.process(script);
        if (!passResult.success()) {
            log.logError("Pass3 failed: %d errors", passResult.errors.size());
            //et.addAll(passResult.errors);
            DeliaExceptionHelper.throwErrors("compile-pass3-failed", passResult.errors);
        }
        Pass4Compiler pass4Compiler = new Pass4Compiler(factorySvc, firstPassResults, dbType, script.errorFormatter);
        passResult = pass4Compiler.process(script);
        if (!passResult.success()) {
            log.logError("Pass4 failed: %d errors", passResult.errors.size());
            //et.addAll(passResult.errors);
            DeliaExceptionHelper.throwErrors("compile-pass4-failed", passResult.errors);
        }
    }

}
