package org.delia.compiler.impl;

import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.commons.lang3.StringUtils;
import org.delia.compiler.antlr.deliaBaseVisitor;
import org.delia.compiler.antlr.deliaParser;
import org.delia.compiler.ast.AST;
import org.delia.compiler.ast.Exp;

import java.util.ArrayList;
import java.util.List;

/**
 * Note. you must manually copy antlr-generated friles from gen/ to org.delia.compiler.antlr
 */
public class DeliaAntlrVisitor extends deliaBaseVisitor<CompilerResults> {
    public CompileScalarValueBuilder builder;

    private static final String PENDING_DELIM = "??";

    private int foo;

    @Override
    public CompilerResults visitDeliaStatement(deliaParser.DeliaStatementContext ctx) {
        CompilerResults cresMain = new CompilerResults((Exp.ElementExp) null);
        for (int i = 0; i < ctx.getChildCount(); ++i) {
            ParseTree c = ctx.getChild(i);
            if (c.getText() == "<EOF>") {
                break;
            }
            CompilerResults cres = visit(c);
            if (cres == null) {
                return null; //later gather all errors and then return
            }
            cresMain.addStatementAst(cres.getStatementAst());
        }
        return cresMain;
    }

    @Override
    public CompilerResults visitConfigure(deliaParser.ConfigureContext ctx) {
        // : 'configure' name '=' scalar # configure
        Token tt = ctx.getStart();
        ParseTree child = ctx.getChild(1);
        String configName = child.getText();
        child = ctx.getChild(3);
        CompilerResults zooScalar = myVisitScalar(child);

        CompilerResults zoo = new CompilerResults((Exp.OperandExp) null);
        AST.ConfigureStatementAst configAst = new AST.ConfigureStatementAst(configName);
        configAst.loc = LocHelper.genLoc(ctx.getStart(), ctx.getStop());
        zoo.addStatementAst(configAst);
        configAst.scalarElem = zooScalar.elem;
        return zoo;
    }

    @Override
    public CompilerResults visitSchema(deliaParser.SchemaContext ctx) {
        // 'schema' name
        ParseTree child = ctx.getChild(1);
        String schemaName = child.getText();

        CompilerResults zoo = new CompilerResults((Exp.OperandExp) null);
        AST.SchemaAst ast = new AST.SchemaAst(schemaName);
        ast.loc = LocHelper.genLoc(ctx.getStart(), ctx.getStop());
        zoo.addStatementAst(ast);
        return zoo;
    }

    @Override
    public CompilerResults visitSchemaOff(deliaParser.SchemaOffContext ctx) {
        // 'schema' 'off'
        String schemaName = null;

        CompilerResults zoo = new CompilerResults((Exp.OperandExp) null);
        AST.SchemaAst ast = new AST.SchemaAst(schemaName);
        ast.loc = LocHelper.genLoc(ctx.getStart(), ctx.getStop());
        zoo.addStatementAst(ast);
        return zoo;
    }

    @Override
    public CompilerResults visitLogStatement(deliaParser.LogStatementContext ctx) {
        // 'log' (name | scalar)
        AST.LogStatementAst logAst = new AST.LogStatementAst(null);
        logAst.loc = LocHelper.genLoc(ctx.getStart(), ctx.getStop());

        ParseTree child = ctx.getChild(1);
        if (child instanceof deliaParser.ScalarContext) {
            CompilerResults zooScalar = myVisitScalar(child);
            logAst.scalarElem = zooScalar.elem;
        } else {
            logAst.varName = child.getText();
        }

        CompilerResults zoo = new CompilerResults((Exp.OperandExp) null);
        zoo.addStatementAst(logAst);
        return zoo;
    }

    @Override
    public CompilerResults visitTypeScalar(deliaParser.TypeScalarContext ctx) {
        // : 'type' SYMBOL name drules? 'end'   # typeScalar
        //     0     1      2    3

        ParseTree child = ctx.getChild(1);
        String typeName = child.getText();
        child = ctx.getChild(2);
        String baseName = child.getText();

        child = ctx.getChild(3);
        List<Exp.RuleClause> rules = parseRules(child);

        CompilerResults zoo = new CompilerResults((Exp.OperandExp) null);
        AST.TypeAst typeAst = new AST.TypeAst(typeName);
        typeAst.loc = LocHelper.genLoc(ctx.getStart(), ctx.getStop());
        zoo.addStatementAst(typeAst);
        typeAst.baseName = baseName;
        typeAst.isScalarType = true;
        typeAst.rules = rules;
        return zoo;
    }

