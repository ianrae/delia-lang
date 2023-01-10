package org.delia.runner;

import org.delia.Delia;
import org.delia.DeliaSession;
import org.delia.compiler.ast.AST;
import org.delia.error.ErrorTracker;
import org.delia.hld.DeliaExecutable;
import org.delia.hld.HLDFirstPassResults;
import org.delia.valuebuilder.ScalarValueBuilder;

/**
 * Allows delia to be executed programmatically from within delia
 * For example a rule class can execute a query using DeliaRunner
 */
public interface DeliaRunner {
    Delia getDelia();

    ScalarValueBuilder createValueBuilder();

    HLDFirstPassResults buildFirstPassResults(AST.DeliaScript script);

    AST.DeliaScript compile(String deliaSrc, ErrorTracker et);
    DeliaExecutable buildExecutable(AST.DeliaScript script);

    DeliaSession execute(DeliaExecutable executable);
}
