package org.delia.lld;

import org.delia.DeliaOptions;
import org.delia.ast.*;
import org.delia.ast.code.*;
import org.delia.compiler.Pass1Compiler;
import org.delia.compiler.ast.AST;
import org.delia.compiler.ast.Exp;
import org.delia.core.FactoryService;
import org.delia.db.DBType;
import org.delia.db.SqlStatement;
import org.delia.hld.DeliaExecutable;
import org.delia.hld.HLDBuilder;
import org.delia.hld.HLDFirstPassResults;
import org.delia.hld.dat.SyntheticDatService;
import org.delia.type.DRelation;
import org.delia.type.DTypeRegistry;
import org.delia.type.DValue;
import org.delia.util.render.ObjectRendererImpl;
import org.delia.valuebuilder.ScalarValueBuilder;
import org.delia.runner.ExecutableBuilder;
import org.delia.dbimpl.ExpTestHelper;

import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

/**
 * 1 Customer[true]
 * 2 Customer[10]
 * 3 Customer[id < 10]
 * 4 Customer[addr == 101]
 * 5 Address[cust == 7]
 * 6 Customer[addr.city = 'toronto']
 * 7 Customer[true].fks();
 * 8 Customer[true].fetch('addr')
 * 9 Customer[true].addr
 * 10 Customer[true].count()
 */
public class LLDBasicTestBase extends TestBase {


    //---
    protected String mainTypeName = "Person";
    protected Exp.DottedExp fieldAndFuncs;
    protected boolean isManyToOne = false;
    protected boolean isMMIsReversed = false;
    protected SyntheticDatService datSvc = new SyntheticDatService();
    protected DeliaExecutable mostRecentexecutable;
    protected DTypeRegistry registry;


    protected DeliaExecutable parseIntoHLD() {
        return HLDTestHelper.parseIntoHLD(factorySvc, new DeliaOptions());
    }

    protected void dumpObj(String title, Object obj) {
        ObjectRendererImpl renderer = new ObjectRendererImpl();
        String json = renderer.render(obj);
        log.log("%s: %s", title, json);
    }

    protected LLD.LLSelect buildAndRun(Exp.WhereClause whereClause) {
        ScalarValueBuilder valueBuilder = createValueBuilder();
        ExpTestHelper expHelper = new ExpTestHelper(factorySvc);
        AST.DeliaScript script = expHelper.buildScriptStart(valueBuilder);
        AST.LetStatementAst letStmt = new AST.LetStatementAst();
        letStmt.typeName = mainTypeName;
        letStmt.whereClause = whereClause;
        script.add(letStmt);
        return buildAndRun(script, 4);
    }

    protected LLD.LLSelect buildAndRunCustomerAddress(Exp.WhereClause whereClause) {
        ScalarValueBuilder valueBuilder = createValueBuilder();
        CustomerAddressHelper caHelper = new CustomerAddressHelper(factorySvc);
        AST.DeliaScript script = caHelper.buildScriptStart(valueBuilder, isManyToOne);
        AST.LetStatementAst letStmt = new AST.LetStatementAst();
        letStmt.typeName = mainTypeName;
        letStmt.whereClause = whereClause;
        letStmt.fieldAndFuncs = fieldAndFuncs;
        script.add(letStmt);
        return buildAndRun(script, 6);
    }
    protected LLD.LLSelect buildAndRunMMCustomerAddress(Exp.WhereClause whereClause) {
        ScalarValueBuilder valueBuilder = createValueBuilder();
        MMCustomerAddressHelper caHelper = new MMCustomerAddressHelper(factorySvc);
        AST.DeliaScript script = caHelper.buildScriptStart(valueBuilder);
        AST.LetStatementAst letStmt = new AST.LetStatementAst();
        letStmt.typeName = mainTypeName;
        letStmt.whereClause = whereClause;
        letStmt.fieldAndFuncs = fieldAndFuncs;
        script.add(letStmt);
        return buildAndRun(script, 6);
    }

    protected LLD.LLSelect buildAndRun(AST.DeliaScript script, int expectedSize) {
        DeliaExecutable executable = xparseIntoHLD(script, factorySvc);
        assertEquals(expectedSize, executable.hldStatements.size());
        mostRecentexecutable = executable;
//        LLDBuilder builder = new LLDBuilder(factorySvc, datSvc);
//        builder.buildLLD(executable);
//        dumpLL(executable.lldStatements);
        LLD.LLSelect ll = (LLD.LLSelect) executable.lldStatements.stream().filter(x -> x instanceof LLD.LLSelect).findAny().get();
        dumpObj("let", ll);

        return ll;
    }
    public DeliaExecutable xparseIntoHLD(AST.DeliaScript script, FactoryService factorySvc) {
        HLDBuilder hldBuilder = new HLDBuilder(factorySvc, datSvc, new DeliaOptions(), "public");
        HLDFirstPassResults firstPassResults = hldBuilder.buildTypesOnly(script);

        ExecutableBuilder execBuilder = new ExecutableBuilder(factorySvc, datSvc, null);
        execBuilder.buildCallback = new MyCallback(datSvc, isMMIsReversed);
        Pass1Compiler pass1Compiler = new Pass1Compiler(factorySvc, firstPassResults, DBType.POSTGRES, script.errorFormatter, null);
        String schema = getSchemaIfPresent(script);
        pass1Compiler.process(script, schema);
        DeliaExecutable executable = execBuilder.buildFromScript(script, firstPassResults, DBType.POSTGRES);
        ExpTestHelper.dumpExec(executable, factorySvc.getLog());
        this.registry = executable.registry;
        return executable;
    }


