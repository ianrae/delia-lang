package org.delia.compiler.parser;

import java.util.List;

import org.codehaus.jparsec.Parser;
import org.codehaus.jparsec.Parsers;
import org.codehaus.jparsec.Token;
import org.delia.compiler.ast.CrudExp;
import org.delia.compiler.ast.DeleteStatementExp;
import org.delia.compiler.ast.DsonExp;
import org.delia.compiler.ast.DsonFieldExp;
import org.delia.compiler.ast.Exp;
import org.delia.compiler.ast.IdentExp;
import org.delia.compiler.ast.InsertStatementExp;
import org.delia.compiler.ast.ListExp;
import org.delia.compiler.ast.QueryExp;
import org.delia.compiler.ast.StringExp;
import org.delia.compiler.ast.UpdateStatementExp;
import org.delia.compiler.ast.UpsertStatementExp;

/**
 * Parser for insert,update,and delete statements
 * 
 * @author Ian Rae
 *
 */
public class CrudParser extends ParserBase {

	public static Parser<Exp> singleFieldValue() {
		return Parsers.or(LetParser.explicitValue(), varName());
	}
	public static Parser<ListExp> multiFieldValue() {
		return Parsers.sequence(term("["), singleFieldValue().many().sepBy(term(",")), term("]"),
				(Token t1, List<List<Exp>> exps, Token t2) -> new ListExp(99, exps));
	}
	public static Parser<Exp> fieldValue() {
		return Parsers.or(multiFieldValue(), singleFieldValue());
	}
	
	public static Parser<IdentExp> dsonFieldName() {
		return Parsers.sequence(ident(), term(":"), 
				(IdentExp fieldNameExp, Token tok) -> fieldNameExp);
	}
	public static Parser<Exp> dsonField() {
		return Parsers.sequence(assocCrudAction().optional(), dsonFieldName().optional(), fieldValue(), 
				(StringExp assocCrudAction, IdentExp fieldNameExp, Exp exp) -> new DsonFieldExp(99, fieldNameExp, exp, assocCrudAction));
	}
	public static Parser<DsonExp> dsonObj() {
		return Parsers.sequence(term("{"), dsonField().many().sepBy(term(",")), term("}"), 
				(Token tok, List<List<Exp>> arg, Token tok2) -> new DsonExp(tok.index(), arg));
	}
	
	public static Parser<StringExp> assocCrudAction() {
		return Parsers.or(term("insert"), term("update"), term("delete"))
				.map(new org.codehaus.jparsec.functors.Map<Token, StringExp>() {
					@Override
					public StringExp map(Token tok) {
						return new StringExp(tok.index(), (String)tok.toString());
					}
				});
	}

	public static Parser<InsertStatementExp> insertStatement() {
		return Parsers.sequence(term("insert"), ident(), dsonObj(), 
				(Token tok, IdentExp typeName, DsonExp dsonExp) -> new InsertStatementExp(99, typeName, dsonExp));
	}
	public static Parser<UpdateStatementExp> updateStatement() {
		return Parsers.sequence(term("update"), QueryParser.partialQuery(), dsonObj(), 
				(Token tok, QueryExp queryExp, DsonExp dsonExp) -> new UpdateStatementExp(99, queryExp, dsonExp));
	}
	public static Parser<UpsertStatementExp> upsertStatement() {
		return Parsers.sequence(term("update"), QueryParser.partialQuery(), dsonObj(), 
				(Token tok, QueryExp queryExp, DsonExp dsonExp) -> new UpsertStatementExp(99, queryExp, dsonExp));
	}
	public static Parser<DeleteStatementExp> deleteStatement() {
		return Parsers.sequence(term("delete"), QueryParser.partialQuery(), 
				(Token tok, QueryExp queryExp) -> new DeleteStatementExp(99, queryExp.typeName, queryExp));
	}
	
	public static Parser<CrudExp> allCrudStatements() {
		return Parsers.or(insertStatement(),
				updateStatement(),
				upsertStatement(),
				deleteStatement());
	}
}