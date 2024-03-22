// Generated from C:/Users/16136/Documents/GitHub/delia/delia-lang/src/test/java/org/delia/antlr/delia.g4 by ANTLR 4.13.1
package org.delia.compiler.antlr;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast", "CheckReturnValue"})
public class deliaParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.13.1", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, T__5=6, T__6=7, T__7=8, T__8=9, 
		T__9=10, T__10=11, T__11=12, T__12=13, T__13=14, T__14=15, T__15=16, T__16=17, 
		T__17=18, T__18=19, T__19=20, T__20=21, T__21=22, T__22=23, T__23=24, 
		T__24=25, T__25=26, T__26=27, T__27=28, T__28=29, T__29=30, T__30=31, 
		T__31=32, T__32=33, T__33=34, T__34=35, T__35=36, BEGF=37, ENDF=38, BEGPAREN=39, 
		ENDPAREN=40, TRUE=41, FALSE=42, NULL=43, SEP=44, DOT=45, NOT=46, SYMBOL=47, 
		DOLLAR=48, NUM=49, COMMENT=50, LINE_COMMENT=51, FloatingPointLiteral=52, 
		StringLiteral=53, StringLiteral2=54, HexDigit=55, WS=56;
	public static final int
		RULE_deliaStatement = 0, RULE_statement = 1, RULE_configureStatement = 2, 
		RULE_schemaStatement = 3, RULE_logStatement = 4, RULE_typeStatement = 5, 
		RULE_scalarTypeStatement = 6, RULE_structTypeStatement = 7, RULE_structFields = 8, 
		RULE_structField = 9, RULE_relationName = 10, RULE_fieldModifiers = 11, 
		RULE_defaultValue = 12, RULE_defargs = 13, RULE_fieldModifier = 14, RULE_drules = 15, 
		RULE_drule = 16, RULE_insertStatement = 17, RULE_valuePairs = 18, RULE_valuePairArg = 19, 
		RULE_crudAction = 20, RULE_elemList = 21, RULE_deleteStatement = 22, RULE_updateStatement = 23, 
		RULE_upsertFlag = 24, RULE_letStatement = 25, RULE_letVar = 26, RULE_fnChain = 27, 
		RULE_fnChainArg = 28, RULE_filter = 29, RULE_cexpr = 30, RULE_elem = 31, 
		RULE_fn = 32, RULE_fnargs = 33, RULE_name = 34, RULE_scalar = 35;
	private static String[] makeRuleNames() {
		return new String[] {
			"deliaStatement", "statement", "configureStatement", "schemaStatement", 
			"logStatement", "typeStatement", "scalarTypeStatement", "structTypeStatement", 
			"structFields", "structField", "relationName", "fieldModifiers", "defaultValue", 
			"defargs", "fieldModifier", "drules", "drule", "insertStatement", "valuePairs", 
			"valuePairArg", "crudAction", "elemList", "deleteStatement", "updateStatement", 
			"upsertFlag", "letStatement", "letVar", "fnChain", "fnChainArg", "filter", 
			"cexpr", "elem", "fn", "fnargs", "name", "scalar"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'configure'", "'='", "'schema'", "'off'", "'log'", "'type'", "'end'", 
			"'{'", "'}'", "'relation'", "'default'", "'optional'", "'unique'", "'primaryKey'", 
			"'serial'", "'one'", "'many'", "'parent'", "'insert'", "':'", "'update'", 
			"'delete'", "'upsert'", "'-noUpdate'", "'let'", "'in'", "'=='", "'!='", 
			"'<'", "'<='", "'>'", "'>='", "'like'", "'and'", "'or'", "'-'", "'['", 
			"']'", "'('", "')'", "'true'", "'false'", "'null'", "','", "'.'", "'!'", 
			null, "'$'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, "BEGF", "ENDF", "BEGPAREN", "ENDPAREN", "TRUE", "FALSE", "NULL", 
			"SEP", "DOT", "NOT", "SYMBOL", "DOLLAR", "NUM", "COMMENT", "LINE_COMMENT", 
			"FloatingPointLiteral", "StringLiteral", "StringLiteral2", "HexDigit", 
			"WS"
		};
	}
	private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}

	@Override
	public String getGrammarFileName() { return "delia.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public deliaParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@SuppressWarnings("CheckReturnValue")
	public static class DeliaStatementContext extends ParserRuleContext {
		public TerminalNode EOF() { return getToken(deliaParser.EOF, 0); }
		public List<StatementContext> statement() {
			return getRuleContexts(StatementContext.class);
		}
		public StatementContext statement(int i) {
			return getRuleContext(StatementContext.class,i);
		}
		public DeliaStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_deliaStatement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).enterDeliaStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).exitDeliaStatement(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof deliaVisitor ) return ((deliaVisitor<? extends T>)visitor).visitDeliaStatement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DeliaStatementContext deliaStatement() throws RecognitionException {
		DeliaStatementContext _localctx = new DeliaStatementContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_deliaStatement);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(73); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(72);
				statement();
				}
				}
				setState(75); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( (((_la) & ~0x3f) == 0 && ((1L << _la) & 140737537114218L) != 0) );
			setState(77);
			match(EOF);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class StatementContext extends ParserRuleContext {
		public LetStatementContext letStatement() {
			return getRuleContext(LetStatementContext.class,0);
		}
		public ConfigureStatementContext configureStatement() {
			return getRuleContext(ConfigureStatementContext.class,0);
		}
		public SchemaStatementContext schemaStatement() {
			return getRuleContext(SchemaStatementContext.class,0);
		}
		public TypeStatementContext typeStatement() {
			return getRuleContext(TypeStatementContext.class,0);
		}
		public InsertStatementContext insertStatement() {
			return getRuleContext(InsertStatementContext.class,0);
		}
		public DeleteStatementContext deleteStatement() {
			return getRuleContext(DeleteStatementContext.class,0);
		}
		public UpdateStatementContext updateStatement() {
			return getRuleContext(UpdateStatementContext.class,0);
		}
		public LogStatementContext logStatement() {
			return getRuleContext(LogStatementContext.class,0);
		}
		public StatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_statement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).enterStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).exitStatement(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof deliaVisitor ) return ((deliaVisitor<? extends T>)visitor).visitStatement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final StatementContext statement() throws RecognitionException {
		StatementContext _localctx = new StatementContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_statement);
		try {
			setState(87);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__24:
			case SYMBOL:
				enterOuterAlt(_localctx, 1);
				{
				setState(79);
				letStatement();
				}
				break;
			case T__0:
				enterOuterAlt(_localctx, 2);
				{
				setState(80);
				configureStatement();
				}
				break;
			case T__2:
				enterOuterAlt(_localctx, 3);
				{
				setState(81);
				schemaStatement();
				}
				break;
			case T__5:
				enterOuterAlt(_localctx, 4);
				{
				setState(82);
				typeStatement();
				}
				break;
			case T__18:
				enterOuterAlt(_localctx, 5);
				{
				setState(83);
				insertStatement();
				}
				break;
			case T__21:
				enterOuterAlt(_localctx, 6);
				{
				setState(84);
				deleteStatement();
				}
				break;
			case T__20:
			case T__22:
				enterOuterAlt(_localctx, 7);
				{
				setState(85);
				updateStatement();
				}
				break;
			case T__4:
				enterOuterAlt(_localctx, 8);
				{
				setState(86);
				logStatement();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ConfigureStatementContext extends ParserRuleContext {
		public ConfigureStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_configureStatement; }
	 
		public ConfigureStatementContext() { }
		public void copyFrom(ConfigureStatementContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class ConfigureContext extends ConfigureStatementContext {
		public NameContext name() {
			return getRuleContext(NameContext.class,0);
		}
		public ScalarContext scalar() {
			return getRuleContext(ScalarContext.class,0);
		}
		public ConfigureContext(ConfigureStatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).enterConfigure(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).exitConfigure(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof deliaVisitor ) return ((deliaVisitor<? extends T>)visitor).visitConfigure(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ConfigureStatementContext configureStatement() throws RecognitionException {
		ConfigureStatementContext _localctx = new ConfigureStatementContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_configureStatement);
		try {
			_localctx = new ConfigureContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(89);
			match(T__0);
			setState(90);
			name();
			setState(91);
			match(T__1);
			setState(92);
			scalar();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class SchemaStatementContext extends ParserRuleContext {
		public SchemaStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_schemaStatement; }
	 
		public SchemaStatementContext() { }
		public void copyFrom(SchemaStatementContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class SchemaContext extends SchemaStatementContext {
		public NameContext name() {
			return getRuleContext(NameContext.class,0);
		}
		public SchemaContext(SchemaStatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).enterSchema(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).exitSchema(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof deliaVisitor ) return ((deliaVisitor<? extends T>)visitor).visitSchema(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class SchemaOffContext extends SchemaStatementContext {
		public SchemaOffContext(SchemaStatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).enterSchemaOff(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).exitSchemaOff(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof deliaVisitor ) return ((deliaVisitor<? extends T>)visitor).visitSchemaOff(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SchemaStatementContext schemaStatement() throws RecognitionException {
		SchemaStatementContext _localctx = new SchemaStatementContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_schemaStatement);
		try {
			setState(98);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,2,_ctx) ) {
			case 1:
				_localctx = new SchemaContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(94);
				match(T__2);
				setState(95);
				name();
				}
				break;
			case 2:
				_localctx = new SchemaOffContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(96);
				match(T__2);
				setState(97);
				match(T__3);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class LogStatementContext extends ParserRuleContext {
		public NameContext name() {
			return getRuleContext(NameContext.class,0);
		}
		public ScalarContext scalar() {
			return getRuleContext(ScalarContext.class,0);
		}
		public LogStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_logStatement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).enterLogStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).exitLogStatement(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof deliaVisitor ) return ((deliaVisitor<? extends T>)visitor).visitLogStatement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LogStatementContext logStatement() throws RecognitionException {
		LogStatementContext _localctx = new LogStatementContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_logStatement);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(100);
			match(T__4);
			setState(103);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case SYMBOL:
				{
				setState(101);
				name();
				}
				break;
			case T__35:
			case TRUE:
			case FALSE:
			case NULL:
			case NUM:
			case FloatingPointLiteral:
			case StringLiteral:
			case StringLiteral2:
				{
				setState(102);
				scalar();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class TypeStatementContext extends ParserRuleContext {
		public ScalarTypeStatementContext scalarTypeStatement() {
			return getRuleContext(ScalarTypeStatementContext.class,0);
		}
		public StructTypeStatementContext structTypeStatement() {
			return getRuleContext(StructTypeStatementContext.class,0);
		}
		public TypeStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_typeStatement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).enterTypeStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).exitTypeStatement(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof deliaVisitor ) return ((deliaVisitor<? extends T>)visitor).visitTypeStatement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TypeStatementContext typeStatement() throws RecognitionException {
		TypeStatementContext _localctx = new TypeStatementContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_typeStatement);
		try {
			setState(107);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,4,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(105);
				scalarTypeStatement();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(106);
				structTypeStatement();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ScalarTypeStatementContext extends ParserRuleContext {
		public ScalarTypeStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_scalarTypeStatement; }
	 
		public ScalarTypeStatementContext() { }
		public void copyFrom(ScalarTypeStatementContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class TypeScalarContext extends ScalarTypeStatementContext {
		public TerminalNode SYMBOL() { return getToken(deliaParser.SYMBOL, 0); }
		public NameContext name() {
			return getRuleContext(NameContext.class,0);
		}
		public DrulesContext drules() {
			return getRuleContext(DrulesContext.class,0);
		}
		public TypeScalarContext(ScalarTypeStatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).enterTypeScalar(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).exitTypeScalar(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof deliaVisitor ) return ((deliaVisitor<? extends T>)visitor).visitTypeScalar(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ScalarTypeStatementContext scalarTypeStatement() throws RecognitionException {
		ScalarTypeStatementContext _localctx = new ScalarTypeStatementContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_scalarTypeStatement);
		int _la;
		try {
			_localctx = new TypeScalarContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(109);
			match(T__5);
			setState(110);
			match(SYMBOL);
			setState(111);
			name();
			setState(113);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 32596740192337920L) != 0)) {
				{
				setState(112);
				drules();
				}
			}

			setState(115);
			match(T__6);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class StructTypeStatementContext extends ParserRuleContext {
		public StructTypeStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_structTypeStatement; }
	 
		public StructTypeStatementContext() { }
		public void copyFrom(StructTypeStatementContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class TypeStructContext extends StructTypeStatementContext {
		public TerminalNode SYMBOL() { return getToken(deliaParser.SYMBOL, 0); }
		public NameContext name() {
			return getRuleContext(NameContext.class,0);
		}
		public StructFieldsContext structFields() {
			return getRuleContext(StructFieldsContext.class,0);
		}
		public DrulesContext drules() {
			return getRuleContext(DrulesContext.class,0);
		}
		public TypeStructContext(StructTypeStatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).enterTypeStruct(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).exitTypeStruct(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof deliaVisitor ) return ((deliaVisitor<? extends T>)visitor).visitTypeStruct(this);
			else return visitor.visitChildren(this);
		}
	}

	public final StructTypeStatementContext structTypeStatement() throws RecognitionException {
		StructTypeStatementContext _localctx = new StructTypeStatementContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_structTypeStatement);
		int _la;
		try {
			_localctx = new TypeStructContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(117);
			match(T__5);
			setState(118);
			match(SYMBOL);
			setState(119);
			name();
			setState(120);
			match(T__7);
			setState(122);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__9 || _la==SYMBOL) {
				{
				setState(121);
				structFields();
				}
			}

			setState(124);
			match(T__8);
			setState(126);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 32596740192337920L) != 0)) {
				{
				setState(125);
				drules();
				}
			}

			setState(128);
			match(T__6);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class StructFieldsContext extends ParserRuleContext {
		public List<StructFieldContext> structField() {
			return getRuleContexts(StructFieldContext.class);
		}
		public StructFieldContext structField(int i) {
			return getRuleContext(StructFieldContext.class,i);
		}
		public List<TerminalNode> SEP() { return getTokens(deliaParser.SEP); }
		public TerminalNode SEP(int i) {
			return getToken(deliaParser.SEP, i);
		}
		public StructFieldsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_structFields; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).enterStructFields(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).exitStructFields(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof deliaVisitor ) return ((deliaVisitor<? extends T>)visitor).visitStructFields(this);
			else return visitor.visitChildren(this);
		}
	}

	public final StructFieldsContext structFields() throws RecognitionException {
		StructFieldsContext _localctx = new StructFieldsContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_structFields);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(130);
			structField();
			setState(135);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==SEP) {
				{
				{
				setState(131);
				match(SEP);
				setState(132);
				structField();
				}
				}
				setState(137);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class StructFieldContext extends ParserRuleContext {
		public TerminalNode SYMBOL() { return getToken(deliaParser.SYMBOL, 0); }
		public NameContext name() {
			return getRuleContext(NameContext.class,0);
		}
		public RelationNameContext relationName() {
			return getRuleContext(RelationNameContext.class,0);
		}
		public FieldModifiersContext fieldModifiers() {
			return getRuleContext(FieldModifiersContext.class,0);
		}
		public DefaultValueContext defaultValue() {
			return getRuleContext(DefaultValueContext.class,0);
		}
		public StructFieldContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_structField; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).enterStructField(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).exitStructField(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof deliaVisitor ) return ((deliaVisitor<? extends T>)visitor).visitStructField(this);
			else return visitor.visitChildren(this);
		}
	}

	public final StructFieldContext structField() throws RecognitionException {
		StructFieldContext _localctx = new StructFieldContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_structField);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(139);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__9) {
				{
				setState(138);
				match(T__9);
				}
			}

			setState(141);
			match(SYMBOL);
			setState(142);
			name();
			setState(144);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==StringLiteral || _la==StringLiteral2) {
				{
				setState(143);
				relationName();
				}
			}

			setState(147);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 520192L) != 0)) {
				{
				setState(146);
				fieldModifiers();
				}
			}

			setState(150);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__10) {
				{
				setState(149);
				defaultValue();
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class RelationNameContext extends ParserRuleContext {
		public RelationNameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_relationName; }
	 
		public RelationNameContext() { }
		public void copyFrom(RelationNameContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class RelationNameStrContext extends RelationNameContext {
		public TerminalNode StringLiteral() { return getToken(deliaParser.StringLiteral, 0); }
		public RelationNameStrContext(RelationNameContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).enterRelationNameStr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).exitRelationNameStr(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof deliaVisitor ) return ((deliaVisitor<? extends T>)visitor).visitRelationNameStr(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class RelationNameStr2Context extends RelationNameContext {
		public TerminalNode StringLiteral2() { return getToken(deliaParser.StringLiteral2, 0); }
		public RelationNameStr2Context(RelationNameContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).enterRelationNameStr2(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).exitRelationNameStr2(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof deliaVisitor ) return ((deliaVisitor<? extends T>)visitor).visitRelationNameStr2(this);
			else return visitor.visitChildren(this);
		}
	}

	public final RelationNameContext relationName() throws RecognitionException {
		RelationNameContext _localctx = new RelationNameContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_relationName);
		try {
			setState(154);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case StringLiteral:
				_localctx = new RelationNameStrContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(152);
				match(StringLiteral);
				}
				break;
			case StringLiteral2:
				_localctx = new RelationNameStr2Context(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(153);
				match(StringLiteral2);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class FieldModifiersContext extends ParserRuleContext {
		public List<FieldModifierContext> fieldModifier() {
			return getRuleContexts(FieldModifierContext.class);
		}
		public FieldModifierContext fieldModifier(int i) {
			return getRuleContext(FieldModifierContext.class,i);
		}
		public FieldModifiersContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_fieldModifiers; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).enterFieldModifiers(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).exitFieldModifiers(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof deliaVisitor ) return ((deliaVisitor<? extends T>)visitor).visitFieldModifiers(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FieldModifiersContext fieldModifiers() throws RecognitionException {
		FieldModifiersContext _localctx = new FieldModifiersContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_fieldModifiers);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(156);
			fieldModifier();
			setState(160);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 520192L) != 0)) {
				{
				{
				setState(157);
				fieldModifier();
				}
				}
				setState(162);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class DefaultValueContext extends ParserRuleContext {
		public TerminalNode BEGPAREN() { return getToken(deliaParser.BEGPAREN, 0); }
		public TerminalNode ENDPAREN() { return getToken(deliaParser.ENDPAREN, 0); }
		public DefargsContext defargs() {
			return getRuleContext(DefargsContext.class,0);
		}
		public DefaultValueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_defaultValue; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).enterDefaultValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).exitDefaultValue(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof deliaVisitor ) return ((deliaVisitor<? extends T>)visitor).visitDefaultValue(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DefaultValueContext defaultValue() throws RecognitionException {
		DefaultValueContext _localctx = new DefaultValueContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_defaultValue);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(163);
			match(T__10);
			setState(164);
			match(BEGPAREN);
			setState(166);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 32244346715635712L) != 0)) {
				{
				setState(165);
				defargs();
				}
			}

			setState(168);
			match(ENDPAREN);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class DefargsContext extends ParserRuleContext {
		public ScalarContext scalar() {
			return getRuleContext(ScalarContext.class,0);
		}
		public NameContext name() {
			return getRuleContext(NameContext.class,0);
		}
		public DefargsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_defargs; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).enterDefargs(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).exitDefargs(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof deliaVisitor ) return ((deliaVisitor<? extends T>)visitor).visitDefargs(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DefargsContext defargs() throws RecognitionException {
		DefargsContext _localctx = new DefargsContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_defargs);
		try {
			setState(172);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__35:
			case TRUE:
			case FALSE:
			case NULL:
			case NUM:
			case FloatingPointLiteral:
			case StringLiteral:
			case StringLiteral2:
				enterOuterAlt(_localctx, 1);
				{
				setState(170);
				scalar();
				}
				break;
			case SYMBOL:
				enterOuterAlt(_localctx, 2);
				{
				setState(171);
				name();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class FieldModifierContext extends ParserRuleContext {
		public FieldModifierContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_fieldModifier; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).enterFieldModifier(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).exitFieldModifier(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof deliaVisitor ) return ((deliaVisitor<? extends T>)visitor).visitFieldModifier(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FieldModifierContext fieldModifier() throws RecognitionException {
		FieldModifierContext _localctx = new FieldModifierContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_fieldModifier);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(174);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 520192L) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class DrulesContext extends ParserRuleContext {
		public List<DruleContext> drule() {
			return getRuleContexts(DruleContext.class);
		}
		public DruleContext drule(int i) {
			return getRuleContext(DruleContext.class,i);
		}
		public List<TerminalNode> SEP() { return getTokens(deliaParser.SEP); }
		public TerminalNode SEP(int i) {
			return getToken(deliaParser.SEP, i);
		}
		public DrulesContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_drules; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).enterDrules(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).exitDrules(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof deliaVisitor ) return ((deliaVisitor<? extends T>)visitor).visitDrules(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DrulesContext drules() throws RecognitionException {
		DrulesContext _localctx = new DrulesContext(_ctx, getState());
		enterRule(_localctx, 30, RULE_drules);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(176);
			drule();
			setState(181);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==SEP) {
				{
				{
				setState(177);
				match(SEP);
				setState(178);
				drule();
				}
				}
				setState(183);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class DruleContext extends ParserRuleContext {
		public CexprContext cexpr() {
			return getRuleContext(CexprContext.class,0);
		}
		public DruleContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_drule; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).enterDrule(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).exitDrule(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof deliaVisitor ) return ((deliaVisitor<? extends T>)visitor).visitDrule(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DruleContext drule() throws RecognitionException {
		DruleContext _localctx = new DruleContext(_ctx, getState());
		enterRule(_localctx, 32, RULE_drule);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(184);
			cexpr(0);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class InsertStatementContext extends ParserRuleContext {
		public InsertStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_insertStatement; }
	 
		public InsertStatementContext() { }
		public void copyFrom(InsertStatementContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class InsertContext extends InsertStatementContext {
		public NameContext name() {
			return getRuleContext(NameContext.class,0);
		}
		public ValuePairsContext valuePairs() {
			return getRuleContext(ValuePairsContext.class,0);
		}
		public InsertContext(InsertStatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).enterInsert(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).exitInsert(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof deliaVisitor ) return ((deliaVisitor<? extends T>)visitor).visitInsert(this);
			else return visitor.visitChildren(this);
		}
	}

	public final InsertStatementContext insertStatement() throws RecognitionException {
		InsertStatementContext _localctx = new InsertStatementContext(_ctx, getState());
		enterRule(_localctx, 34, RULE_insertStatement);
		int _la;
		try {
			_localctx = new InsertContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(186);
			match(T__18);
			setState(187);
			name();
			setState(188);
			match(T__7);
			setState(190);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 140737495171072L) != 0)) {
				{
				setState(189);
				valuePairs();
				}
			}

			setState(192);
			match(T__8);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ValuePairsContext extends ParserRuleContext {
		public ValuePairsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_valuePairs; }
	 
		public ValuePairsContext() { }
		public void copyFrom(ValuePairsContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class VpValuePairsContext extends ValuePairsContext {
		public List<ValuePairArgContext> valuePairArg() {
			return getRuleContexts(ValuePairArgContext.class);
		}
		public ValuePairArgContext valuePairArg(int i) {
			return getRuleContext(ValuePairArgContext.class,i);
		}
		public List<TerminalNode> SEP() { return getTokens(deliaParser.SEP); }
		public TerminalNode SEP(int i) {
			return getToken(deliaParser.SEP, i);
		}
		public VpValuePairsContext(ValuePairsContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).enterVpValuePairs(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).exitVpValuePairs(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof deliaVisitor ) return ((deliaVisitor<? extends T>)visitor).visitVpValuePairs(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ValuePairsContext valuePairs() throws RecognitionException {
		ValuePairsContext _localctx = new ValuePairsContext(_ctx, getState());
		enterRule(_localctx, 36, RULE_valuePairs);
		int _la;
		try {
			_localctx = new VpValuePairsContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(194);
			valuePairArg();
			setState(199);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==SEP) {
				{
				{
				setState(195);
				match(SEP);
				setState(196);
				valuePairArg();
				}
				}
				setState(201);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ValuePairArgContext extends ParserRuleContext {
		public ValuePairArgContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_valuePairArg; }
	 
		public ValuePairArgContext() { }
		public void copyFrom(ValuePairArgContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class VpListContext extends ValuePairArgContext {
		public TerminalNode SYMBOL() { return getToken(deliaParser.SYMBOL, 0); }
		public TerminalNode BEGF() { return getToken(deliaParser.BEGF, 0); }
		public ElemListContext elemList() {
			return getRuleContext(ElemListContext.class,0);
		}
		public TerminalNode ENDF() { return getToken(deliaParser.ENDF, 0); }
		public CrudActionContext crudAction() {
			return getRuleContext(CrudActionContext.class,0);
		}
		public VpListContext(ValuePairArgContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).enterVpList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).exitVpList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof deliaVisitor ) return ((deliaVisitor<? extends T>)visitor).visitVpList(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class VpElemContext extends ValuePairArgContext {
		public TerminalNode SYMBOL() { return getToken(deliaParser.SYMBOL, 0); }
		public ElemContext elem() {
			return getRuleContext(ElemContext.class,0);
		}
		public CrudActionContext crudAction() {
			return getRuleContext(CrudActionContext.class,0);
		}
		public VpElemContext(ValuePairArgContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).enterVpElem(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).exitVpElem(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof deliaVisitor ) return ((deliaVisitor<? extends T>)visitor).visitVpElem(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ValuePairArgContext valuePairArg() throws RecognitionException {
		ValuePairArgContext _localctx = new ValuePairArgContext(_ctx, getState());
		enterRule(_localctx, 38, RULE_valuePairArg);
		int _la;
		try {
			setState(217);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,22,_ctx) ) {
			case 1:
				_localctx = new VpElemContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(203);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 6815744L) != 0)) {
					{
					setState(202);
					crudAction();
					}
				}

				setState(205);
				match(SYMBOL);
				setState(206);
				match(T__19);
				setState(207);
				elem();
				}
				break;
			case 2:
				_localctx = new VpListContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(209);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 6815744L) != 0)) {
					{
					setState(208);
					crudAction();
					}
				}

				setState(211);
				match(SYMBOL);
				setState(212);
				match(T__19);
				setState(213);
				match(BEGF);
				setState(214);
				elemList();
				setState(215);
				match(ENDF);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class CrudActionContext extends ParserRuleContext {
		public CrudActionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_crudAction; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).enterCrudAction(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).exitCrudAction(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof deliaVisitor ) return ((deliaVisitor<? extends T>)visitor).visitCrudAction(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CrudActionContext crudAction() throws RecognitionException {
		CrudActionContext _localctx = new CrudActionContext(_ctx, getState());
		enterRule(_localctx, 40, RULE_crudAction);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(219);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 6815744L) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ElemListContext extends ParserRuleContext {
		public List<ElemContext> elem() {
			return getRuleContexts(ElemContext.class);
		}
		public ElemContext elem(int i) {
			return getRuleContext(ElemContext.class,i);
		}
		public List<TerminalNode> SEP() { return getTokens(deliaParser.SEP); }
		public TerminalNode SEP(int i) {
			return getToken(deliaParser.SEP, i);
		}
		public ElemListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_elemList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).enterElemList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).exitElemList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof deliaVisitor ) return ((deliaVisitor<? extends T>)visitor).visitElemList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ElemListContext elemList() throws RecognitionException {
		ElemListContext _localctx = new ElemListContext(_ctx, getState());
		enterRule(_localctx, 42, RULE_elemList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(221);
			elem();
			setState(226);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==SEP) {
				{
				{
				setState(222);
				match(SEP);
				setState(223);
				elem();
				}
				}
				setState(228);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class DeleteStatementContext extends ParserRuleContext {
		public NameContext name() {
			return getRuleContext(NameContext.class,0);
		}
		public FilterContext filter() {
			return getRuleContext(FilterContext.class,0);
		}
		public DeleteStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_deleteStatement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).enterDeleteStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).exitDeleteStatement(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof deliaVisitor ) return ((deliaVisitor<? extends T>)visitor).visitDeleteStatement(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DeleteStatementContext deleteStatement() throws RecognitionException {
		DeleteStatementContext _localctx = new DeleteStatementContext(_ctx, getState());
		enterRule(_localctx, 44, RULE_deleteStatement);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(229);
			match(T__21);
			setState(230);
			name();
			setState(231);
			filter();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class UpdateStatementContext extends ParserRuleContext {
		public UpdateStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_updateStatement; }
	 
		public UpdateStatementContext() { }
		public void copyFrom(UpdateStatementContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class Update1Context extends UpdateStatementContext {
		public NameContext name() {
			return getRuleContext(NameContext.class,0);
		}
		public FilterContext filter() {
			return getRuleContext(FilterContext.class,0);
		}
		public ValuePairsContext valuePairs() {
			return getRuleContext(ValuePairsContext.class,0);
		}
		public Update1Context(UpdateStatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).enterUpdate1(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).exitUpdate1(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof deliaVisitor ) return ((deliaVisitor<? extends T>)visitor).visitUpdate1(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class Upsert1Context extends UpdateStatementContext {
		public NameContext name() {
			return getRuleContext(NameContext.class,0);
		}
		public FilterContext filter() {
			return getRuleContext(FilterContext.class,0);
		}
		public UpsertFlagContext upsertFlag() {
			return getRuleContext(UpsertFlagContext.class,0);
		}
		public ValuePairsContext valuePairs() {
			return getRuleContext(ValuePairsContext.class,0);
		}
		public Upsert1Context(UpdateStatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).enterUpsert1(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).exitUpsert1(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof deliaVisitor ) return ((deliaVisitor<? extends T>)visitor).visitUpsert1(this);
			else return visitor.visitChildren(this);
		}
	}

	public final UpdateStatementContext updateStatement() throws RecognitionException {
		UpdateStatementContext _localctx = new UpdateStatementContext(_ctx, getState());
		enterRule(_localctx, 46, RULE_updateStatement);
		int _la;
		try {
			setState(254);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__20:
				_localctx = new Update1Context(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(233);
				match(T__20);
				setState(234);
				name();
				setState(235);
				filter();
				setState(236);
				match(T__7);
				setState(238);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 140737495171072L) != 0)) {
					{
					setState(237);
					valuePairs();
					}
				}

				setState(240);
				match(T__8);
				}
				break;
			case T__22:
				_localctx = new Upsert1Context(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(242);
				match(T__22);
				setState(244);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==T__23) {
					{
					setState(243);
					upsertFlag();
					}
				}

				setState(246);
				name();
				setState(247);
				filter();
				setState(248);
				match(T__7);
				setState(250);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 140737495171072L) != 0)) {
					{
					setState(249);
					valuePairs();
					}
				}

				setState(252);
				match(T__8);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class UpsertFlagContext extends ParserRuleContext {
		public UpsertFlagContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_upsertFlag; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).enterUpsertFlag(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).exitUpsertFlag(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof deliaVisitor ) return ((deliaVisitor<? extends T>)visitor).visitUpsertFlag(this);
			else return visitor.visitChildren(this);
		}
	}

	public final UpsertFlagContext upsertFlag() throws RecognitionException {
		UpsertFlagContext _localctx = new UpsertFlagContext(_ctx, getState());
		enterRule(_localctx, 48, RULE_upsertFlag);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(256);
			match(T__23);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class LetStatementContext extends ParserRuleContext {
		public LetStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_letStatement; }
	 
		public LetStatementContext() { }
		public void copyFrom(LetStatementContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class LetscalarContext extends LetStatementContext {
		public LetVarContext letVar() {
			return getRuleContext(LetVarContext.class,0);
		}
		public ElemContext elem() {
			return getRuleContext(ElemContext.class,0);
		}
		public LetscalarContext(LetStatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).enterLetscalar(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).exitLetscalar(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof deliaVisitor ) return ((deliaVisitor<? extends T>)visitor).visitLetscalar(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class LetContext extends LetStatementContext {
		public LetVarContext letVar() {
			return getRuleContext(LetVarContext.class,0);
		}
		public NameContext name() {
			return getRuleContext(NameContext.class,0);
		}
		public FilterContext filter() {
			return getRuleContext(FilterContext.class,0);
		}
		public FnChainContext fnChain() {
			return getRuleContext(FnChainContext.class,0);
		}
		public LetContext(LetStatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).enterLet(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).exitLet(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof deliaVisitor ) return ((deliaVisitor<? extends T>)visitor).visitLet(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class LetNoVarContext extends LetStatementContext {
		public NameContext name() {
			return getRuleContext(NameContext.class,0);
		}
		public FilterContext filter() {
			return getRuleContext(FilterContext.class,0);
		}
		public FnChainContext fnChain() {
			return getRuleContext(FnChainContext.class,0);
		}
		public LetNoVarContext(LetStatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).enterLetNoVar(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).exitLetNoVar(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof deliaVisitor ) return ((deliaVisitor<? extends T>)visitor).visitLetNoVar(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LetStatementContext letStatement() throws RecognitionException {
		LetStatementContext _localctx = new LetStatementContext(_ctx, getState());
		enterRule(_localctx, 50, RULE_letStatement);
		int _la;
		try {
			setState(276);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,30,_ctx) ) {
			case 1:
				_localctx = new LetContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(258);
				match(T__24);
				setState(259);
				letVar();
				setState(260);
				match(T__1);
				setState(261);
				name();
				setState(262);
				filter();
				setState(264);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==DOT) {
					{
					setState(263);
					fnChain();
					}
				}

				}
				break;
			case 2:
				_localctx = new LetscalarContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(266);
				match(T__24);
				setState(267);
				letVar();
				setState(268);
				match(T__1);
				setState(269);
				elem();
				}
				break;
			case 3:
				_localctx = new LetNoVarContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(271);
				name();
				setState(272);
				filter();
				setState(274);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==DOT) {
					{
					setState(273);
					fnChain();
					}
				}

				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class LetVarContext extends ParserRuleContext {
		public LetVarContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_letVar; }
	 
		public LetVarContext() { }
		public void copyFrom(LetVarContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class NoTypeContext extends LetVarContext {
		public TerminalNode SYMBOL() { return getToken(deliaParser.SYMBOL, 0); }
		public List<TerminalNode> DOLLAR() { return getTokens(deliaParser.DOLLAR); }
		public TerminalNode DOLLAR(int i) {
			return getToken(deliaParser.DOLLAR, i);
		}
		public NoTypeContext(LetVarContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).enterNoType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).exitNoType(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof deliaVisitor ) return ((deliaVisitor<? extends T>)visitor).visitNoType(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class WithTypeContext extends LetVarContext {
		public NameContext name() {
			return getRuleContext(NameContext.class,0);
		}
		public TerminalNode SYMBOL() { return getToken(deliaParser.SYMBOL, 0); }
		public List<TerminalNode> DOLLAR() { return getTokens(deliaParser.DOLLAR); }
		public TerminalNode DOLLAR(int i) {
			return getToken(deliaParser.DOLLAR, i);
		}
		public WithTypeContext(LetVarContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).enterWithType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).exitWithType(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof deliaVisitor ) return ((deliaVisitor<? extends T>)visitor).visitWithType(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LetVarContext letVar() throws RecognitionException {
		LetVarContext _localctx = new LetVarContext(_ctx, getState());
		enterRule(_localctx, 52, RULE_letVar);
		try {
			setState(289);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,33,_ctx) ) {
			case 1:
				_localctx = new NoTypeContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(281);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case SYMBOL:
					{
					setState(278);
					match(SYMBOL);
					}
					break;
				case DOLLAR:
					{
					setState(279);
					match(DOLLAR);
					setState(280);
					match(DOLLAR);
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				}
				break;
			case 2:
				_localctx = new WithTypeContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(286);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case SYMBOL:
					{
					setState(283);
					match(SYMBOL);
					}
					break;
				case DOLLAR:
					{
					setState(284);
					match(DOLLAR);
					setState(285);
					match(DOLLAR);
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(288);
				name();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class FnChainContext extends ParserRuleContext {
		public List<TerminalNode> DOT() { return getTokens(deliaParser.DOT); }
		public TerminalNode DOT(int i) {
			return getToken(deliaParser.DOT, i);
		}
		public List<FnChainArgContext> fnChainArg() {
			return getRuleContexts(FnChainArgContext.class);
		}
		public FnChainArgContext fnChainArg(int i) {
			return getRuleContext(FnChainArgContext.class,i);
		}
		public FnChainContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_fnChain; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).enterFnChain(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).exitFnChain(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof deliaVisitor ) return ((deliaVisitor<? extends T>)visitor).visitFnChain(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FnChainContext fnChain() throws RecognitionException {
		FnChainContext _localctx = new FnChainContext(_ctx, getState());
		enterRule(_localctx, 54, RULE_fnChain);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(291);
			match(DOT);
			setState(292);
			fnChainArg();
			setState(297);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==DOT) {
				{
				{
				setState(293);
				match(DOT);
				setState(294);
				fnChainArg();
				}
				}
				setState(299);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class FnChainArgContext extends ParserRuleContext {
		public FnContext fn() {
			return getRuleContext(FnContext.class,0);
		}
		public NameContext name() {
			return getRuleContext(NameContext.class,0);
		}
		public FnChainArgContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_fnChainArg; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).enterFnChainArg(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).exitFnChainArg(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof deliaVisitor ) return ((deliaVisitor<? extends T>)visitor).visitFnChainArg(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FnChainArgContext fnChainArg() throws RecognitionException {
		FnChainArgContext _localctx = new FnChainArgContext(_ctx, getState());
		enterRule(_localctx, 56, RULE_fnChainArg);
		try {
			setState(302);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,35,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(300);
				fn();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(301);
				name();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class FilterContext extends ParserRuleContext {
		public TerminalNode BEGF() { return getToken(deliaParser.BEGF, 0); }
		public CexprContext cexpr() {
			return getRuleContext(CexprContext.class,0);
		}
		public TerminalNode ENDF() { return getToken(deliaParser.ENDF, 0); }
		public FilterContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_filter; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).enterFilter(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).exitFilter(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof deliaVisitor ) return ((deliaVisitor<? extends T>)visitor).visitFilter(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FilterContext filter() throws RecognitionException {
		FilterContext _localctx = new FilterContext(_ctx, getState());
		enterRule(_localctx, 58, RULE_filter);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(304);
			match(BEGF);
			setState(305);
			cexpr(0);
			setState(306);
			match(ENDF);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class CexprContext extends ParserRuleContext {
		public ElemContext elem() {
			return getRuleContext(ElemContext.class,0);
		}
		public TerminalNode BEGPAREN() { return getToken(deliaParser.BEGPAREN, 0); }
		public List<CexprContext> cexpr() {
			return getRuleContexts(CexprContext.class);
		}
		public CexprContext cexpr(int i) {
			return getRuleContext(CexprContext.class,i);
		}
		public TerminalNode ENDPAREN() { return getToken(deliaParser.ENDPAREN, 0); }
		public TerminalNode NOT() { return getToken(deliaParser.NOT, 0); }
		public TerminalNode BEGF() { return getToken(deliaParser.BEGF, 0); }
		public FnargsContext fnargs() {
			return getRuleContext(FnargsContext.class,0);
		}
		public TerminalNode ENDF() { return getToken(deliaParser.ENDF, 0); }
		public CexprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_cexpr; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).enterCexpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).exitCexpr(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof deliaVisitor ) return ((deliaVisitor<? extends T>)visitor).visitCexpr(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CexprContext cexpr() throws RecognitionException {
		return cexpr(0);
	}

	private CexprContext cexpr(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		CexprContext _localctx = new CexprContext(_ctx, _parentState);
		CexprContext _prevctx = _localctx;
		int _startState = 60;
		enterRecursionRule(_localctx, 60, RULE_cexpr, _p);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(319);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__35:
			case TRUE:
			case FALSE:
			case NULL:
			case SYMBOL:
			case DOLLAR:
			case NUM:
			case FloatingPointLiteral:
			case StringLiteral:
			case StringLiteral2:
				{
				setState(309);
				elem();
				}
				break;
			case BEGPAREN:
				{
				setState(310);
				match(BEGPAREN);
				setState(311);
				cexpr(0);
				setState(312);
				match(ENDPAREN);
				}
				break;
			case NOT:
				{
				setState(314);
				match(NOT);
				setState(315);
				match(BEGPAREN);
				setState(316);
				cexpr(0);
				setState(317);
				match(ENDPAREN);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			_ctx.stop = _input.LT(-1);
			setState(335);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,38,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(333);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,37,_ctx) ) {
					case 1:
						{
						_localctx = new CexprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_cexpr);
						setState(321);
						if (!(precpred(_ctx, 2))) throw new FailedPredicateException(this, "precpred(_ctx, 2)");
						setState(322);
						_la = _input.LA(1);
						if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 17045651456L) != 0)) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(323);
						cexpr(3);
						}
						break;
					case 2:
						{
						_localctx = new CexprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_cexpr);
						setState(324);
						if (!(precpred(_ctx, 1))) throw new FailedPredicateException(this, "precpred(_ctx, 1)");
						setState(325);
						_la = _input.LA(1);
						if ( !(_la==T__33 || _la==T__34) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(326);
						cexpr(2);
						}
						break;
					case 3:
						{
						_localctx = new CexprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_cexpr);
						setState(327);
						if (!(precpred(_ctx, 5))) throw new FailedPredicateException(this, "precpred(_ctx, 5)");
						setState(328);
						match(T__25);
						setState(329);
						match(BEGF);
						setState(330);
						fnargs();
						setState(331);
						match(ENDF);
						}
						break;
					}
					} 
				}
				setState(337);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,38,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			unrollRecursionContexts(_parentctx);
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ElemContext extends ParserRuleContext {
		public ScalarContext scalar() {
			return getRuleContext(ScalarContext.class,0);
		}
		public NameContext name() {
			return getRuleContext(NameContext.class,0);
		}
		public FnContext fn() {
			return getRuleContext(FnContext.class,0);
		}
		public List<TerminalNode> DOLLAR() { return getTokens(deliaParser.DOLLAR); }
		public TerminalNode DOLLAR(int i) {
			return getToken(deliaParser.DOLLAR, i);
		}
		public ElemContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_elem; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).enterElem(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).exitElem(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof deliaVisitor ) return ((deliaVisitor<? extends T>)visitor).visitElem(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ElemContext elem() throws RecognitionException {
		ElemContext _localctx = new ElemContext(_ctx, getState());
		enterRule(_localctx, 62, RULE_elem);
		try {
			setState(343);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,39,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(338);
				scalar();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(339);
				name();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(340);
				fn();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(341);
				match(DOLLAR);
				setState(342);
				match(DOLLAR);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class FnContext extends ParserRuleContext {
		public NameContext name() {
			return getRuleContext(NameContext.class,0);
		}
		public TerminalNode BEGPAREN() { return getToken(deliaParser.BEGPAREN, 0); }
		public TerminalNode ENDPAREN() { return getToken(deliaParser.ENDPAREN, 0); }
		public FnargsContext fnargs() {
			return getRuleContext(FnargsContext.class,0);
		}
		public FnContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_fn; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).enterFn(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).exitFn(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof deliaVisitor ) return ((deliaVisitor<? extends T>)visitor).visitFn(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FnContext fn() throws RecognitionException {
		FnContext _localctx = new FnContext(_ctx, getState());
		enterRule(_localctx, 64, RULE_fn);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(345);
			name();
			setState(346);
			match(BEGPAREN);
			setState(348);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 32525821692346368L) != 0)) {
				{
				setState(347);
				fnargs();
				}
			}

			setState(350);
			match(ENDPAREN);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class FnargsContext extends ParserRuleContext {
		public List<ElemContext> elem() {
			return getRuleContexts(ElemContext.class);
		}
		public ElemContext elem(int i) {
			return getRuleContext(ElemContext.class,i);
		}
		public List<TerminalNode> SEP() { return getTokens(deliaParser.SEP); }
		public TerminalNode SEP(int i) {
			return getToken(deliaParser.SEP, i);
		}
		public FnargsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_fnargs; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).enterFnargs(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).exitFnargs(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof deliaVisitor ) return ((deliaVisitor<? extends T>)visitor).visitFnargs(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FnargsContext fnargs() throws RecognitionException {
		FnargsContext _localctx = new FnargsContext(_ctx, getState());
		enterRule(_localctx, 66, RULE_fnargs);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(352);
			elem();
			setState(357);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==SEP) {
				{
				{
				setState(353);
				match(SEP);
				setState(354);
				elem();
				}
				}
				setState(359);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class NameContext extends ParserRuleContext {
		public List<TerminalNode> SYMBOL() { return getTokens(deliaParser.SYMBOL); }
		public TerminalNode SYMBOL(int i) {
			return getToken(deliaParser.SYMBOL, i);
		}
		public List<TerminalNode> DOT() { return getTokens(deliaParser.DOT); }
		public TerminalNode DOT(int i) {
			return getToken(deliaParser.DOT, i);
		}
		public NameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_name; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).enterName(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).exitName(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof deliaVisitor ) return ((deliaVisitor<? extends T>)visitor).visitName(this);
			else return visitor.visitChildren(this);
		}
	}

	public final NameContext name() throws RecognitionException {
		NameContext _localctx = new NameContext(_ctx, getState());
		enterRule(_localctx, 68, RULE_name);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(360);
			match(SYMBOL);
			setState(365);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,42,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(361);
					match(DOT);
					setState(362);
					match(SYMBOL);
					}
					} 
				}
				setState(367);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,42,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ScalarContext extends ParserRuleContext {
		public ScalarContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_scalar; }
	 
		public ScalarContext() { }
		public void copyFrom(ScalarContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class StrContext extends ScalarContext {
		public TerminalNode StringLiteral() { return getToken(deliaParser.StringLiteral, 0); }
		public StrContext(ScalarContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).enterStr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).exitStr(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof deliaVisitor ) return ((deliaVisitor<? extends T>)visitor).visitStr(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class NullValueContext extends ScalarContext {
		public TerminalNode NULL() { return getToken(deliaParser.NULL, 0); }
		public NullValueContext(ScalarContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).enterNullValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).exitNullValue(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof deliaVisitor ) return ((deliaVisitor<? extends T>)visitor).visitNullValue(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class BoolContext extends ScalarContext {
		public TerminalNode TRUE() { return getToken(deliaParser.TRUE, 0); }
		public TerminalNode FALSE() { return getToken(deliaParser.FALSE, 0); }
		public BoolContext(ScalarContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).enterBool(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).exitBool(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof deliaVisitor ) return ((deliaVisitor<? extends T>)visitor).visitBool(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class Str2Context extends ScalarContext {
		public TerminalNode StringLiteral2() { return getToken(deliaParser.StringLiteral2, 0); }
		public Str2Context(ScalarContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).enterStr2(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).exitStr2(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof deliaVisitor ) return ((deliaVisitor<? extends T>)visitor).visitStr2(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class NumContext extends ScalarContext {
		public TerminalNode NUM() { return getToken(deliaParser.NUM, 0); }
		public NumContext(ScalarContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).enterNum(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).exitNum(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof deliaVisitor ) return ((deliaVisitor<? extends T>)visitor).visitNum(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class RealContext extends ScalarContext {
		public TerminalNode FloatingPointLiteral() { return getToken(deliaParser.FloatingPointLiteral, 0); }
		public RealContext(ScalarContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).enterReal(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).exitReal(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof deliaVisitor ) return ((deliaVisitor<? extends T>)visitor).visitReal(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class NegNumContext extends ScalarContext {
		public TerminalNode NUM() { return getToken(deliaParser.NUM, 0); }
		public NegNumContext(ScalarContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).enterNegNum(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).exitNegNum(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof deliaVisitor ) return ((deliaVisitor<? extends T>)visitor).visitNegNum(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class NegRealContext extends ScalarContext {
		public TerminalNode FloatingPointLiteral() { return getToken(deliaParser.FloatingPointLiteral, 0); }
		public NegRealContext(ScalarContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).enterNegReal(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof deliaListener ) ((deliaListener)listener).exitNegReal(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof deliaVisitor ) return ((deliaVisitor<? extends T>)visitor).visitNegReal(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ScalarContext scalar() throws RecognitionException {
		ScalarContext _localctx = new ScalarContext(_ctx, getState());
		enterRule(_localctx, 70, RULE_scalar);
		int _la;
		try {
			setState(378);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,43,_ctx) ) {
			case 1:
				_localctx = new NumContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(368);
				match(NUM);
				}
				break;
			case 2:
				_localctx = new NegNumContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(369);
				match(T__35);
				setState(370);
				match(NUM);
				}
				break;
			case 3:
				_localctx = new RealContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(371);
				match(FloatingPointLiteral);
				}
				break;
			case 4:
				_localctx = new NegRealContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(372);
				match(T__35);
				setState(373);
				match(FloatingPointLiteral);
				}
				break;
			case 5:
				_localctx = new BoolContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(374);
				_la = _input.LA(1);
				if ( !(_la==TRUE || _la==FALSE) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
				break;
			case 6:
				_localctx = new StrContext(_localctx);
				enterOuterAlt(_localctx, 6);
				{
				setState(375);
				match(StringLiteral);
				}
				break;
			case 7:
				_localctx = new Str2Context(_localctx);
				enterOuterAlt(_localctx, 7);
				{
				setState(376);
				match(StringLiteral2);
				}
				break;
			case 8:
				_localctx = new NullValueContext(_localctx);
				enterOuterAlt(_localctx, 8);
				{
				setState(377);
				match(NULL);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public boolean sempred(RuleContext _localctx, int ruleIndex, int predIndex) {
		switch (ruleIndex) {
		case 30:
			return cexpr_sempred((CexprContext)_localctx, predIndex);
		}
		return true;
	}
	private boolean cexpr_sempred(CexprContext _localctx, int predIndex) {
		switch (predIndex) {
		case 0:
			return precpred(_ctx, 2);
		case 1:
			return precpred(_ctx, 1);
		case 2:
			return precpred(_ctx, 5);
		}
		return true;
	}

	public static final String _serializedATN =
		"\u0004\u00018\u017d\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001\u0002"+
		"\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004\u0007\u0004\u0002"+
		"\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002\u0007\u0007\u0007\u0002"+
		"\b\u0007\b\u0002\t\u0007\t\u0002\n\u0007\n\u0002\u000b\u0007\u000b\u0002"+
		"\f\u0007\f\u0002\r\u0007\r\u0002\u000e\u0007\u000e\u0002\u000f\u0007\u000f"+
		"\u0002\u0010\u0007\u0010\u0002\u0011\u0007\u0011\u0002\u0012\u0007\u0012"+
		"\u0002\u0013\u0007\u0013\u0002\u0014\u0007\u0014\u0002\u0015\u0007\u0015"+
		"\u0002\u0016\u0007\u0016\u0002\u0017\u0007\u0017\u0002\u0018\u0007\u0018"+
		"\u0002\u0019\u0007\u0019\u0002\u001a\u0007\u001a\u0002\u001b\u0007\u001b"+
		"\u0002\u001c\u0007\u001c\u0002\u001d\u0007\u001d\u0002\u001e\u0007\u001e"+
		"\u0002\u001f\u0007\u001f\u0002 \u0007 \u0002!\u0007!\u0002\"\u0007\"\u0002"+
		"#\u0007#\u0001\u0000\u0004\u0000J\b\u0000\u000b\u0000\f\u0000K\u0001\u0000"+
		"\u0001\u0000\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001"+
		"\u0001\u0001\u0001\u0001\u0001\u0001\u0003\u0001X\b\u0001\u0001\u0002"+
		"\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0003\u0001\u0003"+
		"\u0001\u0003\u0001\u0003\u0003\u0003c\b\u0003\u0001\u0004\u0001\u0004"+
		"\u0001\u0004\u0003\u0004h\b\u0004\u0001\u0005\u0001\u0005\u0003\u0005"+
		"l\b\u0005\u0001\u0006\u0001\u0006\u0001\u0006\u0001\u0006\u0003\u0006"+
		"r\b\u0006\u0001\u0006\u0001\u0006\u0001\u0007\u0001\u0007\u0001\u0007"+
		"\u0001\u0007\u0001\u0007\u0003\u0007{\b\u0007\u0001\u0007\u0001\u0007"+
		"\u0003\u0007\u007f\b\u0007\u0001\u0007\u0001\u0007\u0001\b\u0001\b\u0001"+
		"\b\u0005\b\u0086\b\b\n\b\f\b\u0089\t\b\u0001\t\u0003\t\u008c\b\t\u0001"+
		"\t\u0001\t\u0001\t\u0003\t\u0091\b\t\u0001\t\u0003\t\u0094\b\t\u0001\t"+
		"\u0003\t\u0097\b\t\u0001\n\u0001\n\u0003\n\u009b\b\n\u0001\u000b\u0001"+
		"\u000b\u0005\u000b\u009f\b\u000b\n\u000b\f\u000b\u00a2\t\u000b\u0001\f"+
		"\u0001\f\u0001\f\u0003\f\u00a7\b\f\u0001\f\u0001\f\u0001\r\u0001\r\u0003"+
		"\r\u00ad\b\r\u0001\u000e\u0001\u000e\u0001\u000f\u0001\u000f\u0001\u000f"+
		"\u0005\u000f\u00b4\b\u000f\n\u000f\f\u000f\u00b7\t\u000f\u0001\u0010\u0001"+
		"\u0010\u0001\u0011\u0001\u0011\u0001\u0011\u0001\u0011\u0003\u0011\u00bf"+
		"\b\u0011\u0001\u0011\u0001\u0011\u0001\u0012\u0001\u0012\u0001\u0012\u0005"+
		"\u0012\u00c6\b\u0012\n\u0012\f\u0012\u00c9\t\u0012\u0001\u0013\u0003\u0013"+
		"\u00cc\b\u0013\u0001\u0013\u0001\u0013\u0001\u0013\u0001\u0013\u0003\u0013"+
		"\u00d2\b\u0013\u0001\u0013\u0001\u0013\u0001\u0013\u0001\u0013\u0001\u0013"+
		"\u0001\u0013\u0003\u0013\u00da\b\u0013\u0001\u0014\u0001\u0014\u0001\u0015"+
		"\u0001\u0015\u0001\u0015\u0005\u0015\u00e1\b\u0015\n\u0015\f\u0015\u00e4"+
		"\t\u0015\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0017\u0001"+
		"\u0017\u0001\u0017\u0001\u0017\u0001\u0017\u0003\u0017\u00ef\b\u0017\u0001"+
		"\u0017\u0001\u0017\u0001\u0017\u0001\u0017\u0003\u0017\u00f5\b\u0017\u0001"+
		"\u0017\u0001\u0017\u0001\u0017\u0001\u0017\u0003\u0017\u00fb\b\u0017\u0001"+
		"\u0017\u0001\u0017\u0003\u0017\u00ff\b\u0017\u0001\u0018\u0001\u0018\u0001"+
		"\u0019\u0001\u0019\u0001\u0019\u0001\u0019\u0001\u0019\u0001\u0019\u0003"+
		"\u0019\u0109\b\u0019\u0001\u0019\u0001\u0019\u0001\u0019\u0001\u0019\u0001"+
		"\u0019\u0001\u0019\u0001\u0019\u0001\u0019\u0003\u0019\u0113\b\u0019\u0003"+
		"\u0019\u0115\b\u0019\u0001\u001a\u0001\u001a\u0001\u001a\u0003\u001a\u011a"+
		"\b\u001a\u0001\u001a\u0001\u001a\u0001\u001a\u0003\u001a\u011f\b\u001a"+
		"\u0001\u001a\u0003\u001a\u0122\b\u001a\u0001\u001b\u0001\u001b\u0001\u001b"+
		"\u0001\u001b\u0005\u001b\u0128\b\u001b\n\u001b\f\u001b\u012b\t\u001b\u0001"+
		"\u001c\u0001\u001c\u0003\u001c\u012f\b\u001c\u0001\u001d\u0001\u001d\u0001"+
		"\u001d\u0001\u001d\u0001\u001e\u0001\u001e\u0001\u001e\u0001\u001e\u0001"+
		"\u001e\u0001\u001e\u0001\u001e\u0001\u001e\u0001\u001e\u0001\u001e\u0001"+
		"\u001e\u0003\u001e\u0140\b\u001e\u0001\u001e\u0001\u001e\u0001\u001e\u0001"+
		"\u001e\u0001\u001e\u0001\u001e\u0001\u001e\u0001\u001e\u0001\u001e\u0001"+
		"\u001e\u0001\u001e\u0001\u001e\u0005\u001e\u014e\b\u001e\n\u001e\f\u001e"+
		"\u0151\t\u001e\u0001\u001f\u0001\u001f\u0001\u001f\u0001\u001f\u0001\u001f"+
		"\u0003\u001f\u0158\b\u001f\u0001 \u0001 \u0001 \u0003 \u015d\b \u0001"+
		" \u0001 \u0001!\u0001!\u0001!\u0005!\u0164\b!\n!\f!\u0167\t!\u0001\"\u0001"+
		"\"\u0001\"\u0005\"\u016c\b\"\n\"\f\"\u016f\t\"\u0001#\u0001#\u0001#\u0001"+
		"#\u0001#\u0001#\u0001#\u0001#\u0001#\u0001#\u0003#\u017b\b#\u0001#\u0000"+
		"\u0001<$\u0000\u0002\u0004\u0006\b\n\f\u000e\u0010\u0012\u0014\u0016\u0018"+
		"\u001a\u001c\u001e \"$&(*,.02468:<>@BDF\u0000\u0005\u0001\u0000\f\u0012"+
		"\u0002\u0000\u0013\u0013\u0015\u0016\u0001\u0000\u001b!\u0001\u0000\""+
		"#\u0001\u0000)*\u0195\u0000I\u0001\u0000\u0000\u0000\u0002W\u0001\u0000"+
		"\u0000\u0000\u0004Y\u0001\u0000\u0000\u0000\u0006b\u0001\u0000\u0000\u0000"+
		"\bd\u0001\u0000\u0000\u0000\nk\u0001\u0000\u0000\u0000\fm\u0001\u0000"+
		"\u0000\u0000\u000eu\u0001\u0000\u0000\u0000\u0010\u0082\u0001\u0000\u0000"+
		"\u0000\u0012\u008b\u0001\u0000\u0000\u0000\u0014\u009a\u0001\u0000\u0000"+
		"\u0000\u0016\u009c\u0001\u0000\u0000\u0000\u0018\u00a3\u0001\u0000\u0000"+
		"\u0000\u001a\u00ac\u0001\u0000\u0000\u0000\u001c\u00ae\u0001\u0000\u0000"+
		"\u0000\u001e\u00b0\u0001\u0000\u0000\u0000 \u00b8\u0001\u0000\u0000\u0000"+
		"\"\u00ba\u0001\u0000\u0000\u0000$\u00c2\u0001\u0000\u0000\u0000&\u00d9"+
		"\u0001\u0000\u0000\u0000(\u00db\u0001\u0000\u0000\u0000*\u00dd\u0001\u0000"+
		"\u0000\u0000,\u00e5\u0001\u0000\u0000\u0000.\u00fe\u0001\u0000\u0000\u0000"+
		"0\u0100\u0001\u0000\u0000\u00002\u0114\u0001\u0000\u0000\u00004\u0121"+
		"\u0001\u0000\u0000\u00006\u0123\u0001\u0000\u0000\u00008\u012e\u0001\u0000"+
		"\u0000\u0000:\u0130\u0001\u0000\u0000\u0000<\u013f\u0001\u0000\u0000\u0000"+
		">\u0157\u0001\u0000\u0000\u0000@\u0159\u0001\u0000\u0000\u0000B\u0160"+
		"\u0001\u0000\u0000\u0000D\u0168\u0001\u0000\u0000\u0000F\u017a\u0001\u0000"+
		"\u0000\u0000HJ\u0003\u0002\u0001\u0000IH\u0001\u0000\u0000\u0000JK\u0001"+
		"\u0000\u0000\u0000KI\u0001\u0000\u0000\u0000KL\u0001\u0000\u0000\u0000"+
		"LM\u0001\u0000\u0000\u0000MN\u0005\u0000\u0000\u0001N\u0001\u0001\u0000"+
		"\u0000\u0000OX\u00032\u0019\u0000PX\u0003\u0004\u0002\u0000QX\u0003\u0006"+
		"\u0003\u0000RX\u0003\n\u0005\u0000SX\u0003\"\u0011\u0000TX\u0003,\u0016"+
		"\u0000UX\u0003.\u0017\u0000VX\u0003\b\u0004\u0000WO\u0001\u0000\u0000"+
		"\u0000WP\u0001\u0000\u0000\u0000WQ\u0001\u0000\u0000\u0000WR\u0001\u0000"+
		"\u0000\u0000WS\u0001\u0000\u0000\u0000WT\u0001\u0000\u0000\u0000WU\u0001"+
		"\u0000\u0000\u0000WV\u0001\u0000\u0000\u0000X\u0003\u0001\u0000\u0000"+
		"\u0000YZ\u0005\u0001\u0000\u0000Z[\u0003D\"\u0000[\\\u0005\u0002\u0000"+
		"\u0000\\]\u0003F#\u0000]\u0005\u0001\u0000\u0000\u0000^_\u0005\u0003\u0000"+
		"\u0000_c\u0003D\"\u0000`a\u0005\u0003\u0000\u0000ac\u0005\u0004\u0000"+
		"\u0000b^\u0001\u0000\u0000\u0000b`\u0001\u0000\u0000\u0000c\u0007\u0001"+
		"\u0000\u0000\u0000dg\u0005\u0005\u0000\u0000eh\u0003D\"\u0000fh\u0003"+
		"F#\u0000ge\u0001\u0000\u0000\u0000gf\u0001\u0000\u0000\u0000h\t\u0001"+
		"\u0000\u0000\u0000il\u0003\f\u0006\u0000jl\u0003\u000e\u0007\u0000ki\u0001"+
		"\u0000\u0000\u0000kj\u0001\u0000\u0000\u0000l\u000b\u0001\u0000\u0000"+
		"\u0000mn\u0005\u0006\u0000\u0000no\u0005/\u0000\u0000oq\u0003D\"\u0000"+
		"pr\u0003\u001e\u000f\u0000qp\u0001\u0000\u0000\u0000qr\u0001\u0000\u0000"+
		"\u0000rs\u0001\u0000\u0000\u0000st\u0005\u0007\u0000\u0000t\r\u0001\u0000"+
		"\u0000\u0000uv\u0005\u0006\u0000\u0000vw\u0005/\u0000\u0000wx\u0003D\""+
		"\u0000xz\u0005\b\u0000\u0000y{\u0003\u0010\b\u0000zy\u0001\u0000\u0000"+
		"\u0000z{\u0001\u0000\u0000\u0000{|\u0001\u0000\u0000\u0000|~\u0005\t\u0000"+
		"\u0000}\u007f\u0003\u001e\u000f\u0000~}\u0001\u0000\u0000\u0000~\u007f"+
		"\u0001\u0000\u0000\u0000\u007f\u0080\u0001\u0000\u0000\u0000\u0080\u0081"+
		"\u0005\u0007\u0000\u0000\u0081\u000f\u0001\u0000\u0000\u0000\u0082\u0087"+
		"\u0003\u0012\t\u0000\u0083\u0084\u0005,\u0000\u0000\u0084\u0086\u0003"+
		"\u0012\t\u0000\u0085\u0083\u0001\u0000\u0000\u0000\u0086\u0089\u0001\u0000"+
		"\u0000\u0000\u0087\u0085\u0001\u0000\u0000\u0000\u0087\u0088\u0001\u0000"+
		"\u0000\u0000\u0088\u0011\u0001\u0000\u0000\u0000\u0089\u0087\u0001\u0000"+
		"\u0000\u0000\u008a\u008c\u0005\n\u0000\u0000\u008b\u008a\u0001\u0000\u0000"+
		"\u0000\u008b\u008c\u0001\u0000\u0000\u0000\u008c\u008d\u0001\u0000\u0000"+
		"\u0000\u008d\u008e\u0005/\u0000\u0000\u008e\u0090\u0003D\"\u0000\u008f"+
		"\u0091\u0003\u0014\n\u0000\u0090\u008f\u0001\u0000\u0000\u0000\u0090\u0091"+
		"\u0001\u0000\u0000\u0000\u0091\u0093\u0001\u0000\u0000\u0000\u0092\u0094"+
		"\u0003\u0016\u000b\u0000\u0093\u0092\u0001\u0000\u0000\u0000\u0093\u0094"+
		"\u0001\u0000\u0000\u0000\u0094\u0096\u0001\u0000\u0000\u0000\u0095\u0097"+
		"\u0003\u0018\f\u0000\u0096\u0095\u0001\u0000\u0000\u0000\u0096\u0097\u0001"+
		"\u0000\u0000\u0000\u0097\u0013\u0001\u0000\u0000\u0000\u0098\u009b\u0005"+
		"5\u0000\u0000\u0099\u009b\u00056\u0000\u0000\u009a\u0098\u0001\u0000\u0000"+
		"\u0000\u009a\u0099\u0001\u0000\u0000\u0000\u009b\u0015\u0001\u0000\u0000"+
		"\u0000\u009c\u00a0\u0003\u001c\u000e\u0000\u009d\u009f\u0003\u001c\u000e"+
		"\u0000\u009e\u009d\u0001\u0000\u0000\u0000\u009f\u00a2\u0001\u0000\u0000"+
		"\u0000\u00a0\u009e\u0001\u0000\u0000\u0000\u00a0\u00a1\u0001\u0000\u0000"+
		"\u0000\u00a1\u0017\u0001\u0000\u0000\u0000\u00a2\u00a0\u0001\u0000\u0000"+
		"\u0000\u00a3\u00a4\u0005\u000b\u0000\u0000\u00a4\u00a6\u0005\'\u0000\u0000"+
		"\u00a5\u00a7\u0003\u001a\r\u0000\u00a6\u00a5\u0001\u0000\u0000\u0000\u00a6"+
		"\u00a7\u0001\u0000\u0000\u0000\u00a7\u00a8\u0001\u0000\u0000\u0000\u00a8"+
		"\u00a9\u0005(\u0000\u0000\u00a9\u0019\u0001\u0000\u0000\u0000\u00aa\u00ad"+
		"\u0003F#\u0000\u00ab\u00ad\u0003D\"\u0000\u00ac\u00aa\u0001\u0000\u0000"+
		"\u0000\u00ac\u00ab\u0001\u0000\u0000\u0000\u00ad\u001b\u0001\u0000\u0000"+
		"\u0000\u00ae\u00af\u0007\u0000\u0000\u0000\u00af\u001d\u0001\u0000\u0000"+
		"\u0000\u00b0\u00b5\u0003 \u0010\u0000\u00b1\u00b2\u0005,\u0000\u0000\u00b2"+
		"\u00b4\u0003 \u0010\u0000\u00b3\u00b1\u0001\u0000\u0000\u0000\u00b4\u00b7"+
		"\u0001\u0000\u0000\u0000\u00b5\u00b3\u0001\u0000\u0000\u0000\u00b5\u00b6"+
		"\u0001\u0000\u0000\u0000\u00b6\u001f\u0001\u0000\u0000\u0000\u00b7\u00b5"+
		"\u0001\u0000\u0000\u0000\u00b8\u00b9\u0003<\u001e\u0000\u00b9!\u0001\u0000"+
		"\u0000\u0000\u00ba\u00bb\u0005\u0013\u0000\u0000\u00bb\u00bc\u0003D\""+
		"\u0000\u00bc\u00be\u0005\b\u0000\u0000\u00bd\u00bf\u0003$\u0012\u0000"+
		"\u00be\u00bd\u0001\u0000\u0000\u0000\u00be\u00bf\u0001\u0000\u0000\u0000"+
		"\u00bf\u00c0\u0001\u0000\u0000\u0000\u00c0\u00c1\u0005\t\u0000\u0000\u00c1"+
		"#\u0001\u0000\u0000\u0000\u00c2\u00c7\u0003&\u0013\u0000\u00c3\u00c4\u0005"+
		",\u0000\u0000\u00c4\u00c6\u0003&\u0013\u0000\u00c5\u00c3\u0001\u0000\u0000"+
		"\u0000\u00c6\u00c9\u0001\u0000\u0000\u0000\u00c7\u00c5\u0001\u0000\u0000"+
		"\u0000\u00c7\u00c8\u0001\u0000\u0000\u0000\u00c8%\u0001\u0000\u0000\u0000"+
		"\u00c9\u00c7\u0001\u0000\u0000\u0000\u00ca\u00cc\u0003(\u0014\u0000\u00cb"+
		"\u00ca\u0001\u0000\u0000\u0000\u00cb\u00cc\u0001\u0000\u0000\u0000\u00cc"+
		"\u00cd\u0001\u0000\u0000\u0000\u00cd\u00ce\u0005/\u0000\u0000\u00ce\u00cf"+
		"\u0005\u0014\u0000\u0000\u00cf\u00da\u0003>\u001f\u0000\u00d0\u00d2\u0003"+
		"(\u0014\u0000\u00d1\u00d0\u0001\u0000\u0000\u0000\u00d1\u00d2\u0001\u0000"+
		"\u0000\u0000\u00d2\u00d3\u0001\u0000\u0000\u0000\u00d3\u00d4\u0005/\u0000"+
		"\u0000\u00d4\u00d5\u0005\u0014\u0000\u0000\u00d5\u00d6\u0005%\u0000\u0000"+
		"\u00d6\u00d7\u0003*\u0015\u0000\u00d7\u00d8\u0005&\u0000\u0000\u00d8\u00da"+
		"\u0001\u0000\u0000\u0000\u00d9\u00cb\u0001\u0000\u0000\u0000\u00d9\u00d1"+
		"\u0001\u0000\u0000\u0000\u00da\'\u0001\u0000\u0000\u0000\u00db\u00dc\u0007"+
		"\u0001\u0000\u0000\u00dc)\u0001\u0000\u0000\u0000\u00dd\u00e2\u0003>\u001f"+
		"\u0000\u00de\u00df\u0005,\u0000\u0000\u00df\u00e1\u0003>\u001f\u0000\u00e0"+
		"\u00de\u0001\u0000\u0000\u0000\u00e1\u00e4\u0001\u0000\u0000\u0000\u00e2"+
		"\u00e0\u0001\u0000\u0000\u0000\u00e2\u00e3\u0001\u0000\u0000\u0000\u00e3"+
		"+\u0001\u0000\u0000\u0000\u00e4\u00e2\u0001\u0000\u0000\u0000\u00e5\u00e6"+
		"\u0005\u0016\u0000\u0000\u00e6\u00e7\u0003D\"\u0000\u00e7\u00e8\u0003"+
		":\u001d\u0000\u00e8-\u0001\u0000\u0000\u0000\u00e9\u00ea\u0005\u0015\u0000"+
		"\u0000\u00ea\u00eb\u0003D\"\u0000\u00eb\u00ec\u0003:\u001d\u0000\u00ec"+
		"\u00ee\u0005\b\u0000\u0000\u00ed\u00ef\u0003$\u0012\u0000\u00ee\u00ed"+
		"\u0001\u0000\u0000\u0000\u00ee\u00ef\u0001\u0000\u0000\u0000\u00ef\u00f0"+
		"\u0001\u0000\u0000\u0000\u00f0\u00f1\u0005\t\u0000\u0000\u00f1\u00ff\u0001"+
		"\u0000\u0000\u0000\u00f2\u00f4\u0005\u0017\u0000\u0000\u00f3\u00f5\u0003"+
		"0\u0018\u0000\u00f4\u00f3\u0001\u0000\u0000\u0000\u00f4\u00f5\u0001\u0000"+
		"\u0000\u0000\u00f5\u00f6\u0001\u0000\u0000\u0000\u00f6\u00f7\u0003D\""+
		"\u0000\u00f7\u00f8\u0003:\u001d\u0000\u00f8\u00fa\u0005\b\u0000\u0000"+
		"\u00f9\u00fb\u0003$\u0012\u0000\u00fa\u00f9\u0001\u0000\u0000\u0000\u00fa"+
		"\u00fb\u0001\u0000\u0000\u0000\u00fb\u00fc\u0001\u0000\u0000\u0000\u00fc"+
		"\u00fd\u0005\t\u0000\u0000\u00fd\u00ff\u0001\u0000\u0000\u0000\u00fe\u00e9"+
		"\u0001\u0000\u0000\u0000\u00fe\u00f2\u0001\u0000\u0000\u0000\u00ff/\u0001"+
		"\u0000\u0000\u0000\u0100\u0101\u0005\u0018\u0000\u0000\u01011\u0001\u0000"+
		"\u0000\u0000\u0102\u0103\u0005\u0019\u0000\u0000\u0103\u0104\u00034\u001a"+
		"\u0000\u0104\u0105\u0005\u0002\u0000\u0000\u0105\u0106\u0003D\"\u0000"+
		"\u0106\u0108\u0003:\u001d\u0000\u0107\u0109\u00036\u001b\u0000\u0108\u0107"+
		"\u0001\u0000\u0000\u0000\u0108\u0109\u0001\u0000\u0000\u0000\u0109\u0115"+
		"\u0001\u0000\u0000\u0000\u010a\u010b\u0005\u0019\u0000\u0000\u010b\u010c"+
		"\u00034\u001a\u0000\u010c\u010d\u0005\u0002\u0000\u0000\u010d\u010e\u0003"+
		">\u001f\u0000\u010e\u0115\u0001\u0000\u0000\u0000\u010f\u0110\u0003D\""+
		"\u0000\u0110\u0112\u0003:\u001d\u0000\u0111\u0113\u00036\u001b\u0000\u0112"+
		"\u0111\u0001\u0000\u0000\u0000\u0112\u0113\u0001\u0000\u0000\u0000\u0113"+
		"\u0115\u0001\u0000\u0000\u0000\u0114\u0102\u0001\u0000\u0000\u0000\u0114"+
		"\u010a\u0001\u0000\u0000\u0000\u0114\u010f\u0001\u0000\u0000\u0000\u0115"+
		"3\u0001\u0000\u0000\u0000\u0116\u011a\u0005/\u0000\u0000\u0117\u0118\u0005"+
		"0\u0000\u0000\u0118\u011a\u00050\u0000\u0000\u0119\u0116\u0001\u0000\u0000"+
		"\u0000\u0119\u0117\u0001\u0000\u0000\u0000\u011a\u0122\u0001\u0000\u0000"+
		"\u0000\u011b\u011f\u0005/\u0000\u0000\u011c\u011d\u00050\u0000\u0000\u011d"+
		"\u011f\u00050\u0000\u0000\u011e\u011b\u0001\u0000\u0000\u0000\u011e\u011c"+
		"\u0001\u0000\u0000\u0000\u011f\u0120\u0001\u0000\u0000\u0000\u0120\u0122"+
		"\u0003D\"\u0000\u0121\u0119\u0001\u0000\u0000\u0000\u0121\u011e\u0001"+
		"\u0000\u0000\u0000\u01225\u0001\u0000\u0000\u0000\u0123\u0124\u0005-\u0000"+
		"\u0000\u0124\u0129\u00038\u001c\u0000\u0125\u0126\u0005-\u0000\u0000\u0126"+
		"\u0128\u00038\u001c\u0000\u0127\u0125\u0001\u0000\u0000\u0000\u0128\u012b"+
		"\u0001\u0000\u0000\u0000\u0129\u0127\u0001\u0000\u0000\u0000\u0129\u012a"+
		"\u0001\u0000\u0000\u0000\u012a7\u0001\u0000\u0000\u0000\u012b\u0129\u0001"+
		"\u0000\u0000\u0000\u012c\u012f\u0003@ \u0000\u012d\u012f\u0003D\"\u0000"+
		"\u012e\u012c\u0001\u0000\u0000\u0000\u012e\u012d\u0001\u0000\u0000\u0000"+
		"\u012f9\u0001\u0000\u0000\u0000\u0130\u0131\u0005%\u0000\u0000\u0131\u0132"+
		"\u0003<\u001e\u0000\u0132\u0133\u0005&\u0000\u0000\u0133;\u0001\u0000"+
		"\u0000\u0000\u0134\u0135\u0006\u001e\uffff\uffff\u0000\u0135\u0140\u0003"+
		">\u001f\u0000\u0136\u0137\u0005\'\u0000\u0000\u0137\u0138\u0003<\u001e"+
		"\u0000\u0138\u0139\u0005(\u0000\u0000\u0139\u0140\u0001\u0000\u0000\u0000"+
		"\u013a\u013b\u0005.\u0000\u0000\u013b\u013c\u0005\'\u0000\u0000\u013c"+
		"\u013d\u0003<\u001e\u0000\u013d\u013e\u0005(\u0000\u0000\u013e\u0140\u0001"+
		"\u0000\u0000\u0000\u013f\u0134\u0001\u0000\u0000\u0000\u013f\u0136\u0001"+
		"\u0000\u0000\u0000\u013f\u013a\u0001\u0000\u0000\u0000\u0140\u014f\u0001"+
		"\u0000\u0000\u0000\u0141\u0142\n\u0002\u0000\u0000\u0142\u0143\u0007\u0002"+
		"\u0000\u0000\u0143\u014e\u0003<\u001e\u0003\u0144\u0145\n\u0001\u0000"+
		"\u0000\u0145\u0146\u0007\u0003\u0000\u0000\u0146\u014e\u0003<\u001e\u0002"+
		"\u0147\u0148\n\u0005\u0000\u0000\u0148\u0149\u0005\u001a\u0000\u0000\u0149"+
		"\u014a\u0005%\u0000\u0000\u014a\u014b\u0003B!\u0000\u014b\u014c\u0005"+
		"&\u0000\u0000\u014c\u014e\u0001\u0000\u0000\u0000\u014d\u0141\u0001\u0000"+
		"\u0000\u0000\u014d\u0144\u0001\u0000\u0000\u0000\u014d\u0147\u0001\u0000"+
		"\u0000\u0000\u014e\u0151\u0001\u0000\u0000\u0000\u014f\u014d\u0001\u0000"+
		"\u0000\u0000\u014f\u0150\u0001\u0000\u0000\u0000\u0150=\u0001\u0000\u0000"+
		"\u0000\u0151\u014f\u0001\u0000\u0000\u0000\u0152\u0158\u0003F#\u0000\u0153"+
		"\u0158\u0003D\"\u0000\u0154\u0158\u0003@ \u0000\u0155\u0156\u00050\u0000"+
		"\u0000\u0156\u0158\u00050\u0000\u0000\u0157\u0152\u0001\u0000\u0000\u0000"+
		"\u0157\u0153\u0001\u0000\u0000\u0000\u0157\u0154\u0001\u0000\u0000\u0000"+
		"\u0157\u0155\u0001\u0000\u0000\u0000\u0158?\u0001\u0000\u0000\u0000\u0159"+
		"\u015a\u0003D\"\u0000\u015a\u015c\u0005\'\u0000\u0000\u015b\u015d\u0003"+
		"B!\u0000\u015c\u015b\u0001\u0000\u0000\u0000\u015c\u015d\u0001\u0000\u0000"+
		"\u0000\u015d\u015e\u0001\u0000\u0000\u0000\u015e\u015f\u0005(\u0000\u0000"+
		"\u015fA\u0001\u0000\u0000\u0000\u0160\u0165\u0003>\u001f\u0000\u0161\u0162"+
		"\u0005,\u0000\u0000\u0162\u0164\u0003>\u001f\u0000\u0163\u0161\u0001\u0000"+
		"\u0000\u0000\u0164\u0167\u0001\u0000\u0000\u0000\u0165\u0163\u0001\u0000"+
		"\u0000\u0000\u0165\u0166\u0001\u0000\u0000\u0000\u0166C\u0001\u0000\u0000"+
		"\u0000\u0167\u0165\u0001\u0000\u0000\u0000\u0168\u016d\u0005/\u0000\u0000"+
		"\u0169\u016a\u0005-\u0000\u0000\u016a\u016c\u0005/\u0000\u0000\u016b\u0169"+
		"\u0001\u0000\u0000\u0000\u016c\u016f\u0001\u0000\u0000\u0000\u016d\u016b"+
		"\u0001\u0000\u0000\u0000\u016d\u016e\u0001\u0000\u0000\u0000\u016eE\u0001"+
		"\u0000\u0000\u0000\u016f\u016d\u0001\u0000\u0000\u0000\u0170\u017b\u0005"+
		"1\u0000\u0000\u0171\u0172\u0005$\u0000\u0000\u0172\u017b\u00051\u0000"+
		"\u0000\u0173\u017b\u00054\u0000\u0000\u0174\u0175\u0005$\u0000\u0000\u0175"+
		"\u017b\u00054\u0000\u0000\u0176\u017b\u0007\u0004\u0000\u0000\u0177\u017b"+
		"\u00055\u0000\u0000\u0178\u017b\u00056\u0000\u0000\u0179\u017b\u0005+"+
		"\u0000\u0000\u017a\u0170\u0001\u0000\u0000\u0000\u017a\u0171\u0001\u0000"+
		"\u0000\u0000\u017a\u0173\u0001\u0000\u0000\u0000\u017a\u0174\u0001\u0000"+
		"\u0000\u0000\u017a\u0176\u0001\u0000\u0000\u0000\u017a\u0177\u0001\u0000"+
		"\u0000\u0000\u017a\u0178\u0001\u0000\u0000\u0000\u017a\u0179\u0001\u0000"+
		"\u0000\u0000\u017bG\u0001\u0000\u0000\u0000,KWbgkqz~\u0087\u008b\u0090"+
		"\u0093\u0096\u009a\u00a0\u00a6\u00ac\u00b5\u00be\u00c7\u00cb\u00d1\u00d9"+
		"\u00e2\u00ee\u00f4\u00fa\u00fe\u0108\u0112\u0114\u0119\u011e\u0121\u0129"+
		"\u012e\u013f\u014d\u014f\u0157\u015c\u0165\u016d\u017a";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}