    private List<Exp.RuleClause> parseRules(ParseTree child) {
        List<Exp.RuleClause> rules = new ArrayList<>();
        if (child instanceof deliaParser.DrulesContext) {
            deliaParser.DrulesContext dctx = (deliaParser.DrulesContext) child;
            for (int i = 0; i < dctx.getChildCount(); i++) {
                ParseTree inner = dctx.getChild(i);
                if (inner.getText().equals(",")) {
                    continue;
                }
                deliaParser.DruleContext rctx = (deliaParser.DruleContext) inner;
                CompilerResults zoo = this.visitCexpr(rctx.cexpr());
                Exp.RuleClause clause = new Exp.RuleClause(zoo.getAsWhereClause().where);
                rules.add(clause);
            }
        }
        return rules;
    }

    @Override
    public CompilerResults visitTypeStruct(deliaParser.TypeStructContext ctx) {
        // 'type' SYMBOL name '{' structFields* '}' drules? 'end'   # typeStruct
        //   0       1    2           4
        ParseTree child = ctx.getChild(1);
        String typeName = child.getText();
        child = ctx.getChild(2);
        String baseName = child.getText();

        AST.TypeAst typeAST = new AST.TypeAst(typeName);
        typeAST.loc = LocHelper.genLoc(ctx.getStart(), ctx.getStop());
        typeAST.baseName = baseName;
        typeAST.isScalarType = false;
        child = ctx.getChild(5);
        if (child instanceof deliaParser.DrulesContext) {
            typeAST.rules = parseRules(child);
        } else {
            child = ctx.getChild(6);
            typeAST.rules = parseRules(child);
        }

        child = ctx.getChild(4);
        if (child instanceof deliaParser.StructFieldsContext) {
            CompilerResults zoo1 = visitStructFields(ctx.structFields());
            AST.TypeAst tmpAST = (AST.TypeAst) zoo1.getStatementAst();
            typeAST.fields.addAll(tmpAST.fields);
        }

        CompilerResults zoo = new CompilerResults((Exp.OperandExp) null);
        zoo.addStatementAst(typeAST);
        return zoo;
    }

    @Override
    public CompilerResults visitStructFields(deliaParser.StructFieldsContext ctx) {
        AST.TypeAst typeAST = new AST.TypeAst("unknown!");
        typeAST.loc = LocHelper.genLoc(ctx.getStart(), ctx.getStop());

        for (int i = 0; i < ctx.structField().size(); i++) {
            CompilerResults zoo1 = visitStructField(ctx.structField(i));
            if (zoo1 == null) {
                continue;
            }
            AST.TypeAst tmpAST = (AST.TypeAst) zoo1.getStatementAst();
            typeAST.fields.add(tmpAST.fields.get(0));
        }

        CompilerResults zoo = new CompilerResults((Exp.OperandExp) null);
        zoo.addStatementAst(typeAST);
        return zoo;
    }

    @Override
    public CompilerResults visitStructField(deliaParser.StructFieldContext ctx) {
        // : 'relation'? SYMBOL name relationName? fieldModifiers?
        ParseTree child = ctx.getChild(0);
        if (child == null) {
            //hmm. line 4:0 mismatched input '}' expecting {'relation', SYMBOL}
            return null; //usually caused by a trailing , in last field
        }
        int offset = 0;
        boolean isRelation = false;
        if (child.getText().equals("relation")) {
            child = ctx.getChild(1);
            offset = 1;
            isRelation = true;
        }
        String fieldName = child.getText(); //SYMBOL
        child = ctx.getChild(offset + 1); //name
        String typeName = child.getText();

        child = ctx.getChild(offset + 2); //maybe relatioName
        String relationName = null;
        if (child instanceof deliaParser.RelationNameStrContext) {
            CompilerResults zooScalar = visitRelationNameStr((deliaParser.RelationNameStrContext) child);
            relationName = zooScalar.elem.strValue();
            offset++;
        } else if (child instanceof deliaParser.RelationNameStr2Context) {
            CompilerResults zooScalar = visitRelationNameStr2((deliaParser.RelationNameStr2Context) child);
            relationName = zooScalar.elem.strValue();
            offset++;
        }

        List<String> modifiers = new ArrayList<>();
        String defaultValue = null;
        if (ctx.getChildCount() > (offset + 2)) {
            child = ctx.getChild(offset + 2);
            for (int i = 0; i < child.getChildCount(); i++) {
                ParseTree xchild = child.getChild(i);
                if (xchild instanceof deliaParser.DefaultValueContext) {
                    defaultValue = parseDefaultValue((deliaParser.DefaultValueContext)xchild);
                } else {
                    String modifier = child.getChild(i).getText();
                    modifiers.add(modifier);
                }
            }
        }
        if (ctx.getChildCount() > (offset + 3)) {
            child = ctx.getChild(offset + 3);
            if (child instanceof deliaParser.DefaultValueContext) {
                defaultValue = parseDefaultValue((deliaParser.DefaultValueContext)child);
            }
        }

        //TODO support relation later

        AST.TypeFieldAst fieldAST = new AST.TypeFieldAst(fieldName);
        fieldAST.loc = LocHelper.genLoc(ctx.getStart(), ctx.getStop());
        fieldAST.typeName = typeName;
        fieldAST.isRelation = isRelation;
        fieldAST.relationName = relationName;
        if (!modifiers.isEmpty()) {
            for (String modifier : modifiers) {
                switch (modifier) {
                    case "optional":
                        fieldAST.isOptional = true;
                        break;
                    case "unique":
                        fieldAST.isUnique = true;
                        break;
                    case "primaryKey":
                        fieldAST.isPrimaryKey = true;
                        break;
                    case "serial":
                        fieldAST.isSerial = true;
                        break;
                    case "parent":
                        fieldAST.isParent = true;
                        break;
                    case "one":
                        fieldAST.isOne = true;
                        break;
                    case "many":
                        fieldAST.isMany = true;
                        break;
                    default: //how report error?
                        break;
                }
            }
        }
        if (defaultValue != null) {
            fieldAST.defaultVal = defaultValue;
        }

        CompilerResults zoo = new CompilerResults((Exp.OperandExp) null);
        AST.TypeAst typeAST = new AST.TypeAst("");
        typeAST.loc = LocHelper.genLoc(ctx.getStart(), ctx.getStop());
        zoo.addStatementAst(typeAST);

        typeAST.fields.add(fieldAST);
        return zoo;
    }

