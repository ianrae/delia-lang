package org.delia.compiler.parser;

import java.util.List;

import org.codehaus.jparsec.Parser;
import org.codehaus.jparsec.Parsers;
import org.codehaus.jparsec.Token;
import org.delia.compiler.ast.FieldQualifierExp;
import org.delia.compiler.ast.IdentExp;
import org.delia.compiler.ast.RuleSetExp;
import org.delia.compiler.ast.StructExp;
import org.delia.compiler.ast.StringExp;
import org.delia.compiler.ast.StructFieldExp;
import org.delia.compiler.ast.StructFieldPrefix;
import org.delia.compiler.ast.TypeStatementExp;

/**
 * parser for the type statement.
 * 
 * @author Ian Rae
 *
 */
public class TypeParser extends ParserBase {
	
	public static Parser<FieldQualifierExp> fieldQualifier() {
		return Parsers.or(term("optional"), term("unique"), term("one"), term("many"), 
				term("primaryKey"), term("parent"), term("serial"))
				.map(new org.codehaus.jparsec.functors.Map<Token, FieldQualifierExp>() {
					@Override
					public FieldQualifierExp map(Token tok) {
						return new FieldQualifierExp(99, tok);
					}
				});
	}
	
	public static Parser<StructFieldPrefix> structFieldPrefix() {
		return Parsers.sequence(Parsers.INDEX, term("relation").optional(), ident(), ident(), LetParser.stringValue().optional(), 
				(Integer pos, Token tokRelation, IdentExp fieldNameExp, IdentExp exp, StringExp relNameExp) 
				-> new StructFieldPrefix(pos, tokRelation, fieldNameExp, exp, relNameExp));
	}

	public static Parser<StructFieldExp> structField() {
		return Parsers.sequence(structFieldPrefix(), fieldQualifier().optional(), fieldQualifier().optional(), fieldQualifier().optional(), fieldQualifier().optional(), 
				(StructFieldPrefix structFieldPrefix, FieldQualifierExp qual1, FieldQualifierExp qual2, FieldQualifierExp qual3, FieldQualifierExp qual4) 
				-> new StructFieldExp(structFieldPrefix, qual1, qual2, qual3, qual4));
	}
	public static Parser<StructExp> structBody() {
		return Parsers.sequence(term("{"), structField().many().sepBy(term(",")), term("}"), 
				(Token tok, List<List<StructFieldExp>> arg, Token tok2) -> new StructExp(tok.index(), arg));
	}
	
	public static Parser<IdentExp> termStruct() {
		return term("struct").<IdentExp>retn(new IdentExp("struct"));
	}
	
	public static Parser<IdentExp> typeBeginning() {
		return Parsers.sequence(term("type"), ident());
	}
	public static Parser<RuleSetExp> typeEnd() {
		return Parsers.or(RuleParser.rules().optional()).
				map(new org.codehaus.jparsec.functors.Map<RuleSetExp, RuleSetExp>() {
					@Override
					public RuleSetExp map(RuleSetExp ruleSetExp) {
						return ruleSetExp;
					}
				});
	}
	
	public static Parser<IdentExp> structOrBaseClass() {
		return Parsers.or(termStruct(), ident());
	}

	public static Parser<TypeStatementExp> typeStatement() {
		return Parsers.sequence(typeBeginning(), structOrBaseClass(), structBody().optional(), typeEnd(), term("end"),
				(IdentExp typeName, IdentExp shapeExp, StructExp structExp, RuleSetExp ruleSetExp, Token tokEnd) -> new TypeStatementExp(typeName.getPos(), typeName, shapeExp, structExp, ruleSetExp));
	}
	
}