// Generated from C:/Users/16136/Documents/GitHub/delia/delia-antlr/src/test/java/org/delia/antlr\delia.g4 by ANTLR 4.9.1
package org.delia.compiler.antlr;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link deliaParser}.
 */
public interface deliaListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link deliaParser#deliaStatement}.
	 * @param ctx the parse tree
	 */
	void enterDeliaStatement(deliaParser.DeliaStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link deliaParser#deliaStatement}.
	 * @param ctx the parse tree
	 */
	void exitDeliaStatement(deliaParser.DeliaStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link deliaParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterStatement(deliaParser.StatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link deliaParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitStatement(deliaParser.StatementContext ctx);
	/**
	 * Enter a parse tree produced by the {@code configure}
	 * labeled alternative in {@link deliaParser#configureStatement}.
	 * @param ctx the parse tree
	 */
	void enterConfigure(deliaParser.ConfigureContext ctx);
	/**
	 * Exit a parse tree produced by the {@code configure}
	 * labeled alternative in {@link deliaParser#configureStatement}.
	 * @param ctx the parse tree
	 */
	void exitConfigure(deliaParser.ConfigureContext ctx);
	/**
	 * Enter a parse tree produced by the {@code schema}
	 * labeled alternative in {@link deliaParser#schemaStatement}.
	 * @param ctx the parse tree
	 */
	void enterSchema(deliaParser.SchemaContext ctx);
	/**
	 * Exit a parse tree produced by the {@code schema}
	 * labeled alternative in {@link deliaParser#schemaStatement}.
	 * @param ctx the parse tree
	 */
	void exitSchema(deliaParser.SchemaContext ctx);
	/**
	 * Enter a parse tree produced by the {@code schemaOff}
	 * labeled alternative in {@link deliaParser#schemaStatement}.
	 * @param ctx the parse tree
	 */
	void enterSchemaOff(deliaParser.SchemaOffContext ctx);
	/**
	 * Exit a parse tree produced by the {@code schemaOff}
	 * labeled alternative in {@link deliaParser#schemaStatement}.
	 * @param ctx the parse tree
	 */
	void exitSchemaOff(deliaParser.SchemaOffContext ctx);
	/**
	 * Enter a parse tree produced by {@link deliaParser#logStatement}.
	 * @param ctx the parse tree
	 */
	void enterLogStatement(deliaParser.LogStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link deliaParser#logStatement}.
	 * @param ctx the parse tree
	 */
	void exitLogStatement(deliaParser.LogStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link deliaParser#typeStatement}.
	 * @param ctx the parse tree
	 */
	void enterTypeStatement(deliaParser.TypeStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link deliaParser#typeStatement}.
	 * @param ctx the parse tree
	 */
	void exitTypeStatement(deliaParser.TypeStatementContext ctx);
	/**
	 * Enter a parse tree produced by the {@code typeScalar}
	 * labeled alternative in {@link deliaParser#scalarTypeStatement}.
	 * @param ctx the parse tree
	 */
	void enterTypeScalar(deliaParser.TypeScalarContext ctx);
	/**
	 * Exit a parse tree produced by the {@code typeScalar}
	 * labeled alternative in {@link deliaParser#scalarTypeStatement}.
	 * @param ctx the parse tree
	 */
	void exitTypeScalar(deliaParser.TypeScalarContext ctx);
	/**
	 * Enter a parse tree produced by the {@code typeStruct}
	 * labeled alternative in {@link deliaParser#structTypeStatement}.
	 * @param ctx the parse tree
	 */
	void enterTypeStruct(deliaParser.TypeStructContext ctx);
	/**
	 * Exit a parse tree produced by the {@code typeStruct}
	 * labeled alternative in {@link deliaParser#structTypeStatement}.
	 * @param ctx the parse tree
	 */
	void exitTypeStruct(deliaParser.TypeStructContext ctx);
	/**
	 * Enter a parse tree produced by {@link deliaParser#structFields}.
	 * @param ctx the parse tree
	 */
	void enterStructFields(deliaParser.StructFieldsContext ctx);
	/**
	 * Exit a parse tree produced by {@link deliaParser#structFields}.
	 * @param ctx the parse tree
	 */
	void exitStructFields(deliaParser.StructFieldsContext ctx);
	/**
	 * Enter a parse tree produced by {@link deliaParser#structField}.
	 * @param ctx the parse tree
	 */
	void enterStructField(deliaParser.StructFieldContext ctx);
	/**
	 * Exit a parse tree produced by {@link deliaParser#structField}.
	 * @param ctx the parse tree
	 */
	void exitStructField(deliaParser.StructFieldContext ctx);
	/**
	 * Enter a parse tree produced by the {@code relationNameStr}
	 * labeled alternative in {@link deliaParser#relationName}.
	 * @param ctx the parse tree
	 */
	void enterRelationNameStr(deliaParser.RelationNameStrContext ctx);
	/**
	 * Exit a parse tree produced by the {@code relationNameStr}
	 * labeled alternative in {@link deliaParser#relationName}.
	 * @param ctx the parse tree
	 */
	void exitRelationNameStr(deliaParser.RelationNameStrContext ctx);
	/**
	 * Enter a parse tree produced by the {@code relationNameStr2}
	 * labeled alternative in {@link deliaParser#relationName}.
	 * @param ctx the parse tree
	 */
	void enterRelationNameStr2(deliaParser.RelationNameStr2Context ctx);
	/**
	 * Exit a parse tree produced by the {@code relationNameStr2}
	 * labeled alternative in {@link deliaParser#relationName}.
	 * @param ctx the parse tree
	 */
	void exitRelationNameStr2(deliaParser.RelationNameStr2Context ctx);
	/**
	 * Enter a parse tree produced by {@link deliaParser#fieldModifiers}.
	 * @param ctx the parse tree
	 */
	void enterFieldModifiers(deliaParser.FieldModifiersContext ctx);
	/**
	 * Exit a parse tree produced by {@link deliaParser#fieldModifiers}.
	 * @param ctx the parse tree
	 */
	void exitFieldModifiers(deliaParser.FieldModifiersContext ctx);
	/**
	 * Enter a parse tree produced by {@link deliaParser#fieldModifier}.
	 * @param ctx the parse tree
	 */
	void enterFieldModifier(deliaParser.FieldModifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link deliaParser#fieldModifier}.
	 * @param ctx the parse tree
	 */
	void exitFieldModifier(deliaParser.FieldModifierContext ctx);
	/**
	 * Enter a parse tree produced by {@link deliaParser#drules}.
	 * @param ctx the parse tree
	 */
	void enterDrules(deliaParser.DrulesContext ctx);
	/**
	 * Exit a parse tree produced by {@link deliaParser#drules}.
	 * @param ctx the parse tree
	 */
	void exitDrules(deliaParser.DrulesContext ctx);
	/**
	 * Enter a parse tree produced by {@link deliaParser#drule}.
	 * @param ctx the parse tree
	 */
	void enterDrule(deliaParser.DruleContext ctx);
	/**
	 * Exit a parse tree produced by {@link deliaParser#drule}.
	 * @param ctx the parse tree
	 */
	void exitDrule(deliaParser.DruleContext ctx);
	/**
	 * Enter a parse tree produced by the {@code insert}
	 * labeled alternative in {@link deliaParser#insertStatement}.
	 * @param ctx the parse tree
	 */
	void enterInsert(deliaParser.InsertContext ctx);
	/**
	 * Exit a parse tree produced by the {@code insert}
	 * labeled alternative in {@link deliaParser#insertStatement}.
	 * @param ctx the parse tree
	 */
	void exitInsert(deliaParser.InsertContext ctx);
	/**
	 * Enter a parse tree produced by the {@code vpValuePairs}
	 * labeled alternative in {@link deliaParser#valuePairs}.
	 * @param ctx the parse tree
	 */
	void enterVpValuePairs(deliaParser.VpValuePairsContext ctx);
	/**
	 * Exit a parse tree produced by the {@code vpValuePairs}
	 * labeled alternative in {@link deliaParser#valuePairs}.
	 * @param ctx the parse tree
	 */
	void exitVpValuePairs(deliaParser.VpValuePairsContext ctx);
	/**
	 * Enter a parse tree produced by the {@code vpElem}
	 * labeled alternative in {@link deliaParser#valuePairArg}.
	 * @param ctx the parse tree
	 */
	void enterVpElem(deliaParser.VpElemContext ctx);
	/**
	 * Exit a parse tree produced by the {@code vpElem}
	 * labeled alternative in {@link deliaParser#valuePairArg}.
	 * @param ctx the parse tree
	 */
	void exitVpElem(deliaParser.VpElemContext ctx);
	/**
	 * Enter a parse tree produced by the {@code vpList}
	 * labeled alternative in {@link deliaParser#valuePairArg}.
	 * @param ctx the parse tree
	 */
	void enterVpList(deliaParser.VpListContext ctx);
	/**
	 * Exit a parse tree produced by the {@code vpList}
	 * labeled alternative in {@link deliaParser#valuePairArg}.
	 * @param ctx the parse tree
	 */
	void exitVpList(deliaParser.VpListContext ctx);
	/**
	 * Enter a parse tree produced by {@link deliaParser#crudAction}.
	 * @param ctx the parse tree
	 */
	void enterCrudAction(deliaParser.CrudActionContext ctx);
	/**
	 * Exit a parse tree produced by {@link deliaParser#crudAction}.
	 * @param ctx the parse tree
	 */
	void exitCrudAction(deliaParser.CrudActionContext ctx);
	/**
	 * Enter a parse tree produced by {@link deliaParser#elemList}.
	 * @param ctx the parse tree
	 */
	void enterElemList(deliaParser.ElemListContext ctx);
	/**
	 * Exit a parse tree produced by {@link deliaParser#elemList}.
	 * @param ctx the parse tree
	 */
	void exitElemList(deliaParser.ElemListContext ctx);
	/**
	 * Enter a parse tree produced by {@link deliaParser#deleteStatement}.
	 * @param ctx the parse tree
	 */
	void enterDeleteStatement(deliaParser.DeleteStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link deliaParser#deleteStatement}.
	 * @param ctx the parse tree
	 */
	void exitDeleteStatement(deliaParser.DeleteStatementContext ctx);
	/**
	 * Enter a parse tree produced by the {@code update1}
	 * labeled alternative in {@link deliaParser#updateStatement}.
	 * @param ctx the parse tree
	 */
	void enterUpdate1(deliaParser.Update1Context ctx);
	/**
	 * Exit a parse tree produced by the {@code update1}
	 * labeled alternative in {@link deliaParser#updateStatement}.
	 * @param ctx the parse tree
	 */
	void exitUpdate1(deliaParser.Update1Context ctx);
	/**
	 * Enter a parse tree produced by the {@code upsert1}
	 * labeled alternative in {@link deliaParser#updateStatement}.
	 * @param ctx the parse tree
	 */
	void enterUpsert1(deliaParser.Upsert1Context ctx);
	/**
	 * Exit a parse tree produced by the {@code upsert1}
	 * labeled alternative in {@link deliaParser#updateStatement}.
	 * @param ctx the parse tree
	 */
	void exitUpsert1(deliaParser.Upsert1Context ctx);
	/**
	 * Enter a parse tree produced by {@link deliaParser#upsertFlag}.
	 * @param ctx the parse tree
	 */
	void enterUpsertFlag(deliaParser.UpsertFlagContext ctx);
	/**
	 * Exit a parse tree produced by {@link deliaParser#upsertFlag}.
	 * @param ctx the parse tree
	 */
	void exitUpsertFlag(deliaParser.UpsertFlagContext ctx);
	/**
	 * Enter a parse tree produced by the {@code let}
	 * labeled alternative in {@link deliaParser#letStatement}.
	 * @param ctx the parse tree
	 */
	void enterLet(deliaParser.LetContext ctx);
	/**
	 * Exit a parse tree produced by the {@code let}
	 * labeled alternative in {@link deliaParser#letStatement}.
	 * @param ctx the parse tree
	 */
	void exitLet(deliaParser.LetContext ctx);
	/**
	 * Enter a parse tree produced by the {@code letscalar}
	 * labeled alternative in {@link deliaParser#letStatement}.
	 * @param ctx the parse tree
	 */
	void enterLetscalar(deliaParser.LetscalarContext ctx);
	/**
	 * Exit a parse tree produced by the {@code letscalar}
	 * labeled alternative in {@link deliaParser#letStatement}.
	 * @param ctx the parse tree
	 */
	void exitLetscalar(deliaParser.LetscalarContext ctx);
	/**
	 * Enter a parse tree produced by the {@code letNoVar}
	 * labeled alternative in {@link deliaParser#letStatement}.
	 * @param ctx the parse tree
	 */
	void enterLetNoVar(deliaParser.LetNoVarContext ctx);
	/**
	 * Exit a parse tree produced by the {@code letNoVar}
	 * labeled alternative in {@link deliaParser#letStatement}.
	 * @param ctx the parse tree
	 */
	void exitLetNoVar(deliaParser.LetNoVarContext ctx);
	/**
	 * Enter a parse tree produced by the {@code noType}
	 * labeled alternative in {@link deliaParser#letVar}.
	 * @param ctx the parse tree
	 */
	void enterNoType(deliaParser.NoTypeContext ctx);
	/**
	 * Exit a parse tree produced by the {@code noType}
	 * labeled alternative in {@link deliaParser#letVar}.
	 * @param ctx the parse tree
	 */
	void exitNoType(deliaParser.NoTypeContext ctx);
	/**
	 * Enter a parse tree produced by the {@code withType}
	 * labeled alternative in {@link deliaParser#letVar}.
	 * @param ctx the parse tree
	 */
	void enterWithType(deliaParser.WithTypeContext ctx);
	/**
	 * Exit a parse tree produced by the {@code withType}
	 * labeled alternative in {@link deliaParser#letVar}.
	 * @param ctx the parse tree
	 */
	void exitWithType(deliaParser.WithTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link deliaParser#fnChain}.
	 * @param ctx the parse tree
	 */
	void enterFnChain(deliaParser.FnChainContext ctx);
	/**
	 * Exit a parse tree produced by {@link deliaParser#fnChain}.
	 * @param ctx the parse tree
	 */
	void exitFnChain(deliaParser.FnChainContext ctx);
	/**
	 * Enter a parse tree produced by {@link deliaParser#fnChainArg}.
	 * @param ctx the parse tree
	 */
	void enterFnChainArg(deliaParser.FnChainArgContext ctx);
	/**
	 * Exit a parse tree produced by {@link deliaParser#fnChainArg}.
	 * @param ctx the parse tree
	 */
	void exitFnChainArg(deliaParser.FnChainArgContext ctx);
	/**
	 * Enter a parse tree produced by {@link deliaParser#filter}.
	 * @param ctx the parse tree
	 */
	void enterFilter(deliaParser.FilterContext ctx);
	/**
	 * Exit a parse tree produced by {@link deliaParser#filter}.
	 * @param ctx the parse tree
	 */
	void exitFilter(deliaParser.FilterContext ctx);
	/**
	 * Enter a parse tree produced by {@link deliaParser#cexpr}.
	 * @param ctx the parse tree
	 */
	void enterCexpr(deliaParser.CexprContext ctx);
	/**
	 * Exit a parse tree produced by {@link deliaParser#cexpr}.
	 * @param ctx the parse tree
	 */
	void exitCexpr(deliaParser.CexprContext ctx);
	/**
	 * Enter a parse tree produced by {@link deliaParser#elem}.
	 * @param ctx the parse tree
	 */
	void enterElem(deliaParser.ElemContext ctx);
	/**
	 * Exit a parse tree produced by {@link deliaParser#elem}.
	 * @param ctx the parse tree
	 */
	void exitElem(deliaParser.ElemContext ctx);
	/**
	 * Enter a parse tree produced by {@link deliaParser#fn}.
	 * @param ctx the parse tree
	 */
	void enterFn(deliaParser.FnContext ctx);
	/**
	 * Exit a parse tree produced by {@link deliaParser#fn}.
	 * @param ctx the parse tree
	 */
	void exitFn(deliaParser.FnContext ctx);
	/**
	 * Enter a parse tree produced by {@link deliaParser#fnargs}.
	 * @param ctx the parse tree
	 */
	void enterFnargs(deliaParser.FnargsContext ctx);
	/**
	 * Exit a parse tree produced by {@link deliaParser#fnargs}.
	 * @param ctx the parse tree
	 */
	void exitFnargs(deliaParser.FnargsContext ctx);
	/**
	 * Enter a parse tree produced by {@link deliaParser#name}.
	 * @param ctx the parse tree
	 */
	void enterName(deliaParser.NameContext ctx);
	/**
	 * Exit a parse tree produced by {@link deliaParser#name}.
	 * @param ctx the parse tree
	 */
	void exitName(deliaParser.NameContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Num}
	 * labeled alternative in {@link deliaParser#scalar}.
	 * @param ctx the parse tree
	 */
	void enterNum(deliaParser.NumContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Num}
	 * labeled alternative in {@link deliaParser#scalar}.
	 * @param ctx the parse tree
	 */
	void exitNum(deliaParser.NumContext ctx);
	/**
	 * Enter a parse tree produced by the {@code NegNum}
	 * labeled alternative in {@link deliaParser#scalar}.
	 * @param ctx the parse tree
	 */
	void enterNegNum(deliaParser.NegNumContext ctx);
	/**
	 * Exit a parse tree produced by the {@code NegNum}
	 * labeled alternative in {@link deliaParser#scalar}.
	 * @param ctx the parse tree
	 */
	void exitNegNum(deliaParser.NegNumContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Real}
	 * labeled alternative in {@link deliaParser#scalar}.
	 * @param ctx the parse tree
	 */
	void enterReal(deliaParser.RealContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Real}
	 * labeled alternative in {@link deliaParser#scalar}.
	 * @param ctx the parse tree
	 */
	void exitReal(deliaParser.RealContext ctx);
	/**
	 * Enter a parse tree produced by the {@code NegReal}
	 * labeled alternative in {@link deliaParser#scalar}.
	 * @param ctx the parse tree
	 */
	void enterNegReal(deliaParser.NegRealContext ctx);
	/**
	 * Exit a parse tree produced by the {@code NegReal}
	 * labeled alternative in {@link deliaParser#scalar}.
	 * @param ctx the parse tree
	 */
	void exitNegReal(deliaParser.NegRealContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Bool}
	 * labeled alternative in {@link deliaParser#scalar}.
	 * @param ctx the parse tree
	 */
	void enterBool(deliaParser.BoolContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Bool}
	 * labeled alternative in {@link deliaParser#scalar}.
	 * @param ctx the parse tree
	 */
	void exitBool(deliaParser.BoolContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Str}
	 * labeled alternative in {@link deliaParser#scalar}.
	 * @param ctx the parse tree
	 */
	void enterStr(deliaParser.StrContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Str}
	 * labeled alternative in {@link deliaParser#scalar}.
	 * @param ctx the parse tree
	 */
	void exitStr(deliaParser.StrContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Str2}
	 * labeled alternative in {@link deliaParser#scalar}.
	 * @param ctx the parse tree
	 */
	void enterStr2(deliaParser.Str2Context ctx);
	/**
	 * Exit a parse tree produced by the {@code Str2}
	 * labeled alternative in {@link deliaParser#scalar}.
	 * @param ctx the parse tree
	 */
	void exitStr2(deliaParser.Str2Context ctx);
	/**
	 * Enter a parse tree produced by the {@code NullValue}
	 * labeled alternative in {@link deliaParser#scalar}.
	 * @param ctx the parse tree
	 */
	void enterNullValue(deliaParser.NullValueContext ctx);
	/**
	 * Exit a parse tree produced by the {@code NullValue}
	 * labeled alternative in {@link deliaParser#scalar}.
	 * @param ctx the parse tree
	 */
	void exitNullValue(deliaParser.NullValueContext ctx);
}