    private String parseDefaultValue(deliaParser.DefaultValueContext dvctx) {
        ParseTree argCtx = dvctx.getChild(2); //default ( someValue )
        Exp.ValueExp vexp = buildStringDVal(argCtx); //removes ' or " delims
        if (vexp == null) {
            return null;
        }
        return vexp.value == null ? null : vexp.value.asString();
    }

    @Override
    public CompilerResults visitInsert(deliaParser.InsertContext ctx) {
        //  : 'insert' name '{' valuePair '}' #insert
        ParseTree child = ctx.getChild(1);
        String typeName = child.getText();

        AST.InsertStatementAst insertAST = new AST.InsertStatementAst();
        insertAST.loc = LocHelper.genLoc(ctx.getStart(), ctx.getStop());
        insertAST.typeName = typeName;

        child = ctx.getChild(3);
        if (child instanceof deliaParser.ValuePairsContext) {
            CompilerResults zoo1 = visitVpValuePairs((deliaParser.VpValuePairsContext) ctx.valuePairs());
            AST.InsertStatementAst tmpAST = (AST.InsertStatementAst) zoo1.getStatementAst();
            insertAST.fields.addAll(tmpAST.fields);
        }

        CompilerResults zoo = new CompilerResults((Exp.OperandExp) null);
        zoo.addStatementAst(insertAST);
        return zoo;
    }

    @Override
    public CompilerResults visitVpValuePairs(deliaParser.VpValuePairsContext ctx) {
//         : crudAction? SYMBOL ':' elem               # vpElem
//         | crudAction? SYMBOL ':' '[' elemList ']'   # vpList

        AST.InsertStatementAst insertAST = new AST.InsertStatementAst();
        insertAST.loc = LocHelper.genLoc(ctx.getStart(), ctx.getStop());

        for (int i = 0; i < ctx.valuePairArg().size(); i++) {
            CompilerResults zoo1 = visitValuePairArg(ctx.valuePairArg(i));
            AST.InsertFieldStatementAst tmpAST = (AST.InsertFieldStatementAst) zoo1.getStatementAst();
            tmpAST.loc = LocHelper.genLoc(ctx.getStart(), ctx.getStop());
            insertAST.fields.add(tmpAST);
        }

        CompilerResults zoo = new CompilerResults((Exp.OperandExp) null);
        zoo.addStatementAst(insertAST);
        return zoo;
    }

    @Override
    public CompilerResults visitVpElem(deliaParser.VpElemContext ctx) {
        return super.visitVpElem(ctx);
    }

    @Override
    public CompilerResults visitVpList(deliaParser.VpListContext ctx) {
        // | SYMBOL ':' '[' elemList ']'   # vpList
        ParseTree child = ctx.getChild(0);
        String fieldName = child.getText();


        AST.InsertFieldStatementAst fieldAST = new AST.InsertFieldStatementAst();
        fieldAST.loc = LocHelper.genLoc(ctx.getStart(), ctx.getStop());
        fieldAST.fieldName = fieldName;
        Exp.ListExp listExp = new Exp.ListExp();
        fieldAST.listExp = listExp;

        for (int i = 0; i < ctx.valueElemList().getChildCount(); i++) {
            child = ctx.getChild(i);
            CompilerResults zoo1 = visitElem(null);
            listExp.listL.add(zoo1.elem);
        }

        CompilerResults zoo = new CompilerResults((Exp.OperandExp) null);
        zoo.addStatementAst(fieldAST);
        return zoo;
    }

