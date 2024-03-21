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
		T__31=32, T__32=33, T__33=34, T__34=35, BEGF=36, ENDF=37, BEGPAREN=38, 
		ENDPAREN=39, TRUE=40, FALSE=41, NULL=42, SEP=43, DOT=44, NOT=45, SYMBOL=46, 
		DOLLAR=47, NUM=48, COMMENT=49, LINE_COMMENT=50, FloatingPointLiteral=51, 
		StringLiteral=52, StringLiteral2=53, HexDigit=54, WS=55;
	public static final int
		RULE_deliaStatement = 0, RULE_statement = 1, RULE_configureStatement = 2, 
		RULE_schemaStatement = 3, RULE_logStatement = 4, RULE_typeStatement = 5, 
		RULE_scalarTypeStatement = 6, RULE_structTypeStatement = 7, RULE_structFields = 8, 
		RULE_structField = 9, RULE_relationName = 10, RULE_fieldModifiers = 11, 
		RULE_fieldModifier = 12, RULE_drules = 13, RULE_drule = 14, RULE_insertStatement = 15, 
		RULE_valuePairs = 16, RULE_valuePairArg = 17, RULE_crudAction = 18, RULE_elemList = 19, 
		RULE_deleteStatement = 20, RULE_updateStatement = 21, RULE_upsertFlag = 22, 
		RULE_letStatement = 23, RULE_letVar = 24, RULE_fnChain = 25, RULE_fnChainArg = 26, 
		RULE_filter = 27, RULE_cexpr = 28, RULE_elem = 29, RULE_fn = 30, RULE_fnargs = 31, 
		RULE_name = 32, RULE_scalar = 33;
	private static String[] makeRuleNames() {
		return new String[] {
			"deliaStatement", "statement", "configureStatement", "schemaStatement", 
			"logStatement", "typeStatement", "scalarTypeStatement", "structTypeStatement", 
			"structFields", "structField", "relationName", "fieldModifiers", "fieldModifier", 
			"drules", "drule", "insertStatement", "valuePairs", "valuePairArg", "crudAction", 
			"elemList", "deleteStatement", "updateStatement", "upsertFlag", "letStatement", 
			"letVar", "fnChain", "fnChainArg", "filter", "cexpr", "elem", "fn", "fnargs", 
			"name", "scalar"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'configure'", "'='", "'schema'", "'off'", "'log'", "'type'", "'end'", 
			"'{'", "'}'", "'relation'", "'optional'", "'unique'", "'primaryKey'", 
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
			"BEGF", "ENDF", "BEGPAREN", "ENDPAREN", "TRUE", "FALSE", "NULL", "SEP", 
			"DOT", "NOT", "SYMBOL", "DOLLAR", "NUM", "COMMENT", "LINE_COMMENT", "FloatingPointLiteral", 
			"StringLiteral", "StringLiteral2", "HexDigit", "WS"
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
			setState(69); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(68);
				statement();
				}
				}
				setState(71); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( (((_la) & ~0x3f) == 0 && ((1L << _la) & 70368768557162L) != 0) );
			setState(73);
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
			setState(83);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__23:
			case SYMBOL:
				enterOuterAlt(_localctx, 1);
				{
				setState(75);
				letStatement();
				}
				break;
			case T__0:
				enterOuterAlt(_localctx, 2);
				{
				setState(76);
				configureStatement();
				}
				break;
			case T__2:
				enterOuterAlt(_localctx, 3);
				{
				setState(77);
				schemaStatement();
				}
				break;
			case T__5:
				enterOuterAlt(_localctx, 4);
				{
				setState(78);
				typeStatement();
				}
				break;
			case T__17:
				enterOuterAlt(_localctx, 5);
				{
				setState(79);
				insertStatement();
				}
				break;
			case T__20:
				enterOuterAlt(_localctx, 6);
				{
				setState(80);
				deleteStatement();
				}
				break;
			case T__19:
			case T__21:
				enterOuterAlt(_localctx, 7);
				{
				setState(81);
				updateStatement();
				}
				break;
			case T__4:
				enterOuterAlt(_localctx, 8);
				{
				setState(82);
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
			setState(85);
			match(T__0);
			setState(86);
			name();
			setState(87);
			match(T__1);
			setState(88);
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
			setState(94);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,2,_ctx) ) {
			case 1:
				_localctx = new SchemaContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(90);
				match(T__2);
				setState(91);
				name();
				}
				break;
			case 2:
				_localctx = new SchemaOffContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(92);
				match(T__2);
				setState(93);
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
			setState(96);
			match(T__4);
			setState(99);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case SYMBOL:
				{
				setState(97);
				name();
				}
				break;
			case T__34:
			case TRUE:
			case FALSE:
			case NULL:
			case NUM:
			case FloatingPointLiteral:
			case StringLiteral:
			case StringLiteral2:
				{
				setState(98);
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
			setState(103);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,4,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(101);
				scalarTypeStatement();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(102);
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
			setState(105);
			match(T__5);
			setState(106);
			match(SYMBOL);
			setState(107);
			name();
			setState(109);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 16298370096168960L) != 0)) {
				{
				setState(108);
				drules();
				}
			}

			setState(111);
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
			setState(113);
			match(T__5);
			setState(114);
			match(SYMBOL);
			setState(115);
			name();
			setState(116);
			match(T__7);
			setState(118);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__9 || _la==SYMBOL) {
				{
				setState(117);
				structFields();
				}
			}

			setState(120);
			match(T__8);
			setState(122);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 16298370096168960L) != 0)) {
				{
				setState(121);
				drules();
				}
			}

			setState(124);
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
			setState(126);
			structField();
			setState(131);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==SEP) {
				{
				{
				setState(127);
				match(SEP);
				setState(128);
				structField();
				}
				}
				setState(133);
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
			setState(135);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__9) {
				{
				setState(134);
				match(T__9);
				}
			}

			setState(137);
			match(SYMBOL);
			setState(138);
			name();
			setState(140);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==StringLiteral || _la==StringLiteral2) {
				{
				setState(139);
				relationName();
				}
			}

			setState(143);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 260096L) != 0)) {
				{
				setState(142);
				fieldModifiers();
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
			setState(147);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case StringLiteral:
				_localctx = new RelationNameStrContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(145);
				match(StringLiteral);
				}
				break;
			case StringLiteral2:
				_localctx = new RelationNameStr2Context(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(146);
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
			setState(149);
			fieldModifier();
			setState(153);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 260096L) != 0)) {
				{
				{
				setState(150);
				fieldModifier();
				}
				}
				setState(155);
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
		enterRule(_localctx, 24, RULE_fieldModifier);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(156);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 260096L) != 0)) ) {
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
		enterRule(_localctx, 26, RULE_drules);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(158);
			drule();
			setState(163);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==SEP) {
				{
				{
				setState(159);
				match(SEP);
				setState(160);
				drule();
				}
				}
				setState(165);
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
		enterRule(_localctx, 28, RULE_drule);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(166);
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
		enterRule(_localctx, 30, RULE_insertStatement);
		int _la;
		try {
			_localctx = new InsertContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(168);
			match(T__17);
			setState(169);
			name();
			setState(170);
			match(T__7);
			setState(172);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 70368747585536L) != 0)) {
				{
				setState(171);
				valuePairs();
				}
			}

			setState(174);
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
		enterRule(_localctx, 32, RULE_valuePairs);
		int _la;
		try {
			_localctx = new VpValuePairsContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(176);
			valuePairArg();
			setState(181);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==SEP) {
				{
				{
				setState(177);
				match(SEP);
				setState(178);
				valuePairArg();
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
		enterRule(_localctx, 34, RULE_valuePairArg);
		int _la;
		try {
			setState(199);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,19,_ctx) ) {
			case 1:
				_localctx = new VpElemContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(185);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 3407872L) != 0)) {
					{
					setState(184);
					crudAction();
					}
				}

				setState(187);
				match(SYMBOL);
				setState(188);
				match(T__18);
				setState(189);
				elem();
				}
				break;
			case 2:
				_localctx = new VpListContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(191);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 3407872L) != 0)) {
					{
					setState(190);
					crudAction();
					}
				}

				setState(193);
				match(SYMBOL);
				setState(194);
				match(T__18);
				setState(195);
				match(BEGF);
				setState(196);
				elemList();
				setState(197);
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
		enterRule(_localctx, 36, RULE_crudAction);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(201);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 3407872L) != 0)) ) {
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
		enterRule(_localctx, 38, RULE_elemList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(203);
			elem();
			setState(208);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==SEP) {
				{
				{
				setState(204);
				match(SEP);
				setState(205);
				elem();
				}
				}
				setState(210);
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
		enterRule(_localctx, 40, RULE_deleteStatement);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(211);
			match(T__20);
			setState(212);
			name();
			setState(213);
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
		enterRule(_localctx, 42, RULE_updateStatement);
		int _la;
		try {
			setState(236);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__19:
				_localctx = new Update1Context(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(215);
				match(T__19);
				setState(216);
				name();
				setState(217);
				filter();
				setState(218);
				match(T__7);
				setState(220);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 70368747585536L) != 0)) {
					{
					setState(219);
					valuePairs();
					}
				}

				setState(222);
				match(T__8);
				}
				break;
			case T__21:
				_localctx = new Upsert1Context(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(224);
				match(T__21);
				setState(226);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==T__22) {
					{
					setState(225);
					upsertFlag();
					}
				}

				setState(228);
				name();
				setState(229);
				filter();
				setState(230);
				match(T__7);
				setState(232);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 70368747585536L) != 0)) {
					{
					setState(231);
					valuePairs();
					}
				}

				setState(234);
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
		enterRule(_localctx, 44, RULE_upsertFlag);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(238);
			match(T__22);
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
		enterRule(_localctx, 46, RULE_letStatement);
		int _la;
		try {
			setState(258);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,27,_ctx) ) {
			case 1:
				_localctx = new LetContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(240);
				match(T__23);
				setState(241);
				letVar();
				setState(242);
				match(T__1);
				setState(243);
				name();
				setState(244);
				filter();
				setState(246);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==DOT) {
					{
					setState(245);
					fnChain();
					}
				}

				}
				break;
			case 2:
				_localctx = new LetscalarContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(248);
				match(T__23);
				setState(249);
				letVar();
				setState(250);
				match(T__1);
				setState(251);
				elem();
				}
				break;
			case 3:
				_localctx = new LetNoVarContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(253);
				name();
				setState(254);
				filter();
				setState(256);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==DOT) {
					{
					setState(255);
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
		enterRule(_localctx, 48, RULE_letVar);
		try {
			setState(271);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,30,_ctx) ) {
			case 1:
				_localctx = new NoTypeContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(263);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case SYMBOL:
					{
					setState(260);
					match(SYMBOL);
					}
					break;
				case DOLLAR:
					{
					setState(261);
					match(DOLLAR);
					setState(262);
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
				setState(268);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case SYMBOL:
					{
					setState(265);
					match(SYMBOL);
					}
					break;
				case DOLLAR:
					{
					setState(266);
					match(DOLLAR);
					setState(267);
					match(DOLLAR);
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(270);
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
		enterRule(_localctx, 50, RULE_fnChain);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(273);
			match(DOT);
			setState(274);
			fnChainArg();
			setState(279);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==DOT) {
				{
				{
				setState(275);
				match(DOT);
				setState(276);
				fnChainArg();
				}
				}
				setState(281);
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
		enterRule(_localctx, 52, RULE_fnChainArg);
		try {
			setState(284);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,32,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(282);
				fn();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(283);
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
		enterRule(_localctx, 54, RULE_filter);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(286);
			match(BEGF);
			setState(287);
			cexpr(0);
			setState(288);
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
		int _startState = 56;
		enterRecursionRule(_localctx, 56, RULE_cexpr, _p);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(301);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__34:
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
				setState(291);
				elem();
				}
				break;
			case BEGPAREN:
				{
				setState(292);
				match(BEGPAREN);
				setState(293);
				cexpr(0);
				setState(294);
				match(ENDPAREN);
				}
				break;
			case NOT:
				{
				setState(296);
				match(NOT);
				setState(297);
				match(BEGPAREN);
				setState(298);
				cexpr(0);
				setState(299);
				match(ENDPAREN);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			_ctx.stop = _input.LT(-1);
			setState(317);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,35,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(315);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,34,_ctx) ) {
					case 1:
						{
						_localctx = new CexprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_cexpr);
						setState(303);
						if (!(precpred(_ctx, 2))) throw new FailedPredicateException(this, "precpred(_ctx, 2)");
						setState(304);
						_la = _input.LA(1);
						if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 8522825728L) != 0)) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(305);
						cexpr(3);
						}
						break;
					case 2:
						{
						_localctx = new CexprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_cexpr);
						setState(306);
						if (!(precpred(_ctx, 1))) throw new FailedPredicateException(this, "precpred(_ctx, 1)");
						setState(307);
						_la = _input.LA(1);
						if ( !(_la==T__32 || _la==T__33) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(308);
						cexpr(2);
						}
						break;
					case 3:
						{
						_localctx = new CexprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_cexpr);
						setState(309);
						if (!(precpred(_ctx, 5))) throw new FailedPredicateException(this, "precpred(_ctx, 5)");
						setState(310);
						match(T__24);
						setState(311);
						match(BEGF);
						setState(312);
						fnargs();
						setState(313);
						match(ENDF);
						}
						break;
					}
					} 
				}
				setState(319);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,35,_ctx);
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
		enterRule(_localctx, 58, RULE_elem);
		try {
			setState(325);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,36,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(320);
				scalar();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(321);
				name();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(322);
				fn();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(323);
				match(DOLLAR);
				setState(324);
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
		enterRule(_localctx, 60, RULE_fn);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(327);
			name();
			setState(328);
			match(BEGPAREN);
			setState(330);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 16262910846173184L) != 0)) {
				{
				setState(329);
				fnargs();
				}
			}

			setState(332);
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
		enterRule(_localctx, 62, RULE_fnargs);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(334);
			elem();
			setState(339);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==SEP) {
				{
				{
				setState(335);
				match(SEP);
				setState(336);
				elem();
				}
				}
				setState(341);
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
		enterRule(_localctx, 64, RULE_name);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(342);
			match(SYMBOL);
			setState(347);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,39,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(343);
					match(DOT);
					setState(344);
					match(SYMBOL);
					}
					} 
				}
				setState(349);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,39,_ctx);
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
		enterRule(_localctx, 66, RULE_scalar);
		int _la;
		try {
			setState(360);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,40,_ctx) ) {
			case 1:
				_localctx = new NumContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(350);
				match(NUM);
				}
				break;
			case 2:
				_localctx = new NegNumContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(351);
				match(T__34);
				setState(352);
				match(NUM);
				}
				break;
			case 3:
				_localctx = new RealContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(353);
				match(FloatingPointLiteral);
				}
				break;
			case 4:
				_localctx = new NegRealContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(354);
				match(T__34);
				setState(355);
				match(FloatingPointLiteral);
				}
				break;
			case 5:
				_localctx = new BoolContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(356);
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
				setState(357);
				match(StringLiteral);
				}
				break;
			case 7:
				_localctx = new Str2Context(_localctx);
				enterOuterAlt(_localctx, 7);
				{
				setState(358);
				match(StringLiteral2);
				}
				break;
			case 8:
				_localctx = new NullValueContext(_localctx);
				enterOuterAlt(_localctx, 8);
				{
				setState(359);
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
		case 28:
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
		"\u0004\u00017\u016b\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001\u0002"+
		"\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004\u0007\u0004\u0002"+
		"\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002\u0007\u0007\u0007\u0002"+
		"\b\u0007\b\u0002\t\u0007\t\u0002\n\u0007\n\u0002\u000b\u0007\u000b\u0002"+
		"\f\u0007\f\u0002\r\u0007\r\u0002\u000e\u0007\u000e\u0002\u000f\u0007\u000f"+
		"\u0002\u0010\u0007\u0010\u0002\u0011\u0007\u0011\u0002\u0012\u0007\u0012"+
		"\u0002\u0013\u0007\u0013\u0002\u0014\u0007\u0014\u0002\u0015\u0007\u0015"+
		"\u0002\u0016\u0007\u0016\u0002\u0017\u0007\u0017\u0002\u0018\u0007\u0018"+
		"\u0002\u0019\u0007\u0019\u0002\u001a\u0007\u001a\u0002\u001b\u0007\u001b"+
		"\u0002\u001c\u0007\u001c\u0002\u001d\u0007\u001d\u0002\u001e\u0007\u001e"+
		"\u0002\u001f\u0007\u001f\u0002 \u0007 \u0002!\u0007!\u0001\u0000\u0004"+
		"\u0000F\b\u0000\u000b\u0000\f\u0000G\u0001\u0000\u0001\u0000\u0001\u0001"+
		"\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001"+
		"\u0001\u0001\u0003\u0001T\b\u0001\u0001\u0002\u0001\u0002\u0001\u0002"+
		"\u0001\u0002\u0001\u0002\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003"+
		"\u0003\u0003_\b\u0003\u0001\u0004\u0001\u0004\u0001\u0004\u0003\u0004"+
		"d\b\u0004\u0001\u0005\u0001\u0005\u0003\u0005h\b\u0005\u0001\u0006\u0001"+
		"\u0006\u0001\u0006\u0001\u0006\u0003\u0006n\b\u0006\u0001\u0006\u0001"+
		"\u0006\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0003"+
		"\u0007w\b\u0007\u0001\u0007\u0001\u0007\u0003\u0007{\b\u0007\u0001\u0007"+
		"\u0001\u0007\u0001\b\u0001\b\u0001\b\u0005\b\u0082\b\b\n\b\f\b\u0085\t"+
		"\b\u0001\t\u0003\t\u0088\b\t\u0001\t\u0001\t\u0001\t\u0003\t\u008d\b\t"+
		"\u0001\t\u0003\t\u0090\b\t\u0001\n\u0001\n\u0003\n\u0094\b\n\u0001\u000b"+
		"\u0001\u000b\u0005\u000b\u0098\b\u000b\n\u000b\f\u000b\u009b\t\u000b\u0001"+
		"\f\u0001\f\u0001\r\u0001\r\u0001\r\u0005\r\u00a2\b\r\n\r\f\r\u00a5\t\r"+
		"\u0001\u000e\u0001\u000e\u0001\u000f\u0001\u000f\u0001\u000f\u0001\u000f"+
		"\u0003\u000f\u00ad\b\u000f\u0001\u000f\u0001\u000f\u0001\u0010\u0001\u0010"+
		"\u0001\u0010\u0005\u0010\u00b4\b\u0010\n\u0010\f\u0010\u00b7\t\u0010\u0001"+
		"\u0011\u0003\u0011\u00ba\b\u0011\u0001\u0011\u0001\u0011\u0001\u0011\u0001"+
		"\u0011\u0003\u0011\u00c0\b\u0011\u0001\u0011\u0001\u0011\u0001\u0011\u0001"+
		"\u0011\u0001\u0011\u0001\u0011\u0003\u0011\u00c8\b\u0011\u0001\u0012\u0001"+
		"\u0012\u0001\u0013\u0001\u0013\u0001\u0013\u0005\u0013\u00cf\b\u0013\n"+
		"\u0013\f\u0013\u00d2\t\u0013\u0001\u0014\u0001\u0014\u0001\u0014\u0001"+
		"\u0014\u0001\u0015\u0001\u0015\u0001\u0015\u0001\u0015\u0001\u0015\u0003"+
		"\u0015\u00dd\b\u0015\u0001\u0015\u0001\u0015\u0001\u0015\u0001\u0015\u0003"+
		"\u0015\u00e3\b\u0015\u0001\u0015\u0001\u0015\u0001\u0015\u0001\u0015\u0003"+
		"\u0015\u00e9\b\u0015\u0001\u0015\u0001\u0015\u0003\u0015\u00ed\b\u0015"+
		"\u0001\u0016\u0001\u0016\u0001\u0017\u0001\u0017\u0001\u0017\u0001\u0017"+
		"\u0001\u0017\u0001\u0017\u0003\u0017\u00f7\b\u0017\u0001\u0017\u0001\u0017"+
		"\u0001\u0017\u0001\u0017\u0001\u0017\u0001\u0017\u0001\u0017\u0001\u0017"+
		"\u0003\u0017\u0101\b\u0017\u0003\u0017\u0103\b\u0017\u0001\u0018\u0001"+
		"\u0018\u0001\u0018\u0003\u0018\u0108\b\u0018\u0001\u0018\u0001\u0018\u0001"+
		"\u0018\u0003\u0018\u010d\b\u0018\u0001\u0018\u0003\u0018\u0110\b\u0018"+
		"\u0001\u0019\u0001\u0019\u0001\u0019\u0001\u0019\u0005\u0019\u0116\b\u0019"+
		"\n\u0019\f\u0019\u0119\t\u0019\u0001\u001a\u0001\u001a\u0003\u001a\u011d"+
		"\b\u001a\u0001\u001b\u0001\u001b\u0001\u001b\u0001\u001b\u0001\u001c\u0001"+
		"\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0001"+
		"\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0003\u001c\u012e\b\u001c\u0001"+
		"\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0001"+
		"\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0001\u001c\u0005"+
		"\u001c\u013c\b\u001c\n\u001c\f\u001c\u013f\t\u001c\u0001\u001d\u0001\u001d"+
		"\u0001\u001d\u0001\u001d\u0001\u001d\u0003\u001d\u0146\b\u001d\u0001\u001e"+
		"\u0001\u001e\u0001\u001e\u0003\u001e\u014b\b\u001e\u0001\u001e\u0001\u001e"+
		"\u0001\u001f\u0001\u001f\u0001\u001f\u0005\u001f\u0152\b\u001f\n\u001f"+
		"\f\u001f\u0155\t\u001f\u0001 \u0001 \u0001 \u0005 \u015a\b \n \f \u015d"+
		"\t \u0001!\u0001!\u0001!\u0001!\u0001!\u0001!\u0001!\u0001!\u0001!\u0001"+
		"!\u0003!\u0169\b!\u0001!\u0000\u00018\"\u0000\u0002\u0004\u0006\b\n\f"+
		"\u000e\u0010\u0012\u0014\u0016\u0018\u001a\u001c\u001e \"$&(*,.02468:"+
		"<>@B\u0000\u0005\u0001\u0000\u000b\u0011\u0002\u0000\u0012\u0012\u0014"+
		"\u0015\u0001\u0000\u001a \u0001\u0000!\"\u0001\u0000()\u0182\u0000E\u0001"+
		"\u0000\u0000\u0000\u0002S\u0001\u0000\u0000\u0000\u0004U\u0001\u0000\u0000"+
		"\u0000\u0006^\u0001\u0000\u0000\u0000\b`\u0001\u0000\u0000\u0000\ng\u0001"+
		"\u0000\u0000\u0000\fi\u0001\u0000\u0000\u0000\u000eq\u0001\u0000\u0000"+
		"\u0000\u0010~\u0001\u0000\u0000\u0000\u0012\u0087\u0001\u0000\u0000\u0000"+
		"\u0014\u0093\u0001\u0000\u0000\u0000\u0016\u0095\u0001\u0000\u0000\u0000"+
		"\u0018\u009c\u0001\u0000\u0000\u0000\u001a\u009e\u0001\u0000\u0000\u0000"+
		"\u001c\u00a6\u0001\u0000\u0000\u0000\u001e\u00a8\u0001\u0000\u0000\u0000"+
		" \u00b0\u0001\u0000\u0000\u0000\"\u00c7\u0001\u0000\u0000\u0000$\u00c9"+
		"\u0001\u0000\u0000\u0000&\u00cb\u0001\u0000\u0000\u0000(\u00d3\u0001\u0000"+
		"\u0000\u0000*\u00ec\u0001\u0000\u0000\u0000,\u00ee\u0001\u0000\u0000\u0000"+
		".\u0102\u0001\u0000\u0000\u00000\u010f\u0001\u0000\u0000\u00002\u0111"+
		"\u0001\u0000\u0000\u00004\u011c\u0001\u0000\u0000\u00006\u011e\u0001\u0000"+
		"\u0000\u00008\u012d\u0001\u0000\u0000\u0000:\u0145\u0001\u0000\u0000\u0000"+
		"<\u0147\u0001\u0000\u0000\u0000>\u014e\u0001\u0000\u0000\u0000@\u0156"+
		"\u0001\u0000\u0000\u0000B\u0168\u0001\u0000\u0000\u0000DF\u0003\u0002"+
		"\u0001\u0000ED\u0001\u0000\u0000\u0000FG\u0001\u0000\u0000\u0000GE\u0001"+
		"\u0000\u0000\u0000GH\u0001\u0000\u0000\u0000HI\u0001\u0000\u0000\u0000"+
		"IJ\u0005\u0000\u0000\u0001J\u0001\u0001\u0000\u0000\u0000KT\u0003.\u0017"+
		"\u0000LT\u0003\u0004\u0002\u0000MT\u0003\u0006\u0003\u0000NT\u0003\n\u0005"+
		"\u0000OT\u0003\u001e\u000f\u0000PT\u0003(\u0014\u0000QT\u0003*\u0015\u0000"+
		"RT\u0003\b\u0004\u0000SK\u0001\u0000\u0000\u0000SL\u0001\u0000\u0000\u0000"+
		"SM\u0001\u0000\u0000\u0000SN\u0001\u0000\u0000\u0000SO\u0001\u0000\u0000"+
		"\u0000SP\u0001\u0000\u0000\u0000SQ\u0001\u0000\u0000\u0000SR\u0001\u0000"+
		"\u0000\u0000T\u0003\u0001\u0000\u0000\u0000UV\u0005\u0001\u0000\u0000"+
		"VW\u0003@ \u0000WX\u0005\u0002\u0000\u0000XY\u0003B!\u0000Y\u0005\u0001"+
		"\u0000\u0000\u0000Z[\u0005\u0003\u0000\u0000[_\u0003@ \u0000\\]\u0005"+
		"\u0003\u0000\u0000]_\u0005\u0004\u0000\u0000^Z\u0001\u0000\u0000\u0000"+
		"^\\\u0001\u0000\u0000\u0000_\u0007\u0001\u0000\u0000\u0000`c\u0005\u0005"+
		"\u0000\u0000ad\u0003@ \u0000bd\u0003B!\u0000ca\u0001\u0000\u0000\u0000"+
		"cb\u0001\u0000\u0000\u0000d\t\u0001\u0000\u0000\u0000eh\u0003\f\u0006"+
		"\u0000fh\u0003\u000e\u0007\u0000ge\u0001\u0000\u0000\u0000gf\u0001\u0000"+
		"\u0000\u0000h\u000b\u0001\u0000\u0000\u0000ij\u0005\u0006\u0000\u0000"+
		"jk\u0005.\u0000\u0000km\u0003@ \u0000ln\u0003\u001a\r\u0000ml\u0001\u0000"+
		"\u0000\u0000mn\u0001\u0000\u0000\u0000no\u0001\u0000\u0000\u0000op\u0005"+
		"\u0007\u0000\u0000p\r\u0001\u0000\u0000\u0000qr\u0005\u0006\u0000\u0000"+
		"rs\u0005.\u0000\u0000st\u0003@ \u0000tv\u0005\b\u0000\u0000uw\u0003\u0010"+
		"\b\u0000vu\u0001\u0000\u0000\u0000vw\u0001\u0000\u0000\u0000wx\u0001\u0000"+
		"\u0000\u0000xz\u0005\t\u0000\u0000y{\u0003\u001a\r\u0000zy\u0001\u0000"+
		"\u0000\u0000z{\u0001\u0000\u0000\u0000{|\u0001\u0000\u0000\u0000|}\u0005"+
		"\u0007\u0000\u0000}\u000f\u0001\u0000\u0000\u0000~\u0083\u0003\u0012\t"+
		"\u0000\u007f\u0080\u0005+\u0000\u0000\u0080\u0082\u0003\u0012\t\u0000"+
		"\u0081\u007f\u0001\u0000\u0000\u0000\u0082\u0085\u0001\u0000\u0000\u0000"+
		"\u0083\u0081\u0001\u0000\u0000\u0000\u0083\u0084\u0001\u0000\u0000\u0000"+
		"\u0084\u0011\u0001\u0000\u0000\u0000\u0085\u0083\u0001\u0000\u0000\u0000"+
		"\u0086\u0088\u0005\n\u0000\u0000\u0087\u0086\u0001\u0000\u0000\u0000\u0087"+
		"\u0088\u0001\u0000\u0000\u0000\u0088\u0089\u0001\u0000\u0000\u0000\u0089"+
		"\u008a\u0005.\u0000\u0000\u008a\u008c\u0003@ \u0000\u008b\u008d\u0003"+
		"\u0014\n\u0000\u008c\u008b\u0001\u0000\u0000\u0000\u008c\u008d\u0001\u0000"+
		"\u0000\u0000\u008d\u008f\u0001\u0000\u0000\u0000\u008e\u0090\u0003\u0016"+
		"\u000b\u0000\u008f\u008e\u0001\u0000\u0000\u0000\u008f\u0090\u0001\u0000"+
		"\u0000\u0000\u0090\u0013\u0001\u0000\u0000\u0000\u0091\u0094\u00054\u0000"+
		"\u0000\u0092\u0094\u00055\u0000\u0000\u0093\u0091\u0001\u0000\u0000\u0000"+
		"\u0093\u0092\u0001\u0000\u0000\u0000\u0094\u0015\u0001\u0000\u0000\u0000"+
		"\u0095\u0099\u0003\u0018\f\u0000\u0096\u0098\u0003\u0018\f\u0000\u0097"+
		"\u0096\u0001\u0000\u0000\u0000\u0098\u009b\u0001\u0000\u0000\u0000\u0099"+
		"\u0097\u0001\u0000\u0000\u0000\u0099\u009a\u0001\u0000\u0000\u0000\u009a"+
		"\u0017\u0001\u0000\u0000\u0000\u009b\u0099\u0001\u0000\u0000\u0000\u009c"+
		"\u009d\u0007\u0000\u0000\u0000\u009d\u0019\u0001\u0000\u0000\u0000\u009e"+
		"\u00a3\u0003\u001c\u000e\u0000\u009f\u00a0\u0005+\u0000\u0000\u00a0\u00a2"+
		"\u0003\u001c\u000e\u0000\u00a1\u009f\u0001\u0000\u0000\u0000\u00a2\u00a5"+
		"\u0001\u0000\u0000\u0000\u00a3\u00a1\u0001\u0000\u0000\u0000\u00a3\u00a4"+
		"\u0001\u0000\u0000\u0000\u00a4\u001b\u0001\u0000\u0000\u0000\u00a5\u00a3"+
		"\u0001\u0000\u0000\u0000\u00a6\u00a7\u00038\u001c\u0000\u00a7\u001d\u0001"+
		"\u0000\u0000\u0000\u00a8\u00a9\u0005\u0012\u0000\u0000\u00a9\u00aa\u0003"+
		"@ \u0000\u00aa\u00ac\u0005\b\u0000\u0000\u00ab\u00ad\u0003 \u0010\u0000"+
		"\u00ac\u00ab\u0001\u0000\u0000\u0000\u00ac\u00ad\u0001\u0000\u0000\u0000"+
		"\u00ad\u00ae\u0001\u0000\u0000\u0000\u00ae\u00af\u0005\t\u0000\u0000\u00af"+
		"\u001f\u0001\u0000\u0000\u0000\u00b0\u00b5\u0003\"\u0011\u0000\u00b1\u00b2"+
		"\u0005+\u0000\u0000\u00b2\u00b4\u0003\"\u0011\u0000\u00b3\u00b1\u0001"+
		"\u0000\u0000\u0000\u00b4\u00b7\u0001\u0000\u0000\u0000\u00b5\u00b3\u0001"+
		"\u0000\u0000\u0000\u00b5\u00b6\u0001\u0000\u0000\u0000\u00b6!\u0001\u0000"+
		"\u0000\u0000\u00b7\u00b5\u0001\u0000\u0000\u0000\u00b8\u00ba\u0003$\u0012"+
		"\u0000\u00b9\u00b8\u0001\u0000\u0000\u0000\u00b9\u00ba\u0001\u0000\u0000"+
		"\u0000\u00ba\u00bb\u0001\u0000\u0000\u0000\u00bb\u00bc\u0005.\u0000\u0000"+
		"\u00bc\u00bd\u0005\u0013\u0000\u0000\u00bd\u00c8\u0003:\u001d\u0000\u00be"+
		"\u00c0\u0003$\u0012\u0000\u00bf\u00be\u0001\u0000\u0000\u0000\u00bf\u00c0"+
		"\u0001\u0000\u0000\u0000\u00c0\u00c1\u0001\u0000\u0000\u0000\u00c1\u00c2"+
		"\u0005.\u0000\u0000\u00c2\u00c3\u0005\u0013\u0000\u0000\u00c3\u00c4\u0005"+
		"$\u0000\u0000\u00c4\u00c5\u0003&\u0013\u0000\u00c5\u00c6\u0005%\u0000"+
		"\u0000\u00c6\u00c8\u0001\u0000\u0000\u0000\u00c7\u00b9\u0001\u0000\u0000"+
		"\u0000\u00c7\u00bf\u0001\u0000\u0000\u0000\u00c8#\u0001\u0000\u0000\u0000"+
		"\u00c9\u00ca\u0007\u0001\u0000\u0000\u00ca%\u0001\u0000\u0000\u0000\u00cb"+
		"\u00d0\u0003:\u001d\u0000\u00cc\u00cd\u0005+\u0000\u0000\u00cd\u00cf\u0003"+
		":\u001d\u0000\u00ce\u00cc\u0001\u0000\u0000\u0000\u00cf\u00d2\u0001\u0000"+
		"\u0000\u0000\u00d0\u00ce\u0001\u0000\u0000\u0000\u00d0\u00d1\u0001\u0000"+
		"\u0000\u0000\u00d1\'\u0001\u0000\u0000\u0000\u00d2\u00d0\u0001\u0000\u0000"+
		"\u0000\u00d3\u00d4\u0005\u0015\u0000\u0000\u00d4\u00d5\u0003@ \u0000\u00d5"+
		"\u00d6\u00036\u001b\u0000\u00d6)\u0001\u0000\u0000\u0000\u00d7\u00d8\u0005"+
		"\u0014\u0000\u0000\u00d8\u00d9\u0003@ \u0000\u00d9\u00da\u00036\u001b"+
		"\u0000\u00da\u00dc\u0005\b\u0000\u0000\u00db\u00dd\u0003 \u0010\u0000"+
		"\u00dc\u00db\u0001\u0000\u0000\u0000\u00dc\u00dd\u0001\u0000\u0000\u0000"+
		"\u00dd\u00de\u0001\u0000\u0000\u0000\u00de\u00df\u0005\t\u0000\u0000\u00df"+
		"\u00ed\u0001\u0000\u0000\u0000\u00e0\u00e2\u0005\u0016\u0000\u0000\u00e1"+
		"\u00e3\u0003,\u0016\u0000\u00e2\u00e1\u0001\u0000\u0000\u0000\u00e2\u00e3"+
		"\u0001\u0000\u0000\u0000\u00e3\u00e4\u0001\u0000\u0000\u0000\u00e4\u00e5"+
		"\u0003@ \u0000\u00e5\u00e6\u00036\u001b\u0000\u00e6\u00e8\u0005\b\u0000"+
		"\u0000\u00e7\u00e9\u0003 \u0010\u0000\u00e8\u00e7\u0001\u0000\u0000\u0000"+
		"\u00e8\u00e9\u0001\u0000\u0000\u0000\u00e9\u00ea\u0001\u0000\u0000\u0000"+
		"\u00ea\u00eb\u0005\t\u0000\u0000\u00eb\u00ed\u0001\u0000\u0000\u0000\u00ec"+
		"\u00d7\u0001\u0000\u0000\u0000\u00ec\u00e0\u0001\u0000\u0000\u0000\u00ed"+
		"+\u0001\u0000\u0000\u0000\u00ee\u00ef\u0005\u0017\u0000\u0000\u00ef-\u0001"+
		"\u0000\u0000\u0000\u00f0\u00f1\u0005\u0018\u0000\u0000\u00f1\u00f2\u0003"+
		"0\u0018\u0000\u00f2\u00f3\u0005\u0002\u0000\u0000\u00f3\u00f4\u0003@ "+
		"\u0000\u00f4\u00f6\u00036\u001b\u0000\u00f5\u00f7\u00032\u0019\u0000\u00f6"+
		"\u00f5\u0001\u0000\u0000\u0000\u00f6\u00f7\u0001\u0000\u0000\u0000\u00f7"+
		"\u0103\u0001\u0000\u0000\u0000\u00f8\u00f9\u0005\u0018\u0000\u0000\u00f9"+
		"\u00fa\u00030\u0018\u0000\u00fa\u00fb\u0005\u0002\u0000\u0000\u00fb\u00fc"+
		"\u0003:\u001d\u0000\u00fc\u0103\u0001\u0000\u0000\u0000\u00fd\u00fe\u0003"+
		"@ \u0000\u00fe\u0100\u00036\u001b\u0000\u00ff\u0101\u00032\u0019\u0000"+
		"\u0100\u00ff\u0001\u0000\u0000\u0000\u0100\u0101\u0001\u0000\u0000\u0000"+
		"\u0101\u0103\u0001\u0000\u0000\u0000\u0102\u00f0\u0001\u0000\u0000\u0000"+
		"\u0102\u00f8\u0001\u0000\u0000\u0000\u0102\u00fd\u0001\u0000\u0000\u0000"+
		"\u0103/\u0001\u0000\u0000\u0000\u0104\u0108\u0005.\u0000\u0000\u0105\u0106"+
		"\u0005/\u0000\u0000\u0106\u0108\u0005/\u0000\u0000\u0107\u0104\u0001\u0000"+
		"\u0000\u0000\u0107\u0105\u0001\u0000\u0000\u0000\u0108\u0110\u0001\u0000"+
		"\u0000\u0000\u0109\u010d\u0005.\u0000\u0000\u010a\u010b\u0005/\u0000\u0000"+
		"\u010b\u010d\u0005/\u0000\u0000\u010c\u0109\u0001\u0000\u0000\u0000\u010c"+
		"\u010a\u0001\u0000\u0000\u0000\u010d\u010e\u0001\u0000\u0000\u0000\u010e"+
		"\u0110\u0003@ \u0000\u010f\u0107\u0001\u0000\u0000\u0000\u010f\u010c\u0001"+
		"\u0000\u0000\u0000\u01101\u0001\u0000\u0000\u0000\u0111\u0112\u0005,\u0000"+
		"\u0000\u0112\u0117\u00034\u001a\u0000\u0113\u0114\u0005,\u0000\u0000\u0114"+
		"\u0116\u00034\u001a\u0000\u0115\u0113\u0001\u0000\u0000\u0000\u0116\u0119"+
		"\u0001\u0000\u0000\u0000\u0117\u0115\u0001\u0000\u0000\u0000\u0117\u0118"+
		"\u0001\u0000\u0000\u0000\u01183\u0001\u0000\u0000\u0000\u0119\u0117\u0001"+
		"\u0000\u0000\u0000\u011a\u011d\u0003<\u001e\u0000\u011b\u011d\u0003@ "+
		"\u0000\u011c\u011a\u0001\u0000\u0000\u0000\u011c\u011b\u0001\u0000\u0000"+
		"\u0000\u011d5\u0001\u0000\u0000\u0000\u011e\u011f\u0005$\u0000\u0000\u011f"+
		"\u0120\u00038\u001c\u0000\u0120\u0121\u0005%\u0000\u0000\u01217\u0001"+
		"\u0000\u0000\u0000\u0122\u0123\u0006\u001c\uffff\uffff\u0000\u0123\u012e"+
		"\u0003:\u001d\u0000\u0124\u0125\u0005&\u0000\u0000\u0125\u0126\u00038"+
		"\u001c\u0000\u0126\u0127\u0005\'\u0000\u0000\u0127\u012e\u0001\u0000\u0000"+
		"\u0000\u0128\u0129\u0005-\u0000\u0000\u0129\u012a\u0005&\u0000\u0000\u012a"+
		"\u012b\u00038\u001c\u0000\u012b\u012c\u0005\'\u0000\u0000\u012c\u012e"+
		"\u0001\u0000\u0000\u0000\u012d\u0122\u0001\u0000\u0000\u0000\u012d\u0124"+
		"\u0001\u0000\u0000\u0000\u012d\u0128\u0001\u0000\u0000\u0000\u012e\u013d"+
		"\u0001\u0000\u0000\u0000\u012f\u0130\n\u0002\u0000\u0000\u0130\u0131\u0007"+
		"\u0002\u0000\u0000\u0131\u013c\u00038\u001c\u0003\u0132\u0133\n\u0001"+
		"\u0000\u0000\u0133\u0134\u0007\u0003\u0000\u0000\u0134\u013c\u00038\u001c"+
		"\u0002\u0135\u0136\n\u0005\u0000\u0000\u0136\u0137\u0005\u0019\u0000\u0000"+
		"\u0137\u0138\u0005$\u0000\u0000\u0138\u0139\u0003>\u001f\u0000\u0139\u013a"+
		"\u0005%\u0000\u0000\u013a\u013c\u0001\u0000\u0000\u0000\u013b\u012f\u0001"+
		"\u0000\u0000\u0000\u013b\u0132\u0001\u0000\u0000\u0000\u013b\u0135\u0001"+
		"\u0000\u0000\u0000\u013c\u013f\u0001\u0000\u0000\u0000\u013d\u013b\u0001"+
		"\u0000\u0000\u0000\u013d\u013e\u0001\u0000\u0000\u0000\u013e9\u0001\u0000"+
		"\u0000\u0000\u013f\u013d\u0001\u0000\u0000\u0000\u0140\u0146\u0003B!\u0000"+
		"\u0141\u0146\u0003@ \u0000\u0142\u0146\u0003<\u001e\u0000\u0143\u0144"+
		"\u0005/\u0000\u0000\u0144\u0146\u0005/\u0000\u0000\u0145\u0140\u0001\u0000"+
		"\u0000\u0000\u0145\u0141\u0001\u0000\u0000\u0000\u0145\u0142\u0001\u0000"+
		"\u0000\u0000\u0145\u0143\u0001\u0000\u0000\u0000\u0146;\u0001\u0000\u0000"+
		"\u0000\u0147\u0148\u0003@ \u0000\u0148\u014a\u0005&\u0000\u0000\u0149"+
		"\u014b\u0003>\u001f\u0000\u014a\u0149\u0001\u0000\u0000\u0000\u014a\u014b"+
		"\u0001\u0000\u0000\u0000\u014b\u014c\u0001\u0000\u0000\u0000\u014c\u014d"+
		"\u0005\'\u0000\u0000\u014d=\u0001\u0000\u0000\u0000\u014e\u0153\u0003"+
		":\u001d\u0000\u014f\u0150\u0005+\u0000\u0000\u0150\u0152\u0003:\u001d"+
		"\u0000\u0151\u014f\u0001\u0000\u0000\u0000\u0152\u0155\u0001\u0000\u0000"+
		"\u0000\u0153\u0151\u0001\u0000\u0000\u0000\u0153\u0154\u0001\u0000\u0000"+
		"\u0000\u0154?\u0001\u0000\u0000\u0000\u0155\u0153\u0001\u0000\u0000\u0000"+
		"\u0156\u015b\u0005.\u0000\u0000\u0157\u0158\u0005,\u0000\u0000\u0158\u015a"+
		"\u0005.\u0000\u0000\u0159\u0157\u0001\u0000\u0000\u0000\u015a\u015d\u0001"+
		"\u0000\u0000\u0000\u015b\u0159\u0001\u0000\u0000\u0000\u015b\u015c\u0001"+
		"\u0000\u0000\u0000\u015cA\u0001\u0000\u0000\u0000\u015d\u015b\u0001\u0000"+
		"\u0000\u0000\u015e\u0169\u00050\u0000\u0000\u015f\u0160\u0005#\u0000\u0000"+
		"\u0160\u0169\u00050\u0000\u0000\u0161\u0169\u00053\u0000\u0000\u0162\u0163"+
		"\u0005#\u0000\u0000\u0163\u0169\u00053\u0000\u0000\u0164\u0169\u0007\u0004"+
		"\u0000\u0000\u0165\u0169\u00054\u0000\u0000\u0166\u0169\u00055\u0000\u0000"+
		"\u0167\u0169\u0005*\u0000\u0000\u0168\u015e\u0001\u0000\u0000\u0000\u0168"+
		"\u015f\u0001\u0000\u0000\u0000\u0168\u0161\u0001\u0000\u0000\u0000\u0168"+
		"\u0162\u0001\u0000\u0000\u0000\u0168\u0164\u0001\u0000\u0000\u0000\u0168"+
		"\u0165\u0001\u0000\u0000\u0000\u0168\u0166\u0001\u0000\u0000\u0000\u0168"+
		"\u0167\u0001\u0000\u0000\u0000\u0169C\u0001\u0000\u0000\u0000)GS^cgmv"+
		"z\u0083\u0087\u008c\u008f\u0093\u0099\u00a3\u00ac\u00b5\u00b9\u00bf\u00c7"+
		"\u00d0\u00dc\u00e2\u00e8\u00ec\u00f6\u0100\u0102\u0107\u010c\u010f\u0117"+
		"\u011c\u012d\u013b\u013d\u0145\u014a\u0153\u015b\u0168";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}