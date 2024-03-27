// Generated from C:/Users/16136/Documents/GitHub/delia/delia-lang/src/test/java/org/delia/antlr/delia.g4 by ANTLR 4.13.1
package org.delia.antlr;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link deliaParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface deliaVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link deliaParser#deliaStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDeliaStatement(deliaParser.DeliaStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link deliaParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStatement(deliaParser.StatementContext ctx);
	/**
	 * Visit a parse tree produced by the {@code configure}
	 * labeled alternative in {@link deliaParser#configureStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConfigure(deliaParser.ConfigureContext ctx);
	/**
	 * Visit a parse tree produced by the {@code schema}
	 * labeled alternative in {@link deliaParser#schemaStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSchema(deliaParser.SchemaContext ctx);
	/**
	 * Visit a parse tree produced by the {@code schemaOff}
	 * labeled alternative in {@link deliaParser#schemaStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSchemaOff(deliaParser.SchemaOffContext ctx);
	/**
	 * Visit a parse tree produced by {@link deliaParser#logStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLogStatement(deliaParser.LogStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link deliaParser#typeStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeStatement(deliaParser.TypeStatementContext ctx);
	/**
	 * Visit a parse tree produced by the {@code typeScalar}
	 * labeled alternative in {@link deliaParser#scalarTypeStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeScalar(deliaParser.TypeScalarContext ctx);
	/**
	 * Visit a parse tree produced by the {@code typeStruct}
	 * labeled alternative in {@link deliaParser#structTypeStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeStruct(deliaParser.TypeStructContext ctx);
	/**
	 * Visit a parse tree produced by {@link deliaParser#structFields}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStructFields(deliaParser.StructFieldsContext ctx);
	/**
	 * Visit a parse tree produced by {@link deliaParser#structField}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStructField(deliaParser.StructFieldContext ctx);
	/**
	 * Visit a parse tree produced by the {@code relationNameStr}
	 * labeled alternative in {@link deliaParser#relationName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRelationNameStr(deliaParser.RelationNameStrContext ctx);
	/**
	 * Visit a parse tree produced by the {@code relationNameStr2}
	 * labeled alternative in {@link deliaParser#relationName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRelationNameStr2(deliaParser.RelationNameStr2Context ctx);
	/**
	 * Visit a parse tree produced by {@link deliaParser#fieldModifiers}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFieldModifiers(deliaParser.FieldModifiersContext ctx);
	/**
	 * Visit a parse tree produced by {@link deliaParser#defaultValue}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDefaultValue(deliaParser.DefaultValueContext ctx);
	/**
	 * Visit a parse tree produced by {@link deliaParser#defargs}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDefargs(deliaParser.DefargsContext ctx);
	/**
	 * Visit a parse tree produced by {@link deliaParser#fieldModifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFieldModifier(deliaParser.FieldModifierContext ctx);
	/**
	 * Visit a parse tree produced by {@link deliaParser#drules}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDrules(deliaParser.DrulesContext ctx);
	/**
	 * Visit a parse tree produced by {@link deliaParser#drule}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDrule(deliaParser.DruleContext ctx);
	/**
	 * Visit a parse tree produced by the {@code insert}
	 * labeled alternative in {@link deliaParser#insertStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInsert(deliaParser.InsertContext ctx);
	/**
	 * Visit a parse tree produced by the {@code vpValuePairs}
	 * labeled alternative in {@link deliaParser#valuePairs}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVpValuePairs(deliaParser.VpValuePairsContext ctx);
	/**
	 * Visit a parse tree produced by the {@code vpElem}
	 * labeled alternative in {@link deliaParser#valuePairArg}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVpElem(deliaParser.VpElemContext ctx);
	/**
	 * Visit a parse tree produced by the {@code vpList}
	 * labeled alternative in {@link deliaParser#valuePairArg}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVpList(deliaParser.VpListContext ctx);
	/**
	 * Visit a parse tree produced by {@link deliaParser#crudAction}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCrudAction(deliaParser.CrudActionContext ctx);
	/**
	 * Visit a parse tree produced by {@link deliaParser#valueElem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitValueElem(deliaParser.ValueElemContext ctx);
	/**
	 * Visit a parse tree produced by {@link deliaParser#valueElemList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitValueElemList(deliaParser.ValueElemListContext ctx);
	/**
	 * Visit a parse tree produced by {@link deliaParser#deleteStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDeleteStatement(deliaParser.DeleteStatementContext ctx);
	/**
	 * Visit a parse tree produced by the {@code update1}
	 * labeled alternative in {@link deliaParser#updateStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUpdate1(deliaParser.Update1Context ctx);
	/**
	 * Visit a parse tree produced by the {@code upsert1}
	 * labeled alternative in {@link deliaParser#updateStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUpsert1(deliaParser.Upsert1Context ctx);
	/**
	 * Visit a parse tree produced by {@link deliaParser#upsertFlag}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUpsertFlag(deliaParser.UpsertFlagContext ctx);
	/**
	 * Visit a parse tree produced by the {@code let}
	 * labeled alternative in {@link deliaParser#letStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLet(deliaParser.LetContext ctx);
	/**
	 * Visit a parse tree produced by the {@code letscalar}
	 * labeled alternative in {@link deliaParser#letStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLetscalar(deliaParser.LetscalarContext ctx);
	/**
	 * Visit a parse tree produced by the {@code letNoVar}
	 * labeled alternative in {@link deliaParser#letStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLetNoVar(deliaParser.LetNoVarContext ctx);
	/**
	 * Visit a parse tree produced by the {@code noType}
	 * labeled alternative in {@link deliaParser#letVar}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNoType(deliaParser.NoTypeContext ctx);
	/**
	 * Visit a parse tree produced by the {@code withType}
	 * labeled alternative in {@link deliaParser#letVar}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWithType(deliaParser.WithTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link deliaParser#fnChain}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFnChain(deliaParser.FnChainContext ctx);
	/**
	 * Visit a parse tree produced by {@link deliaParser#fnChainArg}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFnChainArg(deliaParser.FnChainArgContext ctx);
	/**
	 * Visit a parse tree produced by {@link deliaParser#filter}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFilter(deliaParser.FilterContext ctx);
	/**
	 * Visit a parse tree produced by {@link deliaParser#filterexpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFilterexpr(deliaParser.FilterexprContext ctx);
	/**
	 * Visit a parse tree produced by {@link deliaParser#cexpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCexpr(deliaParser.CexprContext ctx);
	/**
	 * Visit a parse tree produced by {@link deliaParser#elem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitElem(deliaParser.ElemContext ctx);
	/**
	 * Visit a parse tree produced by {@link deliaParser#fn}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFn(deliaParser.FnContext ctx);
	/**
	 * Visit a parse tree produced by {@link deliaParser#fnargs}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFnargs(deliaParser.FnargsContext ctx);
	/**
	 * Visit a parse tree produced by {@link deliaParser#name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitName(deliaParser.NameContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Num}
	 * labeled alternative in {@link deliaParser#scalar}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNum(deliaParser.NumContext ctx);
	/**
	 * Visit a parse tree produced by the {@code NegNum}
	 * labeled alternative in {@link deliaParser#scalar}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNegNum(deliaParser.NegNumContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Real}
	 * labeled alternative in {@link deliaParser#scalar}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReal(deliaParser.RealContext ctx);
	/**
	 * Visit a parse tree produced by the {@code NegReal}
	 * labeled alternative in {@link deliaParser#scalar}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNegReal(deliaParser.NegRealContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Bool}
	 * labeled alternative in {@link deliaParser#scalar}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBool(deliaParser.BoolContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Str}
	 * labeled alternative in {@link deliaParser#scalar}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStr(deliaParser.StrContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Str2}
	 * labeled alternative in {@link deliaParser#scalar}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStr2(deliaParser.Str2Context ctx);
	/**
	 * Visit a parse tree produced by the {@code NullValue}
	 * labeled alternative in {@link deliaParser#scalar}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNullValue(deliaParser.NullValueContext ctx);
}