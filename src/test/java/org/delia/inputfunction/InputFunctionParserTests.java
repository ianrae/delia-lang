package org.delia.inputfunction;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

import org.codehaus.jparsec.Parser;
import org.codehaus.jparsec.Parsers;
import org.codehaus.jparsec.Token;
import org.delia.api.Delia;
import org.delia.api.DeliaSession;
import org.delia.bddnew.NewBDDBase;
import org.delia.builder.ConnectionBuilder;
import org.delia.builder.ConnectionInfo;
import org.delia.builder.DeliaBuilder;
import org.delia.compiler.ast.Exp;
import org.delia.compiler.ast.ExpBase;
import org.delia.compiler.ast.IdentExp;
import org.delia.compiler.ast.StringExp;
import org.delia.compiler.astx.XNAFMultiExp;
import org.delia.compiler.parser.LetParser;
import org.delia.compiler.parser.NameAndFuncParser;
import org.delia.compiler.parser.ParserBase;
import org.delia.compiler.parser.TerminalParser;
import org.delia.dao.DeliaDao;
import org.delia.db.DBInterface;
import org.delia.db.DBType;
import org.delia.db.memdb.MemDBInterface;
import org.delia.type.DTypeRegistry;
import org.junit.Before;
import org.junit.Test;

public class InputFunctionParserTests  extends NewBDDBase {
	
	
	public static class IdentPairExp extends ExpBase {
		public String val1;
		public String val2;

		public IdentPairExp(int pos, String s1, String s2) {
			super(pos);
			this.val1 = s1;
			this.val2 = s2;
		}
		
		public String typeName() {
			return val1;
		}
		public String argName() {
			return val2;
		}

		@Override
		public String strValue() {
			return String.format("%s %s", val1, val2);
		}
	}	
	public static class InputFuncHeaderExp extends ExpBase {
		public String fnName;
		public List<IdentPairExp> argsL = new ArrayList<>();

		public InputFuncHeaderExp(IdentExp fnNameExp, List<List<IdentPairExp>> args) {
			super(fnNameExp.pos);
			this.fnName = fnNameExp.name();
			
			if (args != null) {
				List<IdentPairExp> list = new ArrayList<>();
				if (! args.isEmpty()) {
					for(List<IdentPairExp> sublist : args) {
						for(IdentPairExp inner: sublist) {
							list.add(inner);
						}
					}
				}
				argsL = list;
			}
		}
		
		@Override
		public String strValue() {
			return null;
		}

		@Override
		public String toString() {
			return null;
		}
	}	

	public static class InputFunctionBodyExp extends ExpBase {
		public List<Exp> statementL = new ArrayList<>();

		public InputFunctionBodyExp(int pos, List<List<Exp>> args) {
			super(pos);
			
			if (args != null) {
				List<Exp> list = new ArrayList<>();
				if (! args.isEmpty()) {
					for(List<Exp> sublist : args) {
						for(Exp inner: sublist) {
							list.add(inner);
						}
					}
				}
				statementL = list;
			}
		}
		
		@Override
		public String strValue() {
			String ss = "";
			int i = 0;
			for(Exp exp : statementL) {
				if (i > 0) {
					ss += "," + exp.strValue();
				} else {
					ss += exp.strValue();
				}
				i++;
			}
			return ss;
		}

		@Override
		public String toString() {
			String ss = "";
			int i = 0;
			for(Exp exp : statementL) {
				if (i > 0) {
					ss += "," + exp.toString();
				} else {
					ss += exp.toString();
				}
				i++;
			}
			return ss;
		}
	}	
	public static class InputFunctionDefStatementExp extends ExpBase {
		public String funcName;
		public InputFunctionBodyExp bodyExp;
		public List<IdentPairExp> argsL;

		public InputFunctionDefStatementExp(int pos, InputFuncHeaderExp hdrExp,  InputFunctionBodyExp body) {
			super(pos);
			this.funcName = hdrExp.fnName;
			this.argsL = hdrExp.argsL;
			this.bodyExp = body;
		}
		
		@Override
		public String strValue() {
			String ss = String.format("function %s(", funcName);
			StringJoiner joiner = new StringJoiner(",");
			for(IdentPairExp exp: argsL) {
				joiner.add(exp.strValue());
			}
			ss += String.format("%s){", joiner.toString());
			
			ss += bodyExp.strValue();
			ss = String.format("%s}", ss);
			return ss;
		}

