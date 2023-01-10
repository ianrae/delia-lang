package org.delia.ast;

import org.delia.DeliaOptions;
import org.delia.ast.code.*;
import org.delia.compiler.Pass1Compiler;
import org.delia.compiler.ast.AST;
import org.delia.compiler.ast.Exp;
import org.delia.core.FactoryService;
import org.delia.db.DBType;
import org.delia.hld.DeliaExecutable;
import org.delia.hld.HLD;
import org.delia.hld.HLDBuilder;
import org.delia.hld.HLDFirstPassResults;
import org.delia.hld.dat.SyntheticDatService;
import org.delia.util.render.ObjectRendererImpl;
import org.delia.valuebuilder.ScalarValueBuilder;
import org.delia.runner.ExecutableBuilder;
import org.delia.dbimpl.ExpTestHelper;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

public class HLDBasicTestBase extends TestBase {


    //-----
    protected String mainTypeName = "Person";
    protected Exp.DottedExp fieldAndFuncs;
    protected boolean isManyToOne = false;
    protected boolean isMMIsReversed = false;
    protected SyntheticDatService datSvc = new SyntheticDatService();
    protected boolean addSchemaFlag = true; //add schema 'alpha'

    protected DeliaExecutable parseIntoHLD() {
        return HLDTestHelper.parseIntoHLD(factorySvc, new DeliaOptions());
    }

    protected void dumpObj(String title, Object obj) {
        ObjectRendererImpl renderer = new ObjectRendererImpl();
        String json = renderer.render(obj);
        log.log("%s: %s", title, json);
    }

    protected HLD.LetHLDStatement buildAndRunPerson(Exp.WhereClause whereClause) {
        ScalarValueBuilder valueBuilder = createValueBuilder();
        ExpTestHelper expHelper = new ExpTestHelper(factorySvc);
        AST.DeliaScript script = expHelper.buildScriptStart(valueBuilder);
        AST.LetStatementAst letStmt = new AST.LetStatementAst();
        letStmt.typeName = mainTypeName;
        letStmt.whereClause = whereClause;
        script.add(letStmt);
        return buildAndRunPerson(script, 4);
    }

    protected HLD.LetHLDStatement buildAndRunCustomerAddress(Exp.WhereClause whereClause) {
        ScalarValueBuilder valueBuilder = createValueBuilder();
        CustomerAddressHelper caHelper = new CustomerAddressHelper(factorySvc);
        AST.DeliaScript script = caHelper.buildScriptStart(valueBuilder, isManyToOne);
        AST.LetStatementAst letStmt = new AST.LetStatementAst();
        letStmt.typeName = mainTypeName;
        letStmt.whereClause = whereClause;
        letStmt.fieldAndFuncs = fieldAndFuncs;
        script.add(letStmt);
        return buildAndRunPerson(script, 6);
    }

    protected HLD.LetHLDStatement buildAndRunMMCustomerAddress(Exp.WhereClause whereClause) {
        ScalarValueBuilder valueBuilder = createValueBuilder();
        MMCustomerAddressHelper caHelper = new MMCustomerAddressHelper(factorySvc);
        AST.DeliaScript script = caHelper.buildScriptStart(valueBuilder, addSchemaFlag);
        AST.LetStatementAst letStmt = new AST.LetStatementAst();
        letStmt.typeName = mainTypeName;
        letStmt.whereClause = whereClause;
        letStmt.fieldAndFuncs = fieldAndFuncs;
        script.add(letStmt);
        return buildAndRunPerson(script, 6);
    }

    protected HLD.LetHLDStatement buildAndRunPerson(AST.DeliaScript script, int expectedSize) {
        DeliaExecutable executable = parseIntoHLD(script, factorySvc);
        if (!addSchemaFlag) {
            expectedSize--;
        }
        assertEquals(expectedSize, executable.hldStatements.size());
        HLD.LetHLDStatement hld = (HLD.LetHLDStatement) executable.hldStatements.get(expectedSize - 1);
        dumpObj("let", hld);
        return hld;
    }