    @Override
    public CompilerResults visitDeleteStatement(deliaParser.DeleteStatementContext ctx) {
        //  : 'delete' name filter
        ParseTree child = ctx.getChild(1);
        String typeName = child.getText();

        CompilerResults zoo2 = this.visitFilter(ctx.filter());

        CompilerResults zoo = new CompilerResults((Exp.OperandExp) null);
        AST.DeleteStatementAst deleteAST = new AST.DeleteStatementAst();
        deleteAST.loc = LocHelper.genLoc(ctx.getStart(), ctx.getStop());
        deleteAST.typeName = typeName;
        zoo.addStatementAst(deleteAST);
        deleteAST.whereClause = zoo2.getAsWhereClause();

        return zoo;
    }

    @Override
    public CompilerResults visitUpdate1(deliaParser.Update1Context ctx) {
        //  : 'update' name filter '{' valuePairs? '}'
        ParseTree child = ctx.getChild(1);
        String typeName = child.getText();

        CompilerResults zoo2 = this.visitFilter(ctx.filter());
        child = ctx.getChild(4);
        CompilerResults zoo3 = null;
        if (child instanceof deliaParser.ValuePairsContext) {
            zoo3 = this.visitVpValuePairs((deliaParser.VpValuePairsContext) child);
        }

        CompilerResults zoo = new CompilerResults((Exp.OperandExp) null);
        AST.UpdateStatementAst updateAST = new AST.UpdateStatementAst();
        updateAST.loc = LocHelper.genLoc(ctx.getStart(), ctx.getStop());
        updateAST.typeName = typeName;
        updateAST.whereClause = zoo2.getAsWhereClause();
        if (zoo3 != null) {
            AST.InsertStatementAst insert = (AST.InsertStatementAst) zoo3.getStatementAst();
            updateAST.fields.addAll(insert.fields);
        }

        zoo.addStatementAst(updateAST);
        return zoo;
    }

    @Override
    public CompilerResults visitUpsert1(deliaParser.Upsert1Context ctx) {
        // 'upsert' upsertFlag? name filter '{' valuePairs? '}' #upsert1
        ParseTree child = ctx.getChild(1);
        int offset = 0;
        boolean noUpdateFlag = false;
        if ("-noUpdate".equals(child.getText())) {
            noUpdateFlag = true;
            offset = 1;
            child = ctx.getChild(1 + offset);
        }
        String typeName = child.getText();

        CompilerResults zoo2 = this.visitFilter(ctx.filter());
        child = ctx.getChild(4 + offset);
        CompilerResults zoo3 = null;
        if (child instanceof deliaParser.ValuePairsContext) {
            zoo3 = this.visitVpValuePairs((deliaParser.VpValuePairsContext) child);
        }

        CompilerResults zoo = new CompilerResults((Exp.OperandExp) null);
        AST.UpsertStatementAst upsertAST = new AST.UpsertStatementAst();
        upsertAST.loc = LocHelper.genLoc(ctx.getStart(), ctx.getStop());
        upsertAST.typeName = typeName;
        upsertAST.whereClause = zoo2.getAsWhereClause();
        upsertAST.noUpdateFlag = noUpdateFlag;
        if (zoo3 != null) {
            AST.InsertStatementAst insert = (AST.InsertStatementAst) zoo3.getStatementAst();
            upsertAST.fields.addAll(insert.fields);
        }

        zoo.addStatementAst(upsertAST);
        return zoo;
    }

    public CompilerResults visitValuePairArg(deliaParser.ValuePairArgContext ctx) {
        //   : crudAction? SYMBOL ':' elem
        ParseTree child = ctx.getChild(0);
        String crudAction = null;
        int offset = 0;
        if (child instanceof deliaParser.CrudActionContext) {
            crudAction = child.getText();
            offset = 1;
            child = ctx.getChild(1);
        }

        String fieldName = child.getText();

        AST.InsertFieldStatementAst fieldAST = new AST.InsertFieldStatementAst();
        fieldAST.loc = LocHelper.genLoc(ctx.getStart(), ctx.getStop());
        fieldAST.fieldName = fieldName;
        fieldAST.crudAction = crudAction;

        child = ctx.getChild(2 + offset);
        if (child.getText().equals("[")) {
            child = ctx.getChild(3 + offset);
            Exp.ListExp listExp = new Exp.ListExp();
            fieldAST.listExp = listExp;

            deliaParser.ValueElemListContext elemListContext = (deliaParser.ValueElemListContext) child;
            for (int i = 0; i < elemListContext.getChildCount(); i += 2) { //count by 2 to skip ','
                child = elemListContext.getChild(i);
                if (child instanceof deliaParser.ValueElemContext) {
                    deliaParser.ValueElemContext vvexp = (deliaParser.ValueElemContext) child;
                    child = vvexp.getChild(0); //cexpr
                    child = child.getChild(0);
                    //TODO handle '{' elem ( SEP elem )* '}' later
                }
                CompilerResults zoo1 = visitElem((deliaParser.ElemContext) child);
                listExp.listL.add(zoo1.elem);
            }

        } else {
            ParseTree tmp2 = child.getChild(0);
            if (tmp2 instanceof deliaParser.CexprContext) {
                CompilerResults zooElem = myVisitScalar(tmp2.getChild(0));
                if (zooElem.elem instanceof Exp.NullExp) {
                    fieldAST.valueExp = new Exp.ValueExp();
                    fieldAST.valueExp.value = null;
                } else if (zooElem.elem instanceof Exp.FieldExp) {
                    fieldAST.varExp = (Exp.FieldExp) zooElem.elem;
                } else {
                    fieldAST.valueExp = (Exp.ValueExp) zooElem.elem;
                }
            } else {
                System.out.println("DDDDDDDDDDDDDDDDDD");
            }
        }

        CompilerResults zoo = new CompilerResults((Exp.ElementExp) null);
        zoo.addStatementAst(fieldAST);
        return zoo;
    }