		@Override
		public String toString() {
			String ss = String.format("function %s(", funcName);
			StringJoiner joiner = new StringJoiner(",");
			for(IdentPairExp exp: argsL) {
				joiner.add(exp.strValue());
			}
			ss += String.format("%s){", joiner.toString());
			ss += bodyExp.toString();
			ss = String.format("%s}", ss);
			return ss;
		}
	}	
	
	public static class InputFunctionParser extends ParserBase {
		public static Parser<Exp> fnBodyStatements() {
			return Parsers.or(ident(),
					LetParser.explicitValue(),
					NameAndFuncParser.parseNameAndFuncs()
					);
		}
		
		public static Parser<InputFunctionBodyExp> fnBody() {
			return Parsers.or(fnBodyStatements().many().sepBy(term(","))).
					map(new org.codehaus.jparsec.functors.Map<List<List<Exp>>, InputFunctionBodyExp>() {
						@Override
						public InputFunctionBodyExp map(List<List<Exp>> list) {
							return new InputFunctionBodyExp(99, list);
						}
					});
		}
		
		public static Parser<StringExp> inputFunc() {
			return Parsers.sequence(term("input"), term("function"), 
					(Token fn, Token tok) -> new StringExp(99, ""));
		}
		
		public static Parser<IdentPairExp> identPair() {
			return Parsers.sequence(ident(), ident(), 
					(IdentExp exp1, IdentExp exp2) -> new IdentPairExp(99, exp1.name(), exp2.name()));
		}

		public static Parser<InputFuncHeaderExp> inputFn1() {
			return Parsers.sequence(inputFunc(), ident(), term("("), identPair().many().sepBy(term(",")), term(")"), 
					(StringExp fn, IdentExp exp1, Token tok, List<List<IdentPairExp>> args, Token tok2) -> new InputFuncHeaderExp(exp1, args));
		}
		
		public static Parser<InputFunctionDefStatementExp> inputFunction() {
			return Parsers.sequence(Parsers.INDEX, inputFn1(), term("{"), fnBody(), term("}"), 
					(Integer pos, InputFuncHeaderExp exp1, Token tok, InputFunctionBodyExp body, Token tok2) 
					-> new InputFunctionDefStatementExp(pos, exp1, body));
		}
	}	
	
	
	@Test
	public void test() {
		String src = "input function foo(Customer c) {}";
		InputFunctionDefStatementExp infnExp = parse(src);
		assertEquals("foo", infnExp.funcName);
		assertEquals(1, infnExp.argsL.size());
		chkArg(infnExp, 0, "Customer", "c");
	}
	
	@Test
	public void test2() {
		String src = "input function foo(Customer c, Address a) { 3, 'abc'}";
		InputFunctionDefStatementExp infnExp = parse(src);
		assertEquals("foo", infnExp.funcName);
		assertEquals(2, infnExp.argsL.size());
		chkArg(infnExp, 0, "Customer", "c");
		chkArg(infnExp, 1, "Address", "a");
		
		assertEquals(2, infnExp.bodyExp.statementL.size());
		chkTlang(infnExp, 0, "3");
		chkTlang(infnExp, 1, "abc");
	}


	private void chkTlang(InputFunctionDefStatementExp infnExp, int i, String expected) {
		Exp z = infnExp.bodyExp.statementL.get(i);
		String s = z.strValue();
		assertEquals(expected, s);
	}

	private void chkArg(InputFunctionDefStatementExp infnExp, int i, String expected, String expected2) {
		IdentPairExp pairExp = infnExp.argsL.get(i);
		assertEquals(expected, pairExp.typeName());
		assertEquals(expected2, pairExp.argName());
	}



	// --
//	private DeliaDao dao;
	private Delia delia;
	private DeliaSession session;
	private DTypeRegistry registry;

	@Before
	public void init() {
		DeliaDao dao = this.createDao();
		this.delia = dao.getDelia();
		String src = buildSrc();
		this.session = delia.beginSession(src);
		this.registry = session.getExecutionContext().registry;
	}
	private String buildSrc() {
		String src = " type Customer struct {id int unique, wid int, name string } end";
		return src;
	}

	private DeliaDao createDao() {
		ConnectionInfo info = ConnectionBuilder.dbType(DBType.MEM).build();
		Delia delia = DeliaBuilder.withConnection(info).build();
		return new DeliaDao(delia);
	}
	
	
	private InputFunctionDefStatementExp parse(String src) {
		NameAndFuncParser.initLazy();
		Exp exp = InputFunctionParser.inputFunction().from(TerminalParser.tokenizer, TerminalParser.ignored.skipMany()).parse(src);
		return (InputFunctionDefStatementExp) exp;
	}


	@Override
	public DBInterface createForTest() {
		return new MemDBInterface();
	}
	
	
}