    public DeliaExecutable parseIntoHLD(AST.DeliaScript script, FactoryService factorySvc) {
        HLDBuilder hldBuilder = new HLDBuilder(factorySvc, datSvc, new DeliaOptions(), null);
        HLDFirstPassResults firstPassResults = hldBuilder.buildTypesOnly(script);

        ExecutableBuilder execBuilder = new ExecutableBuilder(factorySvc, datSvc, null);
        execBuilder.buildCallback = new MyCallback(datSvc, isMMIsReversed);

        //need this for schema
        String schema = getSchemaIfPresent(script);
        Pass1Compiler pass1Compiler = new Pass1Compiler(factorySvc, firstPassResults, DBType.POSTGRES, script.errorFormatter, null);
        pass1Compiler.process(script, schema);

        DeliaExecutable executable = execBuilder.buildFromScript(script, firstPassResults, DBType.POSTGRES);
        ExpTestHelper.dumpExec(executable, factorySvc.getLog());
        return executable;
    }



    protected void chkPK(HLD.LetHLDStatement hld, String pkField) {
        HLD.HLDField field = makeFieldList(hld).stream().filter(x -> x.pair.name.equals(pkField)).findAny().orElse(null);
        assertEquals(true, field.isPK());
        assertEquals(1, makeFieldList(hld).stream().filter(x -> x.pair.name.equals(pkField)).collect(Collectors.toList()).size());
    }

    protected List<HLD.HLDField> makeFieldList(HLD.LetHLDStatement hld) {
        List<HLD.HLDField> list = hld.fields.stream().filter(x -> x instanceof HLD.HLDField).map(x -> (HLD.HLDField) x).collect(Collectors.toList());
        return list;
    }

    protected void chkWhere(HLD.LetHLDStatement hld, String expected) {
//        StringJoiner joiner = new StringJoiner(" ");
        String s = String.format("[%s]", hld.whereTok.where.strValue());
        assertEquals(expected, s);
    }

    protected void chkJoins(HLD.LetHLDStatement hld, int expected) {
        assertEquals(mainTypeName, hld.hldTable.getName());
        assertEquals(expected, hld.joinL.size());
    }

    protected void chkOneJoin(HLD.LetHLDStatement hld, String expected, int index, String... fieldNames) {
        assertEquals(mainTypeName, hld.hldTable.getName());
        HLD.HLDJoin join = hld.joinL.get(0);
        assertEquals(fieldNames.length, join.fields.size());
        String joinStr = String.format("%s.%s.%s", join.joinInfo.leftTypeName, join.joinInfo.throughField, join.joinInfo.rightTypeName);
        assertEquals(expected, joinStr);
        for (int i = 0; i < fieldNames.length; i++) {
            String s1 = fieldNames[i];
            String s2 = join.fields.get(i).pair.name;
            s2 = String.format("%s.%s", join.joinInfo.leftTypeName, s2);
            assertEquals(s1, s2);
        }
    }

    protected void chkLetStmt(HLD.LetHLDStatement hld, String... fieldNames) {
        assertEquals(mainTypeName, hld.hldTable.getName());
        assertEquals(fieldNames.length, hld.fields.size());
        for (int i = 0; i < fieldNames.length; i++) {
            String s1 = fieldNames[i];
            String s2 = null;
            HLD.HLDEx hldex = hld.fields.get(i);
            if (hldex instanceof HLD.HLDField) {
                HLD.HLDField hh = (HLD.HLDField) hldex;
                s2 = hh.pair.name;
            } else {
                HLD.HLDFuncEx fff = (HLD.HLDFuncEx) hldex;
                s2 = fff.fnName;
            }
            assertEquals(s1, s2);
        }
//        public Exp.WhereClause whereClause;
//        public List<HLD.HLDJoin> joinL = new ArrayList<>(); //logical joins (and their fields)

    }

    protected void chkAllThreeTypesSame(HLD.LetHLDStatement hld) {
        assertEquals(hld.hldTable, hld.fromType);
        assertEquals(hld.hldTable, hld.resultType);
    }

    protected void chkAllThreeTypes(HLD.LetHLDStatement hld, String fromTypeName, String resultTypeName) {
        assertEquals(fromTypeName, hld.fromType.getName());
        assertEquals(resultTypeName, hld.resultType.getName());
    }

}