    @Override
    public CompilerResults visitLet(deliaParser.LetContext ctx) {
        // : 'let' letVar '=' name filter fnChain?   # let
        CompilerResults zoox = parseNameAndType(ctx.getChild(1));

        CompilerResults zoo1 = this.visitName(ctx.name());
        CompilerResults zoo2 = this.visitFilter(ctx.filter());
        CompilerResults zoo3 = this.visitFnChain(ctx.fnChain());

        CompilerResults zoo = new CompilerResults((Exp.OperandExp) null);
        AST.LetStatementAst letStatementAst = new AST.LetStatementAst();
        letStatementAst.loc = LocHelper.genLoc(ctx.getStart(), ctx.getStop());
        zoo.addLetStatementAst(letStatementAst);
        letStatementAst.varName = zoox.getLetStatementAst().varName;
        letStatementAst.typeName = zoo1.elem.strValue(); // zoox.letStatementAst.typeName;
        letStatementAst.whereClause = zoo2.getAsWhereClause();
        if (zoo3 != null) {
            letStatementAst.fieldAndFuncs = (Exp.DottedExp) zoo3.operandExp;
        }
        return zoo;
    }

    @Override
    public CompilerResults visitLetNoVar(deliaParser.LetNoVarContext ctx) {
        CompilerResults zoo1 = this.visitName(ctx.name());
        CompilerResults zoo2 = this.visitFilter(ctx.filter());
        CompilerResults zoo3 = this.visitFnChain(ctx.fnChain());

        CompilerResults zoo = new CompilerResults((Exp.OperandExp) null);
        AST.LetStatementAst letStatementAst = new AST.LetStatementAst();
        letStatementAst.loc = LocHelper.genLoc(ctx.getStart(), ctx.getStop());
        zoo.addLetStatementAst(letStatementAst);
        letStatementAst.typeName = zoo1.elem.strValue();
        letStatementAst.whereClause = zoo2.getAsWhereClause();
        if (zoo3 != null) {
            letStatementAst.fieldAndFuncs = (Exp.DottedExp) zoo3.operandExp;
        }
        return zoo;
    }

    @Override
    public CompilerResults visitNoType(deliaParser.NoTypeContext ctx) {
        CompilerResults zoo = new CompilerResults((Exp.OperandExp) null);
        AST.LetStatementAst letStatementAst = new AST.LetStatementAst();
        letStatementAst.loc = LocHelper.genLoc(ctx.getStart(), ctx.getStop());
        zoo.addLetStatementAst(letStatementAst);
        letStatementAst.varName = ctx.getText();
        return zoo;
    }

    @Override
    public CompilerResults visitWithType(deliaParser.WithTypeContext ctx) {
        CompilerResults zoo = new CompilerResults((Exp.OperandExp) null);
        AST.LetStatementAst letStatementAst = new AST.LetStatementAst();
        letStatementAst.loc = LocHelper.genLoc(ctx.getStart(), ctx.getStop());
        zoo.addLetStatementAst(letStatementAst);
        letStatementAst.varName = ctx.getChild(0).getText();
        CompilerResults zoo1 = this.visitName(ctx.name());
        letStatementAst.typeName = zoo1.elem.strValue();
        return zoo;
    }

    @Override
    public CompilerResults visitLetscalar(deliaParser.LetscalarContext ctx) {
        CompilerResults zoo = parseNameAndType(ctx.getChild(1));

        /**
         *  | 'let' letVar '=' scalar EOF                # letscalar
         * scalar: NUM              # Num
         *  | '-' NUM               # NegNum
         *  | FloatingPointLiteral  # Real
         *  | '-' FloatingPointLiteral  # NegReal
         *  | (TRUE|FALSE)          # Bool
         *  | StringLiteral         # Str
         *  | StringLiteral2         # Str2
         */
        ParseTree child = ctx.getChild(3);
        CompilerResults zooScalar = myVisitScalar(child);
        zoo.getLetStatementAst().scalarElem = zooScalar.elem;

        return zoo;
    }

