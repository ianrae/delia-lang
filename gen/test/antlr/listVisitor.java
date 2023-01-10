// Generated from C:/Users/16136/Documents/GitHub/delia/delia-antlr/src/test/resources/test/antlr\list.g4 by ANTLR 4.9.1
package test.antlr;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link listParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface listVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link listParser#list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitList(listParser.ListContext ctx);
	/**
	 * Visit a parse tree produced by {@link listParser#elems}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitElems(listParser.ElemsContext ctx);
	/**
	 * Visit a parse tree produced by {@link listParser#elem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitElem(listParser.ElemContext ctx);
}