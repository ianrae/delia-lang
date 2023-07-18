package org.delia.dataimport;

import org.delia.DeliaSession;
import org.delia.compiler.ast.AST;
import org.delia.compiler.ast.Exp;
import org.delia.core.FactoryService;
import org.delia.core.ServiceBase;
import org.delia.error.DeliaError;
import org.delia.hld.DeliaExecutable;
import org.delia.runner.DeliaRunner;
import org.delia.type.DStructType;
import org.delia.type.DValue;

import java.util.List;

public class ImporterService extends ServiceBase {

    private final DeliaRunner deliaRunner;
    private DeliaSession mostRecentSession;

    public ImporterService(FactoryService factorySvc, DeliaRunner deliaRunner) {
        super(factorySvc);
        this.deliaRunner = deliaRunner;
    }

    public List<DValue> insertValues(DStructType structType, List<DValue> values) {
        return buildAndRun(structType, values);
    }

    public DeliaExecutable buildStatement(DStructType structType, List<DValue> values) {
        //build AST script
        AST.DeliaScript script = new AST.DeliaScript();
        String typeName = structType.getName();

        for (DValue dval : values) {
            AST.InsertStatementAst insertAst = new AST.InsertStatementAst();
            insertAst.typeName = typeName;
            for (String fieldName : dval.asStruct().getFieldNames()) {
                AST.InsertFieldStatementAst fieldAst = new AST.InsertFieldStatementAst();

                fieldAst.fieldName = fieldName;
                fieldAst.valueExp = new Exp.ValueExp();
                fieldAst.valueExp.value = dval.asStruct().getField(fieldName);
                //TODO: later support listExp;  //if set then ignore valueExp
                //TODO: later support varExp;
                insertAst.fields.add(fieldAst);
            }
            script.add(insertAst);
        }

        DeliaExecutable executable = deliaRunner.buildExecutable(script);
        return executable;
    }

    protected List<DValue> buildAndRun(DStructType structType, List<DValue> values) {
        DeliaExecutable executable = buildStatement(structType, values);
        DeliaSession session = deliaRunner.execute(executable);
        mostRecentSession = session;
        return session.getFinalResult().getAsDValueList();
    }

    public DeliaRunner getDeliaRunner() {
        return deliaRunner;
    }

    public DeliaSession getMostRecentSession() {
        return mostRecentSession;
    }
}