    private CompilerResults myVisitScalar(ParseTree child) {
        CompilerResults zooScalar = null;
        if (child instanceof deliaParser.BoolContext) {
            deliaParser.BoolContext bctx = (deliaParser.BoolContext) child;
            zooScalar = visitBool(bctx);
        } else if (child instanceof deliaParser.NumContext) {
            deliaParser.NumContext bctx = (deliaParser.NumContext) child;
            zooScalar = visitNum(bctx);
        } else if (child instanceof deliaParser.NegNumContext) {
            deliaParser.NegNumContext bctx = (deliaParser.NegNumContext) child;
            zooScalar = visitNegNum(bctx);
        } else if (child instanceof deliaParser.RealContext) {
            deliaParser.RealContext bctx = (deliaParser.RealContext) child;
            zooScalar = visitReal(bctx);
        } else if (child instanceof deliaParser.NegRealContext) {
            deliaParser.NegRealContext bctx = (deliaParser.NegRealContext) child;
            zooScalar = visitNegReal(bctx);
        } else if (child instanceof deliaParser.StrContext) {
            deliaParser.StrContext bctx = (deliaParser.StrContext) child;
            zooScalar = visitStr(bctx);
        } else if (child instanceof deliaParser.Str2Context) {
            deliaParser.Str2Context bctx = (deliaParser.Str2Context) child;
            zooScalar = visitStr2(bctx);
        } else if (child instanceof deliaParser.NullValueContext) {
            deliaParser.NullValueContext bctx = (deliaParser.NullValueContext) child;
            zooScalar = visitNullValue(bctx);
        } else if (child != null && child.getText().equals("$$")) {
            Exp.FieldExp vexp = new Exp.FieldExp("$$", null);
            zooScalar = new CompilerResults(vexp);
        } else if (child instanceof deliaParser.ElemContext) {
            deliaParser.ElemContext bctx = (deliaParser.ElemContext) child;
            zooScalar = visitElem(bctx);
        } else if (child instanceof deliaParser.NameContext) {
            deliaParser.NameContext bctx = (deliaParser.NameContext) child;
            Exp.FieldExp vexp = new Exp.FieldExp(bctx.getText(), null);
            zooScalar = new CompilerResults(vexp);
            //TODO this covers a var ref. Should also support fns myfn(a,bc). We would parse as DottedExp
        } else {
            throw new RuntimeException("unknown scalar context");
        }
        return zooScalar;
    }

    @Override
    public CompilerResults visitRelationNameStr(deliaParser.RelationNameStrContext ctx) {
        TerminalNode bctx = ctx.StringLiteral();
        CompilerResults zooScalar = doVisitStr(bctx);
        return zooScalar;
    }

    @Override
    public CompilerResults visitRelationNameStr2(deliaParser.RelationNameStr2Context ctx) {
        TerminalNode bctx = ctx.StringLiteral2();
        CompilerResults zooScalar = doVisitStr2(bctx);
        return zooScalar;
    }

    private CompilerResults parseNameAndType(ParseTree child) {
        CompilerResults zoo = null;
        if (child instanceof deliaParser.NoTypeContext) {
            deliaParser.NoTypeContext cctx = (deliaParser.NoTypeContext) child;
            zoo = this.visitNoType(cctx);
        } else {
            deliaParser.WithTypeContext cctx = (deliaParser.WithTypeContext) child;
            zoo = this.visitWithType(cctx);
        }
        return zoo;
    }

    @Override
    public CompilerResults visitFnChain(deliaParser.FnChainContext ctx) {
        if (ctx == null) return null;

        Exp.DottedExp dottedExp = null;
        for (int i = 0; i < ctx.getChildCount(); i++) {
            ParseTree child = ctx.getChild(i);
            if (child.getText().equals(".")) {
                continue;
            }

            CompilerResults zoo = this.visitFnChainArg((deliaParser.FnChainArgContext) child);
            if (zoo.elem instanceof Exp.FieldExp) {
                Exp.FieldExp fexp = (Exp.FieldExp) zoo.elem;
                String[] ar = fexp.fieldName.split("\\.");
                if (ar.length > 1) {
                    for (String ff : ar) {
                        fexp = new Exp.FieldExp(ff, null);
                        dottedExp = addToDotted(fexp, dottedExp);
                    }
                } else {
                    dottedExp = addToDotted(fexp, dottedExp);
                }
            } else {
                dottedExp = addToDotted(zoo.elem, dottedExp);
            }
        }


        CompilerResults zoo1 = new CompilerResults(dottedExp);
        return zoo1;
    }

    private Exp.DottedExp addToDotted(Exp.ElementExp el, Exp.DottedExp dottedExp) {
        if (dottedExp == null) {
            dottedExp = new Exp.DottedExp(el);
        } else {
            dottedExp.chainL.add(el);
        }
        return dottedExp;
    }


