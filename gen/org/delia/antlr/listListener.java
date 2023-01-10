// Generated from C:/Users/16136/Documents/GitHub/delia/delia-antlr/src/test/java/org/delia/antlr\list.g4 by ANTLR 4.9.1
package org.delia.antlr;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link listParser}.
 */
public interface listListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link listParser#list}.
	 * @param ctx the parse tree
	 */
	void enterList(listParser.ListContext ctx);
	/**
	 * Exit a parse tree produced by {@link listParser#list}.
	 * @param ctx the parse tree
	 */
	void exitList(listParser.ListContext ctx);
	/**
	 * Enter a parse tree produced by {@link listParser#elems}.
	 * @param ctx the parse tree
	 */
	void enterElems(listParser.ElemsContext ctx);
	/**
	 * Exit a parse tree produced by {@link listParser#elems}.
	 * @param ctx the parse tree
	 */
	void exitElems(listParser.ElemsContext ctx);
	/**
	 * Enter a parse tree produced by {@link listParser#elem}.
	 * @param ctx the parse tree
	 */
	void enterElem(listParser.ElemContext ctx);
	/**
	 * Exit a parse tree produced by {@link listParser#elem}.
	 * @param ctx the parse tree
	 */
	void exitElem(listParser.ElemContext ctx);
}