    protected void chkPK(LLD.LLSelect lld, String pkField) {
        chkPK(lld, pkField, 1);
    }

    protected void chkPK(LLD.LLSelect lld, String pkField, int expected) {
        LLD.LLField field = makeFieldList(lld).stream().filter(x -> x.physicalPair.name.equals(pkField)).findAny().orElse(null);
        assertEquals(true, lld.table.logicalType.fieldIsPrimaryKey(pkField));
        assertEquals(expected, makeFieldList(lld).stream().filter(x -> x.physicalPair.name.equals(pkField)).collect(Collectors.toList()).size());
    }

    protected List<LLD.LLField> makeFieldList(LLD.LLSelect lld) {
        List<LLD.LLField> list = lld.fields.stream().filter(x -> x instanceof LLD.LLField).map(x -> (LLD.LLField) x).collect(Collectors.toList());
        return list;
    }

    protected void chkWhere(LLD.LLSelect lld, String expected) {
//        StringJoiner joiner = new StringJoiner(" ");
        String s = String.format("[%s]", lld.whereTok.where.strValue());
        assertEquals(expected, s);
    }

    protected void chkJoins(LLD.LLSelect lld, int expected) {
        assertEquals(mainTypeName, lld.table.logicalType.getName());
        assertEquals(expected, lld.joinL.size());
    }

    protected void chkOneJoin(LLD.LLSelect lld, String expected, int index, String... fieldNames) {
        assertEquals(mainTypeName, lld.table.logicalType.getName());
        LLD.LLJoin join = lld.joinL.get(0);
        String joinStr = String.format("%s.%s.%s.%s", join.physicalLeft.getTableName(),
                join.physicalLeft.physicalPair.name, join.physicalRight.getTableName(),
                join.physicalRight.physicalPair.name);
        assertEquals(expected, joinStr);

        assertEquals(fieldNames.length, join.physicalFields.size());
        for (int i = 0; i < fieldNames.length; i++) {
            String s1 = fieldNames[i];
            LLD.LLField ff = join.physicalFields.get(i);
            String s2 = ff.physicalPair.name;
            s2 = String.format("%s.%s", ff.getTableName(), s2);
            assertEquals(s1, s2);
        }
    }

    protected void chkLetStmt(LLD.LLSelect lld, String... fieldNames) {
        assertEquals(mainTypeName, lld.table.logicalType.getName());
        assertEquals(fieldNames.length, lld.fields.size());
        for (int i = 0; i < fieldNames.length; i++) {
            String s1 = fieldNames[i];
            String s2 = null;
            LLD.LLEx llex = lld.fields.get(i);
            if (llex instanceof LLD.LLField) {
                LLD.LLField ff = (LLD.LLField) llex;
                s2 = ff.physicalPair.name;
            } else {
                LLD.LLDFuncEx llff = (LLD.LLDFuncEx) llex;
                s2 = llff.fnName;
            }

            assertEquals(s1, s2);
        }
//        public Exp.WhereClause whereClause;
//        public List<HLD.HLDJoin> joinL = new ArrayList<>(); //logical joins (and their fields)

    }

    protected void dumpLL(List<LLD.LLStatement> statements) {
        log.log("--LL--");
        for (LLD.LLStatement hld : statements) {
            log.log(hld.toString());
        }
        log.log("--LL end.--");
    }

    protected void chkSql(LLD.LLSelect lld, String expected, String... args) {
//        LLDSqlGenerator gen = new LLDSqlGenerator(factorySvc, null, registry);
        SqlStatement sql = lld.sql;

        log.log(sql.sql + paramsToStr(sql));
        assertEquals(expected, sql.sql);

        assertEquals(args.length, sql.paramL.size());
        for(int i = 0; i < args.length; i++) {
            String arg = args[i];
            DValue dval = sql.paramL.get(i);
            if (arg == null) {
                assertEquals(null, dval);
            } else {
                String s = resolveParamAsString(dval);
                assertEquals(arg, s);
            }
        }
    }

    private String paramsToStr(SqlStatement sql) {
        StringJoiner sj = new StringJoiner(",");
        for(DValue dval: sql.paramL) {
            String s = resolveParamAsString(dval);
            sj.add(s);
        }

        String str = sj.toString();
        if (! str.isEmpty()) {
            return String.format(": (%s)", str);
        }
        return str;
    }

    private String resolveParamAsString(DValue dval){
        if (dval.getType().isRelationShape()) {
            DRelation drel = (DRelation) dval.asRelation();
            DValue inner = drel.getForeignKey();
            return inner.asString();
        } else {
            return dval.asString();
        }
    }

}