    @Override
    public CompilerResults visitFilter(deliaParser.FilterContext ctx) {
        CompilerResults zoo = this.visitFilterexpr(ctx.filterexpr());
        return zoo;
    }
    @Override
    public CompilerResults visitFilterexpr(deliaParser.FilterexprContext ctx) {
        ParseTree child = ctx.getChild(0);
        if (child instanceof deliaParser.CexprContext) {
            return visitCexpr((deliaParser.CexprContext) child);
        } else {
            //compositeKeys
            int n = ctx.getChildCount();
            int numKeys = (n - 1) / 2;
            List<CompilerResults> list = new ArrayList<>();
            for(int i = 0; i < numKeys; i++) {
                int index = (2*i) + 1;
                ParseTree inner = ctx.getChild(index);
                if (inner instanceof deliaParser.ElemContext) {
                    deliaParser.ElemContext ectx = (deliaParser.ElemContext) inner;
                    CompilerResults zz = visitElem(ectx);
                    list.add(zz);
                }
            }
            //TODO handle empty list
            Exp.CompositeKeyExp listExp = new Exp.CompositeKeyExp();
            CompilerResults finalzz = new CompilerResults(listExp);
            for (CompilerResults tmp: list) {
                listExp.listL.add(tmp.elem);
            }
            return finalzz;
        }
    }

        @Override
    public CompilerResults visitCexpr(deliaParser.CexprContext ctx) {
        int n = ctx.getChildCount();
        boolean isNegg = false;
        if (n == 4) {
            String tmp = getIthChildText(ctx, 1);
            if ("in".equals(tmp)) {
                return doInExpression(ctx);
            }
            tmp = getIthChildText(ctx, 0);
            isNegg = "!".equals(tmp);
        }

        CompilerResults zz = visitChildren(ctx);
        if (zz == null) {
            if (ctx.exception != null) {
                throw ctx.exception;
            }
        }

//        if (zz != null && zz.elem != null) {
////            System.out.println("zz " + zz.elem.getClass().getSimpleName());
//        }
        if (zz.operandExp != null) {
            if (zz.operandExp instanceof Exp.OperatorExp) {
                Exp.OperatorExp oexp = (Exp.OperatorExp) zz.operandExp;
                oexp.negFlag = isNegg;

                if (n == 3) {
                    if (PENDING_DELIM.equals(oexp.op)) {
                        ParseTree cc = ctx.getChild(1);
                        String realOp = cc.getText();
                        oexp.op = realOp;
                    }
                }
            }
        }


        if (zz.elem instanceof Exp.ListExp) {
            Exp.OperatorExp exp = new Exp.OperatorExp();
            exp.negFlag = false;

//            for (int i = 0; i < ctx.getChildCount(); i++) {
//                ParseTree cc = ctx.getChild(i);
//                System.out.println(String.format("  %s", cc.getText()));
//            }

            if (ctx.getChildCount() > 0 && StringUtils.equals(ctx.getChild(0).getText(), "!")) {
                exp.negFlag = true;
            }

            Exp.ListExp lexp = (Exp.ListExp) zz.elem;
            exp.op1 = new Exp.DottedExp(lexp.listL.get(0));
            exp.op2 = new Exp.DottedExp(lexp.listL.get(1));
            String op = ctx.getChild(1).getText();
            exp.op = op;
            return new CompilerResults(exp);
        } else if (zz.elem instanceof Exp.OperatorExp) {
            Exp.OperatorExp exp = (Exp.OperatorExp) zz.elem;
            exp.negFlag = false;
            if (ctx.getChildCount() > 0 && StringUtils.equals(ctx.getChild(0).getText(), "!")) {
                exp.negFlag = true;
            }
            return zz;
        } else if (zz.elem instanceof Exp.FunctionExp) {
            Exp.FunctionExp fexp = (Exp.FunctionExp) zz.elem;
            fexp.negFlag = isNegg;
            return zz;
        } else {
            return zz;
        }
    }

    private CompilerResults doInExpression(deliaParser.CexprContext ctx) {
        // | cexpr 'in' BEGF cexpr ENDF
        ParseTree cc0 = ctx.getChild(0);
        ParseTree cc3 = ctx.getChild(3);
        CompilerResults cres0 = visit(cc0);
        CompilerResults cres3 = visit(cc3);
        return cres0;
    }

    private String getIthChildText(deliaParser.CexprContext ctx, int i) {
        ParseTree cc = ctx.getChild(i);
        String tmp = cc.getText();
        return tmp;
    }

    @Override
    public CompilerResults visitElem(deliaParser.ElemContext ctx) {
        CompilerResults zoo = visitChildren(ctx);
        return zoo;
    }

    @Override
    public CompilerResults visitFn(deliaParser.FnContext ctx) {
        String s = StringUtils.substringBefore(ctx.getText(), "(");
        Exp.FunctionExp exp = new Exp.FunctionExp(s);
        CompilerResults zz = visitFnargs(ctx.fnargs());
        if (zz.elem == null) {

        } else if (zz.elem instanceof Exp.ListExp) {
            Exp.ListExp lexp = (Exp.ListExp) zz.elem;
            exp.argsL.addAll(lexp.listL);
        } else {
            exp.argsL.add(zz.elem);
        }

        if (s.contains(".")) {
            exp.prefix = StringUtils.substringBeforeLast(exp.fnName, ".");
            exp.fnName = StringUtils.substringAfterLast(exp.fnName, ".");
        }

        return new CompilerResults(exp);
    }

    @Override
    public CompilerResults visitFnargs(deliaParser.FnargsContext ctx) {
        if (ctx == null) {
            return new CompilerResults((Exp.ElementExp) null);
        }
        CompilerResults zoo = visitChildren(ctx);
        return zoo;
    }

    @Override
    public CompilerResults visitName(deliaParser.NameContext ctx) {
        Exp.FieldExp vexp = new Exp.FieldExp(ctx.getText(), null);
        return new CompilerResults(vexp);
    }

    @Override
    public CompilerResults visitNum(deliaParser.NumContext ctx) {
        Exp.ValueExp vexp = new Exp.ValueExp();
//        vexp.value = builder.buildInt(ctx.getText());
        vexp.value = builder.buildEffectiveLongInt(ctx.getText());
        return new CompilerResults(vexp);
    }

    @Override
    public CompilerResults visitNegNum(deliaParser.NegNumContext ctx) {
        Exp.ValueExp vexp = new Exp.ValueExp();
//        vexp.value = builder.buildInt(ctx.getText());
        vexp.value = builder.buildEffectiveLongInt(ctx.getText());
        return new CompilerResults(vexp);
    }

    @Override
    public CompilerResults visitReal(deliaParser.RealContext ctx) {
        Exp.ValueExp vexp = new Exp.ValueExp();
        vexp.value = builder.buildNumber(ctx.getText());
        return new CompilerResults(vexp);
    }

    @Override
    public CompilerResults visitNegReal(deliaParser.NegRealContext ctx) {
        Exp.ValueExp vexp = new Exp.ValueExp();
        vexp.value = builder.buildNumber(ctx.getText());
        return new CompilerResults(vexp);
    }

    @Override
    public CompilerResults visitBool(deliaParser.BoolContext ctx) {
        Exp.ValueExp vexp = new Exp.ValueExp();
        vexp.value = builder.buildBoolean(ctx.getText());
        return new CompilerResults(vexp);
    }

    @Override
    public CompilerResults visitStr(deliaParser.StrContext ctx) {
        return doVisitStr(ctx);
    }

    private CompilerResults doVisitStr(ParseTree ctx) {
        Exp.ValueExp vexp = new Exp.ValueExp();
        int pos = ctx.getText().indexOf('"');
        int posEnd = ctx.getText().lastIndexOf('"');
        String str = ctx.getText().substring(pos + 1, posEnd);
        vexp.value = builder.buildString(str);
        return new CompilerResults(vexp);
    }

    @Override
    public CompilerResults visitStr2(deliaParser.Str2Context ctx) {
        return doVisitStr2(ctx);
    }

    private CompilerResults doVisitStr2(ParseTree ctx) {
        Exp.ValueExp vexp = buildStringDVal(ctx);
        return new CompilerResults(vexp);
    }
    private Exp.ValueExp buildStringDVal(ParseTree ctx) {
        Exp.ValueExp vexp = new Exp.ValueExp();
        int pos = ctx.getText().indexOf('\'');
        if (pos >= 0) {
            int posEnd = ctx.getText().lastIndexOf('\'');
            String str = ctx.getText().substring(pos + 1, posEnd);
            vexp.value = builder.buildString(str);
        } else {
            String str = ctx.getText();
            vexp.value = builder.buildString(str);
        }
        return vexp;
    }

    @Override
    public CompilerResults visitNullValue(deliaParser.NullValueContext ctx) {
        Exp.NullExp vexp = new Exp.NullExp();
        return new CompilerResults(vexp);
    }

    @Override
    protected CompilerResults aggregateResult(CompilerResults aggregate, CompilerResults nextResult) {
        if (aggregate != null && nextResult != null) {
            if (aggregate.elem instanceof Exp.ListExp) {
                Exp.ListExp lexp = (Exp.ListExp) aggregate.elem;
                lexp.listL.add(nextResult.elem);
            } else if (aggregate.operandExp != null && nextResult.operandExp != null) {
                Exp.OperatorExp exp = new Exp.OperatorExp();
                exp.negFlag = false;
                exp.op1 = aggregate.operandExp;
                exp.op = PENDING_DELIM; //set in visitCexp
                exp.op2 = nextResult.operandExp;
                aggregate.operandExp = exp;
            } else {
                Exp.ListExp lexp = new Exp.ListExp();
                lexp.listL.add(aggregate.elem);
                lexp.listL.add(nextResult.elem);
                aggregate.elem = lexp;
            }
        }

        if (aggregate != null) {
            return aggregate;
        } else {
            return nextResult;
        }
    }
}
